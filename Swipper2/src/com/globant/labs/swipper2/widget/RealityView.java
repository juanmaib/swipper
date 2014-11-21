package com.globant.labs.swipper2.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.RelativeLayout;

import com.globant.labs.swipper2.R;
import com.globant.labs.swipper2.models.Place;

public class RealityView extends MonocleComponentViewGroup {

	private float mDefaultPlaceWidth;
	private float mDefaultPlaceHeight;

	public RealityView(Context context) {
		this(context, null);
	}

	public RealityView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RealityView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mDefaultPlaceWidth = getResources().getDimension(R.dimen.item_reality_monocle_width);
		mDefaultPlaceHeight = getResources().getDimension(R.dimen.item_reality_monocle_height);
	}
	
	@Override
	protected void addPlaceView(Place place) {
		RelativeLayout item = (RelativeLayout) getInflater().inflate(
				R.layout.item_place_reality, this, false);
		item.setTag(place.getId());
		addView(item);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		Log.i("onLayout", "changed: " + changed);
		Log.i("onLayout", "left: " + left);
		Log.i("onLayout", "top: " + top);
		Log.i("onLayout", "right: " + right);
		Log.i("onLayout", "bottom: " + bottom);
		for (int i = 0; i < getChildCount(); i++) {
			getChildAt(i).layout(15, 15, (int) (15 + mDefaultPlaceWidth),
					(int) (15 + mDefaultPlaceHeight));
		}
	}
}
