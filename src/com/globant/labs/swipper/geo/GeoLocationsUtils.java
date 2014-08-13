package com.globant.labs.swipper.geo;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.location.Address;

import com.globant.labs.swipper.net.HTTPScraper;
import com.google.android.gms.maps.model.LatLng;

public class GeoLocationsUtils {

	public static LatLng getLocationFromString(String address)
			throws JSONException {

		HTTPScraper scrapper = HTTPScraper.getScraper();

		StringBuilder stringBuilder = new StringBuilder();

		try {
			InputStream stream = scrapper
					.fecthHtmlGet("http://maps.google.com/maps/api/geocode/json?address="
							+ address + "&ka&sensor=false");
			int b;
			while ((b = stream.read()) != -1) {
				stringBuilder.append((char) b);
			}
		} catch (Exception e) {

		}

		JSONObject jsonObject = new JSONObject();
		jsonObject = new JSONObject(stringBuilder.toString());

		double lng = ((JSONArray) jsonObject.get("results")).getJSONObject(0)
				.getJSONObject("geometry").getJSONObject("location")
				.getDouble("lng");

		double lat = ((JSONArray) jsonObject.get("results")).getJSONObject(0)
				.getJSONObject("geometry").getJSONObject("location")
				.getDouble("lat");

		return new LatLng(lat, lng);
	}
	
	public static double calculationByDistance(LatLng StartP, LatLng EndP) {
		int Radius = 6371;// radius of earth in Km
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
		double meter = valueResult % 1000;
		return meter * 1000;
	}
	

	public static List<Address> getStringFromLocation(double lat, double lng) {

		String address = String
				.format(Locale.ENGLISH,
						"http://maps.googleapis.com/maps/api/geocode/json?latlng=%1$f,%2$f&sensor=true&language="
								+ Locale.getDefault().getCountry(), lat, lng);
		
		HTTPScraper scrapper = HTTPScraper.getScraper();

		
		StringBuilder stringBuilder = new StringBuilder();

		List<Address> retList = null;

		try {
			InputStream stream =  scrapper
					.fecthHtmlGet(address);
			int b;
			while ((b = stream.read()) != -1) {
				stringBuilder.append((char) b);
			}

			JSONObject jsonObject = new JSONObject();
			jsonObject = new JSONObject(stringBuilder.toString());

			retList = new ArrayList<Address>();

			if ("OK".equalsIgnoreCase(jsonObject.getString("status"))) {
				JSONArray results = jsonObject.getJSONArray("results");
				for (int i = 0; i < results.length(); i++) {
					JSONObject result = results.getJSONObject(i);
					String indiStr = result.getString("formatted_address");
					Address addr = new Address(Locale.getDefault());
					addr.setAddressLine(0, indiStr);
					retList.add(addr);
				}
			}
		} catch (Exception e) {
			
		}

		return retList;
	}
}
