package com.globant.labs.swipper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.globant.labs.swipper.logic.esculturas.Autor;
import com.globant.labs.swipper.logic.esculturas.Escultura;
import com.globant.labs.swipper.logic.esculturas.Foto;
import com.globant.labs.swipper.logic.esculturas.GeoEscultura;
import com.globant.labs.swipper.net.IRequester;
import com.globant.labs.swipper.net.RestClientResistenciarte;
import com.globant.labs.swipper.utils.Constants;
import com.globant.labs.swipper.utils.Generador;
import com.globant.labs.swipper.utils.Utils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.common.base.Function;
import com.globant.labs.swipper.R;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MapActivity extends ActionBarCustomActivity implements IRequester {

	protected View mLoginFormView;
	protected View mLoginStatusView;

	// Google Map
	private GoogleMap googleMap;
	private Location myLocation;
	private ArrayList<GeoEscultura> listaEsculturas;
	private HashMap<Marker, GeoEscultura> esculturas_per_marker;
	private HashMap<GeoEscultura, View> view_per_esculturas;

	public MapActivity() {
		RestClientResistenciarte client = new RestClientResistenciarte(this);
		client.makeJsonRestRequest();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);

		try {

			mLoginFormView = findViewById(R.id.login_form);
			mLoginStatusView = findViewById(R.id.login_status);
			
			showProgress(true);
			initilizeMap();
		} catch (Exception e) {
		}

	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	protected void showProgress(final boolean show) {
		mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
		mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
	}

	public static void showHome(Context home) {
		Intent intent = new Intent(home, MapActivity.class);
		home.startActivity(intent);
	}

	/**
	 * function to load map. If map is not created it will create it for you
	 * 
	 * @throws Exception
	 * */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void initilizeMap() throws Exception {
		if (googleMap == null) {

			FragmentManager myFragmentManager = getSupportFragmentManager();
			SupportMapFragment mySupportMapFragment = (SupportMapFragment) myFragmentManager
					.findFragmentById(R.id.map);
			googleMap = mySupportMapFragment.getMap();

			googleMap.setMyLocationEnabled(true);
			googleMap.getUiSettings().setMyLocationButtonEnabled(true);

			// Getting LocationManager object from System Service
			// LOCATION_SERVICE
			LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

			// ---set the criteria for best location provider---
			Criteria c = new Criteria();
			c.setAccuracy(Criteria.ACCURACY_FINE);
			// ---OR---
			// c.setAccuracy(Criteria.ACCURACY_COARSE);
			c.setAltitudeRequired(false);
			c.setBearingRequired(false);
			c.setCostAllowed(true);
			c.setPowerRequirement(Criteria.POWER_HIGH);
			// ---get the best location provider---
			String bestProvider = locationManager.getBestProvider(c, true);

			esculturas_per_marker = new HashMap<Marker, GeoEscultura>();
			view_per_esculturas = new HashMap<GeoEscultura, View>();
			
			// Getting Current Location
			if (bestProvider != null) {
				myLocation = locationManager.getLastKnownLocation(bestProvider);

				if (myLocation != null) {
					// Getting latitude of the current location
					double latitude = myLocation.getLatitude();

					// Getting longitude of the current location
					double longitude = myLocation.getLongitude();

					LatLng myPosition = new LatLng(latitude, longitude);
					googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
							myPosition, 14.0f));

				}
			}

			
			googleMap.setInfoWindowAdapter(new MynfoWindowAdapter());
			// check if map is created successfully or not
			if (googleMap == null) {
				Toast.makeText(getApplicationContext(),
						"Sorry! unable to create maps", Toast.LENGTH_SHORT)
						.show();
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.map, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		} else if (id == android.R.id.home) {
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public String getRequestURI() {
		String url = Constants.BASE_URL
				+ "/api/v1/closest_nodes_by_coord?lat=-27.454528&lon=-58.976896&qty_nodes=2000";
		return url;
	}

	@Override
	public void onResponse(String response) {
		JSONArray jsonArray;
		try {
			jsonArray = new JSONArray(response);
			int max = jsonArray.length();

			listaEsculturas = new ArrayList<GeoEscultura>();

			for (int i = 0; i < max; i++) {
				JSONObject jsonObject = jsonArray.optJSONObject(i);

				GeoEscultura localReg = new GeoEscultura();

				Utils.extractFromResponseToObject(localReg, jsonObject);

				listaEsculturas.add(localReg);
			}

			final LatLngBounds.Builder builder = new LatLngBounds.Builder();

			esculturas_per_marker.clear();
			
			for (GeoEscultura locationStore : listaEsculturas) {
				final LatLng pos = new LatLng(locationStore.getNode_latitude(),
						locationStore.getNode_longitude());
				builder.include(pos);
				LatLng pos2 = new LatLng(
						locationStore.getNode_latitude() + 0.001,
						locationStore.getNode_longitude() + 0.001);
				builder.include(pos2);

				int image = R.drawable.ic_action_pin;

				// #148273
				Marker marker = googleMap.addMarker(new MarkerOptions()
						.position(pos)
						.title(locationStore.getNode_title().trim())
						.snippet(
								"Distancia actual:"
										+ locationStore.getDistance() + "(KM)")
						.icon(BitmapDescriptorFactory.fromResource(R.drawable.green_pin)));
				
				esculturas_per_marker.put(marker, locationStore);

			}

			showProgress(false);

			googleMap.setOnCameraChangeListener(new OnCameraChangeListener() {

				public void onCameraChange(CameraPosition arg0) {
					googleMap.animateCamera(CameraUpdateFactory
							.newLatLngBounds(builder.build(), 50));

					googleMap.setOnCameraChangeListener(null);
				}
			});
			
			googleMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
		            @Override
		            public void onInfoWindowClick(Marker marker) {
		            	GeoEscultura esc = esculturas_per_marker.get(marker);
		            	
		               ObraActivity.showHome(MapActivity.this, esc.getNid(), esc.getNode_title().trim());

		            }
		        });

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	public static int convertToPixels(Context context, int nDP)
	{
	    final float conversionScale = context.getResources().getDisplayMetrics().density;

	    return (int) ((nDP * conversionScale) + 0.5f) ;

	}
	
	@Override
	public void onResponse(InputStream result) {
		// TODO Auto-generated method stub

	}

	private class MynfoWindowAdapter implements InfoWindowAdapter {

        // Use default InfoWindow frame
        @Override
        public View getInfoWindow(Marker arg0) {
            return null;
        }

        // Defines the contents of the InfoWindow
        @Override
        public View getInfoContents(Marker arg0) {

        	GeoEscultura distancias2 = esculturas_per_marker.get(arg0);
        	
        	View v = View.inflate(MapActivity.this,
					R.layout.marker_layout, null);

			final LatLng lt = new LatLng(distancias2.getNode_latitude(),
					distancias2.getNode_longitude());

			final TextView texto = (TextView) v.findViewById(R.id.tittle);
			texto.setText(".  "+distancias2.getNode_title()+"  .");

			texto.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(
							android.content.Intent.ACTION_VIEW,
							Uri.parse("http://maps.google.com/maps?daddr="
									+ lt.latitude + "," + lt.longitude));
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.addCategory(Intent.CATEGORY_LAUNCHER);
					intent.setClassName("com.google.android.apps.maps",
							"com.google.android.maps.MapsActivity");
					startActivity(intent);
				}
			});

			view_per_esculturas.put(distancias2, v);
            return v;

        }
    }


	
}
