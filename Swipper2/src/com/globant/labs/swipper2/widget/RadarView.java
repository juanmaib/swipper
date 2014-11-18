package com.globant.labs.swipper2.widget;

import java.util.ArrayList;

import android.content.Context;
import android.location.Location;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.globant.labs.swipper2.models.Place;
import com.globant.labs.swipper2.utils.GeoUtils;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

public class RadarView extends ViewGroup {

	private static final double DEFAULT_RADIUS = 1000;
	// private static final int NORTH_WEST_BEARING = 315;
	// private static final int SOUTH_EAST_BEARING = 135;
	private static final double NORTH_EAST_BEARING = 45;
	private static final double SOUTH_WEST_BEARING = 225;

	private Context mContext;
	private ArrayList<Place> mPlaces;
	private Location mPreviousLocation;
	private Location mCurrentLocation;
	private LatLngBounds mLatLngBounds;
	private double mSpeed;
	private double mRadius;

	public RadarView(Context context) {
		this(context, null);
	}

	public RadarView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RadarView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mPlaces = new ArrayList<Place>();
	}

	public void onLocationChanged(Location location) {
		setCurrentLocation(location);
		setSpeed();
		setBounds();
	}

	private void setCurrentLocation(Location location) {
		setPreviousLocation(mCurrentLocation);
		this.mCurrentLocation = location;
	}

	private Location getCurrentLocation() {
		return mCurrentLocation;
	}

	private double getSpeed() {
		return mSpeed;
	}

	private void setSpeed() {
		if (getCurrentLocation().hasSpeed()) {
			mSpeed = getCurrentLocation().getSpeed();
		} else {
			if (getPreviousLocation() != null) {
				double distance = getCurrentLocation().distanceTo(mPreviousLocation);
				double timeDiff = getCurrentLocation().getTime() - getPreviousLocation().getTime();
				mSpeed = distance / timeDiff;
			} else {
				mSpeed = 0;
			}
		}
	}

	private void setBounds() {
		// Set the bounds of the radar dinamically, according to speed
		// The idea is that between each update, the points do not get
		// completely removed, but just displaced halfway, instead.
		double speedMultiplier = 1 + (getSpeed() / DEFAULT_RADIUS);
		setRadius(DEFAULT_RADIUS * speedMultiplier);
		LatLng currentLatLng = new LatLng(getCurrentLocation().getLatitude(), getCurrentLocation()
				.getLongitude());
		LatLng northEastLatLng = GeoUtils.getDestinationLocation(currentLatLng, getRadius(),
				NORTH_EAST_BEARING);
		LatLng southWestLatLng = GeoUtils.getDestinationLocation(currentLatLng, getRadius(),
				SOUTH_WEST_BEARING);
		mLatLngBounds = LatLngBounds.builder().include(northEastLatLng).include(southWestLatLng)
				.build();
	}

	public void addPlace(Place place) {
		if (!mPlaces.contains(place)) {
			mPlaces.add(place);
			addPlaceView(place);
		}
	}

	public void removePlace(Place place) {
		mPlaces.remove(place);
		removePlaceView(place);
	}

	private void addPlaceView(Place place) {
		View placeView = new View(mContext);
		placeView.setTag(place);
		addView(placeView);
	}

	private void removePlaceView(Place place) {
		View placeView = findViewWithTag(place);
		if (placeView != null) {
			removeView(placeView);
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		// In onLayout you need to call layout method on each child of this
		// ViewGroup and provide desired position (relatively to parent) for
		// them. You can check source code of FrameLayout (one of the simpliest
		// subclasses of ViewGroup) to find out how it works.
		// Parameters:
		// changed: This is a new size or position for this view
		// left: Left position, relative to parent
		// top: Top position, relative to parent
		// right: Right position, relative to parent
		// bottom: Bottom position, relative to parent

		Log.i("onLayout", "changed: " + changed);
		Log.i("onLayout", "left: " + left);
		Log.i("onLayout", "top: " + top);
		Log.i("onLayout", "right: " + right);
		Log.i("onLayout", "bottom: " + bottom);
	}

	public Location getPreviousLocation() {
		return mPreviousLocation;
	}

	public void setPreviousLocation(Location mPreviousLocation) {
		this.mPreviousLocation = mPreviousLocation;
	}

	public double getRadius() {
		return mRadius;
	}

	private void setRadius(double mRadius) {
		this.mRadius = mRadius;
	}

}
