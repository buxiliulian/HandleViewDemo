package com.bxll.handleviewdemo.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.widget.Scroller;

/**
 * HandleTouchEventViewGroup用来演示如何处理事件。
 *
 * @author bxll
 */
public class HandleTouchEventViewGroup extends BaseViewGroup {
    private float mLastX, mLastY;
    private float mStartX, mStartY;
    private int mScaledTouchSlop;

    private int mState = SCROLLING_STATE_IDLE;
    private static final int SCROLLING_STATE_IDLE = 0;
    private static final int SCROLLING_STATE_DRAGGING = 1;
    private static final int SCROLLING_STATE_SETTING = 2;
    private Scroller mScroller;


    public HandleTouchEventViewGroup(Context context) {
        this(context, null);
    }

    public HandleTouchEventViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        mScaledTouchSlop = viewConfiguration.getScaledTouchSlop();
        mScroller = new Scroller(context);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        float x = ev.getX();
        float y = ev.getY();
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                // 重置状态
                setState(SCROLLING_STATE_IDLE);
                // 记录手指按下的坐标
                mLastX = mStartX = x;
                mLastY = mStartY = y;
                // 1. 如果处于无状态，默认不截断
                // 2. 如果处于滑动状态，截断事件
                if (!mScroller.isFinished()) {
                    // 停止定位动作
                    mScroller.abortAnimation();
                    // 设置拖拽状态
                    setState(SCROLLING_STATE_DRAGGING);
                    // 不允许父View截断后续事件
                    requestDisallowIntercept(true);
                    return true;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                // 计算从手指按下时滑动的距离
                float distanceX = Math.abs(x - mStartX);
                float distanceY = Math.abs(y - mStartY);
                if (distanceX > mScaledTouchSlop && distanceX > 2 * distanceY) {
                    //　不允许父View截断后续事件
                    requestDisallowIntercept(true);
                    // 设置拖拽状态
                    setState(SCROLLING_STATE_DRAGGING);
                    // 执行一次拖拽的滑动
                    performDrag(x);
                    // 更新最新事件坐标
                    mLastX = x;
                    mLastY = y;
                    // 截断后续的事件
                    return true;
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:

                // 自己处理ACTION_DOWN，必须返回true
                return true;

            case MotionEvent.ACTION_MOVE:
                if (mState == SCROLLING_STATE_DRAGGING) {
                    // 处于滑动状态就继续执行滑动
                    performDrag(x);
                    // 更新最新坐标点
                    mLastX = x;
                } else {
                    // 不处于滑动状态，就再次检测是否达滑动标准
                    float distanceX = Math.abs(x - mLastX);
                    float distanceY = Math.abs(y - mLastY);
                    if (distanceX > mScaledTouchSlop && distanceX > 2 * distanceY) {
                        setState(SCROLLING_STATE_DRAGGING);
                        requestDisallowIntercept(true);
                        performDrag(x);
                        mLastX = x;
                    }
                }
                return true;

            case MotionEvent.ACTION_UP:
                if (mState == SCROLLING_STATE_DRAGGING) {
                    setState(SCROLLING_STATE_SETTING);
                    // 使用Scroller进行定位操作
                    int contentWidth = getWidth() - getHorizontalPadding();
                    int scrollX = getScrollX();
                    int targetIndex = (scrollX + contentWidth / 2) / contentWidth;
                    mScroller.startScroll(scrollX, 0, targetIndex * contentWidth - scrollX, 0);
                    invalidate();
                }
                return true;
        }
        return super.onTouchEvent(event);
    }

    private void requestDisallowIntercept(boolean disallow) {
        ViewParent parent = getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(disallow);
        }
    }

    private void setState(int state) {
        if (mState == state) {
            return;
        }
        mState = state;
    }

    private void performDrag(float x) {
        int maxScrollX = (getChildCount() - 1) * (getWidth() - getHorizontalPadding());
        int minScrollX = 0;
        float dx = x - mLastX;
        int oldScrollX = getScrollX();
        int newScrollX = (int) (oldScrollX - dx);
        if (dx < 0 && newScrollX >= maxScrollX) {
            scrollTo(maxScrollX, 0);
        } else if (dx > 0 && newScrollX <= minScrollX) {
            scrollTo(0, 0);
        } else {
            scrollTo(newScrollX, 0);
        }
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            invalidate();
        }
    }
}
