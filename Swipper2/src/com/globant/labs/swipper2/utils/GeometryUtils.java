package com.globant.labs.swipper2.utils;

import android.graphics.Point;

import com.globant.labs.swipper2.models.Place;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

public class GeometryUtils {

	public static Point locationToRadarPoint(Place place, LatLngBounds bounds, int size_x,
			int size_y, double azimuth) {

		double min_lat = bounds.southwest.latitude;
		double min_long = bounds.southwest.longitude;
		double max_lat = bounds.northeast.latitude;
		double max_long = bounds.northeast.longitude;

		double place_lat = place.getLocation().latitude;
		double place_long = place.getLocation().longitude;

		double delta_lat = place_lat - min_lat;
		double delta_long = place_long - min_long;
		double max_delta_lat = max_lat - min_lat;
		double max_delta_long = max_long - min_long;

		Point point = new Point();
		point.x = (int) Math.floor((size_x * delta_long) / max_delta_long);
		point.y = (int) Math.floor((size_y * delta_lat) / max_delta_lat);

		// int centerx = (int) Math.floor(size_x / 2);
		// int centery = (int) Math.floor(size_y / 2);
		// Point center = new Point(centerx, centery);
		// return rotatePoint(point, center, azimuth);
		return point;
	}

	public static Point locationToRealityPoint(Place place, LatLngBounds bounds, int size_x,
			double x_fov_multiplier, int size_y, double y_fov_multiplier, double azimuth) {
		// basically a cartesian to polar conversion. in particular, we care
		// about the angle, as that's the value that's gonna give us the
		// horizontal position in the screen of the "place"

		double newAzimuth = normalizeRadian(azimuth);

		// prepare our full canvas size
		double max_canvas_x = size_x * x_fov_multiplier;
		double max_canvas_y = size_y * y_fov_multiplier;
		double max_canvas_center_x = max_canvas_x / 2;
		double max_canvas_center_y = max_canvas_y / 2;
		double excedent_x_per_side = (max_canvas_x - size_x) / 2;
		double excedent_y_per_side = (max_canvas_y - size_y) / 2;
		LatLng centerLatLng = bounds.getCenter();

		LatLng placeLatLng = place.getLocation();
		double distance_x = placeLatLng.longitude - centerLatLng.longitude;
		double distance_y = placeLatLng.latitude - centerLatLng.latitude;
		double max_distance_x = bounds.northeast.longitude - centerLatLng.longitude;
		double max_distance_y = bounds.southwest.latitude - centerLatLng.latitude;
		double distance_ratio_x = distance_x / max_distance_x;
		double distance_ratio_y = distance_y / max_distance_y;

		// Log.i("locationToRealityPoint", "place: " + place);
		// Log.i("locationToRealityPoint", "distance_ratio_x: " +
		// distance_ratio_x);
		// Log.i("locationToRealityPoint", "distance_ratio_y: " +
		// distance_ratio_y);

		double x = max_canvas_center_x * distance_ratio_x;
		double y = max_canvas_center_y * distance_ratio_y;

		double max_angle = 2 * Math.PI;
		double final_x_ratio = normalizeRadian(-(-Math.atan2(Math.sin(y / x), Math.cos(y / x))
				+ newAzimuth))
				/ max_angle;
		double final_x = final_x_ratio * max_canvas_x;

		int newx = (int) Math.round(final_x);
		int newy = (int) Math.round(max_canvas_center_y);

		Point p = new Point(newx, newy);
		// do not forget to convert from our large canvas, to a screen real
		// state based one, based one
		Point q = new Point();
		q.x = (int) Math.round(p.x - excedent_x_per_side);
		q.y = (int) Math.round(p.y - excedent_y_per_side);
		return q;
	}

	public static Point rotatePoint(Point point, Point center, double angle) {
		double sin = Math.sin(Math.toRadians(angle));
		double cos = Math.cos(Math.toRadians(angle));

		point.x -= center.x;
		point.y -= center.y;

		double newx = point.x * cos - point.y * sin;
		double newy = point.x * sin + point.y * cos;

		point.x = (int) (newx + center.x);
		point.y = (int) (newy + center.y);

		return point;
	}

	public static double getPolarAngle(Point point) {
		return Math.atan2(point.y, point.x);
	}

	public static double normalizeRadian(double angle) {
		return ((4 * Math.PI) + angle) % (2 * Math.PI);
	}
}
