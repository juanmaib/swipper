package com.globant.labs.swipper2.models;

import java.util.Map;


import com.globant.labs.swipper2.utils.GeoUtils;
import com.google.android.gms.maps.model.LatLng;
import com.strongloop.android.loopback.Model;

/**
 * @author bruno.demartino
 *
 */
/**
 * @author bruno.demartino
 *
 */
public class Place extends Model {
	private String id;
	private String Name;
	private String Phone;
	private String Address;
	private String Category;
	private LatLng Location;
	private String City;
	private String State;
	private String Country;
	private int LoadOrder;
	
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
	
	public String getPhone() {
		return Phone;
	}
	
	public void setPhone(String phone) {
		Phone = phone;
	}
	
	public String getAddress() {
		return Address;
	}
	
	public void setAddress(String address) {
		Address = address;
	}
		
	public LatLng getLocation() {
		return Location;
	}
	
	public void setLocation(LatLng location) {
		Location = location;
	}
	
	public void setLocation(Map<String, ? extends Object> params) {
		Location = GeoUtils.latLngFromMap(params);
	}

	public String getCategory() {
		return Category;
	}

	public void setCategory(String category) {
		Category = category;
	}

	public String getCity() {
		return City;
	}

	public void setCity(String city) {
		City = city;
	}

	public String getState() {
		return State;
	}

	public void setState(String state) {
		State = state;
	}

	public String getCountry() {
		return Country;
	}
	
	public void setLoadOrder(int loadOrder) {
		LoadOrder = loadOrder;
	}
	
	public int getLoadOrder() {
		return LoadOrder;
	}

	public void setCountry(String country) {
		Country = country;
	}
	
}
