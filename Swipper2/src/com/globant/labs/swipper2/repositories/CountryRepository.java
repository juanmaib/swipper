package com.globant.labs.swipper2.repositories;

import com.globant.labs.swipper2.models.Country;
import com.strongloop.android.loopback.ModelRepository;

public class CountryRepository extends ModelRepository<Country> {
	public CountryRepository() {
		super("country", "countries", Country.class);
	}
	
}
