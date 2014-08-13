package com.globant.labs.swipper;

import java.util.ArrayList;

import com.globant.labs.swipper.fragments.sections.EsculturaItem;
import com.globant.labs.swipper.utils.Constants;
import com.globant.labs.swipper.R;

import pl.polidea.coverflow.CoverFlow;
import pl.polidea.coverflow.ReflectingImageAdapter;
import pl.polidea.coverflow.ResourceImageAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

public class CoverFlowExample extends Activity {

	private TextView textView;
	private LinearLayout coverflowReflect;
	private TextView textViewTitle;

	private static CoverFlowData bitmap;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_cover_flow_example);
		textView = (TextView) findViewById(R.id.statusText);
		textViewTitle = (TextView) findViewById(R.id.tittle);

		textViewTitle.setText(getIntent().getStringExtra(Constants.AUTOR));

		// note resources below are taken using getIdentifier to allow importing
		// this library as library.
		/*
		 * coverflow:imageHeight="150dip" coverflow:imageReflectionRatio="0.2"
		 * coverflow:imageWidth="100dip" coverflow:reflectionGap="2dip"
		 * coverflow:withReflection="true"
		 */

		coverflowReflect = (LinearLayout) findViewById(R.id.coverflowReflect);

		DisplayMetrics metrics = new DisplayMetrics();
		this.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int height = metrics.heightPixels;
		int width = metrics.widthPixels;

		Resources r = getResources();

		CoverFlow reflectingCoverFlow = new CoverFlow(this);
		reflectingCoverFlow.setImageHeight(height / 2);
		reflectingCoverFlow.setImageWidth(width / 2);
		reflectingCoverFlow.setWithReflection(true);
		reflectingCoverFlow.setImageReflectionRatio(0.6f);
		reflectingCoverFlow.setReflectionGap(20f);

		coverflowReflect.addView(reflectingCoverFlow);

		setupCoverFlow(reflectingCoverFlow, true, bitmap.getBm());
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
		textView.setText(String.valueOf(bitmap.getStr().get(bd.size() > 2? bd.size()/2 : 0)));
		setupListeners(mCoverFlow);
	}

	public static void showHome(Context home, CoverFlowData bm, String nombre, int nid) {
		Intent intent = new Intent(home, CoverFlowExample.class);
		bitmap = bm;
		intent.putExtra(Constants.AUTOR, nombre);
		intent.putExtra(Constants.NID, nid);
		home.startActivity(intent);
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
				StandardImageProgrammatic.showHome(CoverFlowExample.this,
						bitmap.getBm().get(position), bitmap.getStr().get(position), getIntent().getIntExtra(Constants.NID,0), bitmap.getStrURL().get(position), bitmap.getObraID().get(position));
			}

		});
		mCoverFlow.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(final AdapterView<?> parent,
					final View view, final int position, final long id) {
				textView.setText(String.valueOf(bitmap.getStr().get(position)));
			}

			@Override
			public void onNothingSelected(final AdapterView<?> parent) {
				// textView.setText("Nothing clicked!");
			}
		});
	}

}