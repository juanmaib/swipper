package com.globant.labs.swipper2.widget;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.location.Location;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.globant.labs.swipper2.MonocleActivity;
import com.globant.labs.swipper2.models.Place;
import com.globant.labs.swipper2.utils.GeoUtils;

public abstract class MonocleComponentViewGroup extends ViewGroup {

	private MonocleActivity mActivity;
	private HashMap<String, Place> mPlaces;
	private LayoutInflater mInflater;

	public MonocleComponentViewGroup(Context context) {
		this(context, null);
	}

	public MonocleComponentViewGroup(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public MonocleComponentViewGroup(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setActivity((MonocleActivity) getContext());
		setPlaces(new HashMap<String, Place>());
		setInflater(getActivity().getLayoutInflater());
	}

	public void onLocationChanged(Location location) {
		removeOutOfScopePlaces();
	}

	public void onPlacesUpdate(List<Place> places) {
		for (Place place : places) {
			if (GeoUtils.isPlaceWithinScope(place, getActivity().getCurrentLocation(),
					getActivity().getRadius(MonocleActivity.BASE_COEFICIENT)))
				addPlace(place);
		}
	}

	public void addPlace(Place place) {
		if (!getPlaces().containsKey(place.getId())) {
			getPlaces().put(place.getId(), place);
			addPlaceView(place);
		}
	}

	// The contract here is that you must add a single tag to the view with the
	// place id string when you override this method. This way, we keep a
	// reference to which view corresponds to which place
	protected abstract void addPlaceView(Place place);

	public void removePlace(Place place) {
		getPlaces().remove(place.getId());
		removePlaceView(place);
	}

	private void removePlaceView(Place place) {
		View placeView = findViewWithTag(place.getId());
		if (placeView != null) {
			removeView(placeView);
		}
	}

	private void removeOutOfScopePlaces() {
		double radius = getActivity().getRadius(MonocleActivity.BASE_COEFICIENT);
		for (int i = 0; i < getChildCount(); i++) {
			String placeId = (String) getChildAt(i).getTag();
			Place place = getPlaces().get(placeId);
			if (!GeoUtils.isPlaceWithinScope(place, getActivity().getCurrentLocation(), radius)) {
				removePlace(place);
			}
		}
	}

	public MonocleActivity getActivity() {
		return mActivity;
	}

	public HashMap<String, Place> getPlaces() {
		return mPlaces;
	}

	public LayoutInflater getInflater() {
		return mInflater;
	}

	private void setActivity(MonocleActivity activity) {
		this.mActivity = activity;
	}

	private void setPlaces(HashMap<String, Place> places) {
		this.mPlaces = places;
	}

	private void setInflater(LayoutInflater inflater) {
		this.mInflater = inflater;
	}

}
