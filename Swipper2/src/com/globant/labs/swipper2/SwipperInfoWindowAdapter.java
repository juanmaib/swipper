package com.globant.labs.swipper2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;

public class SwipperInfoWindowAdapter implements InfoWindowAdapter {

	protected LayoutInflater mLayoutInflater;
	
	public SwipperInfoWindowAdapter(Context context) {
		mLayoutInflater = LayoutInflater.from(context);
	}
	
	@Override
	public View getInfoContents(Marker marker) {
		return null;
	}

	@Override
	public View getInfoWindow(Marker marker) {
		View view = mLayoutInflater.inflate(R.layout.info_window, null);
		
		TextView textView = (TextView) view.findViewById(R.id.placeName);
		textView.setText(marker.getTitle());
		
		TextView textView1 = (TextView) view.findViewById(R.id.distance);
		textView1.setText(marker.getSnippet());
		
		return view;
	}

}
