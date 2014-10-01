package com.globant.labs.swipper2.repositories;

import java.util.Map;

import com.globant.labs.swipper2.models.PlaceDetails;
import com.google.common.collect.ImmutableMap;
import com.strongloop.android.loopback.ModelRepository;
import com.strongloop.android.loopback.callbacks.JsonObjectParser;
import com.strongloop.android.loopback.callbacks.ObjectCallback;
import com.strongloop.android.remoting.adapters.RestContract;
import com.strongloop.android.remoting.adapters.RestContractItem;

public class PlaceDetailsRepository extends ModelRepository<PlaceDetails> {
	public PlaceDetailsRepository() {
		super("place", "places", PlaceDetails.class);
	}
		
	public void details(String idPlace, ObjectCallback<PlaceDetails> callback) {
		
		final Map<String, String> parameters = ImmutableMap.of(
                "idPlace", String.valueOf(idPlace)
                );
		
		invokeStaticMethod(
                "details",
                parameters,
                new JsonObjectParser<PlaceDetails>(this, callback));
                
    }

	@Override
	public RestContract createContract() {
		RestContract contract = super.createContract();
				
		contract.addItem(
				new RestContractItem(
						"/" + getNameForRestUrl() + "/details", "POST"), 
						getClassName() + ".details");
		
		return contract;
	}
}
