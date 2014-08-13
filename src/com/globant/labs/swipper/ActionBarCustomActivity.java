package com.globant.labs.swipper;

import com.globant.labs.swipper.R;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.widget.TextView;

public class ActionBarCustomActivity extends ActionBarActivity {
	
	private boolean active;
	
	public ActionBarCustomActivity() {
	    super();
	}
	
	public boolean isActive() {
		return active;
	}


	public void setActive(boolean active) {
		this.active = active;
	}


	@Override
	public void onResume(){
		active = true;
		super.onResume();
	}
	
	@Override
	public void onPause(){
		active = false;
		super.onPause();
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setIcon(R.drawable.resistenciarte_logo_color);
		int actionBarTitleId = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
		if (actionBarTitleId > 0) {
		    TextView title = (TextView) findViewById(actionBarTitleId);
		    if (title != null) {
		        title.setTextColor(Color.parseColor("#006355"));
		    }
		}
		actionBar.setDisplayHomeAsUpEnabled(true);
	}

}
