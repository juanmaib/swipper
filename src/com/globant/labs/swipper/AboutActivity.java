package com.globant.labs.swipper;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.globant.labs.swipper.comps.TextViewEx;

public class AboutActivity extends ActionBarCustomActivity {

	private TextViewEx txtViewEx;
	private TextViewEx txtViewEx2;
	private TextViewEx txtViewEx3;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);

		txtViewEx = (TextViewEx) findViewById(R.id.text_detailed);
		
		boolean useJustified = false;
		
		txtViewEx.setText(getString(R.string.detailed_about), useJustified);
		
	    txtViewEx2 = (TextViewEx) findViewById(R.id.text_cc);
	    txtViewEx2.setText(getString(R.string.cc_detail), useJustified);
	    
	    txtViewEx3 = (TextViewEx) findViewById(R.id.text_mv);
	    txtViewEx3.setText(getString(R.string.mv_detail), useJustified);
	    
	    TextViewEx txtViewEx4 = (TextViewEx) findViewById(R.id.text_mv_header);
	    txtViewEx4.setText(getString(R.string.disenio), false);
	    
	    TextViewEx txtViewEx5 = (TextViewEx) findViewById(R.id.text_cc_header);
	    txtViewEx5.setText(getString(R.string.idea), false);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.about, menu);
		return true;
	}
	
	public void goMariano(View v){
		goToUrl("https://www.facebook.com/marianovargasdg");
	}
	
	public void goCC(View v){
		goToUrl("http://www.codigociudadano.com.ar/");
	}
	
	public void go42mate(View v){
		goToUrl("http://www.42mate.com/");
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		} else if (id == android.R.id.home) {
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public static void showHome(Context home) {
		Intent intent = new Intent(home, AboutActivity.class);
		home.startActivity(intent);
	}
	
	private void goToUrl (String url) {
        Uri uriUrl = Uri.parse(url);
        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
        startActivity(launchBrowser);
    }

}
