package com.bruce.videocontrollerview;

import android.view.MotionEvent;

/**
 * Created by Brucetoo
 * On 2015/10/21
 * At 10:48
 */
public interface VideoGestureListener {
    /**
     * single tap controller view
     */
    void onSingleTap();

    /**
     * Horizontal scroll to control progress of video
     * @param event
     * @param delta
     */
    void onHorizontalScroll(MotionEvent event, float delta);

    /**
     * vertical scroll listen
     * @param motionEvent
     * @param delta
     * @param direction  left or right edge for control brightness or volume
     */
    void onVerticalScroll(MotionEvent motionEvent, float delta, int direction);
}
