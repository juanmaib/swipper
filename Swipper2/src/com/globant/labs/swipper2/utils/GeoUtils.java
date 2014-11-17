package com.globant.labs.swipper2.utils;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.maps.GeoPoint;

public class GeoUtils {

	public static int MILLION = 1000000;

	public static JSONObject latLngToJson(LatLng latLng) {
		JSONObject jsonObject = new JSONObject();

		try {
			jsonObject.put("lat", latLng.latitude);
			jsonObject.put("lng", latLng.longitude);
		} catch (JSONException e) {
		}

		return jsonObject;
	}

	public static LatLng latLngFromMap(Map<String, ? extends Object> map) {
		return new LatLng((Double) map.get("lat"), (Double) map.get("lng"));
	}

	public static double getDistance(LatLng StartP, LatLng EndP) {
		if (StartP == null || EndP == null)
			return Double.POSITIVE_INFINITY;

		int Radius = 6371; // radius of earth in Km
		double lat1 = StartP.latitude;
		double lat2 = EndP.latitude;
		double lon1 = StartP.longitude;
		double lon2 = EndP.longitude;
		double dLat = Math.toRadians(lat2 - lat1);
		double dLon = Math.toRadians(lon2 - lon1);
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1))
				* Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
		double c = 2 * Math.asin(Math.sqrt(a));
		double valueResult = Radius * c;
		return valueResult;
		// double meter = valueResult % 1000;
		// return meter * 1000;
	}

	/**
	 * Computes the bearing in degrees between two points on Earth.
	 * 
	 * @param p1
	 *            First point
	 * @param p2
	 *            Second point
	 * @return Bearing between the two points in degrees. A value of 0 means due
	 *         north.
	 */
	public static double bearing(GeoPoint p1, GeoPoint p2) {
		double lat1 = p1.getLatitudeE6() / (double) MILLION;
		double lon1 = p1.getLongitudeE6() / (double) MILLION;
		double lat2 = p2.getLatitudeE6() / (double) MILLION;
		double lon2 = p2.getLongitudeE6() / (double) MILLION;

		return bearing(lat1, lon1, lat2, lon2);
	}

	/**
	 * Computes the bearing in degrees between two points on Earth.
	 * 
	 * @param lat1
	 *            Latitude of the first point
	 * @param lon1
	 *            Longitude of the first point
	 * @param lat2
	 *            Latitude of the second point
	 * @param lon2
	 *            Longitude of the second point
	 * @return Bearing between the two points in degrees. A value of 0 means due
	 *         north.
	 */
	public static double bearing(double lat1, double lon1, double lat2, double lon2) {
		double lat1Rad = Math.toRadians(lat1);
		double lat2Rad = Math.toRadians(lat2);
		double deltaLonRad = Math.toRadians(lon2 - lon1);

		double y = Math.sin(deltaLonRad) * Math.cos(lat2Rad);
		double x = Math.cos(lat1Rad) * Math.sin(lat2Rad) - Math.sin(lat1Rad) * Math.cos(lat2Rad)
				* Math.cos(deltaLonRad);
		return radToBearing(Math.atan2(y, x));
	}

	/**
	 * Converts an angle in radians to degrees
	 */
	public static double radToBearing(double rad) {
		return (Math.toDegrees(rad) + 360) % 360;
	}
}
