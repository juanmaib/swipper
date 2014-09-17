package com.globant.labs.swipper2.api;

import android.content.Context;

import com.strongloop.android.loopback.RestAdapter;

public class SwipperRestAdapter extends RestAdapter {

	private static final String API_URL = "http://swipper-luciopoveda.rhcloud.com/api";
	
	public SwipperRestAdapter(Context context) {
		super(context, API_URL);
	}

}
