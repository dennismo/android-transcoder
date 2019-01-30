android-transcoder
=================

Hardware accelerated transcoder for Android, written in pure Java rathwr than using FFmpeg.
 Forked from the net.ypresto.androidtranscode,
 this library provides additional editing capabilities including:

 * Combining multiple files
 * Including only individual segments from files
 * Cross fades between individusl segments and files


## Usage

```java
    MediaTranscoder.getInstance().transcodeVideo(fileDescriptor, file.getAbsolutePath(),
            MediaFormatStrategyPresets.createAndroid720pStrategy(), listener); // or createAndroid720pStrategy([your bitrate here])
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

See `TranscoderActivity.java` in example directory for ready-made transcoder app.

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

- This library raises `RuntimeException`s (like `IlleagalStateException`) in various situations. Please catch it and provide alternate logics. I know this is bad design according to Effective Java; just is TODO.
- Currently this library does not generate streaming-aware mp4 file.
Use [qtfaststart-java](https://github.com/ypresto/qtfaststart-java) to place moov atom at beginning of file.
- Android does not gurantee that all devices have bug-free codecs/accelerators for your codec parameters (especially, resolution). Refer [supported media formats](http://developer.android.com/guide/appendix/media-formats.html) for parameters guaranteed by [CTS](https://source.android.com/compatibility/cts-intro.html).
- This library may not support video files recorded by other device like digital cameras, iOS (mov files, including non-baseline profile h.264), etc.

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
