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

import com.globant.labs.swipper2.widget.CameraPreview;
import com.globant.labs.swipper2.widget.RadarView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class MonocleActivity extends Activity implements AutoFocusCallback, ConnectionCallbacks,
		OnConnectionFailedListener, LocationListener {

	private static final String TAG = MonocleActivity.class.getSimpleName();
	private static final long AUTO_FOCUS_INTERVAL_MS = 3000L;
	private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 5000L;

	private Camera mCamera;
	private CameraPreview mPreview;
	private AsyncTask<Void, Void, Void> mAutoFocusTask;
	private boolean mStopped;
	private boolean mFocusing;

	private TextView mBrand;
	private RadarView mRadar;

	private GoogleApiClient mGoogleApiClient;
	private LocationRequest mLocationRequest;
	private Location mLocation;

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
		mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
		mRadar.onLocationChanged(mLocation);
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
		mLocation = location;
		mRadar.onLocationChanged(location);
	}
}
