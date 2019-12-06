// Test change to see if commit propagates
package net.ypresto.androidtranscoder.tests;

import android.content.res.Resources;
import android.os.ParcelFileDescriptor;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import android.util.Log;

import net.ypresto.androidtranscoder.MediaTranscoder;
import net.ypresto.androidtranscoder.TLog;
import net.ypresto.androidtranscoder.engine.TimeLine;
import net.ypresto.androidtranscoder.format.Android16By9FormatStrategy;
import net.ypresto.androidtranscoder.format.MediaFormatStrategyPresets;


import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class SingleFileTranscoderTest {
    private static final String TAG = "JUnitTranscoder";
    static private String inputFileName1;
    static private String inputFileName2;
    static private String inputFileName3;
    static private String inputFileNamer0;
    static private String inputFileNamer90;
    static private String inputFileNamer180;
    static private String inputFileNamer270;
    static private String inputFileNamea9;
    private static String status = "not started";
    static private int LogLevelForTests = 2;

    static MediaTranscoder.Listener  listener = new MediaTranscoder.Listener() {
        @Override
        public void onTranscodeProgress(double progress) {
            //Log.d(TAG, "Progress " + progress);
        }
        @Override
        public void onTranscodeCompleted() {
            status = "complete";
            Log.d(TAG, "Complete");
        }
        @Override
        public void onTranscodeCanceled() {
            status = "canceled";
        }
        @Override
        public void onTranscodeFailed(Exception e) {
            assertEquals("onTranscodeFailed", "none", e + Log.getStackTraceString(e));
        }
    };

    @BeforeClass
    public static void retrieveVideo ()  {
        inputFileName1 = InstrumentationRegistry.getInstrumentation().getTargetContext().getExternalFilesDir(null) + "/input1.mp4";
        inputFileName2 = InstrumentationRegistry.getInstrumentation().getTargetContext().getExternalFilesDir(null) + "/input2.mp4";
        inputFileNamer0 = InstrumentationRegistry.getInstrumentation().getTargetContext().getExternalFilesDir(null) + "/input3.mp4";
        inputFileNamer90 = InstrumentationRegistry.getInstrumentation().getTargetContext().getExternalFilesDir(null) + "/input4.mp4";
        inputFileNamer180 = InstrumentationRegistry.getInstrumentation().getTargetContext().getExternalFilesDir(null) + "/input5.mp4";
        inputFileNamer270 = InstrumentationRegistry.getInstrumentation().getTargetContext().getExternalFilesDir(null) + "/input6.mp4";
        inputFileNamea9 = InstrumentationRegistry.getInstrumentation().getTargetContext().getExternalFilesDir(null) + "/input7.mp4";
        inputFileName3 = InstrumentationRegistry.getInstrumentation().getTargetContext().getExternalFilesDir(null) + "/output_SingleFileMono.mp4";
        cleanup(inputFileName1);
        cleanup(inputFileName2);
        try {
            InputStream in = InstrumentationRegistry.getInstrumentation().getContext().getResources().openRawResource(net.ypresto.androidtranscoder.example.test.R.raw.poolcleaner);
            OutputStream out = new FileOutputStream(inputFileName1);
            copyFile(in, out);
            in.close();
            out.close();
        } catch(IOException e) {
            assertEquals("Exception on file copy", "none", e + Log.getStackTraceString(e));
        }
        try {
            InputStream in = InstrumentationRegistry.getInstrumentation().getContext().getResources().openRawResource(net.ypresto.androidtranscoder.example.test.R.raw.r0);
            OutputStream out = new FileOutputStream(inputFileNamer0);
            copyFile(in, out);
            in.close();
            out.close();
        } catch(IOException e) {
            assertEquals("Exception on file copy", "none", e + Log.getStackTraceString(e));
        }
        try {
            InputStream in = InstrumentationRegistry.getInstrumentation().getContext().getResources().openRawResource(net.ypresto.androidtranscoder.example.test.R.raw.r90);
            OutputStream out = new FileOutputStream(inputFileNamer90);
            copyFile(in, out);
            in.close();
            out.close();
        } catch(IOException e) {
            assertEquals("Exception on file copy", "none", e + Log.getStackTraceString(e));
        }
        try {
            InputStream in = InstrumentationRegistry.getInstrumentation().getContext().getResources().openRawResource(net.ypresto.androidtranscoder.example.test.R.raw.r180);
            OutputStream out = new FileOutputStream(inputFileNamer180);
            copyFile(in, out);
            in.close();
            out.close();
        } catch(IOException e) {
            assertEquals("Exception on file copy", "none", e + Log.getStackTraceString(e));
        }
        try {
            InputStream in = InstrumentationRegistry.getInstrumentation().getContext().getResources().openRawResource(net.ypresto.androidtranscoder.example.test.R.raw.r270);
            OutputStream out = new FileOutputStream(inputFileNamer270);
            copyFile(in, out);
            in.close();
            out.close();
        } catch(IOException e) {
            assertEquals("Exception on file copy", "none", e + Log.getStackTraceString(e));
        }
        try {
            InputStream in = InstrumentationRegistry.getInstrumentation().getContext().getResources().openRawResource(net.ypresto.androidtranscoder.example.test.R.raw.android9);
            OutputStream out = new FileOutputStream(inputFileNamea9);
            copyFile(in, out);
            in.close();
            out.close();
        } catch(IOException e) {
            assertEquals("Exception on file copy", "none", e + Log.getStackTraceString(e));
        }
        try {
            InputStream in = InstrumentationRegistry.getInstrumentation().getContext().getResources().openRawResource(net.ypresto.androidtranscoder.example.test.R.raw.frogs);
            OutputStream out = new FileOutputStream(inputFileName2);
            copyFile(in, out);
            in.close();
            out.close();
        } catch(IOException e) {
            assertEquals("Exception on file copy", "none", e + Log.getStackTraceString(e));
        }
        try {
            SingleFileToMono();
        } catch(Exception e) {
            assertEquals("Exception on file copy", "none", e + Log.getStackTraceString(e));
        }
    }

    public static void SingleFileToMono() throws InterruptedException, ExecutionException, FileNotFoundException {
        TLog.d(TAG, "@Test " + "SingleFileToMono");
        String outputFileName = InstrumentationRegistry.getInstrumentation().getTargetContext().getExternalFilesDir(null) + "/output_SingleFileMono.mp4";
        cleanup(outputFileName);
        ParcelFileDescriptor in1 = ParcelFileDescriptor.open(new File(inputFileName1), ParcelFileDescriptor.MODE_READ_ONLY);
        TimeLine timeline = new TimeLine(LogLevelForTests)
                .addChannel("A", in1.getFileDescriptor())
                .createSegment()
                .output("A")
                .timeLine();
        (MediaTranscoder.getInstance().transcodeVideo(
                timeline, outputFileName,
                MediaFormatStrategyPresets.createAndroid16x9Strategy720P(Android16By9FormatStrategy.AUDIO_BITRATE_AS_IS, 1),
                listener)
        ).get();
    }

    @Test()
    public void NonStandardProfile() throws InterruptedException, ExecutionException, FileNotFoundException {
        TLog.d(TAG, "@Test " + "Non Standard Profile");
        String outputFileName = InstrumentationRegistry.getInstrumentation().getTargetContext().getExternalFilesDir(null) + "/output7.mp4";
        cleanup(outputFileName);
        ParcelFileDescriptor in1 = ParcelFileDescriptor.open(new File(inputFileNamea9), ParcelFileDescriptor.MODE_READ_ONLY);

        TimeLine timeline = new TimeLine(LogLevelForTests)
                .addChannel("A", in1.getFileDescriptor())
                .createSegment()
                .output("A")
                .timeLine();
        (MediaTranscoder.getInstance().transcodeVideo(
                timeline, outputFileName,
                MediaFormatStrategyPresets.createAndroid16x9Strategy1080P(Android16By9FormatStrategy.AUDIO_BITRATE_AS_IS, 1),
                listener)
        ).get();
    }

    @Test()
    public void DriftFromManyVideos() throws InterruptedException, ExecutionException, FileNotFoundException {
        TLog.d(TAG, "@Test " + "Drift from many videos");
        String outputFileName = InstrumentationRegistry.getInstrumentation().getTargetContext().getExternalFilesDir(null) + "/output7.mp4";
        cleanup(outputFileName);
        ParcelFileDescriptor in1 = ParcelFileDescriptor.open(new File(inputFileNamea9), ParcelFileDescriptor.MODE_READ_ONLY);

        TimeLine timeline = new TimeLine(LogLevelForTests)
                .addChannel("A1", in1.getFileDescriptor())
                .addChannel("A2", in1.getFileDescriptor())
                .addChannel("A3", in1.getFileDescriptor())
                .addChannel("A4", in1.getFileDescriptor())
                .addChannel("A5", in1.getFileDescriptor())
                .addChannel("A6", in1.getFileDescriptor())
                .addChannel("A7", in1.getFileDescriptor())
                .addChannel("A8", in1.getFileDescriptor())
                .addChannel("A9", in1.getFileDescriptor())
                .addChannel("A10", in1.getFileDescriptor())
                .createSegment().output("A1").timeLine()
                .createSegment().output("A2").timeLine()
                .createSegment().output("A3").timeLine()
                .createSegment().output("A4").timeLine()
                .createSegment().output("A5").timeLine()
                .createSegment().output("A6").timeLine()
                .createSegment().output("A7").timeLine()
                .createSegment().output("A8").timeLine()
                .createSegment().output("A9").timeLine()
                .createSegment().output("A10").duration(1000).timeLine();
        (MediaTranscoder.getInstance().transcodeVideo(
                timeline, outputFileName,
                MediaFormatStrategyPresets.createAndroid16x9Strategy1080P(Android16By9FormatStrategy.AUDIO_BITRATE_AS_IS, 1),
                listener)
        ).get();
    }

    @Test()
    public void OrientationR0() {
        runTest(new Transcode() {
            @Override
            public void run() throws IOException, InterruptedException, ExecutionException {
                TLog.d(TAG, "@Test " + "OrientationR0");
                String outputFileName = InstrumentationRegistry.getInstrumentation().getTargetContext().getExternalFilesDir(null) + "/OrientationR0.mp4";
                cleanup(outputFileName);
                ParcelFileDescriptor inr0 = ParcelFileDescriptor.open(new File(inputFileNamer0), ParcelFileDescriptor.MODE_READ_ONLY);
                ParcelFileDescriptor inr90 = ParcelFileDescriptor.open(new File(inputFileNamer90), ParcelFileDescriptor.MODE_READ_ONLY);
                ParcelFileDescriptor inr180 = ParcelFileDescriptor.open(new File(inputFileNamer180), ParcelFileDescriptor.MODE_READ_ONLY);
                ParcelFileDescriptor inr270 = ParcelFileDescriptor.open(new File(inputFileNamer270), ParcelFileDescriptor.MODE_READ_ONLY);
                TimeLine timeline = new TimeLine(LogLevelForTests)
                        .addChannel("A", inr0.getFileDescriptor())
                        .addChannel("B", inr90.getFileDescriptor())
                        .addChannel("C", inr180.getFileDescriptor())
                        .addChannel("D", inr270.getFileDescriptor())
                        .createSegment()
                            .output("A")
                            .duration(1000)
                        .timeLine().createSegment()
                            .output("A", TimeLine.Filter.OPACITY_DOWN_RAMP)
                            .output("B", TimeLine.Filter.OPACITY_UP_RAMP)
                            .duration(1000)
                        .timeLine().createSegment()
                            .output("B")
                            .duration(1000)
                        .timeLine().createSegment()
                            .output("B", TimeLine.Filter.OPACITY_DOWN_RAMP)
                            .output("C", TimeLine.Filter.OPACITY_UP_RAMP)
                            .duration(1000)
                        .timeLine().createSegment()
                            .output("C")
                            .duration(1000)
                        .timeLine().createSegment()
                            .output("C", TimeLine.Filter.OPACITY_DOWN_RAMP)
                            .output("D", TimeLine.Filter.OPACITY_UP_RAMP)
                            .duration(1000)
                        .timeLine().createSegment()
                            .output("D")
                            .duration(1000)
                        .timeLine();
                (MediaTranscoder.getInstance().transcodeVideo(
                        timeline, outputFileName,
                        MediaFormatStrategyPresets.createAndroid16x9Strategy720P(
                                Android16By9FormatStrategy.AUDIO_BITRATE_AS_IS,
                                Android16By9FormatStrategy.AUDIO_CHANNELS_AS_IS),
                        listener)
                ).get();
            }
        });
    }
    @Test()
    public void OrientationR90() {
        runTest(new Transcode() {
            @Override
            public void run() throws IOException, InterruptedException, ExecutionException {
                TLog.d(TAG, "@Test " + "OrientationR90");
                String outputFileName = InstrumentationRegistry.getInstrumentation().getTargetContext().getExternalFilesDir(null) + "/OrientationR90.mp4";
                cleanup(outputFileName);
                ParcelFileDescriptor inr0 = ParcelFileDescriptor.open(new File(inputFileNamer0), ParcelFileDescriptor.MODE_READ_ONLY);
                ParcelFileDescriptor inr90 = ParcelFileDescriptor.open(new File(inputFileNamer90), ParcelFileDescriptor.MODE_READ_ONLY);
                ParcelFileDescriptor inr180 = ParcelFileDescriptor.open(new File(inputFileNamer180), ParcelFileDescriptor.MODE_READ_ONLY);
                ParcelFileDescriptor inr270 = ParcelFileDescriptor.open(new File(inputFileNamer270), ParcelFileDescriptor.MODE_READ_ONLY);
                TimeLine timeline = new TimeLine(LogLevelForTests)
                        .addChannel("A", inr90.getFileDescriptor())
                        .addChannel("B", inr180.getFileDescriptor())
                        .addChannel("C", inr270.getFileDescriptor())
                        .addChannel("D", inr0.getFileDescriptor())
                        .createSegment()
                        .output("A")
                        .duration(1000)
                        .timeLine().createSegment()
                        .output("A", TimeLine.Filter.OPACITY_DOWN_RAMP)
                        .output("B", TimeLine.Filter.OPACITY_UP_RAMP)
                        .duration(1000)
                        .timeLine().createSegment()
                        .output("B")
                        .duration(1000)
                        .timeLine().createSegment()
                        .output("B", TimeLine.Filter.OPACITY_DOWN_RAMP)
                        .output("C", TimeLine.Filter.OPACITY_UP_RAMP)
                        .duration(1000)
                        .timeLine().createSegment()
                        .output("C")
                        .duration(1000)
                        .timeLine().createSegment()
                        .output("C", TimeLine.Filter.OPACITY_DOWN_RAMP)
                        .output("D", TimeLine.Filter.OPACITY_UP_RAMP)
                        .duration(1000)
                        .timeLine().createSegment()
                        .output("D")
                        .duration(1000)
                        .timeLine();
                (MediaTranscoder.getInstance().transcodeVideo(
                        timeline, outputFileName,
                        MediaFormatStrategyPresets.createAndroid16x9Strategy720P(
                                Android16By9FormatStrategy.AUDIO_BITRATE_AS_IS,
                                Android16By9FormatStrategy.AUDIO_CHANNELS_AS_IS),
                        listener)
                ).get();
            }
        });
    }
    @Test()
    public void OrientationR180() {
        runTest(new Transcode() {
            @Override
            public void run() throws IOException, InterruptedException, ExecutionException {
                TLog.d(TAG, "@Test " + "OrientationR180");
                String outputFileName = InstrumentationRegistry.getInstrumentation().getTargetContext().getExternalFilesDir(null) + "/OrientationR180.mp4";
                cleanup(outputFileName);
                ParcelFileDescriptor inr0 = ParcelFileDescriptor.open(new File(inputFileNamer180), ParcelFileDescriptor.MODE_READ_ONLY);
                ParcelFileDescriptor inr90 = ParcelFileDescriptor.open(new File(inputFileNamer270), ParcelFileDescriptor.MODE_READ_ONLY);
                ParcelFileDescriptor inr180 = ParcelFileDescriptor.open(new File(inputFileNamer0), ParcelFileDescriptor.MODE_READ_ONLY);
                ParcelFileDescriptor inr270 = ParcelFileDescriptor.open(new File(inputFileNamer90), ParcelFileDescriptor.MODE_READ_ONLY);

                //Matrix A: 1.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 1.0
                //Matrix B: 1.0 0.0 0.0 0.0 0.0 -1.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 1.0 0.0 1.0
                //Matrix: 0.0 -1.0 0.0 0.0 -1.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 1.0 1.0 0.0 1.0
                //Matrix: 0.0 -1.0 0.0 0.0 -1.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 1.0 1.0 0.0 1.0
                //Matrix: -1.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 1.0 0.0 1.0 0.0 0.0 1.0
                //Matrix: -1.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 1.0 0.0 1.0 0.0 0.0 1.0
                //Matrix: 0.0 1.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 1.0

                TimeLine timeline = new TimeLine(LogLevelForTests)
                        .addChannel("A", inr0.getFileDescriptor())
                        .addChannel("B", inr90.getFileDescriptor())
                        .addChannel("C", inr180.getFileDescriptor())
                        .addChannel("D", inr270.getFileDescriptor())
                        .createSegment()
                        .output("A")
                        .duration(1000)
                        .timeLine().createSegment()
                        .output("A", TimeLine.Filter.OPACITY_DOWN_RAMP)
                        .output("B", TimeLine.Filter.OPACITY_UP_RAMP)
                        .duration(1000)
                        .timeLine().createSegment()
                        .output("B")
                        .duration(1000)
                        .timeLine().createSegment()
                        .output("B", TimeLine.Filter.OPACITY_DOWN_RAMP)
                        .output("C", TimeLine.Filter.OPACITY_UP_RAMP)
                        .duration(1000)
                        .timeLine().createSegment()
                        .output("C")
                        .duration(1000)
                        .timeLine().createSegment()
                        .output("C", TimeLine.Filter.OPACITY_DOWN_RAMP)
                        .output("D", TimeLine.Filter.OPACITY_UP_RAMP)
                        .duration(1000)
                        .timeLine().createSegment()
                        .output("D")
                        .duration(1000)
                        .timeLine();
                (MediaTranscoder.getInstance().transcodeVideo(
                        timeline, outputFileName,
                        MediaFormatStrategyPresets.createAndroid16x9Strategy720P(
                                Android16By9FormatStrategy.AUDIO_BITRATE_AS_IS,
                                Android16By9FormatStrategy.AUDIO_CHANNELS_AS_IS),
                        listener)
                ).get();
            }
        });
    }
    @Test()
    public void OrientationR270() {
        runTest(new Transcode() {
            @Override
            public void run() throws IOException, InterruptedException, ExecutionException {
                TLog.d(TAG, "@Test " + "OrientationR270");
                String outputFileName = InstrumentationRegistry.getInstrumentation().getTargetContext().getExternalFilesDir(null) + "/OrientationR270.mp4";
                cleanup(outputFileName);
                ParcelFileDescriptor inr0 = ParcelFileDescriptor.open(new File(inputFileNamer270), ParcelFileDescriptor.MODE_READ_ONLY);
                ParcelFileDescriptor inr90 = ParcelFileDescriptor.open(new File(inputFileNamer0), ParcelFileDescriptor.MODE_READ_ONLY);
                ParcelFileDescriptor inr180 = ParcelFileDescriptor.open(new File(inputFileNamer90), ParcelFileDescriptor.MODE_READ_ONLY);
                ParcelFileDescriptor inr270 = ParcelFileDescriptor.open(new File(inputFileNamer180), ParcelFileDescriptor.MODE_READ_ONLY);
                TimeLine timeline = new TimeLine(LogLevelForTests)
                        .addChannel("A", inr0.getFileDescriptor())
                        .addChannel("B", inr90.getFileDescriptor())
                        .addChannel("C", inr180.getFileDescriptor())
                        .addChannel("D", inr270.getFileDescriptor())
                        .createSegment()
                        .output("A")
                        .duration(1000)
                        .timeLine().createSegment()
                        .output("A", TimeLine.Filter.OPACITY_DOWN_RAMP)
                        .output("B", TimeLine.Filter.OPACITY_UP_RAMP)
                        .duration(1000)
                        .timeLine().createSegment()
                        .output("B")
                        .duration(1000)
                        .timeLine().createSegment()
                        .output("B", TimeLine.Filter.OPACITY_DOWN_RAMP)
                        .output("C", TimeLine.Filter.OPACITY_UP_RAMP)
                        .duration(1000)
                        .timeLine().createSegment()
                        .output("C")
                        .duration(1000)
                        .timeLine().createSegment()
                        .output("C", TimeLine.Filter.OPACITY_DOWN_RAMP)
                        .output("D", TimeLine.Filter.OPACITY_UP_RAMP)
                        .duration(1000)
                        .timeLine().createSegment()
                        .output("D")
                        .duration(1000)
                        .timeLine();
                (MediaTranscoder.getInstance().transcodeVideo(
                        timeline, outputFileName,
                        MediaFormatStrategyPresets.createAndroid16x9Strategy720P(
                                Android16By9FormatStrategy.AUDIO_BITRATE_AS_IS,
                                Android16By9FormatStrategy.AUDIO_CHANNELS_AS_IS),
                        listener)
                ).get();
            }
        });
    }

    @Test()
    public void SingleFile() {
        runTest(new Transcode() {
            @Override
            public void run() throws IOException, InterruptedException, ExecutionException {
                TLog.d(TAG, "@Test " + "SingleFile");
                String outputFileName = InstrumentationRegistry.getInstrumentation().getTargetContext().getExternalFilesDir(null) + "/output_SingleFile.mp4";
                cleanup(outputFileName);
                ParcelFileDescriptor in1 = ParcelFileDescriptor.open(new File(inputFileName1), ParcelFileDescriptor.MODE_READ_ONLY);
                TimeLine timeline = new TimeLine(LogLevelForTests)
                        .addChannel("A", in1.getFileDescriptor())
                        .createSegment()
                            .output("A")
                        .timeLine();
                (MediaTranscoder.getInstance().transcodeVideo(
                        timeline, outputFileName,
                        MediaFormatStrategyPresets.createAndroid16x9Strategy720P(Android16By9FormatStrategy.AUDIO_BITRATE_AS_IS, Android16By9FormatStrategy.AUDIO_CHANNELS_AS_IS),
                        listener)
                ).get();
            }
        });
    }

    @Test()
    public void SingleFileFast() {
        runTest(new Transcode() {
            @Override
            public void run() throws IOException, InterruptedException, ExecutionException {
                TLog.d(TAG, "@Test " + "SingleFileFast");
                String outputFileName = InstrumentationRegistry.getInstrumentation().getTargetContext().getExternalFilesDir(null) + "/output_SingleFile.mp4";
                cleanup(outputFileName);
                ParcelFileDescriptor in1 = ParcelFileDescriptor.open(new File(inputFileName1), ParcelFileDescriptor.MODE_READ_ONLY);
                TimeLine timeline = new TimeLine(LogLevelForTests)
                        .addChannel("A", in1.getFileDescriptor())

                        .createSegment()
                        .output("A")
                        .duration(50)
                        .seek("A", 50)
                        .timeLine()

                        .createSegment()
                        .output("A")
                        .duration(50)
                        .seek("A", 50)
                        .timeLine()

                        .createSegment()
                        .output("A")
                        .duration(50)
                        .seek("A", 50)
                        .timeLine()

                        .createSegment()
                        .output("A")
                        .duration(50)
                        .seek("A", 50)
                        .timeLine()

                        .createSegment()
                        .output("A")
                        .duration(50)
                        .seek("A", 50)
                        .timeLine()

                        .createSegment()
                        .output("A")
                        .duration(50)
                        .seek("A", 50)
                        .timeLine()

                        .createSegment()
                        .output("A")
                        .duration(50)
                        .seek("A", 50)
                        .timeLine()

                        .createSegment()
                        .output("A")
                        .duration(50)
                        .seek("A", 50)
                        .timeLine()

                        .createSegment()
                        .output("A")
                        .duration(50)
                        .seek("A", 50)
                        .timeLine()

                        .createSegment()
                        .output("A")
                        .duration(50)
                        .seek("A", 50)
                        .timeLine()



                        .createSegment()
                        .output("A")
                        .duration(50)
                        .seek("A", 50)
                        .timeLine()

                        .createSegment()
                        .output("A")
                        .duration(50)
                        .seek("A", 50)
                        .timeLine()

                        .createSegment()
                        .output("A")
                        .duration(50)
                        .seek("A", 50)
                        .timeLine()

                        .createSegment()
                        .output("A")
                        .duration(50)
                        .seek("A", 50)
                        .timeLine()

                        .createSegment()
                        .output("A")
                        .duration(50)
                        .seek("A", 50)
                        .timeLine()

                        .createSegment()
                        .output("A")
                        .duration(50)
                        .seek("A", 50)
                        .timeLine()

                        .createSegment()
                        .output("A")
                        .duration(50)
                        .seek("A", 50)
                        .timeLine()

                        .createSegment()
                        .output("A")
                        .duration(50)
                        .seek("A", 50)
                        .timeLine()

                        .createSegment()
                        .output("A")
                        .duration(50)
                        .seek("A", 50)
                        .timeLine()

                        .createSegment()
                        .output("A")
                        .duration(50)
                        .seek("A", 50)
                        .timeLine()



                        .createSegment()
                        .output("A")
                        .duration(50)
                        .seek("A", 50)
                        .timeLine()

                        .createSegment()
                        .output("A")
                        .duration(50)
                        .seek("A", 50)
                        .timeLine()

                        .createSegment()
                        .output("A")
                        .duration(50)
                        .seek("A", 50)
                        .timeLine()

                        .createSegment()
                        .output("A")
                        .duration(50)
                        .seek("A", 50)
                        .timeLine()

                        .createSegment()
                        .output("A")
                        .duration(50)
                        .seek("A", 50)
                        .timeLine()

                        .createSegment()
                        .output("A")
                        .duration(50)
                        .seek("A", 50)
                        .timeLine()

                        .createSegment()
                        .output("A")
                        .duration(50)
                        .seek("A", 50)
                        .timeLine()

                        .createSegment()
                        .output("A")
                        .duration(50)
                        .seek("A", 50)
                        .timeLine()

                        .createSegment()
                        .output("A")
                        .duration(50)
                        .seek("A", 50)
                        .timeLine()

                        .createSegment()
                        .output("A")
                        .duration(50)
                        .seek("A", 50)
                        .timeLine()



                        .createSegment()
                        .output("A")
                        .duration(50)
                        .seek("A", 50)
                        .timeLine()

                        .createSegment()
                        .output("A")
                        .duration(50)
                        .seek("A", 50)
                        .timeLine()


                        .createSegment()
                        .output("A")
                        .duration(50)
                        .seek("A", 50)
                        .timeLine();

                (MediaTranscoder.getInstance().transcodeVideo(
                        timeline, outputFileName,
                        MediaFormatStrategyPresets.createAndroid16x9Strategy720P(Android16By9FormatStrategy.AUDIO_BITRATE_AS_IS, Android16By9FormatStrategy.AUDIO_CHANNELS_AS_IS),
                        listener)
                ).get();
            }
        });
    }

    @Test()
    public void SingleFileFastMute() {
        runTest(new Transcode() {
            @Override
            public void run() throws IOException, InterruptedException, ExecutionException {
                TLog.d(TAG, "@Test " + "SingleFileFastMute");
                String outputFileName = InstrumentationRegistry.getInstrumentation().getTargetContext().getExternalFilesDir(null) + "/output_SingleFile.mp4";
                cleanup(outputFileName);
                ParcelFileDescriptor in1 = ParcelFileDescriptor.open(new File(inputFileName1), ParcelFileDescriptor.MODE_READ_ONLY);
                TimeLine timeline = new TimeLine(LogLevelForTests)
                        .addChannel("A", in1.getFileDescriptor())

                        .createSegment()
                        .output("A",TimeLine.Filter.MUTE)
                        .duration(50)
                        .seek("A", 50)
                        .timeLine()

                        .createSegment()
                        .output("A",TimeLine.Filter.MUTE)
                        .duration(50)
                        .seek("A", 50)
                        .timeLine()

                        .createSegment()
                        .output("A",TimeLine.Filter.MUTE)
                        .duration(50)
                        .seek("A", 50)
                        .timeLine()

                        .createSegment()
                        .output("A",TimeLine.Filter.MUTE)
                        .duration(50)
                        .seek("A", 50)
                        .timeLine()

                        .createSegment()
                        .output("A",TimeLine.Filter.MUTE)
                        .duration(50)
                        .seek("A", 50)
                        .timeLine()

                        .createSegment()
                        .output("A",TimeLine.Filter.MUTE)
                        .duration(50)
                        .seek("A", 50)
                        .timeLine()

                        .createSegment()
                        .output("A",TimeLine.Filter.MUTE)
                        .duration(50)
                        .seek("A", 50)
                        .timeLine()

                        .createSegment()
                        .output("A",TimeLine.Filter.MUTE)
                        .duration(50)
                        .seek("A", 50)
                        .timeLine()

                        .createSegment()
                        .output("A",TimeLine.Filter.MUTE)
                        .duration(50)
                        .seek("A", 50)
                        .timeLine()

                        .createSegment()
                        .output("A",TimeLine.Filter.MUTE)
                        .duration(50)
                        .seek("A", 50)
                        .timeLine()



                        .createSegment()
                        .output("A",TimeLine.Filter.MUTE)
                        .duration(50)
                        .seek("A", 50)
                        .timeLine()

                        .createSegment()
                        .output("A",TimeLine.Filter.MUTE)
                        .duration(50)
                        .seek("A", 50)
                        .timeLine()

                        .createSegment()
                        .output("A",TimeLine.Filter.MUTE)
                        .duration(50)
                        .seek("A", 50)
                        .timeLine()

                        .createSegment()
                        .output("A",TimeLine.Filter.MUTE)
                        .duration(50)
                        .seek("A", 50)
                        .timeLine()

                        .createSegment()
                        .output("A",TimeLine.Filter.MUTE)
                        .duration(50)
                        .seek("A", 50)
                        .timeLine()

                        .createSegment()
                        .output("A",TimeLine.Filter.MUTE)
                        .duration(50)
                        .seek("A", 50)
                        .timeLine()

                        .createSegment()
                        .output("A",TimeLine.Filter.MUTE)
                        .duration(50)
                        .seek("A", 50)
                        .timeLine()

                        .createSegment()
                        .output("A",TimeLine.Filter.MUTE)
                        .duration(50)
                        .seek("A", 50)
                        .timeLine()

                        .createSegment()
                        .output("A",TimeLine.Filter.MUTE)
                        .duration(50)
                        .seek("A", 50)
                        .timeLine()

                        .createSegment()
                        .output("A",TimeLine.Filter.MUTE)
                        .duration(50)
                        .seek("A", 50)
                        .timeLine()



                        .createSegment()
                        .output("A",TimeLine.Filter.MUTE)
                        .duration(50)
                        .seek("A", 50)
                        .timeLine()

                        .createSegment()
                        .output("A",TimeLine.Filter.MUTE)
                        .duration(50)
                        .seek("A", 50)
                        .timeLine()

                        .createSegment()
                        .output("A",TimeLine.Filter.MUTE)
                        .duration(50)
                        .seek("A", 50)
                        .timeLine()

                        .createSegment()
                        .output("A",TimeLine.Filter.MUTE)
                        .duration(50)
                        .seek("A", 50)
                        .timeLine()

                        .createSegment()
                        .output("A",TimeLine.Filter.MUTE)
                        .duration(50)
                        .seek("A", 50)
                        .timeLine()

                        .createSegment()
                        .output("A",TimeLine.Filter.MUTE)
                        .duration(50)
                        .seek("A", 50)
                        .timeLine()

                        .createSegment()
                        .output("A",TimeLine.Filter.MUTE)
                        .duration(50)
                        .seek("A", 50)
                        .timeLine()

                        .createSegment()
                        .output("A",TimeLine.Filter.MUTE)
                        .duration(50)
                        .seek("A", 50)
                        .timeLine()

                        .createSegment()
                        .output("A",TimeLine.Filter.MUTE)
                        .duration(50)
                        .seek("A", 50)
                        .timeLine()

                        .createSegment()
                        .output("A",TimeLine.Filter.MUTE)
                        .duration(50)
                        .seek("A", 50)
                        .timeLine()


                        .createSegment()
                        .output("A")
                        .timeLine();

                (MediaTranscoder.getInstance().transcodeVideo(
                        timeline, outputFileName,
                        MediaFormatStrategyPresets.createAndroid16x9Strategy720P(Android16By9FormatStrategy.AUDIO_BITRATE_AS_IS, Android16By9FormatStrategy.AUDIO_CHANNELS_AS_IS),
                        listener)
                ).get();
            }
        });
    }
    @Test()
    public void SingleFileFastMute3000() {
        runTest(new Transcode() {
            @Override
            public void run() throws IOException, InterruptedException, ExecutionException {
                TLog.d(TAG, "@Test " + "SingleFileFastMute3000");
                String outputFileName = InstrumentationRegistry.getInstrumentation().getTargetContext().getExternalFilesDir(null) + "/output_SingleFile.mp4";
                cleanup(outputFileName);
                ParcelFileDescriptor in1 = ParcelFileDescriptor.open(new File(inputFileName1), ParcelFileDescriptor.MODE_READ_ONLY);
                TimeLine timeline = new TimeLine(LogLevelForTests)
                        .addChannel("A", in1.getFileDescriptor())

                        .createSegment()
                        .output("A",TimeLine.Filter.MUTE)
                        .duration(50)
                        .seek("A", 250)
                        .timeLine()

                        .createSegment()
                        .output("A",TimeLine.Filter.MUTE)
                        .duration(50)
                        .seek("A", 250)
                        .timeLine()

                        .createSegment()
                        .output("A",TimeLine.Filter.MUTE)
                        .duration(50)
                        .seek("A", 250)
                        .timeLine()

                        .createSegment()
                        .output("A",TimeLine.Filter.MUTE)
                        .duration(50)
                        .seek("A", 250)
                        .timeLine()

                        .createSegment()
                        .output("A",TimeLine.Filter.MUTE)
                        .duration(50)
                        .seek("A", 250)
                        .timeLine()

                        .createSegment()
                        .output("A",TimeLine.Filter.MUTE)
                        .duration(50)
                        .seek("A", 250)
                        .timeLine()

                        .createSegment()
                        .output("A",TimeLine.Filter.MUTE)
                        .duration(50)
                        .seek("A", 250)
                        .timeLine()

                        .createSegment()
                        .output("A",TimeLine.Filter.MUTE)
                        .duration(50)
                        .seek("A", 250)
                        .timeLine()

                        .createSegment()
                        .output("A",TimeLine.Filter.MUTE)
                        .duration(50)
                        .seek("A", 250)
                        .timeLine()

                        .createSegment()
                        .output("A",TimeLine.Filter.MUTE)
                        .duration(50)
                        .seek("A", 250)
                        .timeLine()



                        .createSegment()
                        .output("A")
                        .timeLine();

                (MediaTranscoder.getInstance().transcodeVideo(
                        timeline, outputFileName,
                        MediaFormatStrategyPresets.createAndroid16x9Strategy720P(Android16By9FormatStrategy.AUDIO_BITRATE_AS_IS, Android16By9FormatStrategy.AUDIO_CHANNELS_AS_IS),
                        listener)
                ).get();
            }
        });
    }

    @Test()
    public void QuadFile() {
        runTest(new Transcode() {
            @Override
            public void run() throws IOException, InterruptedException, ExecutionException {
                TLog.d(TAG, "@Test " + "QuadFile");
                String outputFileName = InstrumentationRegistry.getInstrumentation().getTargetContext().getExternalFilesDir(null) + "/output_QuadFile.mp4";
                cleanup(outputFileName);
                ParcelFileDescriptor in1 = ParcelFileDescriptor.open(new File(inputFileName1), ParcelFileDescriptor.MODE_READ_ONLY);
                TimeLine timeline = new TimeLine(LogLevelForTests)
                    .addChannel("A", in1.getFileDescriptor())
                    .addChannel("B", in1.getFileDescriptor())
                    .addChannel("C", in1.getFileDescriptor())
                    .addChannel("D", in1.getFileDescriptor())
                    .createSegment()
                        .output("A")
 //                       .duration(4000)
                        .timeLine()
                    .createSegment()
                        .output("B")
//                        .duration(4000)
                        .timeLine()
                    .createSegment()
                        .output("C")
//                        .duration(4000)
                        .timeLine()
                    .createSegment()
                        .output("D")
//                        .duration(4000)
                        .timeLine();
                (MediaTranscoder.getInstance().transcodeVideo(
                        timeline, outputFileName,
                        MediaFormatStrategyPresets.createAndroid16x9Strategy720P(Android16By9FormatStrategy.AUDIO_BITRATE_AS_IS, Android16By9FormatStrategy.AUDIO_CHANNELS_AS_IS),
                        listener)
                ).get();
            }
        });
    }

    @Test()
    public void CrossfadeStitch() {
        runTest(new Transcode() {
            @Override
            public void run() throws IOException, InterruptedException, ExecutionException {
                TLog.d(TAG, "@Test " + "CrossfadeStitch");
                String outputFileName = InstrumentationRegistry.getInstrumentation().getTargetContext().getExternalFilesDir(null) + "/output_CrossfadeStitch.mp4";
                cleanup(outputFileName);
                ParcelFileDescriptor in1 = ParcelFileDescriptor.open(new File(inputFileName1), ParcelFileDescriptor.MODE_READ_ONLY);
                ParcelFileDescriptor in2 = ParcelFileDescriptor.open(new File(inputFileName2), ParcelFileDescriptor.MODE_READ_ONLY);
                TimeLine timeline = new TimeLine(LogLevelForTests)
                        .addChannel("A", in1.getFileDescriptor())
                        .addChannel("B", in1.getFileDescriptor())
                        .addChannel("C", in1.getFileDescriptor())
                        .addAudioOnlyChannel("D", in2.getFileDescriptor())
                    .createSegment()
                        .output("C")
                        .output("D")
                        .duration(1000)
                    .timeLine().createSegment()
                        .output("C", TimeLine.Filter.OPACITY_DOWN_RAMP)
                        .output("A", TimeLine.Filter.OPACITY_UP_RAMP)
                        .output("D")
                        .duration(2000)
                    .timeLine().createSegment()
                        .duration(1500)
                        .output("A")
                        .output("D")
                    .timeLine().createSegment()
                        .seek("B", 1000)
                        .output("B")
                        .duration(1500)
                        .output("D")
                    .timeLine();
                (MediaTranscoder.getInstance().transcodeVideo(
                        timeline, outputFileName,
                        MediaFormatStrategyPresets.createAndroid16x9Strategy720P(
                                Android16By9FormatStrategy.AUDIO_BITRATE_AS_IS,
                                Android16By9FormatStrategy.AUDIO_CHANNELS_AS_IS),
                        listener)
                ).get();
            }
        });
    }

    @Test()
    public void CrossfadeStitchMute() {
        runTest(new Transcode() {
            @Override
            public void run() throws IOException, InterruptedException, ExecutionException {
                TLog.d(TAG, "@Test " + "CrossfadeStitchMute");
                String outputFileName = InstrumentationRegistry.getInstrumentation().getTargetContext().getExternalFilesDir(null) + "/output_CrossfadeStitch.mp4";
                cleanup(outputFileName);
                ParcelFileDescriptor in1 = ParcelFileDescriptor.open(new File(inputFileName1), ParcelFileDescriptor.MODE_READ_ONLY);
                ParcelFileDescriptor in2 = ParcelFileDescriptor.open(new File(inputFileName2), ParcelFileDescriptor.MODE_READ_ONLY);
                TimeLine timeline = new TimeLine(LogLevelForTests)
                        .addChannel("A", in1.getFileDescriptor())
                        .addChannel("B", in1.getFileDescriptor())
                        .addChannel("C", in1.getFileDescriptor())
                        .addAudioOnlyChannel("D", in2.getFileDescriptor())
                .createSegment()
                        .output("C")
                        .output("D")
                        .duration(1000)
                .timeLine().createSegment()
                        .output("C", TimeLine.Filter.OPACITY_DOWN_RAMP)
                        .output("A", TimeLine.Filter.OPACITY_UP_RAMP)
                        .output("D")
                        .duration(2000)
                .timeLine().createSegment()
                        .duration(1500)
                        .output("A",TimeLine.Filter.MUTE)
                        .output("D")
                .timeLine().createSegment()
                        .seek("B", 1000)
                        .output("B")
                        .duration(1500)
                        .output("D")
                .timeLine();
                (MediaTranscoder.getInstance().transcodeVideo(
                        timeline, outputFileName,
                        MediaFormatStrategyPresets.createAndroid16x9Strategy720P(
                                Android16By9FormatStrategy.AUDIO_BITRATE_AS_IS,
                                Android16By9FormatStrategy.AUDIO_CHANNELS_AS_IS),
                        listener)
                ).get();
            }
        });
    }

    @Test()
    public void CrossfadeStitchDownMix() {
        runTest(new Transcode() {
            @Override
            public void run() throws IOException, InterruptedException, ExecutionException {
                TLog.d(TAG, "@Test " + "CrossfadeStitchDownMix");
                String outputFileName = InstrumentationRegistry.getInstrumentation().getTargetContext().getExternalFilesDir(null) + "/output_CrossfadeStitch2.mp4";
                cleanup(outputFileName);
                ParcelFileDescriptor in1 = ParcelFileDescriptor.open(new File(inputFileName1), ParcelFileDescriptor.MODE_READ_ONLY);
                ParcelFileDescriptor in2 = ParcelFileDescriptor.open(new File(inputFileName2), ParcelFileDescriptor.MODE_READ_ONLY);
                TimeLine timeline = new TimeLine(LogLevelForTests)
                        .addChannel("A", in1.getFileDescriptor())
                        .addChannel("B", in1.getFileDescriptor())
                        .addChannel("C", in1.getFileDescriptor())
                        .addAudioOnlyChannel("D", in2.getFileDescriptor())
                        .createSegment()
                        .output("C")
                        .output("D")
                        .duration(1000)
                        .timeLine().createSegment()
                        .output("C", TimeLine.Filter.OPACITY_DOWN_RAMP)
                        .output("A", TimeLine.Filter.OPACITY_UP_RAMP)
                        .output("D")
                        .duration(2000)
                        .timeLine().createSegment()
                        .duration(1500)
                        .output("A")
                        .output("D")
                        .timeLine().createSegment()
                        .seek("B", 1000)
                        .output("B")
                        .duration(1500)
                        .output("D")
                        .timeLine();
                (MediaTranscoder.getInstance().transcodeVideo(
                        timeline, outputFileName,
                        MediaFormatStrategyPresets.createAndroid16x9Strategy720P(
                                Android16By9FormatStrategy.AUDIO_BITRATE_AS_IS,
                                1),
                        listener)
                ).get();
            }
        });
    }

    @Test()
    public void CrossfadeStitchUpMix() {
        runTest(new Transcode() {
            @Override
            public void run() throws IOException, InterruptedException, ExecutionException {
                TLog.d(TAG, "@Test " + "CrossfadeStitchUpMix");
                String outputFileName = InstrumentationRegistry.getInstrumentation().getTargetContext().getExternalFilesDir(null) + "/output_CrossfadeStitch3.mp4";
                cleanup(outputFileName);
                ParcelFileDescriptor in1 = ParcelFileDescriptor.open(new File(inputFileName1), ParcelFileDescriptor.MODE_READ_ONLY);
                ParcelFileDescriptor in2 = ParcelFileDescriptor.open(new File(inputFileName3), ParcelFileDescriptor.MODE_READ_ONLY);
                TimeLine timeline = new TimeLine(LogLevelForTests)
                        .addChannel("A", in1.getFileDescriptor())
                        .addChannel("B", in1.getFileDescriptor())
                        .addChannel("C", in1.getFileDescriptor())
                        .addAudioOnlyChannel("D", in2.getFileDescriptor())
                        .createSegment()
                        .output("C")
                        .output("D")
                        .duration(1000)
                        .timeLine().createSegment()
                        .output("C", TimeLine.Filter.OPACITY_DOWN_RAMP)
                        .output("A", TimeLine.Filter.OPACITY_UP_RAMP)
                        .output("D")
                        .duration(2000)
                        .timeLine().createSegment()
                        .duration(1500)
                        .output("A")
                        .output("D")
                        .timeLine().createSegment()
                        .seek("B", 1000)
                        .output("B")
                        .duration(1500)
                        .output("D")
                        .timeLine();
                (MediaTranscoder.getInstance().transcodeVideo(
                        timeline, outputFileName,
                        MediaFormatStrategyPresets.createAndroid16x9Strategy720P(
                                Android16By9FormatStrategy.AUDIO_BITRATE_AS_IS,
                                Android16By9FormatStrategy.AUDIO_CHANNELS_AS_IS),
                        listener)
                ).get();
            }
        });
    }

    @Test()
    public void HopScotch() {
        runTest(new Transcode() {
            @Override
            public void run() throws IOException, InterruptedException, ExecutionException {
            TLog.d(TAG, "@Test " + "HopScotch");
            String outputFileName = InstrumentationRegistry.getInstrumentation().getTargetContext().getExternalFilesDir(null) + "/output_HopScotch.mp4";
            cleanup(outputFileName);
            ParcelFileDescriptor in1 = ParcelFileDescriptor.open(new File(inputFileName1), ParcelFileDescriptor.MODE_READ_ONLY);
            TimeLine timeline = new TimeLine(LogLevelForTests)
                .addChannel("A", in1.getFileDescriptor())
                .addChannel("B", in1.getFileDescriptor())
                .createSegment()
                    .output("A")
                    .duration(500)
                .timeLine().createSegment()
                    .output("A", TimeLine.Filter.OPACITY_DOWN_RAMP)
                    .seek("B", 750)
                    .output("B", TimeLine.Filter.OPACITY_UP_RAMP)
                    .duration(500)
                .timeLine().createSegment()
                    .duration(500)
                    .output("B")
                .timeLine().createSegment()
                    .output("B", TimeLine.Filter.OPACITY_DOWN_RAMP)
                    .seek("A", 750)
                    .output("A", TimeLine.Filter.OPACITY_UP_RAMP)
                    .duration(500)
                .timeLine().createSegment()
                    .duration(500)
                    .output("A")
                .timeLine();
            (MediaTranscoder.getInstance().transcodeVideo(
                    timeline, outputFileName,
                    MediaFormatStrategyPresets.createAndroid16x9Strategy720P(Android16By9FormatStrategy.AUDIO_BITRATE_AS_IS, Android16By9FormatStrategy.AUDIO_CHANNELS_AS_IS),
                    listener)
            ).get();
            }
        });
    }
/*
    @Test()
    public void HopScotch2() {
        runTest(new Transcode() {
            @Override
            public void run() throws IOException, InterruptedException, ExecutionException {
                TLog.d(TAG, "@Test " + "HopScotch2");
                String outputFileName = InstrumentationRegistry.getInstrumentation().getTargetContext().getExternalFilesDir(null) + "/output_HopScotch2.mp4";
                cleanup(outputFileName);
                ParcelFileDescriptor in1 = ParcelFileDescriptor.open(new File(inputFileName1), ParcelFileDescriptor.MODE_READ_ONLY);
                TimeLine timeline = new TimeLine(LogLevelForTests)
                        .addChannel("A", in1.getFileDescriptor())
                        .addChannel("B", in1.getFileDescriptor())
                        .createSegment()
                            .output("A")
                            .duration(1000)
                        .timeLine().createSegment()
                            .output("A", TimeLine.Filter.OPACITY_DOWN_RAMP)
                            .seek("B", 1000)
                            .output("B", TimeLine.Filter.OPACITY_UP_RAMP)
                            .duration(500)
                        .timeLine().createSegment()
                            .duration(1500)
                            .output("A")
                            .timeLine();
                (MediaTranscoder.getInstance().transcodeVideo(
                        timeline, outputFileName,
                        MediaFormatStrategyPresets.createAndroid16x9Strategy720P(Android16By9FormatStrategy.AUDIO_BITRATE_AS_IS, Android16By9FormatStrategy.AUDIO_CHANNELS_AS_IS),
                        listener)
                ).get();
            }
        });
    }

    @Test()
    public void HopScotch3() {
        runTest(new Transcode() {
            @Override
            public void run() throws IOException, InterruptedException, ExecutionException {
                TLog.d(TAG, "@Test " + "HopScotch3");
                String outputFileName = InstrumentationRegistry.getInstrumentation().getTargetContext().getExternalFilesDir(null) + "/output_HopScotch3.mp4";
                cleanup(outputFileName);
                ParcelFileDescriptor in1 = ParcelFileDescriptor.open(new File(inputFileName1), ParcelFileDescriptor.MODE_READ_ONLY);
                TimeLine timeline = new TimeLine(LogLevelForTests)
                        .addChannel("A0", in1.getFileDescriptor())
                        .addChannel("A1", in1.getFileDescriptor())
                        .addChannel("A2", in1.getFileDescriptor())
                        .createSegment()
                        .output("A0")
                        .duration(3250)
                        .timeLine().createSegment()
                        .output("A0", TimeLine.Filter.OPACITY_DOWN_RAMP)
                        .output("A1", TimeLine.Filter.OPACITY_UP_RAMP)
                        .duration(1750)
                        .timeLine().createSegment()
                        .output("A1")
                        .duration(3250)
                        .timeLine().createSegment()
                        .output("A1", TimeLine.Filter.OPACITY_DOWN_RAMP)
                        .output("A2", TimeLine.Filter.OPACITY_UP_RAMP)
                        .duration(1750)
                        .timeLine().createSegment()
                        .output("A2")
                        .duration(3250)
                        .timeLine();
                (MediaTranscoder.getInstance().transcodeVideo(
                        timeline, outputFileName,
                        MediaFormatStrategyPresets.createAndroid16x9Strategy720P(Android16By9FormatStrategy.AUDIO_BITRATE_AS_IS, Android16By9FormatStrategy.AUDIO_CHANNELS_AS_IS),
                        listener)
                ).get();
            }
        });
    }

//@Test()
public void ThreeFiles() {
    runTest(new Transcode() {
        @Override
        public void run() throws IOException, InterruptedException, ExecutionException {
            TLog.d(TAG, "@Test " + "ThreeFiles");
            String outputFileName = InstrumentationRegistry.getInstrumentation().getTargetContext().getExternalFilesDir(null) + "/fish13.mp4";
            cleanup(outputFileName);
            ParcelFileDescriptor in1 = ParcelFileDescriptor.open(new File("/storage/emulated/0/DCIM/Camera/20171031_173205.mp4"), ParcelFileDescriptor.MODE_READ_ONLY);
            ParcelFileDescriptor in2 = ParcelFileDescriptor.open(new File("/storage/emulated/0/DCIM/Camera/20171031_173205.mp4"), ParcelFileDescriptor.MODE_READ_ONLY);
            TimeLine timeline = new TimeLine(LogLevelForTests)
                    .addChannel("A1", in1.getFileDescriptor())
                    .addChannel("A2", in2.getFileDescriptor())
                    .createSegment()
                        .output("A1")
                        .duration(5858 - 750)
                        .seek("A1", 22524)
                        .timeLine()
                    .createSegment()
                        .output("A1", TimeLine.Filter.OPACITY_DOWN_RAMP)
                        .output("A2", TimeLine.Filter.OPACITY_UP_RAMP)
                        .seek("A2", 22524 + 5858 + 8251)
                        .duration(750)
                        .timeLine()
                    .createSegment()
                        .output("A1")
                        .seek("A1", 8251 + 750)
                        .duration(5000)
                        .timeLine();
              (MediaTranscoder.getInstance().transcodeVideo(
                    timeline, outputFileName,
                    MediaFormatStrategyPresets.createAndroid16x9Strategy720P(Android16By9FormatStrategy.AUDIO_BITRATE_AS_IS, Android16By9FormatStrategy.AUDIO_CHANNELS_AS_IS),
                    listener)
            ).get();
        }
    });

}
*/

    public interface Transcode {
        void run () throws IOException, InterruptedException, ExecutionException;
    }
    private void runTest(Transcode callback) {
        try {
            callback.run();
        } catch(IOException e) {
            assertEquals("Exception on Transcode", "none", e + Log.getStackTraceString(e));
        } catch(InterruptedException e) {
            assertEquals("Exception on Transcode", "none", e + Log.getStackTraceString(e));
        } catch(ExecutionException e) {
            assertEquals("Exception on Transcode", "none", e + Log.getStackTraceString(e));

        }
    }


    // Helpers
    private static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }
    private static void cleanup(String fileName) {
        (new File(fileName)).delete();
    }
}
