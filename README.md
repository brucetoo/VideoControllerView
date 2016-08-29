# Expandable custom Media Controller View
  [![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-VideoControllerView-green.svg?style=flat)](https://android-arsenal.com/details/1/2668)
# FINAL EFFECT
[YOUTUBE](https://www.youtube.com/watch?v=Cew5WQY3_ws)
Control brightness or volume,or progress

![volume](./demo.gif)

##Feature

- [x] Single tap to show VideoControllerView

- [x] Swipe up-down left edge of screen to control Brightness

- [x] Swipe up-down right edge of screen to control Volume

- [x] Swipe left-right to control media progress

- [x] Show loading before media prepared(In fact,it's not necessary to build in ControllerView)

- [ ] More custom things may be added(Like custom icon,swipe Constant of brightness,volume,progress.)

In short,easy to integrate

------

##How to use

Config VideoControllerView like this

  ```java
  
  
     controller = new VideoControllerView.Builder(this, this)
                    .withVideoTitle("Buck Bunny")
                    .withVideoSurfaceView(mVideoSurface)//to enable toggle display controller view
                    .canControlBrightness(true)
                    .canControlVolume(true)
                    .canSeekVideo(true)
                    .exitIcon(R.drawable.video_top_back)
                    .pauseIcon(R.drawable.ic_media_pause)
                    .playIcon(R.drawable.ic_media_play)
                    .shrinkIcon(R.drawable.ic_media_fullscreen_shrink)
                    .stretchIcon(R.drawable.ic_media_fullscreen_stretch)
                    .build((FrameLayout) findViewById(R.id.videoSurfaceContainer));//layout container that hold video play view
  
      /**
       * Implement VideoMediaController.MediaPlayerControl
        */
  
      @Override
      public boolean canSeekProgress() {
          return true;
      }
  
  
      @Override
      public int getBufferPercentage() {
          return 0;
      }
  
      @Override
      public int getCurrentPosition() {
           if(null != mMediaPlayer)
             return mMediaPlayer.getCurrentPosition();
          else
             return 0;
      }
  
      @Override
      public int getDuration() {
          if(null != mMediaPlayer)
              return mMediaPlayer.getDuration();
          else
              return 0;
      }
  
      @Override
      public boolean isPlaying() {
          if(null != mMediaPlayer)
              return mMediaPlayer.isPlaying();
          else
              return false;
      }
  
      @Override
      public void pause() {
          if(null != mMediaPlayer) {
              mMediaPlayer.pause();
          }
  
      }
  
      @Override
      public void seekTo(int i) {
          if(null != mMediaPlayer) {
              mMediaPlayer.seekTo(i);
          }
      }
  
      @Override
      public void start() {
          if(null != mMediaPlayer) {
              mMediaPlayer.start();
          }
      }
  
      @Override
      public boolean isFullScreen() {
          return getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE ? true : false;
      }
  
      @Override
      public void toggleFullScreen() {
         if(isFullScreen()){
             setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
         }else {
             setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
         }
      }
  
      @Override
      public void exit() {
          resetPlayer();
          finish();
      }
      // End VideoMediaController.MediaPlayerControl
  
  ```


#THANKS
ExampleMediaController from [ExampleMediaController](https://github.com/brightec/ExampleMediaController)

## License

Copyright 2015 Bruce too

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

See [LICENSE](LICENSE) file for details.