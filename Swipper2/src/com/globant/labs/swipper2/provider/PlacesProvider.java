package com.globant.labs.swipper2.provider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;

import com.globant.labs.swipper2.SwipperApp;
import com.globant.labs.swipper2.models.Place;
import com.globant.labs.swipper2.repositories.PlaceRepository;
import com.globant.labs.swipper2.utils.GeoUtils;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.strongloop.android.loopback.RestAdapter;
import com.strongloop.android.loopback.callbacks.ListCallback;

public class PlacesProvider implements ListCallback<Place> {

	protected final static int MAX_RETRIES = 3;
	
	protected PlacesCallback mCallback;
	protected PlaceRepository mRepository;
	protected Multimap<String, Place> mPlaces;
	protected List<Place> mFilteredPlaces;
	protected Set<String> mFilters;
	protected LatLng mCurrentLocation;
	protected LatLngBounds mCurrentBounds;
	protected Comparator<Place> mPlacesComparator;
	protected int mRetriesLeft;
	
	public PlacesProvider(Context context) {
		resetRetries();
		
		RestAdapter restAdapter = ((SwipperApp) context.getApplicationContext()).getRestAdapter();
		mRepository = restAdapter.createRepository(PlaceRepository.class);
		mPlaces = ArrayListMultimap.create();
		mFilteredPlaces = new ArrayList<Place>();
		mFilters = new HashSet<String>();
				
		mPlacesComparator = new Comparator<Place>() {
			
			@Override
			public int compare(Place p0, Place p1) {
				double d0 = getDistanceTo(p0);
				double d1 = getDistanceTo(p1);
				
				if(d0 < d1) return -1;
				if(d0 > d1) return 1;
				return 0;
			}
			
		};
	}
	
	public void setPlacesCallback(PlacesCallback callback) {
		mCallback = callback;
	}
	
	//public void updateLocation(LatLng northWest, LatLng southEast) {
	//	mRepository.nearBy(northWest, southEast, this);
	//}
	
	public boolean updateLocation(LatLngBounds bounds) {
		if(mCurrentBounds == null
				|| !mCurrentBounds.contains(bounds.northeast)
				|| !mCurrentBounds.contains(bounds.southwest)) {
			
			mCurrentBounds = bounds;
			loadPlaces();
			
			return false;
		}else{
			return true;
		}
	}
	
	protected void loadPlaces() {
		if(mCurrentBounds != null) {
			LatLng northWest = new LatLng(
					mCurrentBounds.northeast.latitude + 0.036,
					mCurrentBounds.southwest.longitude - 0.036);
			
			LatLng southEast = new LatLng(
					mCurrentBounds.southwest.latitude - 0.036,
					mCurrentBounds.northeast.longitude + 0.036);
			
			mRepository.nearBy(northWest, southEast, this);
		}
	}
	
	protected void resetRetries() {
		mRetriesLeft = MAX_RETRIES;
	}
		
	public void setFilters(List<String> filters) {
		mFilters.clear();
		for(String filter: filters) {
			mFilters.add(filter);
		}
		refreshFilteredPlaces();
		dispatchPlacesUpdated();
	}
	
	public void addFilter(String filter) {
		mFilters.add(filter);
		refreshFilteredPlaces();
		dispatchPlacesUpdated();
	}
	
	public void removeFilter(String filter) {
		mFilters.remove(filter);
		refreshFilteredPlaces();
		dispatchPlacesUpdated();
	}
	
	public void clearFilters() {
		mFilters.clear();
		refreshFilteredPlaces();
		dispatchPlacesUpdated();
	}
	
	@Override
	public void onSuccess(List<Place> places) {
		resetRetries();
		mPlaces.clear();
		
		for(Place p: places) {
			mPlaces.put(p.getCategory(), p);
		}		
		
		refreshFilteredPlaces();
		dispatchPlacesUpdated();
	}

	@Override
	public void onError(Throwable t) {
		if(mRetriesLeft > 0) {
			if(mCallback != null) {
				mCallback.placesRetry(t);
			}
			mRetriesLeft--;
			loadPlaces();
		}else{
			if(mCallback != null) {
				mCallback.placesError(t);
			}
		}
	}
	
	protected void dispatchPlacesUpdated() {
		if(mCallback != null) {
			mCallback.placesUpdated(getFilteredPlaces());
		}
	}
	
	public void refreshFilteredPlaces() {
		mFilteredPlaces.clear();
		
		if(!mFilters.isEmpty()) {
			for(String filter: mFilters) {
				mFilteredPlaces.addAll(mPlaces.get(filter));
			}			
		}else{
			for(String key: mPlaces.keySet()) {
				mFilteredPlaces.addAll(mPlaces.get(key));
			}
		}
		
		Collections.sort(mFilteredPlaces, mPlacesComparator);
	}
	
	public List<Place> getFilteredPlaces() {
		return mFilteredPlaces;
	}
	
	public int getFilteredPlacesCount() {
		return mFilteredPlaces.size();
	}
	
	public Place getFilteredPlace(int position) {
		return mFilteredPlaces.get(position);
	}
	
	public interface PlacesCallback {
		public void placesUpdated(List<Place> places);
		public void placesRetry(Throwable t);
		public void placesError(Throwable t);
	}
	
	public void setCurrentLocation(LatLng currentLocation) {
		mCurrentLocation = currentLocation;
	}
	
	public double getDistanceTo(Place p) {
		return GeoUtils.getDistance(mCurrentLocation, p.getLocation());
	}

}
