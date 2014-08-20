package com.globant.labs.swipper.places;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class PlacesAPIClient {

	protected static final String API_KEY = "AIzaSyAyeLAbHzmMtrjOO_yVwGYs4Xg7iYbpVdM";
	protected static final String API_URL = "https://maps.googleapis.com/maps/api/place";
	protected static final String FORMAT = "json";
	
	protected static final class APIMethods {
		protected static final String TEXTSEARCH = "textsearch";
	}
	
	private static HttpClient client = new DefaultHttpClient();
		
	public List<Place> textsearch(String query, double lat, double lng) {
		
		HttpResponse response;
        String responseString = null;
        
        HttpGet request = new HttpGet(API_URL + "/"
        								+ APIMethods.TEXTSEARCH + "/"
        								+ FORMAT
        								+ "?query=" + query.replace(" ", "%20")
        								+ "&location=" + lat + "," + lng
        								+ "&radius=100"
        								+ "&key=" + API_KEY);    
    	        
        try {
            response = client.execute(request);
            StatusLine statusLine = response.getStatusLine();
            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
            	
            	ArrayList<Place> places = new ArrayList<Place>();
            	
            	HttpEntity entity = response.getEntity();
            	String responseBody = EntityUtils.toString(entity);
            	JSONObject jsonObject = new JSONObject(responseBody);
            	JSONArray jsonArray = jsonObject.getJSONArray("results");
            	
            	Log.d("SWIPPER", responseBody);
            	
            	for(int i = 0; i < jsonArray.length(); i++) {
            		JSONObject jsonPlace = jsonArray.getJSONObject(i);
            		Place place = new Place();
            		place.setPlace_id(jsonPlace.getString("place_id"));
            		place.setName(jsonPlace.getString("name"));
            		place.setFormatted_address(jsonPlace.getString("formatted_address"));
            		
            		JSONObject geometry = jsonPlace.getJSONObject("geometry");
            		JSONObject location = geometry.getJSONObject("location");
            		place.setLat(location.getDouble("lat"));
            		place.setLng(location.getDouble("lng"));
            		place.setRating(jsonPlace.getDouble("rating"));
            		
            		JSONArray photos = jsonPlace.getJSONArray("photos");
            		for(int j = 0; j < photos.length(); j++) {
            			JSONObject jsonPhoto = photos.getJSONObject(j);
            			PlacePhoto photo = new PlacePhoto();
            			photo.setWidth(jsonPhoto.getInt("width"));
            			photo.setHeight(jsonPhoto.getInt("height"));
            			photo.setPhoto_reference(jsonPhoto.getString("photo_reference"));
            			place.addPhoto(photo);
            		}
            		
            		places.add(place);
            	}
            	
            	
            	
                return places;
            }
        } catch (ClientProtocolException e) {
        	e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        return null;
	}
	
}
