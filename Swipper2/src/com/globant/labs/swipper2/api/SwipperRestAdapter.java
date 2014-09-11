package com.globant.labs.swipper2.api;

import android.content.Context;

import com.strongloop.android.loopback.RestAdapter;
import com.strongloop.android.remoting.adapters.RestContractItem;

public class SwipperRestAdapter extends RestAdapter {

	private static final String API_URL = "http://swipper2-luciopoveda.rhcloud.com/api";
	
	public SwipperRestAdapter(Context context) {
		super(context, API_URL);
		
		getContract().addItem(new RestContractItem("places/nearBy", "POST"), "place.nearBy");
		
	}

}
