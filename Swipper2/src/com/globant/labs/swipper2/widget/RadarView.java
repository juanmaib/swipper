package com.globant.labs.swipper2.widget;

import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

import com.globant.labs.swipper2.MonocleActivity;
import com.globant.labs.swipper2.R;
import com.globant.labs.swipper2.models.Place;
import com.globant.labs.swipper2.utils.DroidUtils;
import com.globant.labs.swipper2.utils.GeometryUtils;
import com.google.android.gms.maps.model.LatLngBounds;

public class RadarView extends MonocleComponentViewGroup {

	private static final int RADAR_POINTS_SIZE_DP = 8;
	private static int RADAR_POINTS_SIZE_PX;
	private static int RADAR_POINTS_SIZE_PX_HALF;

	private static String LODGING;
	private static String TAXI;
	private static String GAS;
	private static String CAR_RENTAL;
	private static String FOOD;
	private static Drawable LODGING_DRAWABLE;
	private static Drawable TAXI_DRAWABLE;
	private static Drawable GAS_DRAWABLE;
	private static Drawable CAR_RENTAL_DRAWABLE;
	private static Drawable FOOD_DRAWABLE;

	private LayoutParams mLayoutParams;

	public RadarView(Context context) {
		this(context, null);
	}

	public RadarView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RadarView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		LODGING = getResources().getString(R.string.lodging);
		TAXI = getResources().getString(R.string.taxi);
		GAS = getResources().getString(R.string.gas);
		CAR_RENTAL = getResources().getString(R.string.carrental);
		FOOD = getResources().getString(R.string.food);

		LODGING_DRAWABLE = getResources().getDrawable(R.drawable.radar_item_lodging);
		TAXI_DRAWABLE = getResources().getDrawable(R.drawable.radar_item_taxi);
		GAS_DRAWABLE = getResources().getDrawable(R.drawable.radar_item_gas);
		CAR_RENTAL_DRAWABLE = getResources().getDrawable(R.drawable.radar_item_car_rental);
		FOOD_DRAWABLE = getResources().getDrawable(R.drawable.radar_item_food);

		RADAR_POINTS_SIZE_PX = DroidUtils.dpToPx(RADAR_POINTS_SIZE_DP, getContext());
		RADAR_POINTS_SIZE_PX_HALF = RADAR_POINTS_SIZE_PX / 2;

		mLayoutParams = new LayoutParams(RADAR_POINTS_SIZE_PX, RADAR_POINTS_SIZE_PX);
	}

	@SuppressWarnings("deprecation")
	protected void addPlaceView(Place place) {
		View placeView = new View(getContext());
		placeView.setLayoutParams(mLayoutParams);
		// cannot use switch e.e
		if (place.getCategory().equals(LODGING)) {
			placeView.setBackgroundDrawable(LODGING_DRAWABLE);
		} else if (place.getCategory().equals(TAXI)) {
			placeView.setBackgroundDrawable(TAXI_DRAWABLE);
		} else if (place.getCategory().equals(GAS)) {
			placeView.setBackgroundDrawable(GAS_DRAWABLE);
		} else if (place.getCategory().equals(CAR_RENTAL)) {
			placeView.setBackgroundDrawable(CAR_RENTAL_DRAWABLE);
		} else if (place.getCategory().equals(FOOD)) {
			placeView.setBackgroundDrawable(FOOD_DRAWABLE);
		} else {
			Toast.makeText(getContext(), "da fuq?", Toast.LENGTH_SHORT).show();
		}
		placeView.setTag(place.getId());
		addView(placeView);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		// In onLayout you need to call layout method on each child of this
		// ViewGroup and provide desired position (relatively to parent) for
		// them. You can check source code of FrameLayout (one of the simpliest
		// subclasses of ViewGroup) to find out how it works.

		if (getActivity().getCurrentLocation() != null) {
			LatLngBounds latLngBounds = getActivity()
					.getBounds(2 * MonocleActivity.BASE_COEFICIENT);
			int size_x = right - left;
			int size_y = bottom - top;
			for (int i = 0; i < getChildCount(); i++) {
				View v = getChildAt(i);
				String placeId = (String) v.getTag();
				Point point = GeometryUtils.locationToRadarPoint(getPlaces().get(placeId), latLngBounds,
						size_x, size_y, getActivity().getAzimuthDegrees());
				v.layout(point.x - RADAR_POINTS_SIZE_PX_HALF, point.y - RADAR_POINTS_SIZE_PX_HALF,
						point.x + RADAR_POINTS_SIZE_PX_HALF, point.y + RADAR_POINTS_SIZE_PX_HALF);
			}
		}
	}
}
