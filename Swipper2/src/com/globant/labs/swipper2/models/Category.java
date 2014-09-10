package com.globant.labs.swipper2.models;

import com.strongloop.android.loopback.Model;

/**
 * @author bruno.demartino
 *
 */
public class Category extends Model {
	private String id;
	private String Name;
	
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
}
