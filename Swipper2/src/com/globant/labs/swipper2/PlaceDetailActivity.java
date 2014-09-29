package com.globant.labs.swipper2;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class PlaceDetailActivity extends ActionBarActivity {

	public static final String PLACE_ID_EXTRA = "place-id-extra";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.place_details);
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		Bundle extras = getIntent().getExtras();
		String placeId = extras.getString(PLACE_ID_EXTRA);
		
		Log.i("SWIPPER", "PlaceId: "+placeId);
		
		setTitle("Starbucks");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem itemIcon = menu.add(R.string.title_section2);
		MenuItemCompat.setShowAsAction(itemIcon, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
		itemIcon.setIcon(R.drawable.marker_food);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	protected void onResume() {
		changeSizeTitle();
		super.onResume();
	}
	
	private void changeSizeTitle() {
	    Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/roboto_regular.ttf");
	    int actionBarTitleId = getResources().getIdentifier("action_bar_title", "id", "android");
	    TextView titleTextView = (TextView) findViewById(actionBarTitleId);
	    titleTextView.setTypeface(typeFace);
	}

	
}
