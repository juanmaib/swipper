package com.globant.labs.swipper.fragments.sections;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.globant.labs.swipper.AutorActivity;
import com.globant.labs.swipper.HomeActivity;
import com.globant.labs.swipper.LocatorActivity;
import com.globant.labs.swipper.NearbyLocations;
import com.globant.labs.swipper.ObraActivity;
import com.globant.labs.swipper.StandardImageProgrammatic;
import com.globant.labs.swipper.fragments.LocatorFragment;
import com.globant.labs.swipper.fragments.PlaceholderFragment;
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

public class HighlightsSectionFragment extends LocatorFragment {

	private ArrayList<Foto> fotos;
	private Escultura localReg;
	private ImageView img;
	private Button textView;
	private View mDeatailedView;
	private Button textAuthor;
	private Button ubic_main;
	
	private int currentPage = 0;
	private Location myLocation;
		
	public HighlightsSectionFragment(Context context) {
		super(context);
		codeName = getClass().getName();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		setFragmentId(R.layout.fragment_home);
		
		super.onCreateView(inflater, container, savedInstanceState);
		
		img = (ImageView) rootView.findViewById(R.id.img);
		
		
		textView = (Button) rootView.findViewById(R.id.title);
		textAuthor = (Button) rootView.findViewById(R.id.author_main);
		ubic_main = (Button) rootView.findViewById(R.id.ubic_main);

		mDeatailedView = rootView.findViewById(R.id.login_form_detailed);
		
		Utils.addTouchEffectoToButtons(rootView);
		return rootView;
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
	public String getRequestURI() {
		return Constants.BASE_URL + "/api/v1/closest_nodes_by_coord?lat="
				+ myLocation.getLatitude() + "&lon="
				+ myLocation.getLongitude() + "&qty_nodes=1&dist=500";
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

		if (!listaEsculturas.isEmpty()) {
			Collections.sort(listaEsculturas);

			int number = 1;

			for (GeoEscultura distancias2 : listaEsculturas) {

				getPhotos(distancias2);

			}

		}
		
		
	}

	private void getPhotos(final GeoEscultura distancias2) {
		IRequester r = new IRequester() {
			@Override
			public void onResponse(String response) {
				try {
					final JSONObject jsonObject = new JSONObject(response);
					JSONArray fotosArrays = jsonObject.getJSONObject("field_fotos")
							.getJSONArray("und");

					
					setUbicacion(jsonObject, ubic_main);

					setAuthor(jsonObject, textAuthor);

					fotos = new ArrayList<Foto>();
					for (int i = 0; i < fotosArrays.length(); i++) {
						Foto foto = new Foto();
						JSONObject jsonObjectFoto = fotosArrays.getJSONObject(i);
						Utils.extractFromResponseToObject(foto, jsonObjectFoto);
						fotos.add(foto);
					}

					
					// Image url
					final String image_url = Constants.BASE_URL + "/sites/default/files/"
							+ fotos.get(0).getFilename();

					Function<Bitmap, Void> afterLogin = new Function<Bitmap, Void>() {
						@Override
						public Void apply(Bitmap bmap) {
							final String text; 
							try {
								text = jsonObject.getString("title");
								textView.setText(text);
								textView.setEnabled(true);
								textView.setOnClickListener(new OnClickListener() {
									
									@Override
									public void onClick(View v) {
										ObraActivity.showHome(HomeActivity.getInstance(), distancias2.getNid(), text);
									}
								});
							} catch (JSONException e) {
								//text = "";
							}
							
							if (bmap != null){
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

								img.setLayoutParams(new LinearLayout.LayoutParams(
										newBmapWidth, newBmapHeight));
							}
							
							showProgress(false);
							mDeatailedView.setVisibility(View.VISIBLE);
							return null;
						}
					};

					getImageLoaderService()
							.DisplayImage(image_url, img, afterLogin);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			private void setUbicacion(JSONObject jsonObject, Button ubic_main) {
				/*
				 * "":{"und":[{"lat":"-27.4492586","lon":"-58.9922044",
				 * "map_width":null,"map_height":null,"zoom":"17","name":""}]}
				 */
				JSONArray aYs;
				try {
					if (jsonObject.isNull("field_mapa")) {
						ubic_main.setVisibility(View.GONE);
						return;
					}
					JSONObject auhtorObject;
					auhtorObject = jsonObject.getJSONObject("field_mapa");

					aYs = auhtorObject.isNull("und") ? null : auhtorObject
							.getJSONArray("und");

					if (aYs == null) {
						ubic_main.setVisibility(View.GONE);
					} else {
						final double lat = Double.valueOf(aYs.getJSONObject(0).getDouble("lat"));
						final double lon = Double.valueOf(aYs.getJSONObject(0).getDouble("lon"));
						ubic_main.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								
								Intent intent = new Intent(
										android.content.Intent.ACTION_VIEW,
										Uri.parse("http://maps.google.com/maps?&daddr="
												+ lat + "," + lon));
								intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								intent.addCategory(Intent.CATEGORY_LAUNCHER);
								intent.setClassName("com.google.android.apps.maps",
										"com.google.android.maps.MapsActivity");
								startActivity(intent);
							}
						});
					}
				} catch (JSONException e) {
					// the author is null
					ubic_main.setVisibility(View.GONE);
					return;
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


	@Override
	public void onResponse(InputStream result) {
	}

	private void setAuthor(JSONObject response, final Button textAuthor) {
		JSONArray aYs;
		try {
			//textAuthor.setVisibility(View.GONE);
			
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
							textAuthor.setEnabled(true);
							textAuthor.setVisibility(View.VISIBLE);
							final int nid = autorId;
							textAuthor.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View v) {
									
									AutorActivity.showHome(HomeActivity.getInstance(), nid, author.getTitle().trim());
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

}
