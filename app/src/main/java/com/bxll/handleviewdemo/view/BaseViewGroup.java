package com.bxll.handleviewdemo.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

/**
 * 只完成基本的测量和布局。
 * 测量: 子View填充父布局
 * 布局: 子View按照父View大小，水平排列。
 *
 * @author bxll
 */
public class BaseViewGroup extends ViewGroup {
    public BaseViewGroup(Context context) {
        this(context, null);
    }

    public BaseViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int childMeasuredWidth = getMeasuredWidth() - getHorizontalPadding();
        int childMeasuredHeight = getMeasuredHeight() - getVerticalPadding();

        int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(childMeasuredWidth, MeasureSpec.EXACTLY);
        int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(childMeasuredHeight, MeasureSpec.EXACTLY);

        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).measure(childWidthMeasureSpec, childHeightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int parentWidth = r - l;
        int parentHeight = b - t;

        int childWidth = parentWidth - getHorizontalPadding();
        int childHeight = parentHeight - getVerticalPadding();

        int leftOffset = getPaddingLeft();
        int childTop = getPaddingTop();
        int childBottom = childTop + childHeight;
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).layout(leftOffset, childTop, leftOffset + childWidth, childBottom);
            leftOffset += childWidth;
        }
    }

    protected int getHorizontalPadding() {
        return getPaddingLeft() + getPaddingRight();
    }

    protected int getVerticalPadding() {
        return getPaddingTop() + getPaddingBottom();
    }

}
