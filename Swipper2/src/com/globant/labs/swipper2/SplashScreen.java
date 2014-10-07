package com.globant.labs.swipper2;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

//import android.os.Handler;

public class SplashScreen extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks,
		GoogleApiClient.OnConnectionFailedListener {

	public static final String LAST_KNOWN_LOCATION_EXTRA = "lastKnownLocation";
	private static final String STATE_RESOLVING_ERROR = "resolving_error";
	protected static final int STEPS = 2;

	protected GoogleApiClient mGoogleApiClient;
	protected Location mLastKnownLocation;
	protected int mStepCount;

	// Request code to use when launching the resolution activity
	private static final int REQUEST_RESOLVE_ERROR = 1001;
	// Unique tag for the error dialog fragment
	private static final String DIALOG_ERROR = "dialog_error";
	// Bool to track whether the app is already resolving an error
	private boolean mResolvingError = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);

		mResolvingError = savedInstanceState != null
				&& savedInstanceState.getBoolean(STATE_RESOLVING_ERROR, false);

		mStepCount = 0;

		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				stepAndTransition();
			}
		}, 1500);

		// Set up the google api client (used later to get the last location)
		mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API)
				.addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();

	}

	@Override
	protected void onStart() {
		super.onStart();
		if (!isConnected()) { // we do not have inet...
			displayUnrecoverableErrorMessage(R.string.network_error);
		} else if (!mResolvingError) { // we do have inet. let's connect to the api (unless we're already doing it)
			mGoogleApiClient.connect();
		}
	}

	@Override
	protected void onStop() {
		mGoogleApiClient.disconnect();
		super.onStop();
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// This callback is important for handling errors that
		// may occur while attempting to connect with Google.
		Log.i("SWIPPER", "onConnectionFailed");
		if (mResolvingError) {
			// Already attempting to resolve an error.
			return;
		} else if (result.hasResolution()) {
			try {
				mResolvingError = true;
				result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
			} catch (SendIntentException e) {
				// There was an error with the resolution intent. Try again.
				mGoogleApiClient.connect();
			}
		} else {
			// Show dialog using GooglePlayServicesUtil.getErrorDialog()
			showErrorDialog(result.getErrorCode());
			mResolvingError = true;
		}
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		// Connected to Google Play services!
		// The good stuff goes here.
		mLastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
		stepAndTransition();
	}

	@Override
	public void onConnectionSuspended(int cause) {
		// The connection has been interrupted.
		// Disable any UI components that depend on Google APIs
		// until onConnected() is called.
		Log.i("SWIPPER", "onConnectionSuspended");
	}

	protected void stepAndTransition() {
		mStepCount++;
		if (mStepCount == STEPS) {
			Intent i = new Intent(SplashScreen.this, MainActivity.class);
			i.putExtra(LAST_KNOWN_LOCATION_EXTRA, mLastKnownLocation);
			startActivity(i);
			finish();
		}
	}

	protected boolean isConnected() {
		ConnectivityManager conectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = conectivityManager.getActiveNetworkInfo();
		return (networkInfo != null && networkInfo.isConnected());
	}

	protected void displayUnrecoverableErrorMessage(int errorMessageId) {
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
		dialogBuilder.setMessage(errorMessageId);
		dialogBuilder.setCancelable(false);
		dialogBuilder.setPositiveButton(android.R.string.yes,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				});
		dialogBuilder.show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_RESOLVE_ERROR) {
			mResolvingError = false;
			if (resultCode == RESULT_OK) {
				// Make sure the app is not already connected or attempting to
				// connect
				if (!mGoogleApiClient.isConnecting() && !mGoogleApiClient.isConnected()) {
					mGoogleApiClient.connect();
				}
			}
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(STATE_RESOLVING_ERROR, mResolvingError);
	}

	// The rest of this code is all about building the error dialog

	/* Creates a dialog for an error message */
	private void showErrorDialog(int errorCode) {
		// Create a fragment for the error dialog
		ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
		// Pass the error that should be displayed
		Bundle args = new Bundle();
		args.putInt(DIALOG_ERROR, errorCode);
		dialogFragment.setArguments(args);
		dialogFragment.show(getSupportFragmentManager(), "errordialog");
	}

	/* Called from ErrorDialogFragment when the dialog is dismissed. */
	public void onDialogDismissed() {
		mResolvingError = false;
	}

	/* A fragment to display an error dialog */
	public static class ErrorDialogFragment extends DialogFragment {
		public ErrorDialogFragment() {
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Get the error code and retrieve the appropriate dialog
			int errorCode = this.getArguments().getInt(DIALOG_ERROR);
			return GooglePlayServicesUtil.getErrorDialog(errorCode, this.getActivity(),
					REQUEST_RESOLVE_ERROR);
		}

		@Override
		public void onDismiss(DialogInterface dialog) {
			((SplashScreen) getActivity()).onDialogDismissed();
		}
	}
}
