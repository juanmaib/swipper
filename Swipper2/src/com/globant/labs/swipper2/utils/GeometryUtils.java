package com.globant.labs.swipper2.utils;

import android.graphics.Point;

import com.globant.labs.swipper2.models.Place;
import com.google.android.gms.maps.model.LatLngBounds;

public class GeometryUtils {

	public static Point locationToPoint(Place place, LatLngBounds bounds, int size_x, int size_y,
			double azimuth) {

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

}
