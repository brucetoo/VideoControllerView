# Expandable custom Media Controller View
  
# FINAL EFFECT
[YOUTUBE](https://www.youtube.com/watch?v=Cew5WQY3_ws)
Control brightness or volume,gif will be here later

![volume](./volume.png)

##Feature

- [x] Single tap to show VideoControllerView

- [x] Swipe up-down left edge of screen to control Brightness

- [x] Swipe up-down right edge of screen to control Volume

- [x] Swipe left-right to control media progress

- [x] Show loading before media prepared(In fact,it's not necessary to build in ControllerView)

In short,easy to integrate

------

##How to use
> 1.init VideoControllerView like this
  ```java
  controller = new VideoControllerView(this);
  
      /**
       * Implement VideoMediaController.MediaPlayerControl
        */
      @Override
      public boolean canPause() {
          return true;
      }
  
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
  
      @Override
      public String getTopTitle() {
          return "buck bunny".toUpperCase();
      }
      // End VideoMediaController.MediaPlayerControl
  
  ```
> 2.after media prepared

  ```java
  // Implement MediaPlayer.OnPreparedListener
      @Override
      public void onPrepared(MediaPlayer mp) {
         //set media player control listen
         controller.setMediaPlayerControlListener(this);
         //set anchor view that hold VideoControllerView
         controller.setAnchorView((FrameLayout) findViewById(R.id.videoSurfaceContainer));
         //if you want to use gesture to control brightness,volume,progress..set this 
         controller.setGestureListener(this);
         //start media
         mMediaPlayer.start();
      }
  // End MediaPlayer.OnPreparedListener
  
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