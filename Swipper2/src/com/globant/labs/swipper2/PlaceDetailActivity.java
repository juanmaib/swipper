package com.globant.labs.swipper2;

import java.text.DecimalFormat;
import java.util.List;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.globant.labs.swipper2.drawer.CategoryMapper;
import com.globant.labs.swipper2.models.Photo;
import com.globant.labs.swipper2.models.Place;
import com.globant.labs.swipper2.models.PlaceDetails;
import com.globant.labs.swipper2.repositories.PlaceDetailsRepository;
import com.strongloop.android.loopback.RestAdapter;
import com.strongloop.android.loopback.callbacks.ObjectCallback;

public class PlaceDetailActivity extends ActionBarActivity implements ObjectCallback<PlaceDetails>{

	public static final String PLACE_ID_EXTRA = "place-id-extra";
	public static final String PLACE_NAME_EXTRA = "place-name-extra";
	public static final String PLACE_CATEGORY_EXTRA = "place-category-extra";
	public static final String PLACE_DISTANCE_EXTRA = "place-distance-extra";
	
	protected int mCategoryStringId;
	protected int mCategoryMarkerId;
	
	protected RelativeLayout mProgressBarLayout;
	protected TextView mAddressTextView;
	protected TextView mCityTextView;
	protected TextView mDistanceTextView;
	protected TextView mPhoneTextView;
	protected TextView mScheduleTextView;
	protected ImageView mNavImageView;
	
	protected PlaceDetails mPlace;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.place_details);
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		Bundle extras = getIntent().getExtras();
		String placeId = extras.getString(PLACE_ID_EXTRA);
		String placeName = extras.getString(PLACE_NAME_EXTRA);
		String placeCategory = extras.getString(PLACE_CATEGORY_EXTRA);
		double placeDistance = extras.getDouble(PLACE_DISTANCE_EXTRA);
		
		Log.i("SWIPPER", "PlaceId: "+placeId);
		
		setTitle(placeName);
		
		mCategoryMarkerId = CategoryMapper.getCategoryMarker(placeCategory);
		mCategoryStringId = CategoryMapper.getCategoryText(placeCategory);
		
		mAddressTextView = (TextView) findViewById(R.id.addressText);
		mCityTextView = (TextView) findViewById(R.id.cityText);
		mDistanceTextView = (TextView) findViewById(R.id.distanceText);
		mPhoneTextView = (TextView) findViewById(R.id.phoneText);
		mScheduleTextView = (TextView) findViewById(R.id.scheduleText);
		mNavImageView = (ImageView) findViewById(R.id.navImage);
		
		mProgressBarLayout = (RelativeLayout) findViewById(R.id.progressBarLayout);
				
		RestAdapter restAdapter = ((SwipperApp) getApplication()).getRestAdapter();
		PlaceDetailsRepository placeDetailsRepo = restAdapter.createRepository(PlaceDetailsRepository.class);
		placeDetailsRepo.details(placeId, this);

		DecimalFormat df = new DecimalFormat("0.00");
		mDistanceTextView.setText(df.format(placeDistance)+" km");
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem itemIcon = menu.add(mCategoryStringId);
		itemIcon.setIcon(mCategoryMarkerId);
		MenuItemCompat.setShowAsAction(itemIcon, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	protected void onResume() {
		changeSizeTitle();
		super.onResume();
	}
	
	private void changeSizeTitle() {
	    Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/roboto_regular.ttf");
	    int actionBarTitleId = getResources().getIdentifier("action_bar_title", "id", "android");
	    TextView titleTextView = (TextView) findViewById(actionBarTitleId);
	    titleTextView.setTypeface(typeFace);
	}
	
	@Override
	public void onSuccess(PlaceDetails placeDetails) {
		
		mPlace = placeDetails;
		
		mAddressTextView.setText(placeDetails.getAddress());
		mCityTextView.setText(placeDetails.getCityId());
		mPhoneTextView.setText(placeDetails.getPhone());
		//mScheduleTextView.setText();
	
		Log.i("SWIPPER", mPlace.getLocation().toString());
		
		
		mNavImageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				displayNavigation();
			}
		});
		
		
		Log.i("SWIPPER", "Obtained PlaceId: "+placeDetails.getId());
		Log.i("SWIPPER", "Obtained Phone: "+placeDetails.getPhone());
		
		List<Photo> photos = placeDetails.getPhotos();
		Log.i("SWIPPER", "Photo count: "+photos.size());
		
		for(Photo photo: photos) {
			Log.i("SWIPPER", "photo start");
			Log.i("SWIPPER", "height: "+photo.getHeight());
			Log.i("SWIPPER", "width: "+photo.getWidth());
			Log.i("SWIPPER", "reference: "+photo.getPhoto_reference());			
		}
		
		mProgressBarLayout.setVisibility(View.GONE);

	}
	
	@Override
	public void onError(Throwable t) {
		String errorMessage = getResources().getString(R.string.error_place_details_not_available);
		Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
		toast.show();
		finish();
	}

	public void displayNavigation() {
		String url = "http://maps.google.com/maps?"
						+ "daddr="
						+ mPlace.getLocation().latitude
						+ ","
						+ mPlace.getLocation().longitude;
		
		Intent intent = new Intent(android.content.Intent.ACTION_VIEW,  Uri.parse(url));
		startActivity(intent);
	}
	
}
