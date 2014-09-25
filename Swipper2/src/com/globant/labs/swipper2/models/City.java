package com.globant.labs.swipper2.models;

import com.strongloop.android.loopback.Model;

public class City extends Model {

	private String id;
	private String Name;
	private String stateId;
	private State state;
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getName() {
		return Name;
	}
	
	public void setName(String name) {
		Name = name;
	}
	
	public String getStateId() {
		return stateId;
	}
	
	public void setStateId(String stateId) {
		this.stateId = stateId;
	}
	
	public State getState() {
		return state;
	}
	
	public void setState(State state) {
		this.state = state;
	}
	
}
