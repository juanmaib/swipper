package com.globant.labs.swipper2;

import java.text.DecimalFormat;
import java.util.List;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.globant.labs.swipper2.drawer.CategoryMapper;
import com.globant.labs.swipper2.models.Photo;
import com.globant.labs.swipper2.models.PlaceDetails;
import com.globant.labs.swipper2.repositories.PlaceDetailsRepository;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.strongloop.android.loopback.RestAdapter;
import com.strongloop.android.loopback.callbacks.ObjectCallback;

public class PlaceDetailActivity extends ActionBarActivity implements ObjectCallback<PlaceDetails>{

	public static final String PHOTOS_API_KEY = "AIzaSyAyeLAbHzmMtrjOO_yVwGYs4Xg7iYbpVdM";
	
	public static final String PLACE_ID_EXTRA = "place-id-extra";
	public static final String PLACE_NAME_EXTRA = "place-name-extra";
	public static final String PLACE_CATEGORY_EXTRA = "place-category-extra";
	public static final String PLACE_DISTANCE_EXTRA = "place-distance-extra";
	
	protected int mCategoryStringId;
	protected int mCategoryMarkerId;
	
	protected RelativeLayout mProgressBarLayout;
	protected LinearLayout mDescriptionLayout;
	protected LinearLayout mPhotosSection;
	protected LinearLayout mPhotosLayout;
	protected ListView mReviewsList;
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
		
		mCategoryMarkerId = CategoryMapper.getCategoryMarker(placeCategory);
		mCategoryStringId = CategoryMapper.getCategoryText(placeCategory);
		
		setTitle(placeName);
		getSupportActionBar().setIcon(mCategoryMarkerId);
		
		mAddressTextView = (TextView) findViewById(R.id.addressText);
		mCityTextView = (TextView) findViewById(R.id.cityText);
		mDistanceTextView = (TextView) findViewById(R.id.distanceText);
		mPhoneTextView = (TextView) findViewById(R.id.phoneText);
		mScheduleTextView = (TextView) findViewById(R.id.scheduleText);
		mNavImageView = (ImageView) findViewById(R.id.navImage);
		
		mProgressBarLayout = (RelativeLayout) findViewById(R.id.progressBarLayout);
		mDescriptionLayout = (LinearLayout) findViewById(R.id.descriptionLayout);
		mPhotosSection = (LinearLayout) findViewById(R.id.photosSection);
		mPhotosLayout = (LinearLayout) findViewById(R.id.photosLayout);
		
		mReviewsList = (ListView) findViewById(R.id.reviewsList);
		
		RestAdapter restAdapter = ((SwipperApp) getApplication()).getRestAdapter();
		PlaceDetailsRepository placeDetailsRepo = restAdapter.createRepository(PlaceDetailsRepository.class);
		placeDetailsRepo.details(placeId, this);

		DecimalFormat df = new DecimalFormat("0.00");
		mDistanceTextView.setText(df.format(placeDistance)+" km");
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.details, menu);
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
		
		Resources r = getResources();
		int rightMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, r.getDisplayMetrics());
		int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, r.getDisplayMetrics());
		
		LinearLayout.LayoutParams imageLayoutParams = new LinearLayout.LayoutParams(
				size, 
				size);
		
		imageLayoutParams.setMargins(0, 0, rightMargin, 0);
		
		for(Photo photo: photos) {
			final ProgressBar progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleLarge);
			progressBar.setLayoutParams(imageLayoutParams);
			mPhotosLayout.addView(progressBar);
			
			final ImageView imageView = new ImageView(this);
			imageView.setLayoutParams(imageLayoutParams);
			imageView.setVisibility(View.GONE);
			mPhotosLayout.addView(imageView);
			
			Picasso.with(this)
			  .load(getPhotoURL(photo.getPhoto_reference()))
			  .resize(size, size)
			  .centerCrop()
			  .into(imageView, new Callback() {

				@Override
				public void onError() {
					progressBar.setVisibility(View.GONE);
				}

				@Override
				public void onSuccess() {
					progressBar.setVisibility(View.GONE);
					imageView.setVisibility(View.VISIBLE);
				}
				  
			  });			
		}
		
		ReviewsAdapter reviewsAdapter = new ReviewsAdapter(this);
		reviewsAdapter.setReviews(placeDetails.getReviews());
		mReviewsList.setAdapter(reviewsAdapter);
		
		mProgressBarLayout.setVisibility(View.GONE);

	}
	
	protected String getPhotoURL(String photoReference) {
		return "https://maps.googleapis.com/maps/api/place/photo" +
				"?maxwidth=300" +
				"&photoreference=" +
				photoReference +
				"&key=" +
				PHOTOS_API_KEY;
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
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		
		if (id == R.id.action_share) {
			
			String placeString = mPlace.getName()+"\n"+mPlace.getAddress()+"\n"+mPlace.getPhone();
			
			Intent sendIntent = new Intent();
			sendIntent.setAction(Intent.ACTION_SEND);
			sendIntent.putExtra(Intent.EXTRA_TEXT, placeString);
			sendIntent.setType("text/plain");
			startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.action_share)));
			
			return true;
		}else if(id == R.id.action_report) {
			
			String placeString = mPlace.getName()+"\n"+mPlace.getAddress()+"\n"+mPlace.getPhone();
			
			Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
		            "mailto","bruno.demartino@globant.com", null));
			emailIntent.putExtra(Intent.EXTRA_SUBJECT, "SWIPPER REPORT");
			emailIntent.putExtra(Intent.EXTRA_TEXT, placeString+"\n\nWhat's the problem?\n...");
			startActivity(Intent.createChooser(emailIntent, getResources().getText(R.string.send_report)));
			
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
}
