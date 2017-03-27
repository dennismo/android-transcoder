package net.ypresto.androidtranscoder.engine;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;

import net.ypresto.androidtranscoder.compat.MediaCodecBufferCompatWrapper;
import net.ypresto.androidtranscoder.utils.MediaExtractorUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class AudioTrackTranscoder implements TrackTranscoder {

    private static final QueuedMuxer.SampleType SAMPLE_TYPE = QueuedMuxer.SampleType.AUDIO;

    private static final int DRAIN_STATE_NONE = 0;
    private static final int DRAIN_STATE_SHOULD_RETRY_IMMEDIATELY = 1;
    private static final int DRAIN_STATE_CONSUMED = 2;

    private final LinkedHashMap<String, MediaExtractor> mExtractors;
    private final QueuedMuxer mMuxer;
    private long mWrittenPresentationTimeUs;
    private long mOutputPresentationTimeExtractedUs;

    private LinkedHashMap<String, Integer> mTrackIndexes;
    private final LinkedHashMap<String, MediaFormat> mInputFormat;
    private final MediaFormat mOutputFormat;

    private final MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();
    private MediaCodec mEncoder;
    private MediaFormat mActualOutputFormat;

    private HashMap<String, MediaCodecBufferCompatWrapper> mDecoderBuffers;
    private MediaCodecBufferCompatWrapper mEncoderBuffers;

    private boolean mIsEncoderEOS;
    private boolean mEncoderStarted;

    private AudioChannel mAudioChannel;
    private boolean mIsSegmentFinished;

    public AudioTrackTranscoder(LinkedHashMap<String, MediaExtractor> extractor,
                                MediaFormat outputFormat, QueuedMuxer muxer) {
        mExtractors = extractor;
        mTrackIndexes = new LinkedHashMap<String, Integer>();
        mOutputFormat = outputFormat;
        mMuxer = muxer;
        mInputFormat = new LinkedHashMap<String, MediaFormat>();
    }
    /**
     * Wraps an extractor -> decoder -> output surface that corresponds to an input channel.
     * The extractor is passed in when created and the start should be called when a segment
     * is found that needs the wrapper.
     */
    private class DecoderWrapper {
        private boolean mIsExtractorEOS;
        private boolean mIsDecoderEOS;
        private boolean mDecoderStarted;
        private MediaExtractor mExtractor;
        private MediaCodecBufferCompatWrapper mDecoderInputBuffers;
        private MediaCodec mDecoder;
        private Integer mTrackIndex;
        DecoderWrapper(MediaExtractor mediaExtractor) {
            mExtractor = mediaExtractor;
        }

        public void start() {
            MediaExtractorUtils.TrackResult trackResult = MediaExtractorUtils.getFirstVideoAndAudioTrack(mExtractor);
            if (trackResult.mAudioTrackFormat != null) {
                int trackIndex = trackResult.mVideoTrackIndex;
                mTrackIndex = trackIndex;
                mExtractor.selectTrack(trackIndex);
                MediaFormat inputFormat = mExtractor.getTrackFormat(trackIndex);

                try {
                    mDecoder = MediaCodec.createDecoderByType(inputFormat.getString(MediaFormat.KEY_MIME));
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
                mDecoder.configure(inputFormat, null, null, 0);
                mDecoder.start();
                mDecoderStarted = true;
                mDecoderInputBuffers =  new MediaCodecBufferCompatWrapper(mDecoder);
            }
        }

        public void release() {
            if (mDecoder != null) {
                mDecoder.stop();
                mDecoder.release();
                mDecoder = null;
            }
        }

    };
    LinkedHashMap<String, DecoderWrapper> mDecoderWrappers = new LinkedHashMap<String, DecoderWrapper>();

    @Override
    public void setupEncoder() {

        try {
            mEncoder = MediaCodec.createEncoderByType(mOutputFormat.getString(MediaFormat.KEY_MIME));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        mEncoder.configure(mOutputFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mEncoder.start();
        mEncoderStarted = true;
        mEncoderBuffers = new MediaCodecBufferCompatWrapper(mEncoder);
        mIsSegmentFinished = false;

    }
    @Override
    public void setupDecoders(TimeLine.Segment segment) {

        // Release any inactive decoders
        for (Map.Entry<String, DecoderWrapper> decoderWrapperEntry : mDecoderWrappers.entrySet()) {
            if (!segment.getChannels().containsKey(decoderWrapperEntry.getKey())) {
                decoderWrapperEntry.getValue().release();
            }
        }

        LinkedHashMap<String, MediaCodec> decoders = new LinkedHashMap<String, MediaCodec>();

        // Start any decoders being opened for the first time
        for (Map.Entry<String, TimeLine.InputChannel> entry : segment.getChannels().entrySet()) {
            DecoderWrapper decoderWrapper = mDecoderWrappers.get(entry.getKey());
            if (decoderWrapper == null) {
                decoderWrapper = new DecoderWrapper(mExtractors.get(entry.getKey()));
                mDecoderWrappers.put(entry.getKey(), decoderWrapper);
                decoders.put(entry.getKey(), decoderWrapper.mDecoder);
            }
            if (!decoderWrapper.mDecoderStarted) {
                decoderWrapper.start();
            }
        }

        // Setup an audio channel that will mix from multiple decoders
        mAudioChannel = new AudioChannel(decoders, mEncoder, mOutputFormat);
    }

    @Override
    public MediaFormat getDeterminedFormat() {
        return mAudioChannel.getDeterminedFormat();
    }

    @Override
    public boolean stepPipeline(TimeLine.Segment outputSegment) {
        boolean stepped = false;

        int status;
        while (drainEncoder(0) != DRAIN_STATE_NONE) stepped = true;
        do {
            status = drainDecoder(outputSegment, 0);
            if (status != DRAIN_STATE_NONE) stepped = true;
            // NOTE: not repeating to keep from deadlock when encoder is full.
        } while (status == DRAIN_STATE_SHOULD_RETRY_IMMEDIATELY);

        while (mAudioChannel.feedEncoder(0)) stepped = true;
        while (drainExtractors(outputSegment, 0) != DRAIN_STATE_NONE) stepped = true;

        return stepped;
    }

    private int drainExtractors(TimeLine.Segment outputSegment, long timeoutUs) {

        boolean sampleProcessed = false;

        for (Map.Entry<String, TimeLine.InputChannel> inputChannelEntry : outputSegment.getChannels().entrySet()) {

            DecoderWrapper decoderWrapper = mDecoderWrappers.get(inputChannelEntry.getKey());
            if (!decoderWrapper.mIsExtractorEOS && inputChannelEntry.getValue().mPresentationOutputTime <= mOutputPresentationTimeExtractedUs) {

                // Find out which track the extractor has samples for next
                int trackIndex = decoderWrapper.mExtractor.getSampleTrackIndex();

                // Sample is for a different track (like audio) ignore
                if (trackIndex >= 0 && trackIndex != decoderWrapper.mTrackIndex) {
                    continue;
                }

                // Get buffer index to be filled
                int result = decoderWrapper.mDecoder.dequeueInputBuffer(timeoutUs);

                // If no buffers available ignore
                if (result < 0)
                    continue;

                // If end of stream
                if (trackIndex < 0) {
                    decoderWrapper.mIsExtractorEOS = true;
                    decoderWrapper.mDecoder.queueInputBuffer(result, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    continue;
                }

                // Get the sample into the buffer
                mOutputPresentationTimeExtractedUs = decoderWrapper.mExtractor.getSampleTime();
                inputChannelEntry.getValue().mPresentationOutputTime = mOutputPresentationTimeExtractedUs;



                int sampleSize = decoderWrapper.mExtractor.readSampleData(decoderWrapper.mDecoderInputBuffers.getInputBuffer(result), 0);
                boolean isKeyFrame = (decoderWrapper.mExtractor.getSampleFlags() & MediaExtractor.SAMPLE_FLAG_SYNC) != 0;
                decoderWrapper.mDecoder.queueInputBuffer(result, 0, sampleSize, decoderWrapper.mExtractor.getSampleTime(), isKeyFrame ? MediaCodec.BUFFER_FLAG_SYNC_FRAME : 0);


                decoderWrapper.mExtractor.advance();
                sampleProcessed = true;
            }
        }
        return  sampleProcessed ? DRAIN_STATE_CONSUMED : DRAIN_STATE_NONE;
    }

    private int drainDecoder(TimeLine.Segment segment, long timeoutUs) {
        boolean consumed = false;

        boolean stillStreaming = false;

        // Go through each decoder in the segment and get it's frame into a texture
        for (Map.Entry<String, TimeLine.InputChannel> inputChannelEntry : segment.getChannels().entrySet()) {
            DecoderWrapper decoderWrapper = mDecoderWrappers.get(inputChannelEntry.getKey());

            int result = decoderWrapper.mDecoder.dequeueOutputBuffer(mBufferInfo, timeoutUs);
            switch (result) {
                case MediaCodec.INFO_TRY_AGAIN_LATER:
                    continue;
                case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                    mAudioChannel.setActualDecodedFormat(decoderWrapper.mDecoder.getOutputFormat());
                case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                    return DRAIN_STATE_SHOULD_RETRY_IMMEDIATELY;
            }
            consumed = true;
            if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                decoderWrapper.mIsDecoderEOS = true;
                mAudioChannel.drainDecoderBufferAndQueue(inputChannelEntry.getKey(), AudioChannel.BUFFER_INDEX_END_OF_STREAM, 0);
            } else if (mBufferInfo.size > 0) {
                mAudioChannel.drainDecoderBufferAndQueue(inputChannelEntry.getKey(), result, mBufferInfo.presentationTimeUs);
                stillStreaming = true;
            }

        }

        if (!stillStreaming)
            mIsSegmentFinished = true;

        return consumed ? DRAIN_STATE_CONSUMED : DRAIN_STATE_NONE;
    }

    private int drainEncoder(long timeoutUs) {
        if (mIsEncoderEOS) return DRAIN_STATE_NONE;

        int result = mEncoder.dequeueOutputBuffer(mBufferInfo, timeoutUs);
        switch (result) {
            case MediaCodec.INFO_TRY_AGAIN_LATER:
                return DRAIN_STATE_NONE;
            case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                if (mActualOutputFormat != null) {
                    throw new RuntimeException("Audio output format changed twice.");
                }
                mActualOutputFormat = mEncoder.getOutputFormat();
                mMuxer.setOutputFormat(SAMPLE_TYPE, mActualOutputFormat);
                return DRAIN_STATE_SHOULD_RETRY_IMMEDIATELY;
            case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                mEncoderBuffers = new MediaCodecBufferCompatWrapper(mEncoder);
                return DRAIN_STATE_SHOULD_RETRY_IMMEDIATELY;
        }

        if (mActualOutputFormat == null) {
            throw new RuntimeException("Could not determine actual output format.");
        }

        if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            mIsEncoderEOS = true;
            mBufferInfo.set(0, 0, 0, mBufferInfo.flags);
        }
        if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
            // SPS or PPS, which should be passed by MediaFormat.
            mEncoder.releaseOutputBuffer(result, false);
            return DRAIN_STATE_SHOULD_RETRY_IMMEDIATELY;
        }
        mMuxer.writeSampleData(SAMPLE_TYPE, mEncoderBuffers.getOutputBuffer(result), mBufferInfo);
        mWrittenPresentationTimeUs = mBufferInfo.presentationTimeUs;
        mEncoder.releaseOutputBuffer(result, false);
        return DRAIN_STATE_CONSUMED;
    }

    @Override
    public long getWrittenPresentationTimeUs() {
        return mWrittenPresentationTimeUs;
    }

    @Override
    public long getOutputPresentationTimeExtractedUs() {
        return mOutputPresentationTimeExtractedUs;
    }


    @Override
    public boolean isSegmentFinished() {
        return mIsSegmentFinished;
    }

    @Override
    public void releaseDecoders() {
        for (Map.Entry<String, DecoderWrapper> decoderWrapperEntry : mDecoderWrappers.entrySet()) {
            decoderWrapperEntry.getValue().release();
        }
    }
    @Override
    public void releaseEncoder() {
        if (mEncoder != null) {
            if (mEncoderStarted) mEncoder.stop();
            mEncoder.release();
            mEncoder = null;
        }
    }
    @Override
    public void release() {
        releaseDecoders();
        releaseEncoder();
    }
}
