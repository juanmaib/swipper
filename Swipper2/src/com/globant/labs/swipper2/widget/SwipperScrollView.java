package com.globant.labs.swipper2.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import android.widget.ScrollView;

public class SwipperScrollView extends ScrollView {

    public SwipperScrollView(Context context) {
        super(context);
    }

    public SwipperScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SwipperScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {   	
    	super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    	
        if (!isFillViewport()) {
            return;
        }

        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode == MeasureSpec.UNSPECIFIED) {
            return;
        }

        if (getChildCount() > 0) {
            final View child = getChildAt(0);
            int height = getMeasuredHeight();
            //if (child.getMeasuredHeight() < height) {
                final FrameLayout.LayoutParams lp = (LayoutParams) child.getLayoutParams();
    
                int childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, getPaddingLeft()
                        + getPaddingRight(), lp.width);
                height -= getPaddingTop();
                height -= getPaddingBottom();
                int childHeightMeasureSpec =
                        MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
    
                child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            //}
        }
    }

}
