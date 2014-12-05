package com.globant.labs.swipper2.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class RealityPlaceView extends RelativeLayout {

	private float mScaleFactor = 1.f;
	private double mPositionY = -1;
	private double mDirection = 0;

	public RealityPlaceView(Context context) {
		super(context);
		setWillNotDraw(false);
	}

	public RealityPlaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setWillNotDraw(false);
	}

	public RealityPlaceView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setWillNotDraw(false);
	}
	
	@Override
	public boolean performClick() {
		return super.performClick();
	}

	public void setScale(float factor) {
		mScaleFactor = factor;
		invalidate();
	}

	public float getScale() {
		return mScaleFactor;
	}

	@Override
	public void onDraw(Canvas canvas) {
		canvas.scale(mScaleFactor, mScaleFactor);
		super.onDraw(canvas);
	}

	public double getPositionY() {
		return mPositionY;
	}

	public void setPositionY(double position_y) {
		this.mPositionY = position_y;
	}

	public double getDirection() {
		return mDirection;
	}

	public void setDirection(double direction) {
		this.mDirection = direction;
	}

}
