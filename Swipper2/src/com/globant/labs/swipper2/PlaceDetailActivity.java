package com.globant.labs.swipper2;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.globant.labs.swipper2.drawer.CategoryMapper;
import com.globant.labs.swipper2.models.GoogleReview;
import com.globant.labs.swipper2.models.Photo;
import com.globant.labs.swipper2.models.PlaceDetails;
import com.globant.labs.swipper2.repositories.PlaceDetailsRepository;
import com.globant.labs.swipper2.widget.ExpandablePanel;
import com.globant.labs.swipper2.widget.ReviewsExpandablePanel;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.strongloop.android.loopback.RestAdapter;
import com.strongloop.android.loopback.callbacks.ObjectCallback;

public class PlaceDetailActivity extends ActionBarActivity implements ObjectCallback<PlaceDetails> {

	public static final String PHOTOS_API_KEY = "AIzaSyDT_7HU59iNKx1zEQDj2wbCGP65BkoEXqs";
	// public static final String PHOTOS_API_KEY =
	// "AIzaSyAyeLAbHzmMtrjOO_yVwGYs4Xg7iYbpVdM";

	public static final String PLACE_ID_EXTRA = "place-id-extra";
	public static final String PLACE_NAME_EXTRA = "place-name-extra";
	public static final String PLACE_CATEGORY_EXTRA = "place-category-extra";
	public static final String PLACE_DISTANCE_EXTRA = "place-distance-extra";

	private int mRetriesLeft = 2;

	protected int mCategoryStringId;
	protected int mCategoryMarkerId;

	protected RelativeLayout mProgressBarLayout;
	protected ExpandablePanel mDescriptionLayout;
	protected ReviewsExpandablePanel mReviewsLayout;
	protected LinearLayout mScheduleLayout;
	protected LinearLayout mPhotosSection;
	protected LinearLayout mPhotosLayout;
	protected LinearLayout mReviewsList;
	protected LinearLayout mNoMoreInfoLayout;
	protected TextView mAddressTextView;
	protected TextView mCityTextView;
	protected TextView mDistanceTextView;
	protected TextView mPhoneTextView;
	protected TextView mScheduleTextView;
	protected TextView mDescriptionText;
	protected ImageButton mNavigateButton;
	protected ImageButton mShareButton;
	protected ImageButton mReportButton;

	protected PlaceDetails mPlace;

	protected String[] mPhotosURLs;

	protected int mDeviceWidth;
	private String mPlaceId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.place_details);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		Bundle extras = getIntent().getExtras();
		mPlaceId = extras.getString(PLACE_ID_EXTRA);
		String placeName = extras.getString(PLACE_NAME_EXTRA);
		String placeCategory = extras.getString(PLACE_CATEGORY_EXTRA);
		double placeDistance = extras.getDouble(PLACE_DISTANCE_EXTRA);

		Log.i("SWIPPER", "PlaceId: " + mPlaceId);

		mCategoryMarkerId = CategoryMapper.getCategoryMarker(placeCategory);
		mCategoryStringId = CategoryMapper.getCategoryText(placeCategory);

		getSupportActionBar().setTitle(placeName);

		mAddressTextView = (TextView) findViewById(R.id.addressText);
		mCityTextView = (TextView) findViewById(R.id.cityText);
		mDistanceTextView = (TextView) findViewById(R.id.distanceText);
		mPhoneTextView = (TextView) findViewById(R.id.phoneText);
		mScheduleTextView = (TextView) findViewById(R.id.scheduleText);
		mDescriptionText = (TextView) findViewById(R.id.descriptionText);
		mProgressBarLayout = (RelativeLayout) findViewById(R.id.progressBarLayout);
		mDescriptionLayout = (ExpandablePanel) findViewById(R.id.descriptionLayout);
		mReviewsLayout = (ReviewsExpandablePanel) findViewById(R.id.reviewsLayout);
		mScheduleLayout = (LinearLayout) findViewById(R.id.scheduleLayout);
		mPhotosSection = (LinearLayout) findViewById(R.id.photosSection);
		mPhotosLayout = (LinearLayout) findViewById(R.id.photosLayout);
		mReviewsList = (LinearLayout) findViewById(R.id.reviewsList);
		mNoMoreInfoLayout = (LinearLayout) findViewById(R.id.noMoreInfoLayout);

		mNavigateButton = (ImageButton) findViewById(R.id.navigateButton);
		mShareButton = (ImageButton) findViewById(R.id.shareButton);
		mReportButton = (ImageButton) findViewById(R.id.reportButton);

		mNavigateButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				navigateAction();
			}
		});

		mShareButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				shareAction();
			}
		});

		mReportButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				reportAction();
			}
		});

		requestDetails();

		DecimalFormat df = new DecimalFormat("0.00");
		mDistanceTextView.setText(df.format(placeDistance) + " km");

	}

	private void requestDetails() {
		RestAdapter restAdapter = ((SwipperApp) getApplication()).getRestAdapter();
		PlaceDetailsRepository placeDetailsRepo = restAdapter
				.createRepository(PlaceDetailsRepository.class);
		placeDetailsRepo.details(mPlaceId, this);
	}

	@Override
	protected void onStart() {
		mDeviceWidth = getDeviceWidth();
		super.onStart();
	}

	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	private int getDeviceWidth() {
		int width = 0;
		WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		if (Build.VERSION.SDK_INT > 12) {
			Point size = new Point();
			display.getSize(size);
			width = size.x;
		} else {
			width = display.getWidth(); // yeah, I know...
		}
		return width;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem itemIcon = menu.add(mCategoryStringId);
		MenuItemCompat.setShowAsAction(itemIcon, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
		itemIcon.setIcon(mCategoryMarkerId);
		itemIcon.setEnabled(false);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onResume() {
		super.onResume();
		changeSizeTitle();
	}

	private void changeSizeTitle() {
		Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/roboto_light.ttf");
		int actionBarTitleId;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			actionBarTitleId = getResources().getIdentifier("action_bar_title", "id", "android");
		} else {
			actionBarTitleId = R.id.action_bar_title;
		}

		TextView titleTextView = (TextView) findViewById(actionBarTitleId);
		titleTextView.setTypeface(typeFace);
	}

	@SuppressLint("InflateParams")
	@Override
	public void onSuccess(PlaceDetails placeDetails) {

		mPlace = placeDetails;

		mAddressTextView.setText(placeDetails.getAddress());
		mCityTextView.setText(placeDetails.getCity() + ", " + placeDetails.getState() + ", "
				+ placeDetails.getCountry());

		mPhoneTextView.setText(placeDetails.getPhone());

		if (placeDetails.getSchedules() != null) {
			Calendar calendar = Calendar.getInstance();
			int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
			mScheduleTextView.setText(placeDetails.getSchedules().get(dayOfWeek));
		} else {
			mScheduleLayout.setVisibility(View.GONE);
		}

		// mScheduleTextView.setText("10:30 am - 20:30 pm");

		boolean hasDescription = false;
		boolean hasReviews = false;
		boolean hasPhotos = false;

		// mPlace.setDescription("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed arcu.");
		// mPlace.setDescription("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nulla nec ipsum eget velit tempus luctus vel eget nisi. Fusce congue condimentum sem, luctus iaculis enim feugiat id. Praesent volutpat, libero.");
		// mPlace.setDescription("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Phasellus lobortis urna et facilisis ullamcorper. Curabitur eleifend accumsan molestie. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Donec imperdiet eu libero ac pharetra. Aenean a eros vel nunc maximus facilisis. Ut porttitor sollicitudin ex, vel molestie sem euismod pharetra. Mauris sodales arcu vel odio eleifend, nec.");

		if (mPlace.getDescription() != null && mPlace.getDescription() != "") {
			mDescriptionText.setText(mPlace.getDescription());
			hasDescription = true;
		} else {
			mDescriptionLayout.setVisibility(View.GONE);
		}

		if (placeDetails.getReviews() != null && placeDetails.getReviews().size() > 0) {

			LayoutInflater inflater = LayoutInflater.from(this);

			for (GoogleReview review : placeDetails.getReviews()) {
				if (review.getText() != null && !review.getText().trim().isEmpty()) {
					View v = inflater.inflate(R.layout.review_item, null);
					TextView vText = (TextView) v.findViewById(R.id.reviewText);
					vText.setText("\"" + review.getText() + "\"\u3000 ");
					RatingBar vBar = (RatingBar) v.findViewById(R.id.reviewRating);
					vBar.setProgress(review.getRating());
					mReviewsList.addView(v);
					hasReviews = true;
				}
			}
		}

		if (!hasReviews) {
			mReviewsLayout.setVisibility(View.GONE);
		}

		List<Photo> photos = placeDetails.getPhotos();
		if (photos != null && photos.size() > 0) {

			Resources r = getResources();
			int rightMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4,
					r.getDisplayMetrics());
			int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100,
					r.getDisplayMetrics());

			LinearLayout.LayoutParams imageLayoutParams = new LinearLayout.LayoutParams(size, size);

			imageLayoutParams.setMargins(0, 0, rightMargin, 0);

			mPhotosURLs = new String[photos.size()];
			for (int i = 0; i < photos.size(); i++) {
				final Photo photo = photos.get(i);
				final ProgressBar progressBar = new ProgressBar(this, null,
						android.R.attr.progressBarStyleLarge);
				progressBar.setLayoutParams(imageLayoutParams);
				mPhotosLayout.addView(progressBar);

				final ImageView imageView = new ImageView(this);
				imageView.setLayoutParams(imageLayoutParams);
				imageView.setVisibility(View.GONE);
				mPhotosLayout.addView(imageView);

				final int index = i;
				mPhotosURLs[i] = getBiggerPhotoURL(photo.getPhoto_reference());
				String photoURL = getPhotoURL(photo.getPhoto_reference());
				Picasso.with(this).load(photoURL).resize(size, size).centerCrop()
						.into(imageView, new Callback() {

							@Override
							public void onError() {
								progressBar.setVisibility(View.GONE);
							}

							@Override
							public void onSuccess() {
								progressBar.setVisibility(View.GONE);
								imageView.setVisibility(View.VISIBLE);
								imageView.setOnClickListener(new OnClickListener() {

									@Override
									public void onClick(View v) {
										Intent gallery = new Intent(PlaceDetailActivity.this,
												GalleryActivity.class);
										// let's collect some info before we go
										// to the gallery activity
										Bundle extras = getIntent().getExtras();
										gallery.putExtra(GalleryActivity.PLACE_NAME_EXTRA,
												extras.getString(PLACE_NAME_EXTRA));
										gallery.putExtra(GalleryActivity.PHOTO_INDEX_EXTRA, index);
										gallery.putExtra(GalleryActivity.PHOTOS_URLS_EXTRA,
												mPhotosURLs);
										// here we go!
										startActivity(gallery);
									}
								});
							}

						});
			}

			hasPhotos = true;

		} else {
			mPhotosSection.setVisibility(View.GONE);
		}

		if (!hasPhotos) {
			if (hasDescription && !hasReviews) {
				mDescriptionLayout.instantExpand();
			} else if (!hasDescription && hasReviews) {
				mReviewsLayout.instantExpand();
			}
		}

		if (!hasDescription && !hasReviews && !hasPhotos) {
			mNoMoreInfoLayout.setVisibility(View.VISIBLE);
		}

		mProgressBarLayout.setVisibility(View.GONE);

	}

	protected String getPhotoURL(String photoReference) {
		return "https://maps.googleapis.com/maps/api/place/photo" + "?maxwidth=300"
				+ "&photoreference=" + photoReference + "&key=" + PHOTOS_API_KEY;
	}

	// ya know what they say...
	protected String getBiggerPhotoURL(String photoReference) {
		return "https://maps.googleapis.com/maps/api/place/photo" + "?maxwidth=" + mDeviceWidth
				+ "&photoreference=" + photoReference + "&key=" + PHOTOS_API_KEY;
	}

	@Override
	public void onError(Throwable t) {
		Log.i("onError", "cause: " + t.getCause() + ", message: " + t.getMessage());
		if (mRetriesLeft > 0) {
			requestDetails();
			mRetriesLeft--;
			String errorMessage = getResources().getString(R.string.error_place_details_retry);
			Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
		} else {
			String errorMessage = getResources().getString(
					R.string.error_place_details_not_available);
			Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
			toast.show();
			finish();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}

	public void navigateAction() {
		String url = "http://maps.google.com/maps?" + "daddr=" + mPlace.getLocation().latitude
				+ "," + mPlace.getLocation().longitude;

		Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url));
		startActivity(intent);
	}

	protected void shareAction() {
		StringBuilder stringBuilder = new StringBuilder().append(mPlace.getCategory()).append("\n")
				.append(mPlace.getName()).append("\n").append(mPlace.getAddress()).append("\n")
				.append(mPlace.getCity()).append(", ").append(mPlace.getState()).append(", ")
				.append(mPlace.getCountry()).append("\n").append(mPlace.getPhone());

		if (mPlace.getUrl() != null) {
			stringBuilder.append("\n").append(mPlace.getUrl());
		}

		Intent sendIntent = new Intent(Intent.ACTION_SEND);
		sendIntent.setType("text/plain");
		sendIntent.putExtra(Intent.EXTRA_TEXT, stringBuilder.toString());

		final PackageManager packageManager = getPackageManager();

		List<Intent> targetedShareIntents = new ArrayList<Intent>();
		List<ResolveInfo> resInfo = packageManager.queryIntentActivities(sendIntent, 0);

		Collections.sort(resInfo, new Comparator<ResolveInfo>() {
			@Override
			public int compare(ResolveInfo first, ResolveInfo second) {
				String firstName = first.loadLabel(packageManager).toString();
				String secondName = second.loadLabel(packageManager).toString();
				return firstName.compareToIgnoreCase(secondName);
			}
		});

		for (ResolveInfo resolveInfo : resInfo) {
			String packageName = resolveInfo.activityInfo.packageName;
			String className = resolveInfo.activityInfo.name;

			if (mPlace.getUrl() != null || !packageName.equals("com.facebook.katana")) {
				Intent targetedShareIntent = new Intent(Intent.ACTION_SEND);
				targetedShareIntent.setType("text/plain");
				targetedShareIntent.putExtra(Intent.EXTRA_TEXT, stringBuilder.toString());
				targetedShareIntent.setPackage(packageName);
				targetedShareIntent.setClassName(packageName, className);

				targetedShareIntents.add(targetedShareIntent);
			}
		}

		Intent chooserIntent = Intent.createChooser(targetedShareIntents
				.remove(targetedShareIntents.size() - 1),
				getResources().getString(R.string.action_share));

		chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
				targetedShareIntents.toArray(new Parcelable[] {}));

		startActivity(chooserIntent);
	}

	protected void reportAction() {
		StringBuilder stringBuilder = new StringBuilder().append(mPlace.getName()).append("\n")
				.append(mPlace.getAddress()).append("\n").append(mPlace.getCity()).append(", ")
				.append(mPlace.getState()).append(", ").append(mPlace.getCountry()).append("\n")
				.append(mPlace.getPhone());

		if (mPlace.getUrl() != null) {
			stringBuilder.append("\n").append(mPlace.getUrl());
		}

		stringBuilder.append("\n\n").append("What's the problem?\n...");

		String address = getResources().getString(R.string.contact_email);

		Intent emailIntent = new Intent(Intent.ACTION_SENDTO,
				Uri.fromParts("mailto", address, null));
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, "SWIPPER REPORT");
		emailIntent.putExtra(Intent.EXTRA_TEXT, stringBuilder.toString());
		startActivity(Intent.createChooser(emailIntent, getResources()
				.getText(R.string.send_report)));

	}
}
