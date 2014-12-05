package com.globant.labs.swipper2.utils;

import android.graphics.Point;

import com.globant.labs.swipper2.models.Place;
import com.google.android.gms.maps.model.LatLngBounds;

public class GeometryUtils {
	/**
	 * This translation angle (90 deg) is used because when facing on azimuth
	 * zero, places at an angle of ninety degrees should be showed up
	 */
	private static final double TRANSLATION_ANGLE = Math.PI / 2;
	private static final double TWO_PI_RADIANS = 2 * Math.PI;

	public static Point locationToRadarPoint(Place place, LatLngBounds bounds,
			int size_x, int size_y, double azimuth) {

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
		return rotatePoint(point, center, -azimuth);
		// return point;
	}

	/**
	 * Basically a cartesian to polar conversion. In particular, we care about
	 * the angle, as that's the value that's gonna give us the horizontal
	 * position in the screen of the "place"
	 */
	public static Point locationToRealityPoint(Place place,
			LatLngBounds bounds, int size_x, double x_fov_multiplier,
			int size_y, double y_fov_multiplier, double azimuthDegrees) {
		// calculate our canvas full size
		double max_canvas_x = size_x * x_fov_multiplier;
		double max_canvas_y = size_y * y_fov_multiplier;

		// aaand the center of the extended canvas
		// double max_canvas_center_x = max_canvas_x / 2;
		double max_canvas_center_y = max_canvas_y / 2;

		// aaand how much space is out of real state
		double excedent_x_per_side = (max_canvas_x - size_x) / 2;
		double excedent_y_per_side = (max_canvas_y - size_y) / 2;

		// get the distance in both axis between our position and the place
		double distance_x = place.getLocation().longitude
				- bounds.getCenter().longitude;
		double distance_y = place.getLocation().latitude
				- bounds.getCenter().latitude;

		// and the angle between 'em
		double place_angle = normalizeRadian(Math.atan2(distance_y, distance_x));
		double azimuth_angle = normalizeRadian(Math.toRadians(azimuthDegrees));

		// now mix and match all angles accordingly
		double mixed_angle = normalizeRadian(place_angle - azimuth_angle
				- TRANSLATION_ANGLE);

		// translate that from a [0,2PI] domain, to a [0,1] one
		double angleRatio = mixed_angle / TWO_PI_RADIANS;

		// and finally get where should the place be on the x axis
		double position_x_in_canvas = max_canvas_x * angleRatio;

		// do not forget to translate back the points and show only the ones
		// that fit on the screen
		return new Point((int) (position_x_in_canvas - excedent_x_per_side),
				(int) (max_canvas_center_y - excedent_y_per_side));
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
