package com.globant.labs.swipper2;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
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
		OnConnectionFailedListener, LocationListener, SensorEventListener {

	public static final double BASE_COEFICIENT = 1;
	private static final long AUTO_FOCUS_INTERVAL_MS = 3000L;
	private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 5000L;
	private static final int SENSOR_DELAY_RADAR = 200000;
	private static final int RADAR_LAYOUT_DELAY_MILLIS = 100;

	public static final double DEFAULT_RADIUS = 1000;
	private static final double NORTH_EAST_BEARING = 45;
	private static final double SOUTH_WEST_BEARING = 225;

	private double mSpeed;

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

	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private Sensor mMagnetometer;
	private float[] mGravity;
	private float[] mGeomagnetic;
	private double mAzimut;

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
			}

			@Override
			public void placesError(Throwable t) {
			}
		});

		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
	}

	@Override
	protected void onStart() {
		mGoogleApiClient.connect();
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mSensorManager.registerListener(this, mAccelerometer, SENSOR_DELAY_RADAR);
		mSensorManager.registerListener(this, mMagnetometer, SENSOR_DELAY_RADAR);
		ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

		scheduler.scheduleAtFixedRate(new Runnable() {
			public void run() {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						mRadar.requestLayout();
					}
				});
			}
		}, 0, RADAR_LAYOUT_DELAY_MILLIS, TimeUnit.MILLISECONDS);
	}

	@Override
	protected void onPause() {
		stopAutoFocus();
		releaseCamera(); // release the camera immediately on pause event
		mSensorManager.unregisterListener(this);
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
		}
	}

	private void autoFocusAgainLater() {
		if (!mStopped && mAutoFocusTask == null) {
			AutoFocusTask newTask = new AutoFocusTask();
			try {
				newTask.execute();
				mAutoFocusTask = newTask;
			} catch (RejectedExecutionException ree) {
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
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
	}

	@Override
	public void onLocationChanged(Location location) {
		setCurrentLocation(location);
		setSpeed();
		mRadar.onLocationChanged(location);
		mPlacesProvider.setCurrentLocation(new LatLng(location.getLatitude(), location
				.getLongitude()));
		if (mPlacesProvider.updateLocation(getBounds(BASE_COEFICIENT))) {
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
		LatLng currentLatLng = new LatLng(getCurrentLocation().getLatitude(), getCurrentLocation()
				.getLongitude());
		LatLng northEastLatLng = GeoUtils.displaceLatLng(currentLatLng, radius, NORTH_EAST_BEARING);
		LatLng southWestLatLng = GeoUtils.displaceLatLng(currentLatLng, radius, SOUTH_WEST_BEARING);
		return LatLngBounds.builder().include(northEastLatLng).include(southWestLatLng).build();
	}

	public double getRadius(double baseCoeficient) {
		double speedMultiplier = baseCoeficient + (getSpeed() / DEFAULT_RADIUS);
		return DEFAULT_RADIUS * speedMultiplier;
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
			mGravity = event.values;
		if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
			mGeomagnetic = event.values;
		if (mGravity != null && mGeomagnetic != null) {
			float R[] = new float[9];
			float I[] = new float[9];
			boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
			if (success) {
				float orientation[] = new float[3];
				SensorManager.getOrientation(R, orientation);
				// orientation contains: azimut, pitch and roll
				mAzimut = orientation[0];
				mRadar.onAzimutChanged(mAzimut);
				// float pitch = orientation[1];
				// float roll = orientation[2];
			}
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}
}
