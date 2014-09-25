package com.globant.labs.swipper2.repositories;

import com.globant.labs.swipper2.models.State;
import com.strongloop.android.loopback.ModelRepository;

public class StateRepository extends ModelRepository<State> {
	public StateRepository() {
		super("state", "states", State.class);
	}
	
}
