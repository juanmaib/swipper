package com.globant.labs.swipper;

import com.globant.labs.swipper.fragments.sections.EsculturaItem;
import com.globant.labs.swipper.logic.esculturas.Escultura;
import com.globant.labs.swipper.utils.Constants;
import com.globant.labs.swipper.utils.Utils;
import com.globant.labs.swipper.R;
import com.polites.android.GestureImageView;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class StandardImageProgrammatic extends ActionBarCustomActivity {

	private static Bitmap bmap;

	protected GestureImageView view;

	private View botonera;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		setContentView(R.layout.empty);
		

		final ActionBar actionBar = getSupportActionBar();
		actionBar.setIcon(R.drawable.resistenciarte_logo_color);
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		actionBar.setTitle(getIntent().getStringExtra(Constants.TITLE_ACTIVITY));
		int actionBarTitleId = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
		if (actionBarTitleId > 0) {
		    TextView title = (TextView) findViewById(actionBarTitleId);
		    if (title != null) {
		        title.setTextColor(Color.parseColor("#006355"));
		    }
		}
		
		LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT);
		view = new GestureImageView(this);

		view.setImageBitmap(bmap);
		view.setLayoutParams(params);

		ViewGroup layout = (ViewGroup) findViewById(R.id.layout);

		layout.addView(view);
		
		botonera = View.inflate(this,
				R.layout.fragment_botones, null);
		
		((ViewGroup) findViewById(R.id.layout2)).addView(botonera);
		
		final int autorId = getIntent().getIntExtra(Constants.AUTOR, 0);
		
		View textAuthor = botonera.findViewById(R.id.author);
		textAuthor.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				AutorActivity.showHome(HomeActivity.getInstance(), autorId, "");
			}
		});
		
		final int obraId = getIntent().getIntExtra(Constants.OBRA, 0);
		
		View detalles = botonera.findViewById(R.id.detalle);
		detalles.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				ObraActivity.showHome(StandardImageProgrammatic.this, obraId, getIntent().getStringExtra(Constants.TITLE_ACTIVITY));
			}
		});
		
		
		View share_btn = botonera.findViewById(R.id.share_btn);
		share_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				EsculturaItem item = new EsculturaItem();
				Escultura esc = new Escultura();
				esc.setTitle(getIntent().getStringExtra(Constants.TITLE_ACTIVITY));
				item.setEscultura(esc);
				item.setImage(getIntent().getStringExtra(Constants.URL));
				Utils.shareEscultura(HomeActivity.getInstance(), item);
			}
		});
		
		
	}

	public static void showHome(Context context, Bitmap imageToSHow, String name, int autorId, String url,  int obraId) {
		Intent intent = new Intent(context, StandardImageProgrammatic.class);
		bmap = imageToSHow;
		intent.putExtra(Constants.TITLE_ACTIVITY, name);
		intent.putExtra(Constants.AUTOR, autorId);
		intent.putExtra(Constants.OBRA, obraId);
		intent.putExtra(Constants.URL, url);
		context.startActivity(intent);
	}

}
