package com.bxll.handleviewdemo.view;

import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.Scroller;

/**
 * 处理多手指滑动的规则:
 * 1. 只有主手指才能决定滑动。
 * 2. 新手指按下就被认作是主手指。
 * 3. 如果主手指抬起，必须重新寻找新的主手指。
 *
 * 处理难点: 当所有手指抬起时，页面到底要向哪个方向滑动？
 * 解决办法: 比较ACTION_DOWN和ACTION_UP的页面，即可知道页面是怎么滑动的。
 */
public class MultipleTouchViewGroup extends BaseViewGroup {
    private int mPrimaryPointerId = INVALID_POINTER_ID;
    private static final int INVALID_POINTER_ID = -1;

    private float mLastX;
    private float mStartX;
    private float mStartY;
    private int mScaledTouchSlop;

    private boolean mBeingDragged;

    private int mStartPage = -1;

    private Scroller mScroller;

    public MultipleTouchViewGroup(Context context) {
        this(context, null);
    }

    public MultipleTouchViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        mScaledTouchSlop = viewConfiguration.getScaledTouchSlop();
        mScroller = new Scroller(context);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                onPrimaryPointerDown(ev);
                mStartPage = getScrollX() / getWidth();
                // 如果处于滑动中，截断
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                    mBeingDragged = true;
                    getParent().requestDisallowInterceptTouchEvent(true);
                    return true;
                }
                // 默认不截断
                break;

            case MotionEvent.ACTION_MOVE:
                PointF primaryPointerPoint = getPrimaryPointerPoint(ev);
                if (canScroll(primaryPointerPoint.x, primaryPointerPoint.y)) {
                    mBeingDragged = true;
                    getParent().requestDisallowInterceptTouchEvent(true);
                    performDrag(primaryPointerPoint.x);
                    mLastX = primaryPointerPoint.x;
                    // 可以滑动就截断事件
                    return true;
                }
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                onPrimaryPointerDown(ev);
                break;

            case MotionEvent.ACTION_POINTER_UP:
                onPrimaryPointerUp(ev);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                reset();
                break;

        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_POINTER_DOWN:
                onPrimaryPointerDown(event);
                break;

            case MotionEvent.ACTION_MOVE:
                PointF primaryPointerPoint = getPrimaryPointerPoint(event);
                if (!mBeingDragged) {
                    if (canScroll(primaryPointerPoint.x, primaryPointerPoint.y)) {
                        mBeingDragged = true;
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                }

                if (mBeingDragged) {
                    performDrag(primaryPointerPoint.x);
                    mLastX = primaryPointerPoint.x;
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                onPrimaryPointerUp(event);
                break;

            case MotionEvent.ACTION_UP:
                if (mBeingDragged) {
                    /*由于有多手指触摸的影响，决定控件到底要滑动到哪个页面，必须由ACTION_DOWN时的页面和ACTION_UP
                     时的页面共同决定 */
                    int currentPage = getScrollX() / getWidth();
                    float pageOffset = (getScrollX() % getWidth()) * 1.f / getWidth();
                    // 滑动30%即可认为滑动到下个页面
                    float truncator = currentPage >= mStartPage ? 0.7f : 0.3f;
                    int targetPage = currentPage + (int)(pageOffset + truncator);
                    mScroller.startScroll(getScrollX(), 0,
                            targetPage * getWidth() - getScrollX(), 0);
                    invalidate();
                }
                reset();
                break;
        }
        return true;
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            invalidate();
        }
    }

    /**
     * 当有新手指按下的时候，就认作是主手指，于是重新记录按下点的坐标，以及更新最新的X坐标。
     *
     * @param event 触摸事件。
     */
    private void onPrimaryPointerDown(MotionEvent event) {
        int pointerIndex = event.getActionIndex();
        mPrimaryPointerId = event.getPointerId(pointerIndex);
        mLastX = mStartX = event.getX(pointerIndex);
        mStartY = event.getY(pointerIndex);
    }

    /**
     * 当主手指抬起时，寻找一个新的主手指，并且更新最新的X坐标值为新主手指的X坐标值。
     *
     * @param event
     */
    private void onPrimaryPointerUp(MotionEvent event) {
        int pointerIndex = event.getActionIndex();
        int pointerId = event.getPointerId(pointerIndex);
        if (pointerId == mPrimaryPointerId) {
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mPrimaryPointerId = event.getPointerId(newPointerIndex);
            mLastX = event.getX(newPointerIndex);
        }
    }

    /**
     * 重置状态。
     */
    private void reset() {
        mPrimaryPointerId = INVALID_POINTER_ID;
        mBeingDragged = false;
    }

    /**
     * 获取主手指在某个事件触发时的坐标。
     *
     * @param event 触摸事件。
     * @return 如果成功，返回坐标点，否则返回null。
     */
    private PointF getPrimaryPointerPoint(MotionEvent event) {
        PointF pointF = null;
        if (mPrimaryPointerId != INVALID_POINTER_ID) {
            int pointerIndex = event.findPointerIndex(mPrimaryPointerId);
            if (pointerIndex != -1) {
                pointF = new PointF(event.getX(pointerIndex), event.getY(pointerIndex));
            }
        }
        return pointF;
    }

    /**
     * 检测是否可以滑动了。
     *
     * @param x 触摸点的x坐标.
     * @param y 触摸点的y坐标。
     * @return true代表达到滑动的标准。
     */
    private boolean canScroll(float x, float y) {
        float distanceX = Math.abs(x - mStartX);
        float distanceY = Math.abs(y - mStartY);
        return distanceX > mScaledTouchSlop && distanceX > 2 * distanceY;
    }

    /**
     * 执行一次滑动。
     *
     * @param x 触摸事件的x坐标值。
     */
    private void performDrag(float x) {
        float dx = x - mLastX;
        int minScrollX = 0;
        int maxScrollX = (getChildCount() - 1) * getWidth();
        int newScrollX = (int) (getScrollX() - dx);
        if (dx > 0 && newScrollX <= minScrollX) {
            scrollTo(minScrollX, 0);
        } else if (dx < 0 && newScrollX >= maxScrollX) {
            scrollTo(maxScrollX, 0);
        } else {
            scrollTo(newScrollX, 0);
        }
    }
}
