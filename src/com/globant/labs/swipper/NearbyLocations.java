package com.globant.labs.swipper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.globant.labs.swipper.fragments.sections.EsculturaItem;
import com.globant.labs.swipper.logic.esculturas.Autor;
import com.globant.labs.swipper.logic.esculturas.Escultura;
import com.globant.labs.swipper.logic.esculturas.Foto;
import com.globant.labs.swipper.logic.esculturas.GeoEscultura;
import com.globant.labs.swipper.net.IRequester;
import com.globant.labs.swipper.net.RestClientResistenciarte;
import com.globant.labs.swipper.utils.Constants;
import com.globant.labs.swipper.utils.Utils;
import com.google.android.gms.maps.model.LatLng;
import com.google.common.base.Function;
import com.globant.labs.swipper.R;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class NearbyLocations extends LocatorActivity implements IRequester {

	private View mLoginStatusView;
	private View mLoginFormView;

	private LinearLayout myLinearLayout;

	private ProgressBar progressBarStreet;
	private Location myLocation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nearby_locations);

		mLoginFormView = findViewById(R.id.home_form);
		mLoginStatusView = findViewById(R.id.login_status);

		myLinearLayout = (LinearLayout) findViewById(R.id.text_view_place);

		mAddress = (TextView) findViewById(R.id.cuurent_location);

		mAddressGPS = (TextView) findViewById(R.id.cuurent_location_by_gps);

		progressBarStreet = (ProgressBar) findViewById(R.id.progress_street);

		mAddress.setVisibility(View.GONE);

		Utils.addTouchEffectoToButtons(mLoginFormView);

		showProgress(true);
		init();
	}

	public static void showHome(Context home) {
		Intent intent = new Intent(home, NearbyLocations.class);
		home.startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.nearby_locations, menu);
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
	public void onLocationChanged(Location location) {
		super.onLocationChanged(location);
		if (location != null) {
			myLocation = location;
			RestClientResistenciarte client = new RestClientResistenciarte(this);
			client.makeJsonRestRequest();
		}
	}

	@Override
	protected void onAdressGet(String address) {
		if (!address.contains("error")) {
			mAddress.setVisibility(View.VISIBLE);
			mAddress.setText(address);
			progressBarStreet.setVisibility(View.GONE);
		} else {
			mAddress.setVisibility(View.GONE);
			progressBarStreet.setVisibility(View.VISIBLE);
		}

	}

	@Override
	protected void showProgress(final boolean show) {
		mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
		mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
	}

	@Override
	public String getRequestURI() {
		return Constants.BASE_URL + "/api/v1/closest_nodes_by_coord?lat="
				+ myLocation.getLatitude() + "&lon="
				+ myLocation.getLongitude() + "&qty_nodes=10&dist=12000";
	}

	@Override
	public void onResponse(String response) {
		ArrayList<GeoEscultura> listaEsculturas = new ArrayList<GeoEscultura>();

		JSONArray jsonArray;
		try {
			jsonArray = new JSONArray(response);
			int max = jsonArray.length();

			for (int i = 0; i < max; i++) {
				JSONObject jsonObject = jsonArray.optJSONObject(i);

				GeoEscultura localReg = new GeoEscultura();

				Utils.extractFromResponseToObject(localReg, jsonObject);

				listaEsculturas.add(localReg);
			}

		} catch (Exception e) {

		}
		myLinearLayout.removeAllViewsInLayout();

		if (!listaEsculturas.isEmpty()) {
			Collections.sort(listaEsculturas);

			int number = 1;

			for (GeoEscultura distancias2 : listaEsculturas) {

				View v = View.inflate(NearbyLocations.this,
						R.layout.fragment_escultura_nearby, null);

				final LatLng lt = new LatLng(distancias2.getNode_latitude(),
						distancias2.getNode_longitude());

				addIMage(v, distancias2);

				final TextView texto = (TextView) v.findViewById(R.id.detalle);
				texto.setText(" " + number + " - "
						+ distancias2.getNode_title());
				number++;
				final Button textoUbic = (Button) v
						.findViewById(R.id.ubicacion);
				textoUbic.setEnabled(true);
				textoUbic.setText((Utils.toDecimalFormat(distancias2
						.getDistance() * 1000)) + " metros aprox.");

				textoUbic.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent intent = new Intent(
								android.content.Intent.ACTION_VIEW,
								Uri.parse("http://maps.google.com/maps?   saddr="
										+ currentLocation.latitude
										+ ","
										+ currentLocation.longitude
										+ "&daddr="
										+ lt.latitude + "," + lt.longitude));
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						intent.addCategory(Intent.CATEGORY_LAUNCHER);
						intent.setClassName("com.google.android.apps.maps",
								"com.google.android.maps.MapsActivity");
						startActivity(intent);
					}
				});

				myLinearLayout.addView(v);

			}

			showProgress(false);
		}
	}

	private void addIMage(final View view, final GeoEscultura distancias2) {
		IRequester r = new IRequester() {

			@Override
			public void onResponse(String response) {
				try {
					final JSONObject jsonObject = new JSONObject(response);
					JSONArray fotosArrays = jsonObject.getJSONObject(
							"field_fotos").getJSONArray("und");

					addAutorName(jsonObject,
							(Button) view.findViewById(R.id.author));

					ArrayList<Foto> fotos = new ArrayList<Foto>();
					for (int i = 0; i < fotosArrays.length(); i++) {
						Foto foto = new Foto();
						JSONObject jsonObjectFoto = fotosArrays
								.getJSONObject(i);
						Utils.extractFromResponseToObject(foto, jsonObjectFoto);
						fotos.add(foto);
					}

					// Image url
					final String image_url = Constants.BASE_URL
							+ "/sites/default/files/"
							+ fotos.get(0).getUri()
									.replaceFirst("public://", "");
					;

					Function<Bitmap, Void> afterLogin = new Function<Bitmap, Void>() {
						@Override
						public Void apply(final Bitmap bmap) {
							View p = view.findViewById(R.id.progress);
							p.setVisibility(View.GONE);
							View iV = view.findViewById(R.id.default_image);
							iV.setVisibility(View.GONE);
							
							DisplayMetrics metrics = new DisplayMetrics();
							HomeActivity.getInstance().getWindowManager()
									.getDefaultDisplay().getMetrics(metrics);
							int height = metrics.heightPixels;
							int width = metrics.widthPixels;

							float bmapWidth = bmap.getWidth();
							float bmapHeight = bmap.getHeight();

							float wRatio = width / bmapWidth;
							float hRatio = height / bmapHeight;

							float ratioMultiplier = wRatio;
							// Untested conditional though I expect this might work
							// for landscape mode
							if (hRatio < wRatio) {
								ratioMultiplier = hRatio;
							}

							int newBmapWidth = (int) (bmapWidth * ratioMultiplier);
							int newBmapHeight = (int) (bmapHeight * ratioMultiplier);

							view.findViewById(R.id.image).setLayoutParams(new FrameLayout.LayoutParams(
									newBmapWidth, newBmapHeight));
							

							Button read = (Button) view.findViewById(R.id.detalle);
							read.setEnabled(true);
							read.setOnClickListener(new OnClickListener() {
								
								@Override
								public void onClick(View v) {
									ObraActivity.showHome(HomeActivity.getInstance(), distancias2.getNid(), distancias2.getNode_title().trim());
								}
							});
							
							view.findViewById(R.id.image).setOnClickListener(
									new OnClickListener() {

										@Override
										public void onClick(View v) {
											int author = 0;
											try{
												author = jsonObject.getJSONObject("field_autor").getJSONArray("und").getJSONObject(0).getInt("target_id");
											} catch(Exception e){
												
											}
											StandardImageProgrammatic.showHome(
													NearbyLocations.this,
													bmap, distancias2.getNode_title().trim(), author, image_url, distancias2.getNid());
										}
									});
							
							Button shareButton = (Button) view.findViewById(R.id.share_btn);
							shareButton.setEnabled(true);
							shareButton.setOnClickListener(new OnClickListener() {
								
								@Override
								public void onClick(View v) {
									EsculturaItem item = new EsculturaItem();
									Escultura d = new Escultura();
									d.setTitle(distancias2.getNode_title());
									item.setEscultura(d);
									item.setImage(image_url);
									Utils.shareEscultura(HomeActivity.getInstance(), item);
								}
							});
							return null;
						}
					};

					HomeActivity
							.getInstance()
							.getImageLoaderService()
							.DisplayImage(image_url,
									(ImageView) view.findViewById(R.id.image),
									afterLogin);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			public String getRequestURI() {
				return Constants.BASE_URL + "/api/v1/node/"
						+ distancias2.getNid();
			}

			@Override
			public void onResponse(InputStream result) {
			}
		};

		RestClientResistenciarte internalCall = new RestClientResistenciarte(r);
		internalCall.makeJsonRestRequest();
	}

	private void addAutorName(JSONObject response, final Button textAuthor) {
		JSONArray aYs;
		try {
			if (response.isNull("field_autor")) {
				textAuthor.setText(Constants.ANONIMO);
				return;
			}

			JSONObject auhtorObject;

			try {
				auhtorObject = response.getJSONObject("field_autor");
			} catch (JSONException e) {
				// the author is null
				textAuthor.setText(Constants.ANONIMO);
				return;
			}

			aYs = auhtorObject.isNull("und") ? null : auhtorObject
					.getJSONArray("und");

			
			if (aYs == null) {
				textAuthor.setText(Constants.ANONIMO);
			} else {
				final int autorId = aYs.getJSONObject(0).getInt("target_id");

				final Autor author = new Autor();

				IRequester authorRequest = new IRequester() {

					@Override
					public void onResponse(String response) {
						try {
							JSONArray jsonArray;
							jsonArray = new JSONArray(response);
							JSONObject jsonObject = jsonArray.optJSONObject(0);
							Utils.extractFromResponseToObject(author,
									jsonObject);
							textAuthor.setText(author.getTitle().trim());
							final int nid = autorId;
							textAuthor.setEnabled(true);
							textAuthor
									.setOnClickListener(new OnClickListener() {

										@Override
										public void onClick(View v) {

											AutorActivity.showHome(
													HomeActivity.getInstance(),
													nid, author.getTitle().trim());
										}
									});
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					@Override
					public String getRequestURI() {
						return Constants.BASE_URL
								+ "/api/v1/node?parameters[nid]=" + autorId;
					}

					@Override
					public void onResponse(InputStream result) {
					}
				};

				RestClientResistenciarte internalCall = new RestClientResistenciarte(
						authorRequest);
				internalCall.makeJsonRestRequest();
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void onResponse(InputStream result) {
		// TODO Auto-generated method stub

	}

}
