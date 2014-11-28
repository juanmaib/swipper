package com.globant.labs.swipper2.widget;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.globant.labs.swipper2.MonocleActivity;
import com.globant.labs.swipper2.R;
import com.globant.labs.swipper2.models.Place;
import com.globant.labs.swipper2.utils.GeometryUtils;
import com.google.android.gms.maps.model.LatLngBounds;

public class RealityView extends MonocleComponentViewGroup {

	private static double DEFAULT_PLACE_HALF_WIDTH;
	private static double DEFAULT_PLACE_HALF_HEIGHT;
	private static final double X_FOV_MULTIPLIER = 2;
	private static final double Y_FOV_MULTIPLIER = 2;

	public RealityView(Context context) {
		this(context, null);
	}

	public RealityView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RealityView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		DEFAULT_PLACE_HALF_WIDTH = getResources().getDimension(R.dimen.item_reality_monocle_width) / 2;
		DEFAULT_PLACE_HALF_HEIGHT = getResources()
				.getDimension(R.dimen.item_reality_monocle_height) / 2;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void addPlaceView(Place place) {
		RelativeLayout placeView = (RelativeLayout) getInflater().inflate(
				R.layout.item_place_reality, this, false);
		// cannot use switch e.e
		if (place.getCategory().equals(getLodgingString())) {
			placeView.setBackgroundDrawable(getLodgingDrawable());
		} else if (place.getCategory().equals(getTaxiString())) {
			placeView.setBackgroundDrawable(getTaxiDrawable());
		} else if (place.getCategory().equals(getGasString())) {
			placeView.setBackgroundDrawable(getGasDrawable());
		} else if (place.getCategory().equals(getCarRentalString())) {
			placeView.setBackgroundDrawable(getCarRentalDrawable());
		} else if (place.getCategory().equals(getFoodString())) {
			placeView.setBackgroundDrawable(getFoodDrawable());
		} else {
			Toast.makeText(getContext(), "da fuq?", Toast.LENGTH_SHORT).show();
		}
		placeView.setTag(place.getId());
		addView(placeView);
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
			int size_x = right - left;
			int size_y = bottom - top;
			for (int i = 0; i < getChildCount(); i++) {
				View v = getChildAt(i);
				String placeId = (String) v.getTag();
				Point point = GeometryUtils.locationToRealityPoint(getPlaces().get(placeId),
						latLngBounds, size_x, X_FOV_MULTIPLIER, size_y, Y_FOV_MULTIPLIER,
						getActivity().getAzimuthDegrees());
				v.layout((int) (point.x - DEFAULT_PLACE_HALF_WIDTH),
						(int) (point.y - DEFAULT_PLACE_HALF_HEIGHT),
						(int) (point.x + DEFAULT_PLACE_HALF_WIDTH),
						(int) (point.y + DEFAULT_PLACE_HALF_HEIGHT));
			}
		}
	}

	@Override
	protected void setUpBackgroundDrawables() {
		setLodgingDrawable(getResources().getDrawable(R.drawable.reality_item_lodging));
		setTaxiDrawable(getResources().getDrawable(R.drawable.reality_item_taxi));
		setGasDrawable(getResources().getDrawable(R.drawable.reality_item_gas));
		setCarRentalDrawable(getResources().getDrawable(R.drawable.reality_item_car_rental));
		setFoodDrawable(getResources().getDrawable(R.drawable.reality_item_food));
	}
}
