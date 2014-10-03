package com.globant.labs.swipper2;

import android.app.Application;

import com.globant.labs.swipper2.api.SwipperRestAdapter;

public class SwipperApp extends Application {

    SwipperRestAdapter adapter;

    public SwipperRestAdapter getRestAdapter() {       
    	if (adapter == null) {
            adapter = new SwipperRestAdapter(getApplicationContext());
        }
        
        return adapter;
    }
    	
}
