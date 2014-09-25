package com.globant.labs.swipper2.fragments;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.globant.labs.swipper2.MainActivity;
import com.globant.labs.swipper2.R;
import com.globant.labs.swipper2.SwipperInfoWindowAdapter;
import com.globant.labs.swipper2.drawer.CategoryMapper;
import com.globant.labs.swipper2.models.Place;
import com.globant.labs.swipper2.provider.PlacesProvider;
import com.globant.labs.swipper2.utils.GeoUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class PlacesMapFragment extends SupportMapFragment {

	protected GoogleMap mMap;
	protected Activity mActivity;
	protected PlacesProvider mPlacesProvider;
	
	protected boolean mFarZoom;
	protected LatLng mLastNorthWest;
	protected LatLng mLastSouthEast;
	
	protected Location mCurrentLocation;
	
	protected Map<String, Marker> mMarkers;
	
	public PlacesMapFragment() {
		super();
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mMarkers = new HashMap<String, Marker>();
    }
	
	@Override
	public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;  
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, container, savedInstanceState);
		
		mMap = getMap();
		mMap.setMyLocationEnabled(true);
		
		return v;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		mPlacesProvider = ((MainActivity) getActivity()).getPlacesProvider();
		
		mMap.setInfoWindowAdapter(new SwipperInfoWindowAdapter(mActivity));
		mMap.setOnCameraChangeListener(new OnCameraChangeListener() {
			
			@Override
			public void onCameraChange(CameraPosition camPosition) {					
				if(camPosition.zoom > 10) {
					LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
				
					if(mPlacesProvider.updateLocation(bounds) && mFarZoom) {	
						displayPlaces(mPlacesProvider.getFilteredPlaces(), mCurrentLocation);
					}
					
					mFarZoom = false;
					
				}else{
					mFarZoom = true;
					clear();
				}
			}
		});
		
		mMap.setOnMyLocationChangeListener(new OnMyLocationChangeListener() {		
			@Override
			public void onMyLocationChange(Location myLocation) {
				mPlacesProvider.setCurrentLocation(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()));
			}
		});
		
		if(mCurrentLocation != null) {
			displayCurrentLocation();
		}
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	    inflater.inflate(R.menu.map, menu);
	    super.onCreateOptionsMenu(menu, inflater);
	}
	
	public void setCurrentLocation(Location location) {
		mCurrentLocation = location;		
	}
	
	public void displayLocation(Location location) {
		if(mMap != null) {
			double latitude = location.getLatitude();
			double longitude = location.getLongitude();
	
			LatLng latLng = new LatLng(latitude, longitude);
	
			//mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
			//mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
			mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
		}
	}
	
	public void displayCurrentLocation() {
		displayLocation(mCurrentLocation);
	}
	
	public void clear() {
		if(mMap != null) {
			mMap.clear();
		}
	}
	
	public void displayPlaces(List<Place> places, Location currentLocation) {
		
		LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
		List<String> keeps = new ArrayList<String>();
	
		LatLng myLocation = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
		DecimalFormat df = new DecimalFormat("0.00"); 
		for(Place p: places) {
			if(!mMarkers.containsKey(p.getId())) {			
				MarkerOptions marker = new MarkerOptions()
					.position(p.getLocation())
					.title(p.getName())
					.snippet(df.format(GeoUtils.getDistance(p.getLocation(), myLocation))+" km")
					.anchor(0.35f, 1.0f)
					.infoWindowAnchor(0.35f, 0.2f)
					.icon(BitmapDescriptorFactory.fromResource(CategoryMapper.getCategoryMarker(p.getCategoryId())));
					
				Marker m = mMap.addMarker(marker);
				mMarkers.put(p.getId(), m);
			}
			
			keeps.add(p.getId());
		}
		
		Iterator<Entry<String, Marker>> it = mMarkers.entrySet().iterator();
		
		while (it.hasNext()) {
			Entry<String, Marker> entry = it.next();
			if(!bounds.contains(entry.getValue().getPosition()) || !keeps.contains(entry.getKey())) {
				entry.getValue().remove();
				it.remove();
			}
		}
	}
	
	@Override
	public void onDestroyView() {
		mMap = null;
		super.onDestroyView();
	}
	
	@Override
	public void onDetach() {
		mActivity = null;
		super.onDetach();
	}
}
