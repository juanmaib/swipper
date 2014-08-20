package com.globant.labs.swipper.places;

import java.util.ArrayList;

public class Place {
	
	private String place_id;
	private String name;
	private String formatted_address;
	private double lat;
	private double lng;
	private double rating;
	private ArrayList<PlacePhoto> photos;
		
	public Place() {
		photos = new ArrayList<PlacePhoto>();
	}
	
	public String getPlace_id() {
		return place_id;
	}
	
	public void setPlace_id(String place_id) {
		this.place_id = place_id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getFormatted_address() {
		return formatted_address;
	}
	
	public void setFormatted_address(String formatted_address) {
		this.formatted_address = formatted_address;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLng() {
		return lng;
	}

	public void setLng(double lng) {
		this.lng = lng;
	}

	public double getRating() {
		return rating;
	}

	public void setRating(double rating) {
		this.rating = rating;
	}

	public ArrayList<PlacePhoto> getPhotos() {
		return photos;
	}

	public void setPhotos(ArrayList<PlacePhoto> photos) {
		this.photos = photos;
	}
	
	public void addPhoto(PlacePhoto photo) {
		photos.add(photo);
	}
	
}
