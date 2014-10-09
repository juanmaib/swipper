package com.globant.labs.swipper2.fragments;

import java.text.DecimalFormat;
import java.util.List;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import ca.weixiao.widget.InfiniteScrollListAdapter;

import com.globant.labs.swipper2.MainActivity;
import com.globant.labs.swipper2.R;
import com.globant.labs.swipper2.drawer.CategoryMapper;
import com.globant.labs.swipper2.models.Place;
import com.globant.labs.swipper2.provider.AbstractPlacesProvider.PlacesCallback;
import com.globant.labs.swipper2.provider.ListPlacesProvider;

public class PlacesAdapter extends InfiniteScrollListAdapter implements PlacesCallback {

	protected MainActivity mActivity;
	protected ListPlacesProvider mProvider;
	protected LayoutInflater mInflater;	
	protected OnClickListener mClickListener;
		
	public PlacesAdapter(ListPlacesProvider provider, MainActivity activity) {
		mActivity = activity;
		mProvider = provider;
		mInflater = LayoutInflater.from(activity);
		
		mClickListener = new OnClickListener() {
			@Override
			public void onClick(View view) {
				int position = (Integer) view.getTag();
				Place p = getItem(position);
				mActivity.displayNavigation(p);
			}
		};
		
		mProvider.setPlacesCallback(this);
		unlock();
		
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

	protected static class ViewHolder {
		public ImageView icon;
		public TextView placeName;
		public TextView placeDistance;
		public TextView placeAddress;
		public TextView placeCity;
		public ImageButton navButton;
	}
	
	public void setDataChanged() {
		notifyDataSetChanged();
	}
	
	public ListPlacesProvider getProvider() {
		return mProvider;
	}

	@Override
	protected void onScrollNext() {
		lock();
		mProvider.loadMore();
	}

	@Override
	@SuppressLint("InflateParams")
	public View getInfiniteScrollListView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if(convertView == null) {
			convertView = mInflater.inflate(R.layout.place_item_rel, null);
			holder = new ViewHolder();
			holder.icon = (ImageView) convertView.findViewById(R.id.placeIcon);
			holder.placeName = (TextView) convertView.findViewById(R.id.placeName);
			holder.placeDistance = (TextView) convertView.findViewById(R.id.placeDistance);
			holder.placeAddress = (TextView) convertView.findViewById(R.id.placeAddress);
			holder.placeCity = (TextView) convertView.findViewById(R.id.placeCity);
			holder.navButton = (ImageButton) convertView.findViewById(R.id.navButton);
			holder.navButton.setOnClickListener(mClickListener);
			holder.navButton.setFocusable(false);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		
		DecimalFormat df = new DecimalFormat("0.00"); 
		
		Place p = getItem(position);
				
		holder.icon.setBackgroundResource(CategoryMapper.getCategoryColor(p.getCategory()));
		holder.icon.setImageResource(CategoryMapper.getCategoryIcon(p.getCategory()));
		holder.placeName.setText(p.getName());
		holder.placeDistance.setText(df.format(mProvider.getDistanceTo(p))+" km");
		holder.placeAddress.setText(p.getAddress());	
		holder.placeCity.setText(
				p.getCity() + ", " +
				p.getState() + ", " +
				p.getCountry());
		holder.navButton.setTag(position);
		
		return convertView;
	}

	@Override
	public void placesUpdated(List<Place> places) {
		Log.i("SWIPPER", "placesUpdated");
		Log.i("SWIPPER", "placesLength: "+places.size());
		notifyDataSetChanged();
		notifyHasMore();
		unlock();
	}

	@Override
	public void placesError(Throwable t) {
		// TODO Auto-generated method stub
		
	}
}
