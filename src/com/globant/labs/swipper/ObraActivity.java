package com.globant.labs.swipper;

import java.io.InputStream;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pl.polidea.coverflow.CoverFlow;
import pl.polidea.coverflow.ReflectingImageAdapter;
import pl.polidea.coverflow.ResourceImageAdapter;

import com.globant.labs.swipper.comps.TextViewEx;
import com.globant.labs.swipper.logic.esculturas.Foto;
import com.globant.labs.swipper.logic.esculturas.FotoObraAutor;
import com.globant.labs.swipper.net.IRequester;
import com.globant.labs.swipper.net.ImageLoader;
import com.globant.labs.swipper.net.RestClientResistenciarte;
import com.globant.labs.swipper.utils.Constants;
import com.globant.labs.swipper.utils.Utils;
import com.google.common.base.Function;
import com.globant.labs.swipper.R;

import android.support.v7.app.ActionBar;
import android.text.Html;
import android.util.DisplayMetrics;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

public class ObraActivity extends ActionBarCustomActivity implements IRequester {

	protected View mLoginFormView;
	protected View mLoginStatusView;

	private LinearLayout coverflowReflect;
	private static CoverFlowData bitmap;

	private ImageLoader imageLoaderService;
	private int nid;
	private CoverFlow reflectingCoverFlow;
	private int authorID;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_obra);

		final ActionBar actionBar = getSupportActionBar();
		actionBar.setIcon(R.drawable.resistenciarte_logo_color);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar
				.setTitle(getIntent().getStringExtra(Constants.TITLE_ACTIVITY));

		coverflowReflect = (LinearLayout) findViewById(R.id.coverflowReflect);

		DisplayMetrics metrics = new DisplayMetrics();
		this.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int height = metrics.heightPixels;
		int width = metrics.widthPixels;

		reflectingCoverFlow = new CoverFlow(this);
		reflectingCoverFlow.setImageHeight(height / 2);
		reflectingCoverFlow.setImageWidth(width / 2);
		reflectingCoverFlow.setWithReflection(true);
		reflectingCoverFlow.setImageReflectionRatio(0.25f);
		reflectingCoverFlow.setReflectionGap(5f);

		coverflowReflect.addView(reflectingCoverFlow);
		coverflowReflect.setVisibility(View.GONE);

		nid = getIntent().getIntExtra(Constants.NID, 0);

		if (nid == 0) {
			HomeActivity.showHome(this);
			finish();
			return;
		}

		mLoginFormView = findViewById(R.id.home_form);
		mLoginStatusView = findViewById(R.id.login_status);

		// initialize tthe image loader service
		// ImageLoader class instance
		imageLoaderService = ImageLoader.getInstance(getApplicationContext());

		showProgress(true);

		RestClientResistenciarte client = new RestClientResistenciarte(this);
		client.makeJsonRestRequest();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.obra, menu);
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

	/**
	 * Shows the progress UI and hides the login form.
	 */
	protected void showProgress(final boolean show) {
		mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
		mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
	}

	public static void showHome(Context home, int nid, String tittle) {
		Intent intent = new Intent(home, ObraActivity.class);
		intent.putExtra(Constants.NID, nid);
		intent.putExtra(Constants.TITLE_ACTIVITY, tittle);
		home.startActivity(intent);
	}

	@Override
	public String getRequestURI() {
		return Constants.BASE_URL + "/api/v1/node/" + nid;
	}

	@Override
	public void onResponse(String response) {
		try {
			JSONObject jsonObject = new JSONObject(response);

			final TextViewEx textoUbic = (TextViewEx) findViewById(R.id.description);
			setDescription(jsonObject, textoUbic);

			authorID = 0;
			try{
				authorID = jsonObject.getJSONObject("field_autor").getJSONArray("und").getJSONObject(0).getInt("target_id");
			} catch(Exception e){
				
			}
			if (!jsonObject.isNull("field_fotos")) {
				try {
					jsonObject.getJSONArray("field_fotos");
					ImageView viewImg = (ImageView) findViewById(R.id.image);
					viewImg.setBackgroundResource(R.drawable.ic_author_default);
					showProgress(false);
					return;
				} catch (Exception e) {
					// everything goes well!
				}
			} else {
				ImageView viewImg = (ImageView) findViewById(R.id.image);
				viewImg.setBackgroundResource(R.drawable.ic_author_default);
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

			showProgress(false);

			final int terminoDeCargar = fotos.size();
			final ArrayList<Bitmap> bmDat = new ArrayList<Bitmap>();
			final ArrayList<String> bmStr = new ArrayList<String>();

			bitmap = new CoverFlowData();
			bitmap.setBm(bmDat);
			bitmap.setStr(bmStr);

			for (final Foto fotoObraAutor : fotos) {

				// Image url
				final String image_url = Constants.BASE_URL
						+ "/sites/default/files/"
						+ fotoObraAutor.getUri().replaceFirst("public://", "");
				;

				Function<Bitmap, Void> afterLogin = new Function<Bitmap, Void>() {
					@Override
					public Void apply(final Bitmap bmap) {

						bmStr.add(image_url);
						bmDat.add(bmap);
						
						
						if (bmDat.size() == terminoDeCargar) {

							View p = findViewById(R.id.wait_image);
							p.setVisibility(View.GONE);
							coverflowReflect.setVisibility(View.VISIBLE);
							
							setupCoverFlow(reflectingCoverFlow, false,
									bitmap.getBm());

						}

						return null;
					}
				};

				imageLoaderService.DisplayImage(image_url, new ImageView(ObraActivity.this), afterLogin);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Setup cover flow.
	 * 
	 * @param mCoverFlow
	 *            the m cover flow
	 * @param reflect
	 *            the reflect
	 */
	private void setupCoverFlow(final CoverFlow mCoverFlow,
			final boolean reflect, ArrayList<Bitmap> bd) {
		BaseAdapter coverImageAdapter;
		if (reflect) {
			coverImageAdapter = new ReflectingImageAdapter(
					new ResourceImageAdapter(this, bd));
		} else {
			coverImageAdapter = new ResourceImageAdapter(this, bd);
		}
		mCoverFlow.setAdapter(coverImageAdapter);
		mCoverFlow.setSelection(bd.size() > 2? bd.size()/2 : 0, true);
		setupListeners(mCoverFlow);
	}

	/**
	 * Sets the up listeners.
	 * 
	 * @param mCoverFlow
	 *            the new up listeners
	 */
	private void setupListeners(final CoverFlow mCoverFlow) {
		mCoverFlow.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(final AdapterView<?> parent,
					final View view, final int position, final long id) {
				StandardImageProgrammatic.showHome(ObraActivity.this, bitmap
						.getBm().get(position), String.valueOf(getTitle()), authorID, bitmap
						.getStr().get(position), nid);
			}

		});
		mCoverFlow.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(final AdapterView<?> parent,
					final View view, final int position, final long id) {
				// textView.setText(String.valueOf(bitmap.getStr().get(position)));
			}

			@Override
			public void onNothingSelected(final AdapterView<?> parent) {
				// textView.setText("Nothing clicked!");
			}
		});
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
