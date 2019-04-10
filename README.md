android-transcoder
=================

Hardware accelerated transcoder for Android that uses MediaCodec rather than FFMPeg.  With it you can

 * Combine multiple files with cross-fades
 * Splice out portions of the file at the beginning middle or end
 * Change video formats (transcode) in the process
 * Do time scaling both speeding up and slowing down portions

The library is forked from the ypresto transcoder and while it follows the same module structure internally it has been substantially reworked to support multiple streams.  If your needs are primarily for a transcoder and don't require the composition editing that library would probably be your best bet. 
## Usage

```java
    

            ParcelFileDescriptor in1 = ParcelFileDescriptor.open(
                    new File(inputFileName1), 
                    ParcelFileDescriptor.MODE_READ_ONLY);

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
            
               MediaTranscoder.Listener listener = new MediaTranscoder.Listener() {
                  @Override
                  public void onTranscodeProgress(double progress) {
                      Log.d(TAG, "Progress Suppressed " + progress);
                  }
                  @Override
                  public void onTranscodeCompleted() {
                      Log.d(TAG, "Finished);
                  }
                  @Override
                  public void onTranscodeCanceled() {
                      Log.d(TAG, "Canceled");
                  }
                  @Override
                  public void onTranscodeFailed(Exception e) {
                      Log.d(TAG, "Exception");
                  }
                };
               
            MediaTranscoder.getInstance().transcodeVideo(
                    timeline, outputFileName,
                    MediaFormatStrategyPresets.createAndroid16x9Strategy720P(
                            Android16By9FormatStrategy.AUDIO_BITRATE_AS_IS, 
                            Android16By9FormatStrategy.AUDIO_CHANNELS_AS_IS),
                    listener);
  

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
  compile 'com.github.selsamman:android-transcoder:0.3.58'
}
```
See the releases for the latest release
## Note (PLEASE READ FIRST)

This library is still rough around the edges so please be aware that
 * It may not process all formats of video
 * It has limited output formats at present
 * Incorrect parameters (especially time values) can raise exceptions
 * It is possible that some breaking changes in the API may occur before we reach a 1.0
 * Contributors are very welcome! 
### TimeLine

To transcode you need a timeline which defines what tracks will appear in the final composition and where they will appear. The TimeLine consists of a sequential set of **segments** that are concatenated together without gaps to define the time sequence of the composition.

You create a timeline by instantiating a new TimeLine object and specifying the level of detail you want in the logs:

````java
TimeLine (int logLevel);
````

***logLevel*** is an int value:
* 6 - Log only errors
* 5 - log errors and warnings (suitable for production)
* 4 - also log information level items such as the setup of each segment
* 3 - log debug information which can be quite chatty
* 4 - maximum logging down the frame and buffer level

The TimeLine object returned can be used to 
* attach assets using the **addChannel** method
* create time segments using the **createSegment** method

### addChannel

Any video assets used in the transcoding must first be added using the addChannel method of the TimeLine.

````java
TimeLine addChannel(String channelName, Fileescriptor fileDescriptor);
TimeLine addChannel(String channelName, FileDescriptor, ChannelType channelType);
````

* ***channelName*** is a string used to refer to the channel in subsequent segments
* ***fileDescriptor*** is the file descriptor of the input track.  You can specify the same fileDescriptor more than once to create overlapping sequences such as for a cross-fade.
* ***channelType*** is an enum which values of public enum VIDEO, AUDIO, AUDIO_VIDEO, IMAGE (Image is not yet supported). It defaults AUDIO_VIDEO if no channelType is specified.  You can use VIDEO to suppress the audio of a video file and AUDIO to suppress the video.

***addChannel*** returns a ***TimeLine*** object so you can chain additional channels or create a segment.

### createSegment

Multiple segments be attached to the TimeLine to define the individual sequential portions of the composition.  You create a segment by calling createSegment.

````Java
    Segment createSegment();
```` 

A Segment object is returned and you can then define the characteristics of the semgnet by calling these Segment methods:

* ***duration*** defines the length of the segment in milliseconds
* ***output*** defines which channels are to be output
* ***seek*** causes portions of the input track to be skipped

Since each of these methods returns a Segment object you can chain them together and when you are finished chain the ***timeline*** method to get back to a TimeLine

```java
    .timeLine().createSegment()
        .output("B", TimeLine.Filter.OPACITY_DOWN_RAMP)
        .seek("A", 750)
        .output("A", TimeLine.Filter.OPACITY_UP_RAMP)
        .duration(500)
    .timeLine()
```` 

### duration

The ***duration*** method of the ***Segment*** defines the duration of the segment.  

````java
    Segment duration(long timeInMilliseconds)
````
***Duration*** is optional and if omitted the duration will be the remaining portion of the track.  It is generally OK if the duration is larger than the remaining time in the track, however the segment will end when any of the tracks reach the end so when doing complex operations like combining tracks for cross-fades it is important for the duration to be correct.
### seek

The ***seek*** method of the ***Segment*** defines an input track that you want to skip some part of and the amount you want to skip

```java
    Segment seek(String channelName, long amountToSeek);
```` 
* ***channelName*** is the name of a channel defined using ***addChannel***
* ***amountToSeek*** is the number of milliseconds to skip over

Note that at present it is critical that you not seek past the end of the track and then have subsequent segments as there are important presentation time calculations that depend on decoding at least part of each track in a segment.

### output

***Output*** defines which channels are to be output as part of this segment as well as any special filtering that should occur on them

````Java
    Segment output(String channelName);
    Segment output(String channelName, Filter filter);
````
* ***channelName*** is the name of a channel defined using ***addChannel***
* ***filter*** is the special handling for the track during this segment and may be one of these values:
  * ***OPACITY_UP_RAMP***  increase opacity from transparent to non-opaque over the course of the segment
  * ***OPACITY_DOWN_RAMP*** decrease opacity from non-opaque down to transparent over the course of the segment
  
### timeScale

The ***timeScale*** Segment method applies to the most recently added channel in the segment added with ***output*** and specifies the amount of the input track to be consumed.  

```java
    Segment timeScale(long inputDuration);
``` 

The difference between the duration of the segment as specified by ***duration*** and the duration of the track to be consumed during the course of processing the segment consitutes times scaling.  If the timeScale duration is larger the output will appear in fast motion and if it is smaller it will appear in slow motion.


### Transcoding
Once you have built up your ***TimeLine*** you transcode it using the  ***MediaTranscoder*** class.  It has a static ***getInstance*** method to get an instance.  You then call the ***transcodeVideo*** method to do the transcoding 

 ```java
 Future<Void> transcodeVideo(final TimeLine timeLine, 
    final String outPath, 
    final MediaFormatStrategy outFormatStrategy, 
    final Listener listener);
 ```
* ***timeLine*** is the time line that defines the composition
* ***outPath*** is the path of the output file to be created
* ***outFormatStrategy*** defines the output format
* ***lister*** is a callback for monitoring progress 
 
 The ***transcode*** method returns a ***Future*** so you need to invoke the get() method if you want to wait for the transcode to complete.  

The output formats are best derived from ***MediaFormatStrategyPresets*** which include static methods to create these strategies:
* ***MediaFormatStrategy createAndroid720pStrategy();***  
create 720P videos
* ***MediaFormatStrategy createAndroid16x9Strategy720P(int audioBitrate, int audioChannels);***  
create 720P video and specify audio parameters
* ***MediaFormatStrategy createAndroid16x9Strategy1080P(int audioBitrate, int audioChannels);***   
create 720P video and specify audio parameters

Note that you can specify ndroid16By9FormatStrategy.AUDIO_BITRATE_AS_IS and Android16By9FormatStrategy.AUDIO_CHANNELS_AS_IS if you don't want to keep the audio format unchanged.

The listener supports methods for monitoring progress, cancel (not implemented at present) and completion)

```java
 public interface Listener {
        void onTranscodeProgress(double progress);
        void onTranscodeCompleted();
        void onTranscodeCanceled();
        void onTranscodeFailed(Exception exception);
    }
```
### Tests

The example/src/androidTest/java/net.ypresto.androidtranscoder/tests/SingleFileTranscoderTest is a single file with lots of test cases in it.  To run it:
 
 * Go to run/debug configurations in Android Studio
 * Add and add an Instrumented Test
 * Set the Module: to the example folder
 * Set the Test: to Class
 * set the Class: to net.ypresto.androidtranscoder.tests.SingleFileTranscoderTest
 
 You can now run this on the device of your choice.  Best to test on an actual device.
 
 Would appreciate forking and creating a failing test if any transcoding issues are found.  You can use /storage/emulated/0/DCIM/Camera/ as the directory of the file description to run a test that transcodes a file in your gallery.   You can add new files in androidTest/res/Raw and then copy them using this pattern that is in the  @BeforeClass retrieveVideo section of the test file
 
 ```java
         try {
             InputStream in = InstrumentationRegistry.getContext().getResources().openRawResource(
                     net.ypresto.androidtranscoder.example.test.R.raw.poolcleaner);
             OutputStream out = new FileOutputStream(inputFileName1);
             copyFile(in, out);
             in.close();
             out.close();
         } catch(IOException e) {
             assertEquals("Exception on file copy", "none", e + Log.getStackTraceString(e));
         }
```` 
### License


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

### References for Android Low-Level Media APIs

* http://bigflake.com/mediacodec/
* https://github.com/google/grafika
* https://android.googlesource.com/platform/frameworks/av/+/lollipop-release/media/libstagefright
