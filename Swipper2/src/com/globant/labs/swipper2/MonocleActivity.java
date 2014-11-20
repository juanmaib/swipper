package com.globant.labs.swipper2;

import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import android.app.Activity;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.globant.labs.swipper2.models.Place;
import com.globant.labs.swipper2.provider.PlacesProvider;
import com.globant.labs.swipper2.provider.PlacesProvider.PlacesCallback;
import com.globant.labs.swipper2.utils.GeoUtils;
import com.globant.labs.swipper2.widget.CameraPreview;
import com.globant.labs.swipper2.widget.RadarView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

public class MonocleActivity extends Activity implements AutoFocusCallback, ConnectionCallbacks,
		OnConnectionFailedListener, LocationListener {

	public static final double MULTIPLIER_PLACE_PROVIDER = 1.1;

	private static final String TAG = MonocleActivity.class.getSimpleName();
	private static final long AUTO_FOCUS_INTERVAL_MS = 3000L;
	private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 5000L;

	public static final double DEFAULT_RADIUS = 1000;
	// private static final int NORTH_WEST_BEARING = 315;
	// private static final int SOUTH_EAST_BEARING = 135;
	private static final double NORTH_EAST_BEARING = 45;
	private static final double SOUTH_WEST_BEARING = 225;

	private double mSpeed;
	private double mRadius;

	private Camera mCamera;
	private CameraPreview mPreview;
	private AsyncTask<Void, Void, Void> mAutoFocusTask;
	private boolean mStopped;
	private boolean mFocusing;

	private TextView mBrand;
	private RadarView mRadar;

	private GoogleApiClient mGoogleApiClient;
	private LocationRequest mLocationRequest;
	private Location mCurrentLocation;
	private Location mPreviousLocation;

	private PlacesProvider mPlacesProvider;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_monocle);

		// Set up the google api client
		mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API)
				.addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();

		mBrand = (TextView) findViewById(R.id.et_brand_monocle);
		mBrand.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/roboto_italic.ttf"));

		// Create an instance of Camera
		mCamera = getCameraInstance();

		// Create our Preview view and set it as the content of our activity.
		mPreview = new CameraPreview(this, mCamera);
		FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
		preview.addView(mPreview);

		checkAndEnableAutoFocus();

		mRadar = (RadarView) findViewById(R.id.radar_monocle);

		// Create a new global location parameters object
		mLocationRequest = LocationRequest.create();

		// Set the update interval
		mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

		// Use high accuracy
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

		mPlacesProvider = new PlacesProvider(this);
		mPlacesProvider.refreshFilteredPlaces();
		mPlacesProvider.setPlacesCallback(new PlacesCallback() {

			@Override
			public void placesUpdated(List<Place> places) {
				mRadar.onPlacesUpdate(places);
			}

			@Override
			public void placesRetry(Throwable t) {
				Log.i("setPlacesCallback", "placesRetry. Throwable: " + t);
			}

			@Override
			public void placesError(Throwable t) {
				Log.i("setPlacesCallback", "placesError. Throwable: " + t);
			}
		});
	}

	@Override
	protected void onStart() {
		mGoogleApiClient.connect();
		super.onStart();
	}

	@Override
	protected void onPause() {
		stopAutoFocus();
		releaseCamera(); // release the camera immediately on pause event
		super.onPause();
	}

	@Override
	protected void onStop() {
		mGoogleApiClient.disconnect();
		super.onStop();
	}

	private void checkAndEnableAutoFocus() {
		// get Camera parameters
		Camera.Parameters params = mCamera.getParameters();

		List<String> focusModes = params.getSupportedFocusModes();
		if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
			// Continuous focus mode is supported
			// set the focus mode
			params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
			// set Camera parameters
			mCamera.setParameters(params);
		} else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
			// Autofocus mode is supported
			// set the focus mode
			params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
			// set Camera parameters
			mCamera.setParameters(params);
			startAutoFocus();
		}
	}

	private void releaseCamera() {
		if (mCamera != null) {
			mPreview.getHolder().removeCallback(mPreview);
			mCamera.release(); // release the camera for other applications
			mCamera = null;
		}
	}

	/** A safe way to get an instance of the Camera object. */
	public static Camera getCameraInstance() {
		Camera c = null;
		try {
			c = Camera.open(); // attempt to get a Camera instance
		} catch (Exception e) {
			// Camera is not available (in use or does not exist)
		}
		return c; // returns null if camera is unavailable
	}

	private final class AutoFocusTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			try {
				Thread.sleep(AUTO_FOCUS_INTERVAL_MS);
			} catch (InterruptedException e) {
				// continue
			}
			startAutoFocus();
			return null;
		}
	}

	private void startAutoFocus() {
		mAutoFocusTask = null;
		if (!mStopped && !mFocusing) {
			try {
				mCamera.autoFocus(this);
				mFocusing = true;
			} catch (RuntimeException re) {
				// Have heard RuntimeException reported in Android 4.0.x+;
				// continue?
				Log.w(TAG, "Unexpected exception while focusing", re);
				// Try again later to keep cycle going
				autoFocusAgainLater();
			}
		}
	}

	private void stopAutoFocus() {
		mStopped = true;
		cancelAutoFocusTask();
		// Doesn't hurt to call this even if not focusing
		try {
			mCamera.cancelAutoFocus();
		} catch (RuntimeException re) {
			// Have heard RuntimeException reported in Android 4.0.x+; continue?
			Log.w(TAG, "Unexpected exception while cancelling focusing", re);
		}
	}

	private void autoFocusAgainLater() {
		if (!mStopped && mAutoFocusTask == null) {
			AutoFocusTask newTask = new AutoFocusTask();
			try {
				newTask.execute();
				mAutoFocusTask = newTask;
			} catch (RejectedExecutionException ree) {
				Log.w(TAG, "Could not request auto focus", ree);
			}
		}
	}

	private synchronized void cancelAutoFocusTask() {
		if (mAutoFocusTask != null) {
			if (mAutoFocusTask.getStatus() != AsyncTask.Status.FINISHED) {
				mAutoFocusTask.cancel(true);
			}
			mAutoFocusTask = null;
		}
	}

	@Override
	public void onAutoFocus(boolean success, Camera camera) {
		mFocusing = false;
		autoFocusAgainLater();
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
		mRadar.onLocationChanged(mCurrentLocation);
		LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
				mLocationRequest, this);
	}

	@Override
	public void onConnectionSuspended(int cause) {
		Log.i("onConnectionSuspended", "cause: " + cause);
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Log.i("onConnectionFailed", "result: " + result);
	}

	@Override
	public void onLocationChanged(Location location) {
		setCurrentLocation(location);
		setSpeed();
		mRadar.onLocationChanged(location);
		mPlacesProvider.setCurrentLocation(new LatLng(location.getLatitude(), location
				.getLongitude()));
		if (mPlacesProvider.updateLocation(getBounds(MULTIPLIER_PLACE_PROVIDER))) {
			mRadar.onPlacesUpdate(mPlacesProvider.getFilteredPlaces());
		}
	}

	private double getSpeed() {
		return mSpeed;
	}

	private void setSpeed() {
		if (getCurrentLocation().hasSpeed()) {
			mSpeed = getCurrentLocation().getSpeed();
		} else {
			if (getPreviousLocation() != null) {
				double distance = getCurrentLocation().distanceTo(mPreviousLocation);
				double timeDiff = getCurrentLocation().getTime() - getPreviousLocation().getTime();
				mSpeed = distance / timeDiff;
			} else {
				mSpeed = 0;
			}
		}
	}

	private void setCurrentLocation(Location location) {
		setPreviousLocation(mCurrentLocation);
		this.mCurrentLocation = location;
	}

	private Location getCurrentLocation() {
		return mCurrentLocation;
	}

	private Location getPreviousLocation() {
		return mPreviousLocation;
	}

	public void setPreviousLocation(Location mPreviousLocation) {
		this.mPreviousLocation = mPreviousLocation;
	}

	public LatLngBounds getBounds(double baseCoeficient) {
		// Set the bounds of the radar dinamically, according to speed
		// The idea is that between each update, the points do not get
		// completely replaced, but just displaced halfway, instead.
		double radius = getRadius(baseCoeficient);
		Log.i("getBounds", "radius: " + radius);
		LatLng currentLatLng = new LatLng(getCurrentLocation().getLatitude(), getCurrentLocation()
				.getLongitude());
		// LatLng northEastLatLng =
		// GeoUtils.getDestinationLocation(currentLatLng, radius,
		// NORTH_EAST_BEARING);
		// LatLng southWestLatLng =
		// GeoUtils.getDestinationLocation(currentLatLng, radius,
		// SOUTH_WEST_BEARING);
		LatLng northEastLatLng = GeoUtils.displaceLatLng(currentLatLng, radius, NORTH_EAST_BEARING);
		LatLng southWestLatLng = GeoUtils.displaceLatLng(currentLatLng, radius, SOUTH_WEST_BEARING);
		Log.i("getBounds", "northEastLatLng: " + northEastLatLng);
		Log.i("getBounds", "southWestLatLng: " + southWestLatLng);
		double distance = GeoUtils.getDistance(northEastLatLng, southWestLatLng) * 1000;
		Log.i("getBounds", "distance: " + distance);
		return LatLngBounds.builder().include(northEastLatLng).include(southWestLatLng).build();
	}

	public double getRadius(double baseCoeficient) {
		double speedMultiplier = baseCoeficient + (getSpeed() / DEFAULT_RADIUS);
		return DEFAULT_RADIUS * speedMultiplier;
	}
}
