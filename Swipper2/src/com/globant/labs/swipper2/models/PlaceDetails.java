package com.globant.labs.swipper2.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * @author bruno.demartino
 *
 */
public class PlaceDetails extends Place {

	private String Url;
	private String description;
	private List<Photo> photos;
	private ArrayList<GoogleReview> reviews;
	private ArrayList<String> schedules;

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
	
	public ArrayList<GoogleReview> getReviews() {
		return reviews;
	}
	
	public void setReviews(List<Map<String, ? extends Object>> reviews) {
		this.reviews = new ArrayList<GoogleReview>();
		
		for(Map<String, ? extends Object> element: reviews) {
			GoogleReview r = new GoogleReview();
			r.setAuthor_name((String) element.get("author_name"));
			r.setAuthor_url((String) element.get("author_url"));
			r.setLanguage((String) element.get("language"));
			r.setRating((Integer) element.get("rating"));
			r.setText((String) element.get("text"));
			r.setTime((Integer) element.get("time"));
			
			this.reviews.add(r);
		}
	}

	public ArrayList<String> getSchedules() {
		return schedules;
	}

	public void setOpening_hours(ArrayList<String> schedules) {
		this.schedules = schedules;
	}

	public String getUrl() {
		return Url;
	}

	public void setUrl(String url) {
		Url = url;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

}
