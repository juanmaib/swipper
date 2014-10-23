package com.globant.labs.swipper2;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.globant.labs.swipper2.drawer.CategoriesAdapter;
import com.globant.labs.swipper2.drawer.CategoryMapper;
import com.globant.labs.swipper2.drawer.DrawerCategoryItem;
import com.globant.labs.swipper2.drawer.NavigationDrawerFragment;
import com.globant.labs.swipper2.fragments.PlacesAdapter;
import com.globant.labs.swipper2.fragments.PlacesListFragment;
import com.globant.labs.swipper2.fragments.PlacesMapFragment;
import com.globant.labs.swipper2.models.Place;
import com.globant.labs.swipper2.provider.PlacesProvider;
import com.globant.labs.swipper2.provider.PlacesProvider.PlacesCallback;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.model.LatLng;

public class MainActivity extends ActionBarActivity implements
		NavigationDrawerFragment.NavigationDrawerCallbacks, LocationListener, ViewTreeObserver.OnGlobalLayoutListener {

	private static final String PREF_WALKTHROUGH_DISPLAYED = "walkthrough_displayed";
	
	// implements NavigationDrawerFragment.NavigationDrawerCallbacks,
	// OnConnectionFailedListener, LocationListener,
	// OnMyLocationButtonClickListener, ConnectionCallbacks

	protected PlacesProvider mPlacesProvider;
	
	protected boolean mFarZoom;
	protected LatLng mLastNorthWest;
	protected LatLng mLastSouthEast;
	
	protected Location mCurrentLocation;
	
	//protected LocationManager mLocationManager;
	
	protected ViewPager mViewPager;
	protected MainFragmentsAdapter mFragmentsAdapter;
	
	protected PlacesMapFragment mMapFragment;
	protected PlacesListFragment mListFragment;
	
	protected boolean mDisplayedWalkthrough;
	
	/**
	 * Fragment managing the behaviors, interactions and presentation of the
	 * navigation drawer.
	 */
	private NavigationDrawerFragment mNavigationDrawerFragment;

	/**
	 * Used to store the last screen title. For use in
	 * {@link #restoreActionBar()}.
	 */
	//private CharSequence mTitle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		mDisplayedWalkthrough = sp.getBoolean(PREF_WALKTHROUGH_DISPLAYED, false);
		
		mPlacesProvider = new PlacesProvider(this);
		
		mFarZoom = false;
		
		mViewPager = (ViewPager) findViewById(R.id.viewPager);
		mFragmentsAdapter = new MainFragmentsAdapter(getSupportFragmentManager(), mPlacesProvider, this);
		mViewPager.setAdapter(mFragmentsAdapter);
		mViewPager.setCurrentItem(MainFragmentsAdapter.MAP_FRAGMENT);
		// Preload the "other" "page"
		mViewPager.setOffscreenPageLimit(1);
		
		//mTitle = getTitle();

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
			//mMapFragment = (PlacesMapFragment) getSupportFragmentManager()
			//		.findFragmentById(R.id.map);
			mMapFragment = mFragmentsAdapter.getMapFragment();
			mListFragment = mFragmentsAdapter.getListFragment();
									
			// Get last known position from splash activity
			Bundle extras = getIntent().getExtras();
			if (extras != null) {
			    mCurrentLocation = (Location) extras.get(SplashScreen.LAST_KNOWN_LOCATION_EXTRA);
			}
					
			//Center the map around last known position (or 0,0 if we couldn't obtain the user location)
			//mMapFragment.getMap().animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastKnownPosition[0], lastKnownPosition[1]), 15));

			// Getting LocationManager object from System Service
			// LOCATION_SERVICE
			//mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

			// Creating a criteria object to retrieve provider
			//Criteria criteria = new Criteria();

			// Getting the name of the best provider
			//String provider = mLocationManager.getBestProvider(criteria, true);

			// Getting Current Location
			//mCurrentLocation = mLocationManager.getLastKnownLocation(provider);

			if (mCurrentLocation != null) {
				onLocationChanged(mCurrentLocation);
			}
			
			mPlacesProvider.setPlacesCallback(new PlacesCallback() {
				
				@Override
				public void placesUpdated(List<Place> places) {
					((PlacesAdapter) mListFragment.getListAdapter()).setDataChanged();
					mMapFragment.displayPlaces(places, mCurrentLocation);
				}
				
				@Override
				public void placesRetry(Throwable t) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void placesError(Throwable t) {
					Log.i("SWIPPER", "Places error");
					t.printStackTrace();
				}
				
			});	

			
			//showCoachMarks();

			//mLocationManager.requestLocationUpdates(provider, 20000, 0, this);
			//locationManager.requestSingleUpdate(provider, this, null);
			
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
		mNavigationDrawerFragment.getView().getViewTreeObserver().addOnGlobalLayoutListener(this);
	}
			
	public PlacesProvider getPlacesProvider() {
		return mPlacesProvider;
	}
	
	public Context getContext() {
		return this;
	}

	public void restoreActionBar() {
		ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		//actionBar.setTitle(mTitle);
		//actionBar.setTitle("");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!mNavigationDrawerFragment.isDrawerOpen()) {
			// Only show items in the action bar relevant to this screen
			// if the drawer is not showing. Otherwise, let the drawer
			// decide what to show in the action bar.
			//getMenuInflater().inflate(R.menu.main, menu);
			menu.clear();
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
		int id = item.getItemId();
		
		
		if (id == R.id.action_list) {
			mViewPager.setCurrentItem(MainFragmentsAdapter.LIST_FRAGMENT);			
			return true;
		}else if(id == R.id.action_map) {
			mViewPager.setCurrentItem(MainFragmentsAdapter.MAP_FRAGMENT);
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}

	public void displayNavigation(Place p) {
		String url = "http://maps.google.com/maps?"
						+ "daddr="
						+ p.getLocation().latitude
						+ ","
						+ p.getLocation().longitude;
		
		Intent intent = new Intent(android.content.Intent.ACTION_VIEW,  Uri.parse(url));
		startActivity(intent);
	}
	
	// Implementation of {@link LocationListener}.
	@Override
	public void onLocationChanged(Location location) {
		mCurrentLocation = location;
		//mLocationManager.removeUpdates(this);
		mMapFragment.setCurrentLocation(mCurrentLocation);
		mMapFragment.displayCurrentLocation();
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
	
	public void showCoachMarks(){
	    final Dialog dialog = new Dialog(this, R.style.Theme_Transparent);
	    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
	    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
	    dialog.setContentView(R.layout.coach_marks);
	    dialog.setCanceledOnTouchOutside(true);
	    
	    View masterView = dialog.findViewById(R.id.coach_marks_master_view);
	    masterView.setOnClickListener(new View.OnClickListener() {
	        @Override
	        public void onClick(View view) {
	            dialog.dismiss();
	        }
	    });
	    
	    dialog.setOnDismissListener(new OnDismissListener() {			
			@Override
			public void onDismiss(DialogInterface dialog) {
				mDisplayedWalkthrough = true;
				SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplication());
				sp.edit().putBoolean(PREF_WALKTHROUGH_DISPLAYED, true).commit();
			}
		});
	    
		ListView listView = (ListView) mNavigationDrawerFragment.getView().findViewById(R.id.drawerListView);
		
		int listViewWidth = listView.getWidth();
		int itemHeight = listView.getChildAt(0).getHeight();
		
		int[] location = new int[2];				
		
		listView.getChildAt(0).getLocationOnScreen(location);
		int firstItemY = location[1];
		
		listView.findViewById(R.id.apply_button).getLocationOnScreen(location);
		int secondItemY = location[1];
					
		ImageView coachCategories = (ImageView) dialog.findViewById(R.id.coachCategories);
		RelativeLayout.LayoutParams lpCategories = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		lpCategories.setMargins(listViewWidth, firstItemY + itemHeight / 2, 0, 0);					
		coachCategories.setLayoutParams(lpCategories);
		
		ImageView coachApply = (ImageView) dialog.findViewById(R.id.coachApply);
		RelativeLayout.LayoutParams lpApply = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		lpApply.setMargins(listViewWidth / 2, secondItemY + itemHeight, 0, 0);
		coachApply.setLayoutParams(lpApply);
	    
	    dialog.show();
	}

	@Override
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	public void onGlobalLayout() {	
		View drawerView = mNavigationDrawerFragment.getView();
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			drawerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
		}else{
			drawerView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
		}
					
		if(!mDisplayedWalkthrough) {
			showCoachMarks();
		}
	}
		

}
