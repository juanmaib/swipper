package com.globant.labs.swipper2;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
//import android.os.Handler;

public class SplashScreen extends Activity {

	// Splash screen timer
	// private static int SPLASH_TIME_OUT = 2000;
	
	// Position coordinates
	private double[] lastKnownPosition;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
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

			// Remove the timer if you want to do productive stuff
			// Don't forget to start the activity after that! :)
			// new Handler().postDelayed(new Runnable() {
			//
			// @Override
			// public void run() {
			// // After doing all the necessary stuff, launch the main
			// // activity
			// Intent i = new Intent(SplashScreen.this, MainActivity.class);
			// startActivity(i);
			// // Finish this activity
			// finish();
			// }
			// }, SPLASH_TIME_OUT);
			
			// After doing all the necessary stuff, launch the main
			// activity
			Intent i = new Intent(SplashScreen.this, MainActivity.class);
			i.putExtra("lastKnownPosition", lastKnownPosition);
			startActivity(i);
			// Finish this activity
			finish();
		}

		private double[] getPosition() {
			LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			List<String> providers = lm.getProviders(true);

			// Loop over the array backwards, and if you get an accurate
			// location, then break out the loop.

			Location l = null;

			for (int i = providers.size() - 1; i >= 0; i--) {
				l = lm.getLastKnownLocation(providers.get(i));
				if (l != null)
					break;
			}

			double[] position = new double[2];
			if (l != null) {
				position[0] = l.getLatitude();
				position[1] = l.getLongitude();
			}
			
			return position;
		}

	}
}
