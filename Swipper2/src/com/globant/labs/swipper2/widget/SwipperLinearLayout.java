package com.globant.labs.swipper2.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

public class SwipperLinearLayout extends LinearLayout {

    public SwipperLinearLayout(Context context) {
        super(context);
    }

    public SwipperLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @SuppressLint("NewApi")
    public SwipperLinearLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    	//setMeasuredDimension(getMeasuredWidth(), MeasureSpec.getSize(heightMeasureSpec));
    	int newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getMode(heightMeasureSpec), MeasureSpec.UNSPECIFIED);
    	super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    	
    	int childCount = getChildCount();
    	for(int i = 0; i < childCount; i++) {
    		View child = getChildAt(i);
    		child.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, child.getMeasuredHeight(), 0));
    	}
    	
    	//super.onMeasure(widthMeasureSpec, newHeightMeasureSpec);
    }
	
}
