package com.globant.labs.swipper;

import java.text.DecimalFormat;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.globant.labs.swipper.logic.esculturas.GeoEscultura;
import com.globant.labs.swipper.places.Place;
import com.globant.labs.swipper.places.PlacesAPIClient;
import com.squareup.picasso.Picasso;

public class PlaceInfoActivity extends ActionBarActivity {

	protected static String LOG_TAG = "SWIPPER";
	public static String EXTRA_QUERY = "extra_query";
	public static String EXTRA_LAT = "extra_query_lat";
	public static String EXTRA_LNG = "extra_query_lng";
	
	protected String mQuery;
	protected Double mQueryLat;
	protected Double mQueryLng;
	
	protected TextView mPlaceNameTextView;
	protected TextView mPlaceRatingTextView;
	protected TextView mPlaceAddressTextView;
	protected TextView mPlacePhoneTextView;
	protected ImageView mPlaceImageView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_place_info);
		
		Intent intent = getIntent();
		mQuery = intent.getStringExtra(EXTRA_QUERY);
		mQueryLat = intent.getDoubleExtra(EXTRA_LAT, 0);
		mQueryLng = intent.getDoubleExtra(EXTRA_LNG, 0);
		
		if(mQuery == null) {
			Log.e(LOG_TAG, "Place query missing!");
			finish();
		}
		
		mPlaceNameTextView = (TextView) findViewById(R.id.placeNameTextView);
		mPlaceRatingTextView = (TextView) findViewById(R.id.placeRatingTextView);
		mPlaceAddressTextView = (TextView) findViewById(R.id.placeAddressTextView);
		mPlacePhoneTextView = (TextView) findViewById(R.id.placePhoneTextView);
		mPlaceImageView = (ImageView) findViewById(R.id.placeImageView);
		
		mPlaceNameTextView.setText(intent.getStringExtra(EXTRA_QUERY));
		mPlaceAddressTextView.setText(
				intent.getStringExtra("DIR") + ", "	+ 
				intent.getStringExtra("CIUDAD") + ", " +
				intent.getStringExtra("PROV"));
		
		mPlacePhoneTextView.setText(intent.getStringExtra("TEL"));
		
		GeoEscultura geoEscultura = new GeoEscultura();
		geoEscultura.setNode_title(mQuery);
		geoEscultura.setNode_latitude(mQueryLat);
		geoEscultura.setNode_longitude(mQueryLng);
		
		AsyncPlace asyncPlace = new AsyncPlace();
		asyncPlace.execute(geoEscultura);
		
	}
	
	protected PlaceInfoActivity getContext() {
		return this;
	}
	
	private class AsyncPlace extends AsyncTask<GeoEscultura, Void, List<Place>> {

		@Override
		protected List<Place> doInBackground(GeoEscultura... params) {
			PlacesAPIClient apiClient = new PlacesAPIClient();
			return apiClient.textsearch(params[0].getNode_title(), params[0].getNode_latitude(), params[0].getNode_longitude());
		}
		
		protected void onPostExecute(List<Place> result) {
			//Log.d("SWIPPER", result.getPlace_id()+" "+result.getName());
			//mPlaceNameTextView.setText(result.getName());
			if(result != null && result.size() > 0) {
				mPlaceRatingTextView.setText(new DecimalFormat("#.##").format(result.get(0).getRating()));
				Picasso.with(getContext()).load("" +
						"https://maps.googleapis.com/maps/api/place/photo" +
						"?maxwidth=400" +
						"&photoreference=" + result.get(0).getPhotos().get(0).getPhoto_reference() +
						"&key=AIzaSyBIuXfrOunbMGsPA21rye1fSI6YMRJMe-Y").into(mPlaceImageView);
			}
			//mPlaceAddressTextView.setText(result.getFormatted_address());			
		}
		
	}

}
