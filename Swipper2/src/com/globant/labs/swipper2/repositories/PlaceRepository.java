package com.globant.labs.swipper2.repositories;

import java.util.Map;

import com.globant.labs.swipper2.models.Place;
import com.globant.labs.swipper2.utils.GeoUtils;
import com.google.android.gms.maps.model.LatLng;
import com.google.common.collect.ImmutableMap;
import com.strongloop.android.loopback.ModelRepository;
import com.strongloop.android.loopback.callbacks.JsonArrayParser;
import com.strongloop.android.loopback.callbacks.ListCallback;
import com.strongloop.android.remoting.adapters.RestContract;
import com.strongloop.android.remoting.adapters.RestContractItem;

public class PlaceRepository extends ModelRepository<Place> {
	public PlaceRepository() {
		super("place", "places", Place.class);
	}
	
	public void nearBy(LatLng northWest, LatLng southEast, ListCallback<Place> callback) {
		
		final Map<String, String> parameters = ImmutableMap.of(
                "northWest", GeoUtils.latLngToJson(northWest).toString(),
                "southEast", GeoUtils.latLngToJson(southEast).toString()
                );
		
		invokeStaticMethod(
                "nearBy",
                parameters,
                new JsonArrayParser<Place>(this, callback));
                
    }
	
	public void storedBy(LatLng here, int page, ListCallback<Place> callback) {
		
		final Map<String, String> parameters = ImmutableMap.of(
                "here", GeoUtils.latLngToJson(here).toString(),
                "page", String.valueOf(page)
                );
		
		invokeStaticMethod(
                "storedBy",
                parameters,
                new JsonArrayParser<Place>(this, callback));
                
    }

	@Override
	public RestContract createContract() {
		RestContract contract = super.createContract();
		
		contract.addItem(
				new RestContractItem(
						"/" + getNameForRestUrl() + "/nearBy", "POST"), 
						getClassName() + ".nearBy");
		
		contract.addItem(
				new RestContractItem(
						"/" + getNameForRestUrl() + "/storedBy", "GET"), 
						getClassName() + ".storedBy");
		
		return contract;
	}
}
