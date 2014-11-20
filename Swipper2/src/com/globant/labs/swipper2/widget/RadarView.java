package com.globant.labs.swipper2.widget;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.globant.labs.swipper2.MonocleActivity;
import com.globant.labs.swipper2.R;
import com.globant.labs.swipper2.models.Place;
import com.globant.labs.swipper2.utils.DroidUtils;
import com.globant.labs.swipper2.utils.GeoUtils;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

public class RadarView extends ViewGroup {

	private static final double COEFICIENT_RADAR = 1;
	private static final int RADAR_POINTS_SIZE_DP = 8;
	private static int RADAR_POINTS_SIZE_PX;
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

	private Context mContext;
	private MonocleActivity mActivity;
	private HashMap<String, Place> mPlaces;
	private Location mCurrentLocation;
	private LayoutParams mLayoutParams;
	private Resources mResources;

	public RadarView(Context context) {
		this(context, null);
	}

	public RadarView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RadarView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mPlaces = new HashMap<String, Place>();
		mContext = context;
		mActivity = (MonocleActivity) context;
		RADAR_POINTS_SIZE_PX = DroidUtils.dpToPx(RADAR_POINTS_SIZE_DP, mContext);
		mLayoutParams = new LayoutParams(RADAR_POINTS_SIZE_PX, RADAR_POINTS_SIZE_PX);
		mResources = mContext.getResources();

		LODGING = mResources.getString(R.string.lodging);
		TAXI = mResources.getString(R.string.taxi);
		GAS = mResources.getString(R.string.gas);
		CAR_RENTAL = mResources.getString(R.string.carrental);
		FOOD = mResources.getString(R.string.food);

		LODGING_DRAWABLE = mResources.getDrawable(R.drawable.radar_item_lodging);
		TAXI_DRAWABLE = mResources.getDrawable(R.drawable.radar_item_taxi);
		GAS_DRAWABLE = mResources.getDrawable(R.drawable.radar_item_gas);
		CAR_RENTAL_DRAWABLE = mResources.getDrawable(R.drawable.radar_item_car_rental);
		FOOD_DRAWABLE = mResources.getDrawable(R.drawable.radar_item_food);
	}

	public void onLocationChanged(Location location) {
		mCurrentLocation = location;
		removeOutOfScopePlaces();
	}

	public void onPlacesUpdate(List<Place> places) {
		for (Place place : places) {
			addPlace(place);
		}
		removeOutOfScopePlaces();
	}

	public void addPlace(Place place) {
		if (!mPlaces.containsKey(place.getId())) {
			mPlaces.put(place.getId(), place);
			addPlaceView(place);
		}
	}

	@SuppressWarnings("deprecation")
	private void addPlaceView(Place place) {
		View placeView = new View(mContext);
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
			Toast.makeText(mContext, "da fuq?", Toast.LENGTH_SHORT).show();
		}
		placeView.setTag(place.getId());
		addView(placeView);
	}

	public void removePlace(Place place) {
		mPlaces.remove(place.getId());
		removePlaceView(place);
	}

	private void removePlaceView(Place place) {
		View placeView = findViewWithTag(place.getId());
		if (placeView != null) {
			removeView(placeView);
		}
	}

	private void removeOutOfScopePlaces() {
		for (int i = 0; i < getChildCount(); i++) {
			String placeId = (String) getChildAt(i).getTag();
			Place place = mPlaces.get(placeId);
			Double distance = GeoUtils.getDistance(
					new LatLng(place.getLocation().latitude, place.getLocation().longitude),
					new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude())) * 1000;
			if (distance > MonocleActivity.DEFAULT_RADIUS) {
				removePlace(place);
			}
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		// In onLayout you need to call layout method on each child of this
		// ViewGroup and provide desired position (relatively to parent) for
		// them. You can check source code of FrameLayout (one of the simpliest
		// subclasses of ViewGroup) to find out how it works.

		if (mCurrentLocation != null) {
			LatLngBounds latLngBounds = mActivity.getBounds(COEFICIENT_RADAR);
			int size_x = right - left;
			int size_y = bottom - top;
			int count = getChildCount();
			Log.i("onLayout", "count: " + count);
			for (int i = 0; i < count; i++) {
				View v = getChildAt(i);
				int half_child_width = RADAR_POINTS_SIZE_PX / 2;
				int half_child_height = RADAR_POINTS_SIZE_PX / 2;
				String placeId = (String) v.getTag();
				Place place = mPlaces.get(placeId);
				Point point = locationToPoint(place, latLngBounds, size_x, size_y);
				v.layout(point.x - half_child_width, point.y - half_child_height, point.x
						+ half_child_width, point.y + half_child_height);
			}
		}
	}

	private Point locationToPoint(Place place, LatLngBounds bounds, int size_x, int size_y) {

		double min_lat = bounds.southwest.latitude;
		double min_long = bounds.southwest.longitude;
		double max_lat = bounds.northeast.latitude;
		double max_long = bounds.northeast.longitude;

		double place_lat = place.getLocation().latitude;
		double place_long = place.getLocation().longitude;

		double delta_lat = place_lat - min_lat;
		double delta_long = place_long - min_long;
		double max_delta_lat = max_lat - min_lat;
		double max_delta_long = max_long - min_long;

		Point p = new Point();
		p.x = (int) Math.floor((size_x * delta_long) / max_delta_long);
		p.y = (int) Math.floor((size_y * delta_lat) / max_delta_lat);
		return p;
	}

	private boolean between(double min, double max, double value) {
		return ((value >= min) && (value <= max));
	}
}
