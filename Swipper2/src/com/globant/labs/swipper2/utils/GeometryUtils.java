package com.globant.labs.swipper2.utils;

import android.graphics.Point;

import com.globant.labs.swipper2.models.Place;
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

		int centerx = (int) Math.floor(size_x / 2);
		int centery = (int) Math.floor(size_y / 2);
		Point center = new Point(centerx, centery);
		return rotatePoint(point, center, azimuth);
	}

	public static Point locationToRealityPoint(Place place, LatLngBounds bounds, int size_x,
			double x_fov_multiplier, int size_y, double y_fov_multiplier, double azimuth) {
		// basically a cartesian to polar conversion. in particular, we care
		// about the angle, as that's the value that's gonna give us the
		// horizontal position in the screen of the "place"

		// let's delegate most of the hard work to our sibling method
		Point point = locationToRadarPoint(place, bounds, size_x, size_y, azimuth);
		// now let's transform the points domain from a
		// "left to right, top to bottom" system, to a "4 quadrant" one
		point.x = point.x - (size_x / 2);
		point.y = point.y - (size_y / 2);
		double angle = -getPolarAngle(point);
		angle = normalizeNegativeRadian(angle);
		// angle = angle + (Math.PI / 2);
		// normalizePositiveRadian(angle);
		// the angle domain is (-PI;PI), so let's convert that to a screen
		// related value.
		int canvas_width = (int) (size_x * x_fov_multiplier);
		int x = (int) ((canvas_width - (canvas_width * (angle / (2 * Math.PI)))) - (canvas_width / 2));
		return new Point(x, size_y / 2);
	}

	public static Point rotatePoint(Point point, Point center, double angle) {
		double sin = Math.sin(GeoUtils.getRadians(angle));
		double cos = Math.cos(GeoUtils.getRadians(angle));

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

	public static double normalizeNegativeRadian(double angle) {
		if (angle < 0)
			angle = (2 * Math.PI) + angle;
		return angle;
	}

	public static void normalizePositiveRadian(double angle) {
		if (angle > (2 * Math.PI))
			angle = angle - (2 * Math.PI);
	}
}
