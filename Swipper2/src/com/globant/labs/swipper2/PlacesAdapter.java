package com.globant.labs.swipper2;

import java.text.DecimalFormat;

import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.globant.labs.swipper2.drawer.CategoryMapper;
import com.globant.labs.swipper2.models.Place;
import com.globant.labs.swipper2.provider.PlacesProvider;

public class PlacesAdapter extends BaseAdapter {

	protected MainActivity mActivity;
	protected PlacesProvider mProvider;
	protected LayoutInflater mInflater;
	protected OnClickListener mClickListener;

	public PlacesAdapter(PlacesProvider provider, MainActivity activity) {
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
	public View getView(int position, View convertView, ViewGroup parent) {
		
		ViewHolder holder;
		if(convertView == null) {
			convertView = mInflater.inflate(R.layout.place_item_rel, null);
			holder = new ViewHolder();
			holder.iconLayout = (LinearLayout) convertView.findViewById(R.id.placeIconLayout);
			holder.icon = (ImageView) convertView.findViewById(R.id.placeIcon);
			holder.placeName = (TextView) convertView.findViewById(R.id.placeName);
			holder.placeDistance = (TextView) convertView.findViewById(R.id.placeDistance);
			holder.placeAddress = (TextView) convertView.findViewById(R.id.placeAddress);
			holder.placeCity = (TextView) convertView.findViewById(R.id.placeCity);
			holder.navButton = (ImageButton) convertView.findViewById(R.id.navButton);
			holder.navButton.setOnClickListener(mClickListener);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		
		DecimalFormat df = new DecimalFormat("0.00"); 
		
		Place p = getItem(position);
		
		holder.iconLayout.setBackgroundResource(CategoryMapper.getCategoryColor(p.getCategoryId()));
		holder.icon.setImageResource(CategoryMapper.getCategoryIcon(p.getCategoryId()));
		holder.placeName.setText(p.getName());
		holder.placeDistance.setText(df.format(mProvider.getDistanceTo(p))+" km");
		holder.placeAddress.setText(p.getAddress());	
		holder.placeCity.setText(p.getCityId());
		holder.navButton.setTag(position);
		
		return convertView;
	}

	protected static class ViewHolder {
		public LinearLayout iconLayout;
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
}
