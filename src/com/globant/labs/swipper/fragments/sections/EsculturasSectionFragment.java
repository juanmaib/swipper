package com.globant.labs.swipper.fragments.sections;

import java.io.InputStream;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.globant.labs.swipper.AutorActivity;
import com.globant.labs.swipper.HomeActivity;
import com.globant.labs.swipper.ObraActivity;
import com.globant.labs.swipper.R;
import com.globant.labs.swipper.StandardImageProgrammatic;
import com.globant.labs.swipper.fragments.PlaceholderFragment;
import com.globant.labs.swipper.logic.esculturas.Autor;
import com.globant.labs.swipper.logic.esculturas.Escultura;
import com.globant.labs.swipper.logic.esculturas.Foto;
import com.globant.labs.swipper.net.IRequester;
import com.globant.labs.swipper.net.RestClientResistenciarte;
import com.globant.labs.swipper.utils.Constants;
import com.globant.labs.swipper.utils.Utils;
import com.google.common.base.Function;

public class EsculturasSectionFragment extends PlaceholderFragment {

	ProgressDialog pDialog;
	private LinearLayout myLinearLayout;
	private int currentPage = 0;
	private Button button2;
	private Button button;


	public EsculturasSectionFragment(Context context) {
		super(context);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		setFragmentId(R.layout.fragment_home2);

		super.onCreateView(inflater, container, savedInstanceState);
	
		myLinearLayout = (LinearLayout) rootView.findViewById(R.id.text_view_place);
		
		RestClientResistenciarte client = new RestClientResistenciarte(this);
		client.makeJsonRestRequest();
		
		
		button = (Button) rootView.findViewById(R.id.btn_load);
		button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				currentPage++;
				new loadMoreListView().execute();
			}
		});
		
		button2 = (Button) rootView.findViewById(R.id.btn_load_before);
		button2.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				currentPage--;
				if (currentPage < 0){
					currentPage = 0;
				}
				button2.setVisibility(View.VISIBLE);
				new loadMoreListView().execute();
			}
		});
		button2.setVisibility(View.GONE);

		return rootView;
	}

	@Override
	public String getRequestURI() {
		return Constants.BASE_URL + "/api/v1/node?page="+currentPage+"&pagesize=5&parameters[type]=escultura";
	}

	@Override
	public void onResponse(String response) {
		JSONArray jsonArray;
		
		ArrayList<Escultura> esculturas = new ArrayList<Escultura>();
		try {
			jsonArray = new JSONArray(response);
			int max = jsonArray.length();
			
			if (max < Constants.MAX_NUMBER_ITEMS){
				button.setVisibility(View.GONE);
			}
			
			for (int i = 0; i < max; i++) {
				JSONObject jsonObject = jsonArray.optJSONObject(i);

				Escultura localReg = new Escultura();

				EsculturaItem item = new EsculturaItem();
				Utils.extractFromResponseToObject(localReg, jsonObject);

				item.setEscultura(localReg);
				
				esculturas.add(localReg);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		

		myLinearLayout.removeAllViewsInLayout();


		for (Escultura item : esculturas) {

			final Escultura distancias2 = item;
			
			View v = View.inflate(HomeActivity.getInstance(),
					R.layout.fragment_escultura, null);

			addIMage(v, item);
			
			

			final TextView texto = (TextView) v.findViewById(R.id.detalle);
			texto.setText(distancias2.getTitle());

			myLinearLayout.addView(v);

		}

		if (esculturas.isEmpty()){
			RestClientResistenciarte client = new RestClientResistenciarte(this);
			client.makeJsonRestRequest();
		} else {
			showProgress(false);
		}
	}
	

	private void addIMage(final View view, final Escultura distancias2) {
		IRequester r = new IRequester() {

			@Override
			public void onResponse(String response) {
				final JSONObject jsonObject;
				try {
					jsonObject = new JSONObject(response);
					JSONArray fotosArrays = jsonObject.getJSONObject(
							"field_fotos").getJSONArray("und");

					addAutorName(jsonObject,
							(Button) view.findViewById(R.id.author));

					final Button textoUbic = (Button) view.findViewById(R.id.ubicacion);
					setUbicacion(jsonObject, textoUbic);
					
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

							Button read = (Button) view.findViewById(R.id.detalle);
							read.setEnabled(true);
							read.setOnClickListener(new OnClickListener() {
								
								@Override
								public void onClick(View v) {
									ObraActivity.showHome(HomeActivity.getInstance(), distancias2.getNid(), distancias2.getTitle().trim());
								}
							});
							
							
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
							
							view.findViewById(R.id.image).setOnClickListener(new OnClickListener() {
								
								@Override
								public void onClick(View v) {
									int author = 0;
									try{
										author = jsonObject.getJSONObject("field_autor").getJSONArray("und").getJSONObject(0).getInt("target_id");
									} catch(Exception e){
										
									}
									StandardImageProgrammatic.showHome(HomeActivity.getInstance(), bmap, distancias2.getTitle(), author, image_url, distancias2.getNid() );
								}
							});
							
							Button shareButton = (Button) view.findViewById(R.id.share_btn);
							shareButton.setEnabled(true);
							shareButton.setOnClickListener(new OnClickListener() {
								
								@Override
								public void onClick(View v) {
									EsculturaItem item = new EsculturaItem();
									item.setEscultura(distancias2);
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

			private void setUbicacion(JSONObject jsonObject, Button ubic_main) {
				/*
				 * "":{"und":[{"lat":"-27.4492586","lon":"-58.9922044",
				 * "map_width":null,"map_height":null,"zoom":"17","name":""}]}
				 * 
				 * field_ubicacion":{"und":[{"value":"Perón, Juan Domingo Nº 454\t\t\t\t",
				 * "safe_value":"Perón, Juan Domingo Nº 454\t\t\t\t","format":null}]}
				 */
				final JSONArray aYs;
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
						
						ubic_main.setEnabled(true);
						ubic_main.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								double lat = 0;
								double lon = 0;
								try {
									lat = Double.valueOf(aYs.getJSONObject(0).getDouble("lat"));
									lon = Double.valueOf(aYs.getJSONObject(0).getDouble("lon"));
								} catch (JSONException e) {
								}
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
						
						if (jsonObject.isNull("field_ubicacion")) {
							ubic_main.setText("Ver en el Mapa");
							return;
						}
						JSONObject ubiObject;
						ubiObject = jsonObject.getJSONObject("field_ubicacion");

						JSONArray aYs2 = ubiObject.isNull("und") ? null : ubiObject
								.getJSONArray("und");
						if (aYs2 == null) {
							ubic_main.setText("Ver en el Mapa");
							return;
						} else {
							final String value = String.valueOf(aYs2.getJSONObject(0).getString("value")).trim();
							ubic_main.setText(value);
						}
						
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
	
	private void addAutorName(JSONObject response, final Button textAuthor) {
		JSONArray aYs;
		try {
			if (response.isNull("field_autor")){
				textAuthor.setText(Constants.ANONIMO);
				return;
			}
			
			JSONObject auhtorObject;
			
			try{
				auhtorObject = response.getJSONObject("field_autor");
			} catch (JSONException e){
				//the author is null
				textAuthor.setText(Constants.ANONIMO);
				return;
			}
			
			aYs = auhtorObject.isNull("und")? null:auhtorObject.getJSONArray("und");
			
			if (aYs == null){
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
							Utils.extractFromResponseToObject(author, jsonObject);
							textAuthor.setText(author.getTitle().trim());
							textAuthor.setEnabled(true);
							textAuthor.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View v) {
									
									AutorActivity.showHome(HomeActivity.getInstance(), autorId, author.getTitle());
								}
							});
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					@Override
					public String getRequestURI() {
						return Constants.BASE_URL + "/api/v1/node?parameters[nid]="
								+ autorId;
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

	/**
	 * Async Task that send a request to url Gets new list view data Appends to
	 * list view
	 * */
	private class loadMoreListView extends AsyncTask<Void, Void,  ArrayList<EsculturaItem> > {

		@Override
		protected void onPreExecute() {
			showProgress(true);
			if (currentPage == 0){
				button2.setVisibility(View.GONE);
			} else {
				button2.setVisibility(View.VISIBLE);
			}
			
		}

		protected ArrayList<EsculturaItem> doInBackground(Void... unused) {
			try {
				IRequester r = new IRequester() {
					
					@Override
					public void onResponse(InputStream result) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public String getRequestURI() {
						return Constants.BASE_URL + "/api/v1/node?page="+currentPage+"&pagesize=5&parameters[type]=escultura";
					}

					@Override
					public void onResponse(String response) {
						JSONArray jsonArray;
						ArrayList<Escultura> esculturas = new ArrayList<Escultura>();
						try {
							jsonArray = new JSONArray(response);
							int max = jsonArray.length();
							
							if(max == 0){
								//no more results
								currentPage--;
								button.setVisibility(View.GONE);
								showProgress(false);
								return;
							}
							
							myLinearLayout.removeAllViewsInLayout();
							for (int i = 0; i < max; i++) {
								JSONObject jsonObject = jsonArray.optJSONObject(i);

								Escultura localReg = new Escultura();

								EsculturaItem item = new EsculturaItem();
								Utils.extractFromResponseToObject(localReg, jsonObject);

								item.setEscultura(localReg);
								
								esculturas.add(localReg);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						

						for (Escultura item : esculturas) {

							Escultura distancias2 = item;
							
							View v = View.inflate(HomeActivity.getInstance(),
									R.layout.fragment_escultura, null);


							final ImageView imgView = (ImageView) v.findViewById(R.id.image);

							addIMage(v, item);

							final TextView texto = (TextView) v.findViewById(R.id.detalle);
							texto.setText(distancias2 != null?distancias2.getTitle():"Desconocido");

							myLinearLayout.addView(v);

						}
						
						
						final ScrollView scroll = (ScrollView) mLoginFormView;
						scroll.postDelayed(new Runnable() {
							@Override
							public void run() {
								scroll.smoothScrollTo(0, 0);
								scroll.fullScroll(ScrollView.FOCUS_UP);
								showProgress(false);
							}
						}, 600);
						
					}

				};
				RestClientResistenciarte client = new RestClientResistenciarte(r);
				client.makeJsonRestRequest();

			} catch (Exception e) {
				return null;
			}
			return new ArrayList<EsculturaItem>();
		}

		protected void onPostExecute( ArrayList<EsculturaItem>  unused) {
			
		}
	}
}
