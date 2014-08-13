package com.globant.labs.swipper.net;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import android.os.AsyncTask;
import android.util.Log;

public class RestClientResistenciarte {

	public static final String GET_RANDOM_SCULTURE = "random_sculture";

	private static HTTPScraper scrapper = HTTPScraper.getScraper();

	private IRequester requester;

	public RestClientResistenciarte(IRequester requester) {
		this.requester = requester;
	}

	public void makeJsonRestRequest() {
		new ReadEsculturasJSONResponseTask()
				.execute(requester.getRequestURI());
	}

	private class ReadEsculturasJSONResponseTask extends
			AsyncTask<String, Void, String> {

		protected String doInBackground(String... params) {
			return scrapper.fecthHtmlGetString(params[0]);
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			requester.onResponse("");
		}

		protected void onPostExecute(String result) {
			requester.onResponse(result);
		}
	}
	
	public void makeRestRequest() {
		new ReadEsculturasResponseTask()
				.execute(requester.getRequestURI());
	}

	private class ReadEsculturasResponseTask extends
			AsyncTask<String, Void, InputStream> {

		protected InputStream doInBackground(String... params) {
			InputStream inputStream = null;
			try {
				inputStream = scrapper.fecthHtmlGet(params[0]);
			} catch (Exception e) {
				Log.d("readJSONFeed", e.getLocalizedMessage());
			}
			return inputStream;
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			requester.onResponse("");
		}
		
		protected void onPostExecute(InputStream result) {
			requester.onResponse(result);
		}
	}
}
