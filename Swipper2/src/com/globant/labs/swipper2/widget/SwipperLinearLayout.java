package com.globant.labs.swipper2.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class SwipperLinearLayout extends LinearLayout {

    public SwipperLinearLayout(Context context) {
        super(context);
    }

    public SwipperLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		
		switch(heightMode) {
			case MeasureSpec.UNSPECIFIED: 
				Log.i("SWIPPER", "UNSPECIFIED "+heightSize);
				break;
			case MeasureSpec.AT_MOST:
				Log.i("SWIPPER", "AT_MOST "+heightSize);
				break;
			case MeasureSpec.EXACTLY:
				Log.i("SWIPPER", "EXACTLY "+heightSize);
				break;
		}
		
		//super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		int resultingHeight = 0;
		
		if (getChildCount() > 0) {
			int availableHeight = MeasureSpec.getSize(heightMeasureSpec) - getPaddingBottom() - getPaddingTop();
			float weightSum = 0;
			
			
			Log.i("SWIPPER", "start avail height: "+availableHeight);
			for(int i = 0; i < getChildCount(); i++) {
				View child = getChildAt(i);
				if(child.getVisibility() != View.GONE) {
					final LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) child.getLayoutParams();
					if(lp.height != 0) {
						child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
						availableHeight -= child.getMeasuredHeight();
					}else{
						weightSum += lp.weight;
					}
				}
			}
			
			Log.i("SWIPPER", "end avail height: "+availableHeight);
			
			for(int i = 0; i < getChildCount(); i++) {
				View child = getChildAt(i);
				if(child.getVisibility() != View.GONE) {
					final LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) child.getLayoutParams();
					if(lp.height == 0) {
						int height = (int) Math.ceil(lp.weight * availableHeight / weightSum);
						child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
						availableHeight -= child.getMeasuredHeight();
					}
				}
			}
			
			for(int i = 0; i < getChildCount(); i++) {
				View child = getChildAt(i);
				if(child.getVisibility() != View.GONE) {
					Log.i("SWIPPER", "child: "+i+" height: "+child.getMeasuredHeight());
					resultingHeight += child.getMeasuredHeight();
				}
			}
		}
		
		Log.i("SWIPPER", "Resulting height: "+resultingHeight);
		
		setMeasuredDimension(
				MeasureSpec.getSize(widthMeasureSpec) | MEASURED_STATE_MASK,
				resultingHeight | MEASURED_STATE_MASK);
	}
    
    
}
