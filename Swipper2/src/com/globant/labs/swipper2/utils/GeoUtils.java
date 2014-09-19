package com.globant.labs.swipper2.utils;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;

public class GeoUtils {

	public static JSONObject latLngToJson(LatLng latLng) {
		JSONObject jsonObject = new JSONObject();
			
		try {
			jsonObject.put("lat", latLng.latitude);
			jsonObject.put("lng", latLng.longitude);
		} catch (JSONException e) {	}
				
		return jsonObject;
	}
	
	public static LatLng latLngFromMap(Map<String, ? extends Object> map) {
		return new LatLng(
				(Double) map.get("lat"),
				(Double) map.get("lng"));
	}
	
	public static boolean isInBounds(LatLng location, LatLng northWest, LatLng southEast) {
		if(location.latitude < southEast.latitude) return false;
		if(location.latitude > northWest.latitude) return false;
		if(location.longitude < northWest.longitude) return false;
		if(location.longitude > southEast.longitude) return false;
		
		return true;
	}
	
	public static double getDistance(LatLng StartP, LatLng EndP) {
		int Radius = 6371;	// radius of earth in Km
		double lat1 = StartP.latitude;
		double lat2 = EndP.latitude;
		double lon1 = StartP.longitude;
		double lon2 = EndP.longitude;
		double dLat = Math.toRadians(lat2 - lat1);
		double dLon = Math.toRadians(lon2 - lon1);
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
				+ Math.cos(Math.toRadians(lat1))
				* Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
				* Math.sin(dLon / 2);
		double c = 2 * Math.asin(Math.sqrt(a));
		double valueResult = Radius * c;
		return valueResult;
		//double meter = valueResult % 1000;
		//return meter * 1000;
	}
}
