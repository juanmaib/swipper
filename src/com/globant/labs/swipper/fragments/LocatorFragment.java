package com.globant.labs.swipper.fragments;

import java.text.DecimalFormat;

import android.app.Activity;
import android.app.Dialog;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.globant.labs.swipper.net.IRequester;
import com.globant.labs.swipper.net.ImageLoader;
import com.globant.labs.swipper.utils.Utils;
import com.google.android.gms.maps.model.LatLng;
import com.globant.labs.swipper.R;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;

public abstract class LocatorFragment extends PlaceholderFragment implements
		IRequester, LocationListener {

	protected LocationManager locationManager;
	protected String provider;

	protected Dialog currentDialog;

	protected TextView mAddress;
	protected TextView mAddressGPS;

	protected LatLng currentLocation;

	protected boolean activeActivity = true;

	protected final int RETRY_COUNT_MAX = 3;

	protected int retries = 0;

	public LocatorFragment(Context act) {
		super(act);
	}

	protected void init() {
		LocationManager service = (LocationManager) context
				.getSystemService(context.LOCATION_SERVICE);
		boolean enabledGPS = service
				.isProviderEnabled(LocationManager.GPS_PROVIDER);
		boolean enabledWiFi = service
				.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

		// Check if enabled and if not send user to the GSP settings
		// Better solution would be to display a dialog and suggesting to
		// go to the settings
		if (!enabledGPS && !enabledWiFi) {
			((Activity) context).showDialog(Utils.GPS_NOT_TURNED_ON);
		}

		locationManager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);
		// Define the criteria how to select the locatioin provider -> use
		// default
		provider = enabledGPS ? LocationManager.GPS_PROVIDER
				: LocationManager.NETWORK_PROVIDER;
		Location location = locationManager.getLastKnownLocation(provider);

		if (location == null) {
			provider = !enabledGPS ? LocationManager.GPS_PROVIDER
					: LocationManager.NETWORK_PROVIDER;
			location = locationManager.getLastKnownLocation(provider);
		}
		if (location != null) {
			onLocationChanged(location);
		}
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (locationManager != null) {
			tryToGetBestLocationProvider();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (locationManager != null) {
			locationManager.removeUpdates(this);
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		if (locationManager != null) {
			locationManager.removeUpdates(this);
		}
	}

	private void tryToGetBestLocationProvider() {
		LocationManager service = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
		boolean enabledGPS = service
				.isProviderEnabled(LocationManager.GPS_PROVIDER);
		boolean enabledWiFi = service
				.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		// Check if enabled and if not send user to the GSP settings
		// Better solution would be to display a dialog and suggesting to
		// go to the settings
		if (!enabledGPS && !enabledWiFi) {
			if (currentDialog == null || !currentDialog.isShowing()) {
				((Activity) context).showDialog(Utils.GPS_NOT_TURNED_FF);
			}
		}

		if (locationManager != null) {
			locationManager.requestLocationUpdates(provider, 2500, 100, this);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (rootView == null) {
			try {
				rootView = inflater.inflate(getFragmentId(), container, false);
				mLoginFormView = rootView.findViewById(R.id.login_form);
				mLoginStatusView = rootView.findViewById(R.id.login_status);

				showProgress(true);
			} catch (InflateException e) {

			}
		} else {
			ViewGroup parent = (ViewGroup) rootView.getParent();
			if (parent != null) {
				parent.removeView(rootView);
			}
		}
		init();
		return rootView;
	}

	@Override
	public void onLocationChanged(Location location) {
		double lat = location.getLatitude();
		double lng = location.getLongitude();
		this.currentLocation = new LatLng(lat, lng);
	}

	@Override
	public void onProviderDisabled(String provider) {
		init();
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		init();
	}

	@Override
	public void onProviderEnabled(String provider) {
		init();
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	protected void showProgress(final boolean show) {
		mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
		mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
	}

}
