package com.globant.labs.swipper2.repositories;

import com.globant.labs.swipper2.models.City;
import com.strongloop.android.loopback.ModelRepository;
import com.strongloop.android.loopback.callbacks.JsonArrayParser;
import com.strongloop.android.loopback.callbacks.ListCallback;
import com.strongloop.android.remoting.adapters.RestContract;
import com.strongloop.android.remoting.adapters.RestContractItem;

public class CityRepository extends ModelRepository<City> {
	public CityRepository() {
		super("city", "cities", City.class);
	}
	
	public void allCities(ListCallback<City> callback) {
		invokeStaticMethod(
                "allCities",
                null,
                new JsonArrayParser<City>(this, callback));  
	}
	
	@Override
	public RestContract createContract() {
		RestContract contract = super.createContract();
		
		contract.addItem(
				new RestContractItem(
						"/" + getNameForRestUrl() + "/allCities", "POST"), 
						getClassName() + ".allCities");
		
		return contract;
	}
}
