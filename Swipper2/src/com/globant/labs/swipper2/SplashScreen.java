package com.globant.labs.swipper2;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;

public class SplashScreen extends Activity implements GoogleApiClient.ConnectionCallbacks,
		GoogleApiClient.OnConnectionFailedListener, LocationListener {

	private GoogleApiClient mGoogleApiClient;
	private double[] lastKnownPosition;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		// Set up the google api client (used later to get the last location)
		mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API)
				.addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
		// Showing splashscreen while fetching last known position.
		new PrefetchData().execute();
	}

	private class PrefetchData extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			// Do here everything we need to, or let the timer end :)
			lastKnownPosition = getPosition();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			// After doing all the necessary stuff, launch the main
			// activity
			Intent i = new Intent(SplashScreen.this, MainActivity.class);
			i.putExtra("lastKnownPosition", lastKnownPosition);
			startActivity(i);
			// Finish this activity
			finish();
		}

		private double[] getPosition() {
			Location l = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
			double[] position = new double[2];
			if (l != null) {
				position[0] = l.getLatitude();
				position[1] = l.getLongitude();
			}
			return position;
		}

	}

	@Override
	public void onLocationChanged(Location arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		// TODO Auto-generated method stub
		
	}
}
