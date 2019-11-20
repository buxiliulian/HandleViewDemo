package com.bxll.handleviewdemo.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.customview.widget.ViewDragHelper;

public class VDHLayout extends ViewGroup {
    ViewDragHelper mViewDragHelper;

    public VDHLayout(Context context) {
        this(context, null);
    }

    public VDHLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mViewDragHelper = ViewDragHelper.create(this, new ViewDragHelper.Callback() {
            @Override
            public boolean tryCaptureView(@NonNull View child, int pointerId) {
                return true;
            }

            @Override
            public int clampViewPositionHorizontal(@NonNull View child, int left, int dx) {
                return Math.min(Math.max(0, left), getWidth() - child.getWidth());
            }

            @Override
            public int clampViewPositionVertical(@NonNull View child, int top, int dy) {
                return 0;
            }

            @Override
            public int getViewHorizontalDragRange(@NonNull View child) {
                return getWidth() - child.getWidth();
            }

            @Override
            public int getViewVerticalDragRange(@NonNull View child) {
                return 0;
            }

            @Override
            public void onViewReleased(@NonNull View releasedChild, float xvel, float yvel) {
                if (mViewDragHelper.settleCapturedViewAt(0, 0)) {
                    invalidate();
                }
            }
        });
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mViewDragHelper.continueSettling(true)) {
            invalidate();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed) {
            View first = getChildAt(0);
            first.layout(getPaddingLeft(), getPaddingTop(), getPaddingLeft() + first.getMeasuredWidth(),
                    getPaddingTop() + first.getMeasuredHeight());
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mViewDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mViewDragHelper.processTouchEvent(event);
        return true;
    }
}
