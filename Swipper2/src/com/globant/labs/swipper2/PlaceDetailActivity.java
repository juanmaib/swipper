package com.globant.labs.swipper2;

import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

public class PlaceDetailActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.place_details);
		
		setTitle("Startbucks");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem itemIcon = menu.add(R.string.title_section2);
		MenuItemCompat.setShowAsAction(itemIcon, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
		itemIcon.setIcon(R.drawable.marker_food);
		return super.onCreateOptionsMenu(menu);
	}
	
}
