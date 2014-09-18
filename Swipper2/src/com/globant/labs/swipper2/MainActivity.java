package com.globant.labs.swipper2;

import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.globant.labs.swipper2.drawer.CategoriesAdapter;
import com.globant.labs.swipper2.drawer.CategoryMapper;
import com.globant.labs.swipper2.drawer.DrawerCategoryItem;
import com.globant.labs.swipper2.drawer.NavigationDrawerFragment;
import com.globant.labs.swipper2.models.Place;
import com.globant.labs.swipper2.provider.PlacesProvider;
import com.globant.labs.swipper2.provider.PlacesProvider.PlacesCallback;
import com.globant.labs.swipper2.utils.GeoUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends ActionBarActivity implements
		NavigationDrawerFragment.NavigationDrawerCallbacks, LocationListener {

	// implements NavigationDrawerFragment.NavigationDrawerCallbacks,
	// OnConnectionFailedListener, LocationListener,
	// OnMyLocationButtonClickListener, ConnectionCallbacks

	protected PlacesProvider mPlacesProvider;
	
	protected boolean mFarZoom;
	protected LatLng mLastNorthWest;
	protected LatLng mLastSouthEast;
	
	/**
	 * Fragment managing the behaviors, interactions and presentation of the
	 * navigation drawer.
	 */
	private NavigationDrawerFragment mNavigationDrawerFragment;

	/**
	 * Used to store the last screen title. For use in
	 * {@link #restoreActionBar()}.
	 */
	private CharSequence mTitle;

	private GoogleMap mMap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mPlacesProvider = new PlacesProvider(this);
		mFarZoom = false;
		
		mTitle = getTitle();

		// Getting Google Play availability status
		int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());

		// Showing status
		if (status != ConnectionResult.SUCCESS) { // Google Play Services are
													// not available
			int requestCode = 10;
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, requestCode);
			dialog.show();
		} else { // Google Play Services are available

			// Getting reference to the SupportMapFragment of activity_main.xml
			SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map);

			// Getting GoogleMap object from the fragment
			mMap = fm.getMap();
			
			// Enabling MyLocation Layer of Google Map
			mMap.setMyLocationEnabled(true);
			
			// Getting LocationManager object from System Service
			// LOCATION_SERVICE
			LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

			// Creating a criteria object to retrieve provider
			Criteria criteria = new Criteria();

			// Getting the name of the best provider
			String provider = locationManager.getBestProvider(criteria, true);

			// Getting Current Location
			Location location = locationManager.getLastKnownLocation(provider);

			if (location != null) {
				onLocationChanged(location);
			}
			
			mPlacesProvider.setPlacesCallback(new PlacesCallback() {
				
				@Override
				public void placesUpdated(List<Place> places) {
					mMap.clear();
					generateMarkers(places);
				}
				
				@Override
				public void placesError(Throwable t) {
					Log.i("SWIPPER", "Places error");
					t.printStackTrace();
				}
			});	
			
			mMap.setOnCameraChangeListener(new OnCameraChangeListener() {
				
				@Override
				public void onCameraChange(CameraPosition camPosition) {					
					if(camPosition.zoom > 10) {
						LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
						LatLng northWest = new LatLng(bounds.northeast.latitude, bounds.southwest.longitude);
						LatLng southEast = new LatLng(bounds.southwest.latitude, bounds.northeast.longitude);
						
						if(mLastNorthWest == null
								|| mLastSouthEast == null
								|| !GeoUtils.isInBounds(northWest, mLastNorthWest, mLastSouthEast)
								|| !GeoUtils.isInBounds(southEast, mLastNorthWest, mLastSouthEast)) {
						
							mLastNorthWest = northWest;
							mLastSouthEast = southEast;
							mPlacesProvider.updateLocation(northWest, southEast);					
						}else if(mFarZoom) {
							mFarZoom = false;
							generateMarkers(mPlacesProvider.getFilteredPlaces());
						}
					}else{
						mFarZoom = true;
						mMap.clear();
					}
				}
			});
			//locationManager.requestLocationUpdates(provider, 20000, 0, this);
			locationManager.requestSingleUpdate(provider, this, null);
		}

		// Get the drawer
		mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager()
				.findFragmentById(R.id.navigation_drawer);

		// Set up the drawer.
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
				(DrawerLayout) findViewById(R.id.drawer_layout));
		
		CategoriesAdapter catAdapter = new CategoriesAdapter(getContext());
		for(DrawerCategoryItem cat : CategoryMapper.getStaticCategories()) {
			cat.setChecked(true);
			cat.setAppliedState(true);
			catAdapter.addCategory(cat);
		}
		
		mNavigationDrawerFragment.setAdapter(catAdapter);

	}
	
	public void generateMarkers(List<Place> places) {
		for(Place p: places) {
			MarkerOptions marker = new MarkerOptions()
				.position(p.getLocation())
				.title(p.getName())
				.icon(BitmapDescriptorFactory.fromResource(CategoryMapper.getCategoryIcon(p.getCategoryId())));
			mMap.addMarker(marker);
		}
	}
	
	public Context getContext() {
		return this;
	}

	public void onSectionAttached(int number) {
		switch (number) {
		case 1:
			mTitle = getString(R.string.title_section1);
			break;
		case 2:
			mTitle = getString(R.string.title_section2);
			break;
		case 3:
			mTitle = getString(R.string.title_section3);
			break;
		case 4:
			mTitle = getString(R.string.title_section4);
			break;
		case 5:
			mTitle = getString(R.string.title_section5);
			break;
		case 6:
			mTitle = getString(R.string.title_section6);
			break;
		}
	}

	public void restoreActionBar() {
		ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		//actionBar.setTitle(mTitle);
		actionBar.setTitle("");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!mNavigationDrawerFragment.isDrawerOpen()) {
			// Only show items in the action bar relevant to this screen
			// if the drawer is not showing. Otherwise, let the drawer
			// decide what to show in the action bar.
			getMenuInflater().inflate(R.menu.main, menu);
			restoreActionBar();
			return true;
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		//int id = item.getItemId();
		//if (id == R.id.action_settings) {
		//	return true;
		//}
		return super.onOptionsItemSelected(item);
	}

	// Implementation of {@link LocationListener}.
	@Override
	public void onLocationChanged(Location location) {
		// Getting latitude of the current location
		double latitude = location.getLatitude();

		// Getting longitude of the current location
		double longitude = location.getLongitude();

		// Creating a LatLng object for the current location
		LatLng latLng = new LatLng(latitude, longitude);

		// Showing the current location in Google Map
		mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

		// Zoom in the Google Map
		mMap.animateCamera(CameraUpdateFactory.zoomTo(15));		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onSelectionApplied(List<String> ids) {
		mPlacesProvider.setFilters(ids);
	}

}
