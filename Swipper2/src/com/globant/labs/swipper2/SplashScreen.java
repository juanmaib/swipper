package com.globant.labs.swipper2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
//import android.os.Handler;

public class SplashScreen extends Activity implements GoogleApiClient.ConnectionCallbacks,
		GoogleApiClient.OnConnectionFailedListener {

	public static final String LAST_KNOWN_LOCATION_EXTRA = "lastKnownLocation";
	protected static final int STEPS = 2;
	
	protected GoogleApiClient mGoogleApiClient;
	protected Location mLastKnownLocation;
	protected int mStepCount;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		
		mStepCount = 0;
		
		if(!isConnected()) {
			displayUnrecoverableErrorMessage(R.string.network_error);
		}else{
			final Handler handler = new Handler();
		    handler.postDelayed(new Runnable() {
		      @Override
		      public void run() {
		        stepAndTransition();
		      }
		    }, 1500);			
			
			// Set up the google api client (used later to get the last location)
			mGoogleApiClient = new GoogleApiClient.Builder(this)
					.addApi(LocationServices.API)
					.addConnectionCallbacks(this)
					.addOnConnectionFailedListener(this)
					.build();
			
			mGoogleApiClient.connect();
		}
		
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		Log.i("SWIPPER", "onConnectionFailed");
	}

	@Override
	public void onConnected(Bundle arg0) {		
		mLastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
		stepAndTransition();
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		Log.i("SWIPPER", "onConnectionSuspended");
	}
	
	protected void stepAndTransition() {
		mStepCount++;
		if(mStepCount == STEPS) {
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
		dialogBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {		
			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});
		dialogBuilder.show();
	}
}
