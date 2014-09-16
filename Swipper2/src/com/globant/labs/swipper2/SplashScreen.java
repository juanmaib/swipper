package com.globant.labs.swipper2;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;

public class SplashScreen extends Activity {

	// Splash screen timer
	private static int SPLASH_TIME_OUT = 2000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		// Showing splashscreen while downloading the necessary data before
		// launching the app. Will use AsyncTask to do it.
		new PrefetchData().execute();
	}

	private class PrefetchData extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			// Do here everything we need to, or let the timer end :)
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			// Remove the timer if you want to do productive stuff
			// Don't forget to start the activity after that! :)
			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					// After doing all the necessary stuff, launch the main
					// activity
					Intent i = new Intent(SplashScreen.this, MainActivity.class);
					startActivity(i);
					// Finish this activity
					finish();
				}
			}, SPLASH_TIME_OUT);
		}
	}
}
