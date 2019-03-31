android-transcoder
=================

Hardware accelerated transcoder for Android that uses MediaCodec rather than FFMPeg.  With it you can

 * Combine multiple files with cross-fades
 * Splice out portions of the file at the beginning middle or end
 * Change video formats (transcode) in the process
 * Do time scaling both speeding up and slowing down portions


## Usage

```java
    MediaTranscoder.getInstance().transcodeVideo(fileDescriptor, file.getAbsolutePath(),
            MediaFormatStrategyPresets.createAndroid720pStrategy(), listener); 

// or createAndroid720pStrategy([your bitrate here])
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
```

More documentation will be forthcoming.  For now refer to SingleFileTransactionTest.java

## Quick Setup

### Gradle

Available from jitpack.io

```groovy
repositories {
  maven {
    name "jitpack"
    url "https://jitpack.io"
  }
}
dependencies {
  compile 'com.github.selsamman:android-transcoder:0.3.57'
}
```




## Note (PLEASE READ FIRST)

This library is still rough around the edges so please be aware that
 * It may not process all formats of video
 * It has limited output formats at present
 * Incorrect parameters (especially time values) can raise exceptions
 
## License

```
Copyright (C) 2016-2019 Sam Elsamman
Copyright (C) 2014-2016 Yuya Tanaka

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

## References for Android Low-Level Media APIs

- http://bigflake.com/mediacodec/
- https://github.com/google/grafika
- https://android.googlesource.com/platform/frameworks/av/+/lollipop-release/media/libstagefright
