package com.globant.labs.swipper2.fragments;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.globant.labs.swipper2.MainActivity;
import com.globant.labs.swipper2.PlaceDetailActivity;
import com.globant.labs.swipper2.R;
import com.globant.labs.swipper2.SwipperInfoWindowAdapter;
import com.globant.labs.swipper2.drawer.CategoryMapper;
import com.globant.labs.swipper2.models.Place;
import com.globant.labs.swipper2.provider.PlacesProvider;
import com.globant.labs.swipper2.utils.GeoUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
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
	protected Map<String, Place> mIdsMap;
	
	protected TextView mStatusTextView;
	protected String mStatusText;
	
	protected String mStatusLoadingString;
	protected String mStatusDoneString;
	protected String mStatusNoPlacesString;
	protected String mStatusZoomInString;
	
	public PlacesMapFragment() {
		super();
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mMarkers = new HashMap<String, Marker>();
        mIdsMap = new HashMap<String, Place>();
        mStatusText = "";
        
        Resources resources = getResources();
        mStatusLoadingString = resources.getString(R.string.status_loading);
        mStatusDoneString = resources.getString(R.string.status_done);
        mStatusNoPlacesString = resources.getString(R.string.status_no_places);
        mStatusZoomInString = resources.getString(R.string.status_zoom_in);
    }
	
	@Override
	public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;  
    }
	
	@Override
	@SuppressLint("InflateParams")
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		RelativeLayout view = (RelativeLayout) inflater.inflate(R.layout.status_bar, null);
		
		FrameLayout mapFrame = (FrameLayout) super.onCreateView(inflater, container, savedInstanceState);
		
		view.addView(mapFrame);
		LayoutParams layoutParams = new LayoutParams(mapFrame.getLayoutParams());
		layoutParams.addRule(RelativeLayout.ABOVE, R.id.statusText);
		mapFrame.setLayoutParams(layoutParams);
				
		mMap = getMap();
		mMap.setMyLocationEnabled(true);
		
		mStatusTextView = (TextView) view.findViewById(R.id.statusText);
		mStatusTextView.setText(mStatusText);
		
		return view;
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
					
					setStatusText(mStatusLoadingString);
					
					LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
				
					if(mPlacesProvider.updateLocation(bounds)) {	
						displayPlaces(mPlacesProvider.getFilteredPlaces(), mCurrentLocation);
					}
					
					mFarZoom = false;
					
				}else{
					setStatusText(mStatusZoomInString);
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
		
		mMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
			@Override
			public void onInfoWindowClick(Marker marker) {
				Place p = mIdsMap.get(marker.getId());
				
				Intent intent = new Intent(getActivity(), PlaceDetailActivity.class);
				intent.putExtra(PlaceDetailActivity.PLACE_ID_EXTRA, p.getId());
				intent.putExtra(PlaceDetailActivity.PLACE_NAME_EXTRA, p.getName());
				intent.putExtra(PlaceDetailActivity.PLACE_CATEGORY_EXTRA, p.getCategory());
				intent.putExtra(PlaceDetailActivity.PLACE_DISTANCE_EXTRA, mPlacesProvider.getDistanceTo(p));
				startActivity(intent);
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
			mMarkers.clear();
			mIdsMap.clear();
		}
	}
	
	public void displayPlaces(List<Place> places, Location currentLocation) {
		LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
		List<String> keeps = new ArrayList<String>();
		
		LatLng myLocation = null;
		if(currentLocation != null) {
			myLocation = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
		}
		
		DecimalFormat df = new DecimalFormat("0.00"); 
		for(Place p: places) {
			if(!mMarkers.containsKey(p.getId())) {			
				MarkerOptions marker = new MarkerOptions()
					.position(p.getLocation())
					.title(p.getName())
					.snippet(df.format(GeoUtils.getDistance(p.getLocation(), myLocation))+" km")
					.anchor(0.35f, 1.0f)
					.infoWindowAnchor(0.35f, 0.2f)
					.icon(BitmapDescriptorFactory.fromResource(CategoryMapper.getCategoryMarker(p.getCategory())));
					
				Marker m = mMap.addMarker(marker);
				mMarkers.put(p.getId(), m);
				mIdsMap.put(m.getId(), p);
			}
			
			keeps.add(p.getId());
		}
		
		Iterator<Entry<String, Marker>> it = mMarkers.entrySet().iterator();
		
		while (it.hasNext()) {
			Entry<String, Marker> entry = it.next();
			if(!bounds.contains(entry.getValue().getPosition()) || !keeps.contains(entry.getKey())) {
				mIdsMap.remove(entry.getValue().getId());
				entry.getValue().remove();
				it.remove();
			}
		}
		
		if(mMarkers.size() == 0) {
			setStatusText(mStatusNoPlacesString);
		}else{
			setStatusText(mStatusDoneString);
		}

	}

	protected void setStatusText(String text) {
		mStatusText = text;
		if(mStatusTextView != null) {
			mStatusTextView.setText(mStatusText);
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
