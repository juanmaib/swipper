package com.globant.labs.swipper;

import java.io.InputStream;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.globant.labs.swipper.comps.TextViewEx;
import com.globant.labs.swipper.fragments.sections.EsculturaItem;
import com.globant.labs.swipper.logic.esculturas.Escultura;
import com.globant.labs.swipper.logic.esculturas.Foto;
import com.globant.labs.swipper.logic.esculturas.FotoObraAutor;
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
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class AutorActivity extends ActionBarCustomActivity implements
		IRequester {

	protected View mLoginFormView;
	protected View mLoginStatusView;

	private ImageLoader imageLoaderService;
	private int nid;
	private Button see_gallery;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_autor);

		final ActionBar actionBar = getSupportActionBar();
		actionBar.setIcon(R.drawable.resistenciarte_logo_color);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setTitle(getIntent().getStringExtra(Constants.TITLE_ACTIVITY));
		
		nid = getIntent().getIntExtra(Constants.NID, 0);

		if (nid == 0) {
			HomeActivity.showHome(this);
			finish();
			return;
		}
				
		see_gallery = (Button)  findViewById(R.id.see_gallery);
		see_gallery.setVisibility(View.GONE);
		
		mLoginFormView = findViewById(R.id.home_form);
		mLoginStatusView = findViewById(R.id.login_status);

		
		// initialize tthe image loader service
		// ImageLoader class instance
		imageLoaderService = ImageLoader.getInstance(getApplicationContext());

		showProgress(true);
		RestClientResistenciarte client = new RestClientResistenciarte(this);
		client.makeJsonRestRequest();
	}

	private void addImages(final String autorName) {
		IRequester r = new IRequester() {

			@Override
			public void onResponse(String response) {
				JSONObject jsonObject;
				try {
					jsonObject = new JSONObject(response);

					JSONArray fotosArrays = jsonObject.getJSONArray("data");

					ArrayList<FotoObraAutor> fotos = new ArrayList<FotoObraAutor>();
					for (int i = 0; i < fotosArrays.length(); i++) {
						FotoObraAutor foto = new FotoObraAutor();
						JSONObject jsonObjectFoto = fotosArrays
								.getJSONObject(i);
						Utils.extractFromResponseToObject(foto, jsonObjectFoto);
						fotos.add(foto);

					}

					final int terminoDeCargar = fotos.size();
					final int idx = 0;
					final ArrayList<Bitmap> bmDat = new ArrayList<Bitmap>();
					final ArrayList<String> bmStr = new ArrayList<String>();
					final ArrayList<String> bmURL = new ArrayList<String>();
					final ArrayList<Integer> bmObraID = new ArrayList<Integer>();
					
					final CoverFlowData cv = new CoverFlowData();
					cv.setBm(bmDat);
					cv.setStr(bmStr);
					cv.setStrURL(bmURL);
					cv.setObraID(bmObraID);
					
					for (final FotoObraAutor fotoObraAutor : fotos) {
						// Image url
						final String image_url = fotoObraAutor.getImage();
						
						final View v = View.inflate(AutorActivity.this,
								R.layout.escultura_loading, null);
						
						final ImageView imSex = (ImageView) v.findViewById(R.id.image_to_load);
						
						
						Function<Bitmap, Void> afterLogin = new Function<Bitmap, Void>() {
							@Override
							public Void apply(Bitmap bmap) {
								View p = v.findViewById(R.id.progress);
								p.setVisibility(View.GONE);
								View iV = v.findViewById(R.id.default_image);
								iV.setVisibility(View.GONE);
								
								bmStr.add(fotoObraAutor.getName());
								bmDat.add(bmap);
								bmObraID.add(fotoObraAutor.getId());
								bmURL.add(fotoObraAutor.getImage());
								
								if (bmDat.size() == terminoDeCargar){
									see_gallery.setVisibility(View.VISIBLE);
									see_gallery.setOnClickListener(new OnClickListener() {
										
										@Override
										public void onClick(View v) {
											CoverFlowExample.showHome(AutorActivity.this, cv, autorName, nid);
										}
									});
									
								}
								return null;
							}
						};

						imageLoaderService.DisplayImage(image_url, imSex,
								afterLogin);
					}

				} catch (Exception e) {
				}
			}

			@Override
			public String getRequestURI() {
				return Constants.BASE_URL + "/api/v1/esculturas?autor=" + nid;
			}

			@Override
			public void onResponse(InputStream result) {
			}
		};

		RestClientResistenciarte internalCall = new RestClientResistenciarte(r);
		internalCall.makeJsonRestRequest();
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	protected void showProgress(final boolean show) {
		mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
		mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
	}

	public static void showHome(Context home, int nid, String autor) {
		Intent intent = new Intent(home, AutorActivity.class);
		intent.putExtra(Constants.NID, nid);
		intent.putExtra(Constants.TITLE_ACTIVITY, autor);
		home.startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.autor, menu);
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
		return Constants.BASE_URL + "/api/v1/node/" + nid;
	}

	@Override
	public void onResponse(String response) {
		try {
			JSONObject jsonObject = new JSONObject(response);
			
			final ActionBar actionBar = getSupportActionBar();
			actionBar.setIcon(R.drawable.resistenciarte_logo_color);
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setTitle(jsonObject.getString("title").trim());
			
			addImages(jsonObject.getString("title").trim());
			
			final TextViewEx textoUbic = (TextViewEx) findViewById(R.id.description);
			setDescription(jsonObject, textoUbic);

			if (!jsonObject.isNull("field_fotos")) {
				try {
					jsonObject.getJSONArray("field_fotos");
					showProgress(false);
					return;
				} catch (Exception e) {
					// everything goes well!
				}
			} else {
				showProgress(false);
				return;
			}
			JSONArray fotosArrays = jsonObject.getJSONObject("field_fotos")
					.getJSONArray("und");
			ArrayList<Foto> fotos = new ArrayList<Foto>();
			for (int i = 0; i < fotosArrays.length(); i++) {
				Foto foto = new Foto();
				JSONObject jsonObjectFoto = fotosArrays.getJSONObject(i);
				Utils.extractFromResponseToObject(foto, jsonObjectFoto);
				fotos.add(foto);
			}

			// Image url
			String image_url = Constants.BASE_URL + "/sites/default/files/"
					+ fotos.get(0).getUri().replaceFirst("public://", "");
			;

			Function<Bitmap, Void> afterLogin = new Function<Bitmap, Void>() {
				@Override
				public Void apply(Bitmap bmap) {
					View p = findViewById(R.id.progress);
					p.setVisibility(View.GONE);
					View iV = findViewById(R.id.default_image);
					iV.setVisibility(View.GONE);
					ImageView m = (ImageView) findViewById(R.id.image);
					m.setVisibility(View.VISIBLE);
					
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

					m.setLayoutParams(new FrameLayout.LayoutParams(
							newBmapWidth, newBmapHeight));
					
					showProgress(false);
					return null;
				}
			};

			imageLoaderService.DisplayImage(image_url,
					(ImageView) findViewById(R.id.image), afterLogin);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setDescription(JSONObject jsonObject, TextViewEx descriptionBtn) {
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
				final String value = String.valueOf(
						aYs.getJSONObject(0).getString("value")).trim();
				descriptionBtn.setText(Html.fromHtml(value));
			}
		} catch (JSONException e) {
			// the author is null
			descriptionBtn.setVisibility(View.GONE);
			return;
		}

	}

	@Override
	public void onResponse(InputStream result) {
		// TODO Auto-generated method stub

	}

}
