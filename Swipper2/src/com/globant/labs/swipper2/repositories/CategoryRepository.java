package com.globant.labs.swipper2.repositories;

import com.globant.labs.swipper2.models.State;
import com.strongloop.android.loopback.ModelRepository;

public class CategoryRepository extends ModelRepository<State> {
	public CategoryRepository() {
		super("state", "states", State.class);
	}
	
}
