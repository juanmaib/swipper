package com.globant.labs.swipper2.utils;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;

import com.globant.labs.swipper2.models.Place;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.maps.GeoPoint;

public class GeoUtils {

	private static final int MILLION = 1000000;
	private static final int EARTH_RADIUS = 6371009;
	private static final double RAD_CONV = 180 / Math.PI;

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

	public static LatLng displaceLatLng(LatLng latLng, double distance, double bearing) {

		// ALL ANGLES IN RADIANS

		// φ2 = asin( sin φ1 ⋅ cos δ + cos φ1 ⋅ sin δ ⋅ cos θ )
		// λ2 = λ1 + atan2( sin θ ⋅ sin δ ⋅ cos φ1, cos δ − sin φ1 ⋅ sin φ2 )
		// where φ is latitude, λ is longitude, θ is the bearing (clockwise from
		// north), δ is the angular distance d/R; d being the distance
		// travelled, R the earth’s radius

		double lat1 = getRadians(latLng.latitude);
		double lng1 = getRadians(latLng.longitude);
		double b = getRadians(bearing);
		double dR = distance / EARTH_RADIUS;

		double lat2 = Math.asin(Math.sin(lat1) * Math.cos(dR) + Math.cos(lat1) * Math.sin(dR)
				* Math.cos(b));

		double lng2 = lng1
				+ Math.atan2(Math.sin(b) * Math.sin(dR) * Math.cos(lat1),
						Math.cos(dR) - Math.sin(lat1) * Math.sin(lat2));

		return new LatLng(getDegree(lat2), getDegree(lng2));
	}

	/**
	 * Gets the location specified by a start location, a bearing, and a
	 * distance.
	 * 
	 * @param latLng
	 *            The start location.
	 * @param bearing
	 *            The bearing in degrees.
	 * @param distance
	 *            The distance in meters.
	 * @return The destination location.
	 */
	@Deprecated
	public static LatLng getDestinationLocation(LatLng latLng, double bearing, double distance) {
		double lat1 = getRadians(latLng.latitude);
		double lon1 = getRadians(latLng.longitude);
		double b = getRadians(bearing);
		double dr = distance / EARTH_RADIUS;

		double lat2 = Math.asin(Math.sin(lat1) * Math.cos(dr) + Math.cos(lat1) * Math.sin(dr)
				* Math.cos(b));
		double lon2 = lon1
				+ Math.atan2(Math.sin(b) * Math.sin(dr) * Math.cos(lat1),
						Math.cos(dr) - Math.sin(lat1) * Math.sin(lat2));
		lon2 = (lon2 + 3 * Math.PI) % (2 * Math.PI) - Math.PI;

		double lat2d = getDegree(lat2);
		double lon2d = getDegree(lon2);
		return new LatLng(lat2d, lon2d);
	}

	public static double getRadians(double angle) {
		return angle / RAD_CONV;
	}

	public static double getDegree(double rad) {
		return rad * RAD_CONV;
	}

	public static boolean isPlaceWithinScope(Place place, Location location, double distance) {
		return (GeoUtils.getDistance(new LatLng(place.getLocation().latitude,
				place.getLocation().longitude),
				new LatLng(location.getLatitude(), location.getLongitude()))) * 1000 <= distance;
	}
}
