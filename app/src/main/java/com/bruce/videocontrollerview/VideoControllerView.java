package com.bruce.videocontrollerview;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.Formatter;
import java.util.Locale;

/**
 * Created by Bruce Too
 * On 7/12/16.
 * At 16:09
 */
public class VideoControllerView extends FrameLayout implements VideoGestureListener {

    private static final String TAG = "VideoControllerView";

    private static final int HANDLER_ANIMATE_OUT = 1;// out animate
    private static final int HANDLER_UPDATE_PROGRESS = 2;//cycle update progress
    private static final long PROGRESS_SEEK = 500;

    private MediaPlayerControlListener mMediaPlayer;// control media play
    private Activity mContext;
    private ViewGroup mAnchorView;//anchor view
    private View mRootView; // root view of this
    private SeekBar mSeekBar; //seek bar for video
    private TextView mEndTime, mCurrentTime;
    private boolean mIsShowing;//controller view showing
    private boolean mIsDragging; //is dragging seekBar
    private StringBuilder mFormatBuilder;
    private Formatter mFormatter;
    private GestureDetector mGestureDetector;//gesture detector

    //top layout
    private View mTopLayout;
    private ImageButton mBackButton;
    private TextView mTitleText;
    //center layout
    private View mCenterLayout;
    private ImageView mCenterImage;
    private ProgressBar mCenterPorgress;
    private float mCurBrightness = -1;
    private int mCurVolume = -1;
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
    }

    public VideoControllerView(Activity context) {
        super(context);
        mContext = context;
    }


    /**
     * Handler prevent leak memory.
     */
    private static class ControllerViewHandler extends Handler {
        private final WeakReference<VideoControllerView> mView;

        ControllerViewHandler(VideoControllerView view) {
            mView = new WeakReference<>(view);
        }

        @Override
        public void handleMessage(Message msg) {
            VideoControllerView view = mView.get();
            if (view == null || view.mMediaPlayer == null) {
                return;
            }

            int pos;
            switch (msg.what) {
                case HANDLER_ANIMATE_OUT:
                    view.hide();
                    break;
                case HANDLER_UPDATE_PROGRESS://cycle update seek bar progress
                    pos = view.setSeekProgress();
                    if (!view.mIsDragging && view.mIsShowing && view.mMediaPlayer.isPlaying()) {//just in case
                        //cycle update
                        msg = obtainMessage(HANDLER_UPDATE_PROGRESS);
                        sendMessageDelayed(msg, 1000 - (pos % 1000));
                    }
                    break;
            }
        }
    }

    /**
     * Inflate view from exit xml layout
     *
     * @return the root view of {@link VideoControllerView}
     */
    private View makeControllerView() {
        LayoutInflater inflate = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRootView = inflate.inflate(R.layout.media_controller, null);
        initControllerView();

        return mRootView;
    }

    /**
     * find all views inside {@link VideoControllerView}
     * and init params
     */
    private void initControllerView() {
        //top layout
        mTopLayout = mRootView.findViewById(R.id.layout_top);
        mBackButton = (ImageButton) mRootView.findViewById(R.id.top_back);
        if (mBackButton != null) {
            mBackButton.requestFocus();
            mBackButton.setOnClickListener(mBackListener);
        }

        mTitleText = (TextView) mRootView.findViewById(R.id.top_title);

        //center layout
        mCenterLayout = mRootView.findViewById(R.id.layout_center);
        mCenterLayout.setVisibility(GONE);
        mCenterImage = (ImageView) mRootView.findViewById(R.id.image_center_bg);
        mCenterPorgress = (ProgressBar) mRootView.findViewById(R.id.progress_center);

        //bottom layout
        mBottomLayout = mRootView.findViewById(R.id.layout_bottom);
        mPauseButton = (ImageButton) mRootView.findViewById(R.id.bottom_pause);
        if (mPauseButton != null) {
            mPauseButton.requestFocus();
            mPauseButton.setOnClickListener(mPauseListener);
        }

        mFullscreenButton = (ImageButton) mRootView.findViewById(R.id.bottom_fullscreen);
        if (mFullscreenButton != null) {
            mFullscreenButton.requestFocus();
            mFullscreenButton.setOnClickListener(mFullscreenListener);
        }

        mSeekBar = (SeekBar) mRootView.findViewById(R.id.bottom_seekbar);
        if (mSeekBar != null) {
            mSeekBar.setOnSeekBarChangeListener(mSeekListener);
            mSeekBar.setMax(1000);
        }

        mEndTime = (TextView) mRootView.findViewById(R.id.bottom_time);
        mCurrentTime = (TextView) mRootView.findViewById(R.id.bottom_time_current);

        //init formatter
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
    }

    /**
     * show controller view
     */
    private void show() {

        if (!mIsShowing && mAnchorView != null) {

            //add controller view to bottom of the AnchorView
            FrameLayout.LayoutParams tlp = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            mAnchorView.addView(VideoControllerView.this, tlp);

            ViewAnimator.putOn(mTopLayout)
                    .waitForSize(new ViewAnimator.Listeners.Size() {
                        @Override
                        public void onSize(ViewAnimator viewAnimator) {
                            viewAnimator.animate()
                                    .translationY(-mTopLayout.getHeight(), 0)
                                    .duration(500)
                                    .andAnimate(mBottomLayout)
                                    .translationY(mBottomLayout.getHeight(), 0)
                                    .duration(500)
                                    .start(new ViewAnimator.Listeners.Start() {
                                        @Override
                                        public void onStart() {
                                            mIsShowing = true;
                                            mHandler.sendEmptyMessage(HANDLER_UPDATE_PROGRESS);
                                        }
                                    });
                        }
                    });

        }

        setSeekProgress();
        if (mPauseButton != null) {
            mPauseButton.requestFocus();
            if (!mMediaPlayer.canPause()) {
                mPauseButton.setEnabled(false);
            }
        }

        togglePausePlay();
        toggleFullScreen();
        //update progress
        mHandler.sendEmptyMessage(HANDLER_UPDATE_PROGRESS);

    }

    /**
     * toggle {@link VideoControllerView} show or not
     * this can be called when {@link View#onTouchEvent(MotionEvent)} happened
     */
    public void toggleControllerView() {
        if (!isShowing()) {
            show();
        } else {
            //animate out controller view
            Message msg = mHandler.obtainMessage(HANDLER_ANIMATE_OUT);
            //remove exist one first
            mHandler.removeMessages(HANDLER_ANIMATE_OUT);
            mHandler.sendMessageDelayed(msg, 100);
        }
    }


    /**
     * if {@link VideoControllerView} is visible
     *
     * @return showing or not
     */
    public boolean isShowing() {
        return mIsShowing;
    }

    /**
     * hide controller view with animation
     * With custom animation
     */
    private void hide() {
        if (mAnchorView == null) {
            return;
        }

        ViewAnimator.putOn(mTopLayout)
                .animate()
                .translationY(-mTopLayout.getHeight())
                .duration(500)

                .andAnimate(mBottomLayout)
                .translationY(mBottomLayout.getHeight())
                .duration(500)
                .end(new ViewAnimator.Listeners.End() {
                    @Override
                    public void onEnd() {
                        mAnchorView.removeView(VideoControllerView.this);
                        mHandler.removeMessages(HANDLER_UPDATE_PROGRESS);
                        mIsShowing = false;
                    }
                });
    }

    /**
     * convert string to time
     *
     * @param timeMs time to be formatted
     * @return 00:00:00
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
     * set {@link #mSeekBar} progress
     * and video play time {@link #mCurrentTime}
     *
     * @return current play position
     */
    private int setSeekProgress() {
        if (mMediaPlayer == null || mIsDragging) {
            return 0;
        }

        int position = mMediaPlayer.getCurrentPosition();
        int duration = mMediaPlayer.getDuration();
        if (mSeekBar != null) {
            if (duration > 0) {
                // use long to avoid overflow
                long pos = 1000L * position / duration;
                mSeekBar.setProgress((int) pos);
            }
            //get buffer percentage
            int percent = mMediaPlayer.getBufferPercentage();
            //set buffer progress
            mSeekBar.setSecondaryProgress(percent * 10);
        }

        if (mEndTime != null)
            mEndTime.setText(stringToTime(duration));
        if (mCurrentTime != null)
            mCurrentTime.setText(stringToTime(position));

        mTitleText.setText(mMediaPlayer.getTopTitle());
        return position;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                mCurVolume = -1;
                mCurBrightness = -1;
                mCenterLayout.setVisibility(GONE);
//                break;// do need bread,should let gestureDetector to handle event
            default://gestureDetector handle other MotionEvent
                mGestureDetector.onTouchEvent(event);
        }
        return true;

    }

    /**
     * toggle pause or play
     */
    private void togglePausePlay() {
        if (mRootView == null || mPauseButton == null || mMediaPlayer == null) {
            return;
        }

        if (mMediaPlayer.isPlaying()) {
            mPauseButton.setImageResource(R.drawable.ic_media_pause);
        } else {
            mPauseButton.setImageResource(R.drawable.ic_media_play);
        }
    }

    /**
     * toggle full screen or not
     */
    public void toggleFullScreen() {
        if (mRootView == null || mFullscreenButton == null || mMediaPlayer == null) {
            return;
        }

        if (mMediaPlayer.isFullScreen()) {
            mFullscreenButton.setImageResource(R.drawable.ic_media_fullscreen_shrink);
        } else {
            mFullscreenButton.setImageResource(R.drawable.ic_media_fullscreen_stretch);
        }
    }

    private void doPauseResume() {
        if (mMediaPlayer == null) {
            return;
        }

        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        } else {
            mMediaPlayer.start();
        }
        togglePausePlay();
    }

    private void doToggleFullscreen() {
        if (mMediaPlayer == null) {
            return;
        }

        mMediaPlayer.toggleFullScreen();
    }

    /**
     * Seek bar drag listener
     */
    private SeekBar.OnSeekBarChangeListener mSeekListener = new SeekBar.OnSeekBarChangeListener() {
        public void onStartTrackingTouch(SeekBar bar) {
            show();
            mIsDragging = true;
            mHandler.removeMessages(HANDLER_UPDATE_PROGRESS);
        }

        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
            if (mMediaPlayer == null) {
                return;
            }

            if (!fromuser) {
                return;
            }

            long duration = mMediaPlayer.getDuration();
            long newPosition = (duration * progress) / 1000L;
            mMediaPlayer.seekTo((int) newPosition);
            if (mCurrentTime != null)
                mCurrentTime.setText(stringToTime((int) newPosition));
        }

        public void onStopTrackingTouch(SeekBar bar) {
            mIsDragging = false;
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
            mMediaPlayer.exit();
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
     *
     * @param player self
     */
    public void setMediaPlayerControlListener(MediaPlayerControlListener player) {
        mMediaPlayer = player;
        togglePausePlay();
        toggleFullScreen();
    }

    /**
     * set anchor view
     *
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
        View v = makeControllerView();
        addView(v, frameParams);
    }

    /**
     * set gesture listen to control media player
     * include screen brightness and volume of video
     * and seek video play
     */
    public void setGestureListener() {
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mGestureDetector = new GestureDetector(mContext, new ViewGestureListener(mContext, this));
    }


    @Override
    public void onSingleTap() {
        toggleControllerView();
    }

    @Override
    public void onHorizontalScroll(boolean seekForward) {
        if (mMediaPlayer.canSeek()) {
            if (seekForward) {// seek forward
                seekForWard();
            } else {  //seek backward
                seekBackWard();
            }
        }
    }

    private void seekBackWard() {
        if (mMediaPlayer == null) {
            return;
        }

        int pos = mMediaPlayer.getCurrentPosition();
        pos -= PROGRESS_SEEK;
        mMediaPlayer.seekTo(pos);
        setSeekProgress();

        show();
    }

    private void seekForWard() {
        if (mMediaPlayer == null) {
            return;
        }

        int pos = mMediaPlayer.getCurrentPosition();
        pos += PROGRESS_SEEK;
        mMediaPlayer.seekTo(pos);
        setSeekProgress();

        show();
    }

    @Override
    public void onVerticalScroll(float percent, int direction) {
        if (direction == ViewGestureListener.SWIPE_LEFT) {
            mCenterImage.setImageResource(R.drawable.video_bright_bg);
            updateBrightness(percent);
        } else {
            mCenterImage.setImageResource(R.drawable.video_volume_bg);
            updateVolume(percent);
        }
    }

    /**
     * update volume by seek percent
     *
     * @param percent seek percent
     */
    private void updateVolume(float percent) {

        mCenterLayout.setVisibility(VISIBLE);

        if (mCurVolume == -1) {
            mCurVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            if (mCurVolume < 0) {
                mCurVolume = 0;
            }
        }

        int volume = (int) (percent * mMaxVolume) + mCurVolume;
        if (volume > mMaxVolume) {
            volume = mMaxVolume;
        }

        if (volume < 0) {
            volume = 0;
        }
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);

        int progress = volume * 100 / mMaxVolume;
        mCenterPorgress.setProgress(progress);
    }

    /**
     * update brightness by seek percent
     *
     * @param percent seek percent
     */
    private void updateBrightness(float percent) {

        if (mCurBrightness == -1) {
            mCurBrightness = mContext.getWindow().getAttributes().screenBrightness;
            if (mCurBrightness <= 0.01f) {
                mCurBrightness = 0.01f;
            }
        }

        mCenterLayout.setVisibility(VISIBLE);

        WindowManager.LayoutParams attributes = mContext.getWindow().getAttributes();
        attributes.screenBrightness = mCurBrightness + percent;
        if (attributes.screenBrightness >= 1.0f) {
            attributes.screenBrightness = 1.0f;
        } else if (attributes.screenBrightness <= 0.01f) {
            attributes.screenBrightness = 0.01f;
        }
        mContext.getWindow().setAttributes(attributes);

        float p = attributes.screenBrightness * 100;
        mCenterPorgress.setProgress((int) p);

    }


    /**
     * Interface of Media Controller View Which can be callBack
     * when {@link android.media.MediaPlayer} or some other media
     * players work
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
         *
         * @return total time
         */
        int getDuration();

        /**
         * get video current position
         *
         * @return current position
         */
        int getCurrentPosition();

        /**
         * seek video to exactly position
         *
         * @param position position
         */
        void seekTo(int position);

        /**
         * video is playing state
         *
         * @return is video playing
         */
        boolean isPlaying();

        /**
         * get buffer percent
         *
         * @return percent
         */
        int getBufferPercentage();

        /**
         * if the video can pause
         *
         * @return can pause video
         */
        boolean canPause();

        /**
         * can seek video progress
         *
         * @return can seek video progress
         */
        boolean canSeek();

        /**
         * video is full screen
         * in order to control image src...
         *
         * @return fullScreen
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