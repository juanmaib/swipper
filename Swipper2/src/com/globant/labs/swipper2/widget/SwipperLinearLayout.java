package com.globant.labs.swipper2.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
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
		int resultingHeight = 0;
		
		if (getChildCount() > 0) {
			float weightSum = 0;

			int availableHeight = MeasureSpec.getSize(heightMeasureSpec) - getPaddingBottom() - getPaddingTop();

			int unspecified = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
			
			for(int i = 0; i < getChildCount(); i++) {
				View child = getChildAt(i);
				if(child.getVisibility() != View.GONE) {
					LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) child.getLayoutParams();
					if(lp.height != 0) {
						child.measure(widthMeasureSpec, unspecified);
						resultingHeight += child.getMeasuredHeight();
						availableHeight -= child.getMeasuredHeight();			
					}else{
						weightSum += lp.weight;
					}
				}
			}
			
			for(int i = 0; i < getChildCount(); i++) {
				View child = getChildAt(i);
				if(child.getVisibility() != View.GONE) {
					LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) child.getLayoutParams();
					if(lp.height == 0) {
						int height = (int) Math.ceil(lp.weight * availableHeight / weightSum);
						child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
						resultingHeight += child.getMeasuredHeight();
						availableHeight -= child.getMeasuredHeight();
						weightSum -= lp.weight;
					}
				}
			}

		}

		setMeasuredDimension(
				MeasureSpec.getSize(widthMeasureSpec) | MEASURED_STATE_MASK,
				resultingHeight | MEASURED_STATE_MASK);
	}
    
    
}
