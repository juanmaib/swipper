package com.globant.labs.swipper2.models;

import com.strongloop.android.loopback.Model;

public class GoogleReview extends Model {

	private String author_name;
    private String author_url;
    private String language;
    private int rating;
    private String text;
    private int time;
    
	public String getAuthor_name() {
		return author_name;
	}
	
	public void setAuthor_name(String author_name) {
		this.author_name = author_name;
	}
	
	public String getAuthor_url() {
		return author_url;
	}
	
	public void setAuthor_url(String author_url) {
		this.author_url = author_url;
	}
	
	public String getLanguage() {
		return language;
	}
	
	public void setLanguage(String language) {
		this.language = language;
	}
	
	public int getRating() {
		return rating;
	}
	
	public void setRating(int rating) {
		this.rating = rating;
	}
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public int getTime() {
		return time;
	}
	
	public void setTime(int time) {
		this.time = time;
	}
	
}
