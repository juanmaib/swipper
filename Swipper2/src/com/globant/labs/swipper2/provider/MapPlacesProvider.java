package com.globant.labs.swipper2.provider;

import java.util.List;

import android.content.Context;

import com.globant.labs.swipper2.models.Place;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

public class MapPlacesProvider extends AbstractPlacesProvider {

	protected LatLngBounds mCurrentBounds;
	
	public MapPlacesProvider(Context context) {
		super(context);
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
	
	@Override
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
	
	@Override
	public void onSuccess(List<Place> places) {
		mPlaces.clear();
		super.onSuccess(places);
	}

}
