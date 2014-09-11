package com.globant.labs.swipper2;

import com.globant.labs.swipper2.api.SwipperRestAdapter;

import android.app.Application;

public class SwipperApp extends Application {

    SwipperRestAdapter adapter;

    public SwipperRestAdapter getRestAdapter() {       
    	if (adapter == null) {
            adapter = new SwipperRestAdapter(getApplicationContext());
        }
        
        return adapter;
    }
	
}
