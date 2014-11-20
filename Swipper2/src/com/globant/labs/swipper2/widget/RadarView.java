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

	private Context mContext;
	private MonocleActivity mActivity;
	private HashMap<String, Place> mPlaces;
	private Location mCurrentLocation;
	private LayoutParams mLayoutParams;
	private Resources mResources;
	private double mAzimut;

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
		RADAR_POINTS_SIZE_PX_HALF = RADAR_POINTS_SIZE_PX / 2;
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
		mAzimut = 0;
	}

	public void onLocationChanged(Location location) {
		Log.i("RadarView", "onLocationChanged");
		mCurrentLocation = location;
		removeOutOfScopePlaces();
	}

	public void onPlacesUpdate(List<Place> places) {
		Log.i("RadarView", "onPlacesUpdate");
		for (Place place : places) {
			if (isPlaceWithinScope(place, mCurrentLocation,
					mActivity.getRadius(MonocleActivity.BASE_COEFICIENT)))
				addPlace(place);
		}
	}

	public void onAzimutChanged(double azimut) {
		if (azimut < 0) {
			mAzimut = 360 + GeoUtils.getDegree(azimut);
		} else {
			mAzimut = GeoUtils.getDegree(azimut);
		}
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

	private boolean isPlaceWithinScope(Place place, Location location, double distance) {
		return (GeoUtils.getDistance(new LatLng(place.getLocation().latitude,
				place.getLocation().longitude), new LatLng(mCurrentLocation.getLatitude(),
				mCurrentLocation.getLongitude()))) * 1000 <= distance;
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

		Log.i("RadarView", "onLayout");
		if (mCurrentLocation != null) {
			LatLngBounds latLngBounds = mActivity.getBounds(2 * MonocleActivity.BASE_COEFICIENT);
			int size_x = right - left;
			int size_y = bottom - top;
			int count = getChildCount();
			for (int i = 0; i < count; i++) {
				View v = getChildAt(i);
				String placeId = (String) v.getTag();
				Point point = locationToPoint(mPlaces.get(placeId), latLngBounds, size_x, size_y);
				v.layout(point.x - RADAR_POINTS_SIZE_PX_HALF, point.y - RADAR_POINTS_SIZE_PX_HALF,
						point.x + RADAR_POINTS_SIZE_PX_HALF, point.y + RADAR_POINTS_SIZE_PX_HALF);
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

		Point point = new Point();
		point.x = (int) Math.floor((size_x * delta_long) / max_delta_long);
		point.y = (int) Math.floor((size_y * delta_lat) / max_delta_lat);

		int centerx = (int) Math.floor(size_x / 2);
		int centery = (int) Math.floor(size_y / 2);
		Point center = new Point(centerx, centery);
		return rotatePoint(point, center, mAzimut);
	}

	private Point rotatePoint(Point point, Point center, double angle) {
		double sin = Math.sin(GeoUtils.getRadians(angle));
		double cos = Math.cos(GeoUtils.getRadians(angle));

		point.x -= center.x;
		point.y -= center.y;

		double newx = point.x * cos - point.y * sin;
		double newy = point.x * sin + point.y * cos;

		point.x = (int) (newx + center.x);
		point.y = (int) (newy + center.y);

		return point;
	}
}
