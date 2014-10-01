package com.globant.labs.swipper2.models;

import com.strongloop.android.loopback.Model;

public class Photo extends Model {

	private int height;
	private int width;
	private String photo_reference;

	public int getHeight() {
		return height;
	}
	
	public void setHeight(int height) {
		this.height = height;
	}
	
	public int getWidth() {
		return width;
	}
	
	public void setWidth(int width) {
		this.width = width;
	}
	
	public String getPhoto_reference() {
		return photo_reference;
	}
	
	public void setPhoto_reference(String photo_reference) {
		this.photo_reference = photo_reference;
	}
	
}
