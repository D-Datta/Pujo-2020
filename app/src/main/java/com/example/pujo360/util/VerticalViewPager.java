package com.example.pujo360.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import com.example.pujo360.R;


public class VerticalViewPager extends ViewPager {

    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;

    private int mSwipeOrientation;

    public VerticalViewPager(@NonNull Context context) {
        super(context);
        mSwipeOrientation = HORIZONTAL;
        //init();
    }

    public VerticalViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setSwipeOrientation(context, attrs);
        //init();
    }

//    private void init() {
//        setPageTransformer(true, new VerticalPageTransformer());
//        //setOverScrollMode(OVER_SCROLL_NEVER);
//    }

    private static class VerticalPageTransformer implements ViewPager.PageTransformer {

        @Override
        public void transformPage(@NonNull View page, float position) {

        if (position < -1) {
                // This page is way off-screen to the left
                page.setAlpha(0);
            } else if (position <= 1) {
                page.setAlpha(1);

                // Counteract the default slide transition
                page.setTranslationX(page.getWidth() * -position);

                // set Y position to swipe in from top
                float yPosition = position * page.getHeight();
                page.setTranslationY(yPosition);
            } else {
                // This page is way off screen to the right
                page.setAlpha(0);
            }
        }
    }

    /**
     * Swaps the X and Y coordinates of your touch event.
     */
    private MotionEvent swapXY(MotionEvent ev) {
        float width = getWidth();
        float height = getHeight();

        float newX = (ev.getY() / height) * width;
        float newY = (ev.getX() / width) * height;

        ev.setLocation(newX, newY);

        return ev;
    }

//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent ev){
//        boolean intercepted = super.onInterceptTouchEvent(swapXY(ev));
//        swapXY(ev); // return touch coordinates to original reference frame for any child views
//        return intercepted;
//    }
//
//    @SuppressLint("ClickableViewAccessibility")
//    @Override
//    public boolean onTouchEvent(MotionEvent ev) {
//        return super.onTouchEvent(swapXY(ev));
//    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(mSwipeOrientation == VERTICAL ? swapXY(event) : event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (mSwipeOrientation == VERTICAL) {
            boolean intercepted = super.onInterceptHoverEvent(swapXY(event));
            swapXY(event);
            return intercepted;
        }
        return super.onInterceptTouchEvent(event);
    }

    public void setSwipeOrientation(int swipeOrientation) {
        if (swipeOrientation == HORIZONTAL || swipeOrientation == VERTICAL)
            mSwipeOrientation = swipeOrientation;
        else
            throw new IllegalStateException("Swipe Orientation can be either CustomViewPager.HORIZONTAL" +
                    " or CustomViewPager.VERTICAL");
        initSwipeMethods();
    }

    private void setSwipeOrientation(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.VerticalViewPager);
        mSwipeOrientation = typedArray.getInteger(R.styleable.VerticalViewPager_swipe_orientation, 0);
        typedArray.recycle();
        initSwipeMethods();
    }

    private void initSwipeMethods() {
        if (mSwipeOrientation == VERTICAL) {
            // The majority of the work is done over here
            setPageTransformer(true, new VerticalPageTransformer());
            // The easiest way to get rid of the overscroll drawing that happens on the left and right
            setOverScrollMode(OVER_SCROLL_NEVER);
        }
    }
}
