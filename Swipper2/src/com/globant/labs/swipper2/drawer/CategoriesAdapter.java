package com.globant.labs.swipper2.drawer;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.globant.labs.swipper2.R;

public class CategoriesAdapter extends BaseAdapter {
	
	protected static final int ON_LEVEL = 1;
	protected static final int OFF_LEVEL = 0;
		
	protected LayoutInflater mInflater;
	protected ArrayList<DrawerItem> mCategories;
	
	protected final String mOnString;
	protected final String mOffString;
	
	protected int mSelectionCount;
	
	public CategoriesAdapter(Context ctx) {
		mInflater = LayoutInflater.from(ctx);
		mCategories = new ArrayList<DrawerItem>();
		
		mCategories.add(new DrawerAllItem());
		mSelectionCount = 0;
		
		mOnString = ctx.getString(R.string.on);
		mOffString = ctx.getString(R.string.off);
	}
	
	public void addCategory(String cat) {
		mCategories.add(CategoryMapper.getCategoryDisplay(cat));
	}
	
	public void addCategory(DrawerCategoryItem catDisplay) {
		mCategories.add(catDisplay);
		if(catDisplay.isChecked()) {
			mSelectionCount++;
		}
	}

	public void toggleCategory(int position) {
		DrawerItem item = getItem(position);
		item.setChecked(!item.isChecked());
		
		if(position == 0) {
			boolean status = item.isChecked();
			
			if(status) {
				mSelectionCount = mCategories.size() - 1;
			}else {
				mSelectionCount = 0;
			}
			
			for(DrawerItem cat : mCategories) {
				cat.setChecked(status);
			}
		}else {
			mSelectionCount += item.isChecked() ? 1 : -1;
			if(!item.isChecked()) {
				getItem(0).setChecked(false);
			}
		}

		notifyDataSetChanged();
	}
	
	public int getSelectionCount() {
		return mSelectionCount;
	}
	
	public void applyChanges() {
		for(DrawerItem cat : mCategories) {
			cat.applyState();
		}
	}
	
	public void resetChanges() {
		mSelectionCount = 0;
		
		mCategories.get(0).resetState();
		
		for(int i = 1; i < mCategories.size(); i++) {
			DrawerItem item = mCategories.get(i);
			item.resetState();
			mSelectionCount += item.isChecked() ? 1 : 0;			
		}
		
		notifyDataSetChanged();
	}
	
	public List<String> getCheckedIds() {
		ArrayList<String> chekedIds = new ArrayList<String>();
		for(int i = 1; i < mCategories.size(); i++) {
			DrawerCategoryItem item = (DrawerCategoryItem) mCategories.get(i);
			if(item.isChecked()) {
				chekedIds.add(item.getName());
			}
		}
		return chekedIds;	
	}
	
	@Override
	public int getCount() {
		return mCategories.size();
	}

	@Override
	public DrawerItem getItem(int position) {
		return mCategories.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	@Override
	@SuppressLint("InflateParams")
	public View getView(int position, View convertView, ViewGroup parent) {
		
		ViewHolder holder;
		
		if(convertView == null) {
			convertView = mInflater.inflate(R.layout.drawer_item, null);
			holder = new ViewHolder();
			holder.categoryColor = convertView.findViewById(R.id.categoryColor);
			holder.categoryName = (TextView) convertView.findViewById(R.id.categoryName);
			holder.categoryToggle = (ImageView) convertView.findViewById(R.id.categoryToggle);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		
		DrawerItem category = getItem(position);
		holder.categoryColor.setBackgroundResource(category.getColor());
		holder.categoryName.setText(category.getName());
		
		if(category.isChecked()) {
			holder.categoryToggle.setImageLevel(ON_LEVEL);
			holder.categoryToggle.setContentDescription(mOnString);	
		}else{
			holder.categoryToggle.setImageLevel(OFF_LEVEL);
			holder.categoryToggle.setContentDescription(mOffString);
		}
		
				
		return convertView;
	}
	
	protected static class ViewHolder {
		public View categoryColor;
		public TextView categoryName;
		public ImageView categoryToggle;
	}

}
