package com.bruce.videocontrollerview;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.WindowManager;

/**
 * Created by Brucetoo
 * On 2015/10/21
 * At 9:58
 */
public class ViewGestureListener implements GestureDetector.OnGestureListener {

    private static final String TAG = "ViewGestureListener";

    private static final int SWIPE_THRESHOLD = 60;//threshold of swipe
    public static final int SWIPE_LEFT = 1;
    public static final int SWIPE_RIGHT = 2;
    private VideoGestureListener listener;
    private Context context;

    public ViewGestureListener(Context context,VideoGestureListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        listener.onSingleTap();
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        float deltaX = e2.getX() - e1.getX();
        float deltaY = e2.getY() - e1.getY();
        if (Math.abs(deltaX) > Math.abs(deltaY)) {
            if (Math.abs(deltaX) > SWIPE_THRESHOLD) {
                listener.onHorizontalScroll(e2, deltaX);
            }
            return true;
        } else {
            if (Math.abs(deltaY) > SWIPE_THRESHOLD) {
                if(e1.getX() < getDeviceWidth(context)*1.0/5) {//left edge
                    listener.onVerticalScroll(e2,-deltaY,SWIPE_LEFT);
                }else if(e1.getX() > getDeviceWidth(context)*4.0/5){//right edge
                    listener.onVerticalScroll(e2,-deltaY,SWIPE_RIGHT);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }


    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    public static int getDeviceWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics mDisplayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(mDisplayMetrics);
        return mDisplayMetrics.widthPixels;
    }

    public static int getDeviceHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics mDisplayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(mDisplayMetrics);
        return mDisplayMetrics.heightPixels;
    }

}
