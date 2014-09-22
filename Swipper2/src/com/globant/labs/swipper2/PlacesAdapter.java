package com.globant.labs.swipper2;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.globant.labs.swipper2.models.Place;
import com.globant.labs.swipper2.provider.PlacesProvider;

public class PlacesAdapter extends BaseAdapter {

	protected PlacesProvider mProvider;
	
	public PlacesAdapter(PlacesProvider provider) {
		mProvider = provider;
	}
	
	@Override
	public int getCount() {
		return mProvider.getFilteredPlacesCount();
	}

	@Override
	public Place getItem(int position) {
		return mProvider.getFilteredPlace(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		// TODO Auto-generated method stub
		return null;
	}

}
