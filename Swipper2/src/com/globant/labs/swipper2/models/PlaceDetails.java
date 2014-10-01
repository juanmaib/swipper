package com.globant.labs.swipper2.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * @author bruno.demartino
 *
 */
public class PlaceDetails extends Place {

	private List<Photo> photos;

	public List<Photo> getPhotos() {
		return photos;
	}
	
	public void setPhotos(List<Map<String, ? extends Object>> photos) {
		this.photos = new ArrayList<Photo>();
		
		for(Map<String, ? extends Object> element: photos) {
			Photo p = new Photo();
			p.setHeight((Integer) element.get("height"));
			p.setWidth((Integer) element.get("width"));
			p.setPhoto_reference((String) element.get("photo_reference"));

			this.photos.add(p);
		}
	}
	
}
