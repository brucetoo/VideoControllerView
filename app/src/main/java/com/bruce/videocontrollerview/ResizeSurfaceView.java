package com.bruce.videocontrollerview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Brucetoo
 * On 2015/10/19
 * At 21:53
 */
public class ResizeSurfaceView extends SurfaceView {
    private static final int MARGIN_DP = 0;//margin of ResizeSurfaceView
    public ResizeSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ResizeSurfaceView(Context context) {
        super(context);
    }

    /**
     * adjust SurfaceView area according to video width and height
     * @param surfaceViewWidth original
     * @param surfaceViewHeight
     * @param videoWidth
     * @param videoHeight
     */
    public void adjustSize(int surfaceViewWidth, int surfaceViewHeight, int videoWidth, int videoHeight) {
        if (videoWidth > 0 && videoHeight > 0) {
            ViewGroup.LayoutParams lp = getLayoutParams();
            DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
            int windowWidth = displayMetrics.widthPixels;
            int windowHeight = displayMetrics.heightPixels;
            int margin = (int) (getContext().getResources().getDisplayMetrics().density* MARGIN_DP);
            float videoRatio = 0;
            if (windowWidth < windowHeight) {
                videoRatio = ((float) (videoWidth)) / videoHeight;
            } else {
                videoRatio = ((float) (videoHeight)) / videoWidth;
            }
            if (windowWidth < windowHeight) {// portrait
                if (videoWidth > videoHeight) {
                    if (surfaceViewWidth / videoRatio > surfaceViewHeight) {
                        lp.height = surfaceViewHeight;
                        lp.width = (int) (surfaceViewHeight * videoRatio);
                    } else {
                        lp.height = (int) (surfaceViewWidth / videoRatio);
                        lp.width = surfaceViewWidth;
                    }
                } else if (videoWidth <= videoHeight) {
                    if (surfaceViewHeight * videoRatio > surfaceViewWidth) {
                        lp.height = (int) (surfaceViewWidth / videoRatio);
                        lp.width = surfaceViewWidth;
                    } else {
                        lp.height = surfaceViewHeight;
                        lp.width = (int) (surfaceViewHeight * videoRatio);
                    }
                }
            } else if (windowWidth > windowHeight) {// landscape
                if (videoWidth > videoHeight) {//video is landscape
                    if (windowWidth * videoRatio > videoHeight) {
                        lp.height = windowHeight - margin;
                        lp.width = (int) ((windowHeight - margin) / videoRatio);
                    } else {
                        lp.height = (int) (windowWidth * videoRatio);
                        lp.width = windowWidth;
                    }
                } else if (videoWidth < videoHeight) {//video is portrait
                    lp.width = (int) ((windowHeight - margin) / videoRatio);
                    lp.height = windowHeight - margin;
                } else {
                    lp.height = windowHeight- margin;
                    lp.width = lp.height;
                }
            }
            setLayoutParams(lp);
            getHolder().setFixedSize(videoWidth, videoHeight);
            setVisibility(View.VISIBLE);
        }
    }
}