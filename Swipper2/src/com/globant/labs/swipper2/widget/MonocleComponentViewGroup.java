package com.globant.labs.swipper2.widget;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.globant.labs.swipper2.MonocleActivity;
import com.globant.labs.swipper2.R;
import com.globant.labs.swipper2.models.Place;
import com.globant.labs.swipper2.utils.GeoUtils;

public abstract class MonocleComponentViewGroup extends FrameLayout {

	private static String LODGING;
	private static String TAXI;
	private static String GAS;
	private static String CAR_RENTAL;
	private static String FOOD;

	private Drawable mLodgingDrawable;
	private Drawable mTaxiDrawable;
	private Drawable mGasDrawable;
	private Drawable mCarRentalDrawable;
	private Drawable mFoodDrawable;

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
		setInflater(LayoutInflater.from(context));
		setUpCategoriesStrings();
		setUpBackgroundDrawables();
	}

	private void setUpCategoriesStrings() {
		setLodgingString(getResources().getString(R.string.lodging));
		setTaxiString(getResources().getString(R.string.taxi));
		setGasString(getResources().getString(R.string.gas));
		setCarRentalString(getResources().getString(R.string.carrental));
		setFoodString(getResources().getString(R.string.food));
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

	protected abstract void setUpBackgroundDrawables();

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

	public static String getLodgingString() {
		return LODGING;
	}

	public static String getTaxiString() {
		return TAXI;
	}

	public static String getGasString() {
		return GAS;
	}

	public static String getCarRentalString() {
		return CAR_RENTAL;
	}

	public static String getFoodString() {
		return FOOD;
	}

	private static void setLodgingString(String lodging) {
		LODGING = lodging;
	}

	private static void setTaxiString(String taxi) {
		TAXI = taxi;
	}

	private static void setGasString(String gas) {
		GAS = gas;
	}

	private static void setCarRentalString(String carRental) {
		CAR_RENTAL = carRental;
	}

	private static void setFoodString(String food) {
		FOOD = food;
	}

	protected Drawable getLodgingDrawable() {
		return mLodgingDrawable;
	}

	protected Drawable getTaxiDrawable() {
		return mTaxiDrawable;
	}

	protected Drawable getGasDrawable() {
		return mGasDrawable;
	}

	protected Drawable getCarRentalDrawable() {
		return mCarRentalDrawable;
	}

	protected Drawable getFoodDrawable() {
		return mFoodDrawable;
	}

	protected void setLodgingDrawable(Drawable lodgingDrawable) {
		mLodgingDrawable = lodgingDrawable;
	}

	protected void setTaxiDrawable(Drawable taxiDrawable) {
		mTaxiDrawable = taxiDrawable;
	}

	protected void setGasDrawable(Drawable gasDrawable) {
		mGasDrawable = gasDrawable;
	}

	protected void setCarRentalDrawable(Drawable carRentalDrawable) {
		mCarRentalDrawable = carRentalDrawable;
	}

	protected void setFoodDrawable(Drawable foodDrawable) {
		mFoodDrawable = foodDrawable;
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
