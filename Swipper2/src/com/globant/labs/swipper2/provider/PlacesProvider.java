package com.globant.labs.swipper2.provider;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;

import com.globant.labs.swipper2.SwipperApp;
import com.globant.labs.swipper2.models.Place;
import com.globant.labs.swipper2.repositories.PlaceRepository;
import com.google.android.gms.maps.model.LatLng;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.strongloop.android.loopback.RestAdapter;
import com.strongloop.android.loopback.callbacks.ListCallback;

public class PlacesProvider implements ListCallback<Place> {

	protected PlacesCallback mCallback;
	protected PlaceRepository mRepository;
	protected Multimap<String, Place> mPlaces;
	protected List<Place> mFilteredPlaces;
	protected Set<String> mFilters;
	
	public PlacesProvider(Context context) {
		RestAdapter restAdapter = ((SwipperApp) context.getApplicationContext()).getRestAdapter();
		mRepository = restAdapter.createRepository(PlaceRepository.class);
		mPlaces = ArrayListMultimap.create();
		mFilteredPlaces = new ArrayList<Place>();
		mFilters = new HashSet<String>();
	}
	
	public void setPlacesCallback(PlacesCallback callback) {
		mCallback = callback;
	}
	
	public void updateLocation(LatLng northWest, LatLng southEast) {
		mRepository.nearBy(northWest, southEast, this);
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
		mPlaces.clear();
		
		for(Place p: places) {
			mPlaces.put(p.getCategoryId(), p);
		}		
		
		refreshFilteredPlaces();
		dispatchPlacesUpdated();
	}

	@Override
	public void onError(Throwable t) {
		if(mCallback != null) {
			mCallback.placesError(t);
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
		public void placesError(Throwable t);
	}
	
}
