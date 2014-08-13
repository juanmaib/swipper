package com.globant.labs.swipper;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

import com.globant.labs.swipper.comps.ConfirmationDialog;
import com.globant.labs.swipper.geo.GeoLocationsUtils;
import com.globant.labs.swipper.utils.Utils;
import com.google.android.gms.maps.model.LatLng;
import com.globant.labs.swipper.R;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;

public abstract class LocatorActivity extends ActionBarCustomActivity implements LocationListener {

	protected LocationManager locationManager;
	protected String provider;

	protected Dialog currentDialog;
	
	protected TextView mAddress;
	protected TextView mAddressGPS;
	
	protected LatLng currentLocation;
	
	protected boolean activeActivity = true;
	
	protected final int RETRY_COUNT_MAX = 3;
	
	protected int retries = 0;
	
	public boolean isActiveActivity() {
		return activeActivity;
	}

	public void setActiveActivity(boolean activeActivity) {
		this.activeActivity = activeActivity;
	}

	protected void init(){
		LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
		boolean enabledGPS = service
				.isProviderEnabled(LocationManager.GPS_PROVIDER);
		boolean enabledWiFi = service
				.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

		// Check if enabled and if not send user to the GSP settings
		// Better solution would be to display a dialog and suggesting to
		// go to the settings
		if (!enabledGPS && !enabledWiFi) {
			showDialog(Utils.GPS_NOT_TURNED_ON);
		}

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		// Define the criteria how to select the locatioin provider -> use
		// default
		provider = enabledGPS?LocationManager.GPS_PROVIDER:LocationManager.NETWORK_PROVIDER;
		Location location = locationManager.getLastKnownLocation(provider);

		if (location == null){
			provider = !enabledGPS?LocationManager.GPS_PROVIDER:LocationManager.NETWORK_PROVIDER;
			location = locationManager.getLastKnownLocation(provider);
		}
		// Initialize the location fields
		if (location != null) {
			double lat = location.getLatitude();
			double lng = location.getLongitude();
			this.currentLocation = new LatLng(lat, lng);
			onLocationChanged(location);
		} 
	}
	
	/* Request updates at startup */
	@Override
	public void onResume() {
		super.onResume();
		setActiveActivity(true);
		
		tryToGetBestLocationProvider();
	}
	
	private void tryToGetBestLocationProvider() {
		LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
		boolean enabledGPS = service
				.isProviderEnabled(LocationManager.GPS_PROVIDER);
		boolean enabledWiFi = service
				.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		// Check if enabled and if not send user to the GSP settings
		// Better solution would be to display a dialog and suggesting to
		// go to the settings
		if (!enabledGPS && !enabledWiFi) {
			if (currentDialog == null || !currentDialog.isShowing()){
				showDialog(Utils.GPS_NOT_TURNED_FF);
			}
		}
		
		if (currentLocation!=null){
			DecimalFormat decimalFormat = new DecimalFormat("##.##");
			mAddressGPS.setText("GPS Lat:"+decimalFormat.format(currentLocation.latitude)+" Long:"+decimalFormat.format(currentLocation.longitude));
		}
		
		if (locationManager != null){
			locationManager.requestLocationUpdates(provider, 2500, 100, this);
		}
	}

	@Override
	public Dialog onCreateDialog(int id) {
		
		final Context context = this;
		switch (id) {
		case Utils.GPS_NOT_TURNED_ON:
			currentDialog = ConfirmationDialog.create(this, id,
					R.string.gps_not_turned_on,
					getString(R.string.gps_not_turned_on_details),
					R.string.confirm, new Runnable() {
						@Override
						public void run() {
							Intent intent = new Intent(
									Settings.ACTION_LOCATION_SOURCE_SETTINGS);
							startActivity(intent);
						}
					});
			return currentDialog;
		case Utils.GPS_NOT_TURNED_FF:
			currentDialog =  ConfirmationDialog.create(this, id,
					R.string.gps_not_turned_on_yet,
					getString(R.string.gps_not_turned_on_details),
					R.string.confirm,R.string.cancel, new Runnable() {
						@Override
						public void run() {
							Intent intent = new Intent(
									Settings.ACTION_LOCATION_SOURCE_SETTINGS);
							startActivity(intent);
						}
					}, new Runnable() {
						@Override
						public void run() {
							HomeActivity.showHome(context);
						}
					});
			return currentDialog;
		}

		return super.onCreateDialog(id);
	}

	@Override
	public void onPause() {
		super.onPause();
		activeActivity = false;
		if (locationManager != null) {
			locationManager.removeUpdates(this);
		}
	}
	
	@Override
	public void onStop() {
		super.onStop();
		activeActivity = false;
		if (locationManager != null) {
			locationManager.removeUpdates(this);
		}
	}
	


	@Override
	public void onLocationChanged(Location location) {
		double lat = location.getLatitude();
		double lng = location.getLongitude();
		this.currentLocation = new LatLng(lat, lng);
		(new GetAddressTask(this)).execute(location);
	}

	@Override
	public void onProviderDisabled(String provider) {
		tryToGetBestLocationProvider();
	}


	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		tryToGetBestLocationProvider();
	}

	@Override
	public void onProviderEnabled(String provider) {
		tryToGetBestLocationProvider();
	}

	private class GetAddressTask extends AsyncTask<Location, Void, String> {
		Context mContext;

		public GetAddressTask(Context context) {
			super();
			mContext = context;
		}

		/**
		 * Get a Geocoder instance, get the latitude and longitude look up the
		 * address, and return it
		 * 
		 * @params params One or more Location objects
		 * @return A string containing the address of the current location, or
		 *         an empty string if no address can be found, or an error
		 *         message
		 */
		@Override
		protected String doInBackground(Location... params) {
			Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
			// Get the current location from the input parameter list
			Location loc = params[0];
			// Create a list to contain the result address
			boolean useGoogleWeb = false;
			List<Address> addresses = null;
			try {
				/*
				 * Return 1 address.
				 */
				addresses = geocoder.getFromLocation(loc.getLatitude(),
						loc.getLongitude(), 1);
			} catch (Exception e1) {
				retries++;
				if (RETRY_COUNT_MAX == retries){
					retries = 0;
					useGoogleWeb = true;
					addresses = GeoLocationsUtils.getStringFromLocation(loc.getLatitude(),
						loc.getLongitude());
				}
			}
			// If the reverse geocode returned an address
			if (addresses != null && addresses.size() > 0) {
				// Get the first address
				Address address = addresses.get(0);
				/*
				 * Format the first line of address (if available), city, and
				 * country name.
				 */
				if (!useGoogleWeb){
					String addressText = String.format(
				
						"%s, %s, %s",
						// If there's a street address, add it
						address.getMaxAddressLineIndex() > 0 ? address
								.getAddressLine(0) : "",
						// Locality is usually a city
						address.getLocality(),
						// The country of the address
						address.getCountryName());
				// Return the text
				return addressText;
				} else {
					return  address
							.getAddressLine(0).replaceFirst("Province", "");
				}
			} else {
				return "error";
			}
		}

		

		@Override
		protected void onPostExecute(String address) {
			// Display the results of the lookup.
			if (address.equalsIgnoreCase("error") && isActiveActivity()) {
				init();
			} else {
				onAdressGet(address);
			}
		}

	}
	
	protected abstract void onAdressGet(String address);
	
	protected abstract void showProgress(boolean newValue);
}
