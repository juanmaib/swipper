package com.globant.labs.swipper2;

import android.app.Application;

import com.globant.labs.swipper2.api.SwipperRestAdapter;
import com.globant.labs.swipper2.provider.CitiesProvider;

public class SwipperApp extends Application {

    SwipperRestAdapter adapter;
    CitiesProvider citiesProvider;

    public SwipperRestAdapter getRestAdapter() {       
    	if (adapter == null) {
            adapter = new SwipperRestAdapter(getApplicationContext());
        }
        
        return adapter;
    }
    
    public CitiesProvider getCitiesProvider() {
    	if(citiesProvider == null) {
    		citiesProvider = new CitiesProvider(getApplicationContext());
    	}
    	
    	return citiesProvider;
    }
	
}
