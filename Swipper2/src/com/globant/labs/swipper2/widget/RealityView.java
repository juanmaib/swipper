package com.globant.labs.swipper2.widget;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.globant.labs.swipper2.MonocleActivity;
import com.globant.labs.swipper2.R;
import com.globant.labs.swipper2.models.Place;
import com.globant.labs.swipper2.utils.GeometryUtils;
import com.google.android.gms.maps.model.LatLngBounds;

public class RealityView extends MonocleComponentViewGroup {

	private float mDefaultPlaceHalfWidth;
	private float mDefaultPlaceHalfHeight;

	public RealityView(Context context) {
		this(context, null);
	}

	public RealityView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RealityView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mDefaultPlaceHalfWidth = getResources().getDimension(R.dimen.item_reality_monocle_width) / 2;
		mDefaultPlaceHalfHeight = getResources().getDimension(R.dimen.item_reality_monocle_height) / 2;
	}

	@Override
	protected void addPlaceView(Place place) {
		RelativeLayout item = (RelativeLayout) getInflater().inflate(R.layout.item_place_reality,
				this, false);
		item.setTag(place.getId());
		addView(item);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		// here we must tell each view where to position itself.
		// for this case, we want to put them in a way such that when the user
		// is looking with the camera straight to a place, it is positioned
		// exactly in the middle of the screen. if it's just about to become
		// visible, or just about to go out of the FOV, it should be displayed
		// halfway. if the place is out of the FOV, it shouldn't be in the
		// screen altogether.
		if (getActivity().getCurrentLocation() != null) {
			LatLngBounds latLngBounds = getActivity()
					.getBounds(2 * MonocleActivity.BASE_COEFICIENT);
			int size_x = (right - left) * 4;
			int size_y = bottom - top;
			for (int i = 0; i < getChildCount(); i++) {
				View v = getChildAt(i);
				String placeId = (String) v.getTag();
				Point point = GeometryUtils.locationToRealityPoint(getPlaces().get(placeId), latLngBounds,
						size_x, size_y, getActivity().getAzimuthDegrees());
				v.layout((int) (point.x - mDefaultPlaceHalfWidth),
						(int) (point.y - mDefaultPlaceHalfHeight),
						(int) (point.x + mDefaultPlaceHalfWidth),
						(int) (point.y + mDefaultPlaceHalfHeight));
			}
		}
	}
}
