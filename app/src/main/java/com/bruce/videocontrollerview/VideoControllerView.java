package com.bruce.videocontrollerview;

/**
 * Created by Brucetoo
 * On 2015/10/19
 * At 16:33
 */

import android.animation.LayoutTransition;
import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.Formatter;
import java.util.Locale;

public class VideoControllerView extends FrameLayout implements VideoGestureListener {

    private static final String TAG = "VideoControllerView";

    private static final int HANDLER_ANIMATE_OUT = 1;// out animate
    private static final int HANDLER_UPDATE_PROGRESS = 2;//cycle update progress
    private static final long PROGRESS_SEEK = 5000;
    private MediaPlayerControlListener mPlayer;// control media play
    private Activity mContext;
    private ViewGroup mAnchorView;//anchor view
    private View mRootView; // root view of this
    private SeekBar mSeekBar;
    private TextView mEndTime, mCurrentTime;
    private boolean mShowing;//controller view showing?
    private boolean mDragging;
    private StringBuilder mFormatBuilder;
    private Formatter mFormatter;
    private GestureDetector mGestureDetector;
    private VideoGestureListener mVideoGestureListener;
    //top layout
    private View mTopLayout;//this can custom animate layout
    private ImageButton mBackButton;
    private TextView mTitleText;
    //center layout
    private View mCenterLayout;
    private ImageView mCenterImage;
    private ProgressBar mCenterPorgress;
    private float mCurBrightness;
    private float mCurVolume;
    private AudioManager mAudioManager;
    private int mMaxVolume;
    //bottom layout
    private View mBottomLayout;
    private ImageButton mPauseButton;
    private ImageButton mFullscreenButton;
    private Handler mHandler = new ControllerViewHandler(this);

    public VideoControllerView(Activity context, AttributeSet attrs) {
        super(context, attrs);
        mRootView = null;
        mContext = context;
        Log.i(TAG, TAG);
    }

    public VideoControllerView(Activity context, boolean useFastForward) {
        super(context);
        mContext = context;
        Log.i(TAG, TAG);
    }

    public VideoControllerView(Activity context) {
        this(context, true);
        Log.i(TAG, TAG);
    }


    /**
     * Handler prevent leak memory.
     */
    private static class ControllerViewHandler extends Handler {
        private final WeakReference<VideoControllerView> mView;

        ControllerViewHandler(VideoControllerView view) {
            mView = new WeakReference<VideoControllerView>(view);
        }

        @Override
        public void handleMessage(Message msg) {
            VideoControllerView view = mView.get();
            if (view == null || view.mPlayer == null) {
                return;
            }

            int pos;
            switch (msg.what) {
                case HANDLER_ANIMATE_OUT:
                    view.hide();
                    break;
                case HANDLER_UPDATE_PROGRESS://cycle update seek bar progress
                    pos = view.setSeekProgress();
                    if (!view.mDragging && view.mShowing && view.mPlayer.isPlaying()) {//just in case
                        //cycle update
                        msg = obtainMessage(HANDLER_UPDATE_PROGRESS);
                        sendMessageDelayed(msg, 1000 - (pos % 1000));
                    }
                    break;
            }
        }
    }

    /**
     * init controller view
     * @return
     */
    protected View makeControllerView() {
        LayoutInflater inflate = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRootView = inflate.inflate(R.layout.media_controller, null);
        initControllerView(mRootView);

        return mRootView;
    }

    private void initControllerView(View v) {
        //top layout
        mTopLayout = v.findViewById(R.id.layout_top);
        mBackButton = (ImageButton) v.findViewById(R.id.top_back);
        if(mBackButton != null){
            mBackButton.requestFocus();
            mBackButton.setOnClickListener(mBackListener);
        }

        mTitleText = (TextView) v.findViewById(R.id.top_title);

        //center layout
        mCenterLayout = v.findViewById(R.id.layout_center);
        mCenterLayout.setVisibility(GONE);
        mCenterImage = (ImageView) v.findViewById(R.id.image_center_bg);
        mCenterPorgress = (ProgressBar) v.findViewById(R.id.progress_center);

        //bottom layout
        mBottomLayout = v.findViewById(R.id.layout_bottom);
        mPauseButton = (ImageButton) v.findViewById(R.id.bottom_pause);
        if (mPauseButton != null) {
            mPauseButton.requestFocus();
            mPauseButton.setOnClickListener(mPauseListener);
        }

        mFullscreenButton = (ImageButton) v.findViewById(R.id.bottom_fullscreen);
        if (mFullscreenButton != null) {
            mFullscreenButton.requestFocus();
            mFullscreenButton.setOnClickListener(mFullscreenListener);
        }

        mSeekBar = (SeekBar) v.findViewById(R.id.bottom_seekbar);
        if (mSeekBar != null) {
            SeekBar seeker = (SeekBar) mSeekBar;
            seeker.setOnSeekBarChangeListener(mSeekListener);
            mSeekBar.setMax(1000);
        }

        mEndTime = (TextView) v.findViewById(R.id.bottom_time);
        mCurrentTime = (TextView) v.findViewById(R.id.bottom_time_current);

        //init formatter
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
    }

    /**
     * show controller view
     */
    private void show() {
        if (!mShowing && mAnchorView != null) {

            //animate anchorview when layout changes
            //equals android:animateLayoutChanges="true"
            mAnchorView.setLayoutTransition(new LayoutTransition());
            setSeekProgress();
            if (mPauseButton != null) {
                mPauseButton.requestFocus();
                if(!mPlayer.canPause()){
                    mPauseButton.setEnabled(false);
                }
            }

            //add controller view to bottom of the AnchorView
            FrameLayout.LayoutParams tlp = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
//            (int) (mContext.getResources().getDisplayMetrics().density * 45)
            mAnchorView.addView(this, tlp);
            mShowing = true;//set view state
        }
        togglePausePlay();
        toggleFullScreen();
        //update progress
        mHandler.sendEmptyMessage(HANDLER_UPDATE_PROGRESS);

    }

    /**
     * Control if show controllerview
     */
    public void toggleContollerView(){
        if(!isShowing()){
            show();
        }else {
            //animate out controller view
            Message msg = mHandler.obtainMessage(HANDLER_ANIMATE_OUT);
            mHandler.removeMessages(HANDLER_ANIMATE_OUT);
            mHandler.sendMessageDelayed(msg, 100);
        }
    }

    private void animateOut() {
        TranslateAnimation trans = new TranslateAnimation(Animation.RELATIVE_TO_SELF,0,Animation.RELATIVE_TO_SELF,0,Animation.RELATIVE_TO_SELF,0,Animation.RELATIVE_TO_SELF,1);
        trans.setInterpolator(new AccelerateInterpolator());
        setAnimation(trans);
    }

    /**
     * get isShowing?
     * @return
     */
    public boolean isShowing() {
        return mShowing;
    }

    /**
     * hide controller view with animation
     * Just use LayoutTransition
       mAnchorView.setLayoutTransition(new LayoutTransition());
       equals android:animateLayoutChanges="true"
     */
    private void hide() {
        if (mAnchorView == null) {
            return;
        }

        try {
            mAnchorView.removeView(this);
            mHandler.removeMessages(HANDLER_UPDATE_PROGRESS);
        } catch (IllegalArgumentException ex) {
            Log.w("MediaController", "already removed");
        }
        mShowing = false;
    }

    /**
     * convert string to time
     * @param timeMs
     * @return
     */
    private String stringToTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    /**
     * set seekbar progress
     * @return
     */
    private int setSeekProgress() {
        if (mPlayer == null || mDragging) {
            return 0;
        }

        int position = mPlayer.getCurrentPosition();
        int duration = mPlayer.getDuration();
        if (mSeekBar != null) {
            if (duration > 0) {
                // use long to avoid overflow
                long pos = 1000L * position / duration;
                mSeekBar.setProgress((int) pos);
            }
            //get buffer percentage
            int percent = mPlayer.getBufferPercentage();
            //set buffer progress
            mSeekBar.setSecondaryProgress(percent * 10);
        }

        if (mEndTime != null)
            mEndTime.setText(stringToTime(duration));
        if (mCurrentTime != null)
            mCurrentTime.setText(stringToTime(position));

        mTitleText.setText(mPlayer.getTopTitle());
        return position;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(null != mGestureDetector){
           mGestureDetector.onTouchEvent(event);
        }
//        toggleContollerView();
        return true;
    }

    /**
     * toggle pause or play
     */
    private void togglePausePlay() {
        if (mRootView == null || mPauseButton == null || mPlayer == null) {
            return;
        }

        if (mPlayer.isPlaying()) {
            mPauseButton.setImageResource(R.drawable.ic_media_pause);
        } else {
            mPauseButton.setImageResource(R.drawable.ic_media_play);
        }
    }

    /**
     * toggle full screen or not
     */
    public void toggleFullScreen() {
        if (mRootView == null || mFullscreenButton == null || mPlayer == null) {
            return;
        }

        if (mPlayer.isFullScreen()) {
            mFullscreenButton.setImageResource(R.drawable.ic_media_fullscreen_shrink);
        } else {
            mFullscreenButton.setImageResource(R.drawable.ic_media_fullscreen_stretch);
        }
    }

    private void doPauseResume() {
        if (mPlayer == null) {
            return;
        }

        if (mPlayer.isPlaying()) {
            mPlayer.pause();
        } else {
            mPlayer.start();
        }
        togglePausePlay();
    }

    private void doToggleFullscreen() {
        if (mPlayer == null) {
            return;
        }

        mPlayer.toggleFullScreen();
    }

    /**
     * Seek bar drag listener
     */
    private OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {
        public void onStartTrackingTouch(SeekBar bar) {
            show();
            mDragging = true;
            mHandler.removeMessages(HANDLER_UPDATE_PROGRESS);
        }

        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
            if (mPlayer == null) {
                return;
            }

            if (!fromuser) {
                return;
            }

            long duration = mPlayer.getDuration();
            long newposition = (duration * progress) / 1000L;
            mPlayer.seekTo((int) newposition);
            if (mCurrentTime != null)
                mCurrentTime.setText(stringToTime((int) newposition));
        }

        public void onStopTrackingTouch(SeekBar bar) {
            mDragging = false;
            setSeekProgress();
            togglePausePlay();
            show();
            mHandler.sendEmptyMessage(HANDLER_UPDATE_PROGRESS);
        }
    };

    @Override
    public void setEnabled(boolean enabled) {
        if (mPauseButton != null) {
            mPauseButton.setEnabled(enabled);
        }
        if (mSeekBar != null) {
            mSeekBar.setEnabled(enabled);
        }
        super.setEnabled(enabled);
    }



    /**
     * set top back click listener
     */
    private View.OnClickListener mBackListener = new View.OnClickListener() {
        public void onClick(View v) {
            mPlayer.exit();
        }
    };


    /**
     * set pause click listener
     */
    private View.OnClickListener mPauseListener = new View.OnClickListener() {
        public void onClick(View v) {
            doPauseResume();
            show();
        }
    };

    /**
     * set full screen click listener
     */
    private View.OnClickListener mFullscreenListener = new View.OnClickListener() {
        public void onClick(View v) {
            doToggleFullscreen();
            show();
        }
    };

    /**
     * setMediaPlayerControlListener update play state
     * @param player self
     */
    public void setMediaPlayerControlListener(MediaPlayerControlListener player) {
        mPlayer = player;
        togglePausePlay();
        toggleFullScreen();
    }

    /**
     * set anchor view
     * @param view view that hold controller view
     */
    public void setAnchorView(ViewGroup view) {
        mAnchorView = view;
        FrameLayout.LayoutParams frameParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        //remove all before add view
        removeAllViews();
//        setBackgroundColor(Color.BLUE);
        View v = makeControllerView();
        addView(v, frameParams);
    }

    /**
     * set gesture listen to control media player
     * include screen brightness and volume of video
     * @param context
     */
    public void setGestureListener(Context context){
        mVideoGestureListener = this;
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mGestureDetector = new GestureDetector(context,new ViewGestureListener(context,mVideoGestureListener));
    }


  //implement ViewGestureListener
    @Override
    public void onSingleTap() {
         toggleContollerView();
    }

    @Override
    public void onHorizontalScroll(MotionEvent event, float delta) {
         if(event.getPointerCount() == 1 && mPlayer.canSeekProgress()){
             if(delta > 0){// seek forward
                 seekForWard();
             }else {  //seek backward
                 seekBackWard();
             }
         }
    }

    private void seekBackWard() {
        if (mPlayer == null) {
            return;
        }

        int pos = mPlayer.getCurrentPosition();
        pos -= PROGRESS_SEEK;
        mPlayer.seekTo(pos);
        setSeekProgress();

        show();
    }

    private void seekForWard() {
        if (mPlayer == null) {
            return;
        }

        int pos = mPlayer.getCurrentPosition();
        pos += PROGRESS_SEEK;
        mPlayer.seekTo(pos);
        setSeekProgress();

        show();
    }

    @Override
    public void onVerticalScroll(MotionEvent motionEvent, float delta, int direction) {
           if(motionEvent.getPointerCount() == 1){
               if(direction == ViewGestureListener.SWIPE_LEFT){
                   mCenterImage.setImageResource(R.drawable.video_bright_bg);
                   updateBrightness(delta);
               }else {
                   mCenterImage.setImageResource(R.drawable.video_volume_bg);
                   updateVolume(delta);
               }
               postDelayed(new Runnable() {
                   @Override
                   public void run() {
                       mCenterLayout.setVisibility(GONE);
                   }
               },1000);
           }
    }

    private void updateVolume(float delta) {

       mCenterLayout.setVisibility(VISIBLE);
       mCurVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
       if(mCurVolume < 0){
           mCurVolume = 0;
       }

       Log.e("mCurVolume:",""+mCurVolume);
       Log.e("delta:",""+delta);
      int volume = (int) (delta * mMaxVolume/ViewGestureListener.getDeviceHeight(mContext) + mCurVolume);
        if(volume > mMaxVolume){
            volume = mMaxVolume;
        }

        if(volume < 0){
            volume = 0;
        }
      mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);

      float percent = (float) ((volume * 1.0 / mMaxVolume) * 100);
        Log.e("volume:",""+volume);
        Log.e("percent:",""+percent);
      mCenterPorgress.setProgress((int) percent);
    }

    /**
     * update brightness
     * @param delta
     */
    private void updateBrightness(float delta) {
        mCurBrightness = mContext.getWindow().getAttributes().screenBrightness;
        if(mCurBrightness <= 0.01f){
            mCurBrightness = 0.01f;
        }

        mCenterLayout.setVisibility(VISIBLE);

        WindowManager.LayoutParams attributes = mContext.getWindow().getAttributes();
        attributes.screenBrightness = mCurBrightness + delta/ViewGestureListener.getDeviceHeight(mContext);
        if(attributes.screenBrightness >= 1.0f){
            attributes.screenBrightness = 1.0f;
        }else if(attributes.screenBrightness <= 0.01f){
            attributes.screenBrightness = 0.01f;
        }
        mContext.getWindow().setAttributes(attributes);

        float percent = attributes.screenBrightness * 100;
        mCenterPorgress.setProgress((int) percent);

    }

    //end of ViewGestureListener

    /**
     * Interface of Media Controller View
     */
    public interface MediaPlayerControlListener {
        /**
         * start play video
         */
        void start();

        /**
         * pause video
         */
        void pause();

        /**
         * get video total time
         * @return
         */
        int getDuration();

        /**
         * get current position
         * @return
         */
        int getCurrentPosition();

        /**
         * seek to position
         * @param pos
         */
        void seekTo(int pos);

        /**
         * video is playing state
         * @return
         */
        boolean isPlaying();

        /**
         * get buffer date
         * @return
         */
        int getBufferPercentage();

        /**
         * if the video can pause
         * @return
         */
        boolean canPause();

        /**
         * can seek video progress
         * @return
         */
        boolean canSeekProgress();

        /**
         * video is full screen
         * in order to control image src...
         * @return
         */
        boolean isFullScreen();

        /**
         * toggle fullScreen
         */
        void toggleFullScreen();

        /**
         * exit media player
         */
        void exit();

        /**
         * get top title name
         */
        String getTopTitle();
    }

}