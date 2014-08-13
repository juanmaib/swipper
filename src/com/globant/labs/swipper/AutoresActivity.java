package com.globant.labs.swipper;

import java.io.InputStream;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.globant.labs.swipper.fragments.sections.EsculturaItem;
import com.globant.labs.swipper.logic.esculturas.Autor;
import com.globant.labs.swipper.logic.esculturas.Escultura;
import com.globant.labs.swipper.logic.esculturas.Foto;
import com.globant.labs.swipper.net.IRequester;
import com.globant.labs.swipper.net.ImageLoader;
import com.globant.labs.swipper.net.RestClientResistenciarte;
import com.globant.labs.swipper.utils.Constants;
import com.globant.labs.swipper.utils.Utils;
import com.google.common.base.Function;
import com.globant.labs.swipper.R;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class AutoresActivity extends ActionBarCustomActivity implements
		IRequester {

	ProgressDialog pDialog;
	private LinearLayout myLinearLayout;
	private int currentPage = 0;
	private Button button2;
	private Button button;

	protected View mLoginFormView;
	protected View mLoginStatusView;

	private ImageLoader imageLoaderService;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_autores);

		myLinearLayout = (LinearLayout) findViewById(R.id.text_view_place);

		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = findViewById(R.id.login_status);

		// initialize tthe image loader service
		// ImageLoader class instance
		imageLoaderService = ImageLoader.getInstance(getApplicationContext());

		showProgress(true);

		RestClientResistenciarte client = new RestClientResistenciarte(this);
		client.makeJsonRestRequest();

		button = (Button) findViewById(R.id.btn_load);
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				currentPage++;
				new loadMoreListView().execute();
			}
		});

		button2 = (Button) findViewById(R.id.btn_load_before);
		button2.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				currentPage--;
				if (currentPage < 0) {
					currentPage = 0;
				}
				button2.setVisibility(View.VISIBLE);
				new loadMoreListView().execute();
			}
		});
		button2.setVisibility(View.GONE);

	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	protected void showProgress(final boolean show) {
		mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
		mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
	}

	public static void showHome(Context home) {
		Intent intent = new Intent(home, AutoresActivity.class);
		home.startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.autores, menu);
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
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public String getRequestURI() {
		return Constants.BASE_URL + "/api/v1/node?page=" + currentPage
				+ "&pagesize=5&parameters[type]=autores";
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

		for (final Escultura item : esculturas) {

			Escultura distancias2 = item;

			View v = View.inflate(AutoresActivity.this,
					R.layout.fragment_autor, null);

			addIMage(v, item);
			final int nid = item.getNid();
			final TextView texto = (TextView) v.findViewById(R.id.tittle);
			texto.setText(distancias2.getTitle().trim());
			texto.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					
					AutorActivity.showHome(AutoresActivity.this, nid, item.getTitle());
				}
			});

			myLinearLayout.addView(v);

		}

		if (esculturas.isEmpty()) {
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
				try {
					JSONObject jsonObject = new JSONObject(response);

					final Button textoUbic = (Button) view
							.findViewById(R.id.description);
					setDescription(jsonObject, textoUbic);
					final int nid = distancias2.getNid();
					textoUbic.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							
							AutorActivity.showHome(AutoresActivity.this, nid, distancias2.getTitle());
						}
					});

					if (!jsonObject.isNull("field_fotos")) {
						try {
							jsonObject.getJSONArray("field_fotos");
							ImageView viewImg = (ImageView) view
									.findViewById(R.id.image);
							viewImg.setBackgroundResource(R.drawable.ic_author_default);
							return;
						} catch (Exception e) {
							//everything goes well!
						}
					} else {
						ImageView viewImg = (ImageView) view
								.findViewById(R.id.image);
						viewImg.setBackgroundResource(R.drawable.ic_author_default);
						return;
					}
					JSONArray fotosArrays = jsonObject.getJSONObject(
							"field_fotos").getJSONArray("und");
					ArrayList<Foto> fotos = new ArrayList<Foto>();
					for (int i = 0; i < fotosArrays.length(); i++) {
						Foto foto = new Foto();
						JSONObject jsonObjectFoto = fotosArrays
								.getJSONObject(i);
						Utils.extractFromResponseToObject(foto, jsonObjectFoto);
						fotos.add(foto);
					}

					// Image url
					String image_url = Constants.BASE_URL
							+ "/sites/default/files/"
							+ fotos.get(0).getUri()
									.replaceFirst("public://", "");
					;

					Function<Bitmap, Void> afterLogin = new Function<Bitmap, Void>() {
						@Override
						public Void apply(Bitmap bmap) {
							View p = view.findViewById(R.id.progress);
							p.setVisibility(View.GONE);
							View iV = view.findViewById(R.id.default_image);
							iV.setVisibility(View.GONE);
							return null;
						}
					};

					imageLoaderService.DisplayImage(image_url,
							(ImageView) view.findViewById(R.id.image),
							afterLogin);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			private void setDescription(JSONObject jsonObject,
					Button descriptionBtn) {
				/*
				 * "":{"und":[{"lat":"-27.4492586","lon":"-58.9922044",
				 * "map_width":null,"map_height":null,"zoom":"17","name":""}]}
				 * 
				 * field_ubicacion":{"und":[{"value":"Perón, Juan Domingo Nº
				 * 454\t\t\t\t",
				 * "safe_value":"Perón, Juan Domingo Nº 454\t\t\t\t"
				 * ,"format":null}]}
				 */
				JSONArray aYs;
				try {
					if (jsonObject.isNull("body")) {
						descriptionBtn.setVisibility(View.GONE);
						return;
					}
					JSONObject auhtorObject;
					auhtorObject = jsonObject.getJSONObject("body");

					aYs = auhtorObject.isNull("und") ? null : auhtorObject
							.getJSONArray("und");

					if (aYs == null) {
						descriptionBtn.setVisibility(View.GONE);
					} else {
						final String value = String
								.valueOf("<b>[Leer mas..]</b>");
						descriptionBtn.setText(Html.fromHtml(value));
					}
				} catch (JSONException e) {
					// the author is null
					descriptionBtn.setVisibility(View.GONE);
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
		// TODO Auto-generated method stub

	}

	/**
	 * Async Task that send a request to url Gets new list view data Appends to
	 * list view
	 * */
	private class loadMoreListView extends
			AsyncTask<Void, Void, ArrayList<EsculturaItem>> {

		@Override
		protected void onPreExecute() {
			showProgress(true);
			if (currentPage == 0) {
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
						return Constants.BASE_URL + "/api/v1/node?page="
								+ currentPage
								+ "&pagesize=5&parameters[type]=autores";
					}

					@Override
					public void onResponse(String response) {
						JSONArray jsonArray;
						ArrayList<Escultura> esculturas = new ArrayList<Escultura>();
						try {
							jsonArray = new JSONArray(response);
							int max = jsonArray.length();

							if (max == 0) {
								// no more results
								currentPage--;
								button.setVisibility(View.GONE);
								showProgress(false);
								return;
							}

							myLinearLayout.removeAllViewsInLayout();
							for (int i = 0; i < max; i++) {
								JSONObject jsonObject = jsonArray
										.optJSONObject(i);

								Escultura localReg = new Escultura();

								EsculturaItem item = new EsculturaItem();
								Utils.extractFromResponseToObject(localReg,
										jsonObject);

								item.setEscultura(localReg);

								esculturas.add(localReg);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}

						for (final Escultura item : esculturas) {

							Escultura distancias2 = item;

							View v = View.inflate(AutoresActivity.this,
									R.layout.fragment_autor, null);

							addIMage(v, item);

							final TextView texto = (TextView) v
									.findViewById(R.id.tittle);
							texto.setText(distancias2.getTitle().trim());

							final int nid = item.getNid();
							texto.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View v) {
									
									AutorActivity.showHome(AutoresActivity.this, nid, item.getTitle());
								}
							});
							
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
				RestClientResistenciarte client = new RestClientResistenciarte(
						r);
				client.makeJsonRestRequest();

			} catch (Exception e) {
				return null;
			}
			return new ArrayList<EsculturaItem>();
		}

		protected void onPostExecute(ArrayList<EsculturaItem> unused) {
		}
	}

}
