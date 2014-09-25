package com.globant.labs.swipper2.models;

import java.util.Map;


import com.globant.labs.swipper2.utils.GeoUtils;
import com.google.android.gms.maps.model.LatLng;
import com.strongloop.android.loopback.Model;

/**
 * @author bruno.demartino
 *
 */
public class Place extends Model {
	private String id;
	private String Name;
	private String Phone;
	private String Address;
	private String cityId;
	private String categoryId;
	private LatLng Location;
	private City City;
	
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
	
	public String getCityId() {
		return cityId;
	}

	public void setCityId(String cityId) {
		this.cityId = cityId;
	}

	public String getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
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

	public City getCity() {
		return City;
	}

	public void setCity(City city) {
		City = city;
	}
	
}
