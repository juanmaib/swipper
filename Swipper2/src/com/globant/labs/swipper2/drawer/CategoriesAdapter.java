package com.globant.labs.swipper2.drawer;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.globant.labs.swipper2.R;
import com.globant.labs.swipper2.models.Category;

public class CategoriesAdapter extends BaseAdapter {

	protected ArrayList<CategoryDisplay> mCategories;
	protected LayoutInflater mInflater;
	
	public CategoriesAdapter(Context ctx) {
		mInflater = LayoutInflater.from(ctx);
		mCategories = new ArrayList<CategoryDisplay>();
	}
	
	public void addCategory(Category cat) {
		mCategories.add(CategoryMapper.getCategoryDisplay(cat));
	}
	
	@Override
	public int getCount() {
		return mCategories.size();
	}

	@Override
	public CategoryDisplay getItem(int position) {
		return mCategories.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		ViewHolder holder;
		
		if(convertView == null) {
			convertView = mInflater.inflate(R.layout.drawer_item, null);
			holder = new ViewHolder();
			holder.categoryName = (TextView) convertView.findViewById(R.id.categoryName);
			holder.categoryColor = convertView.findViewById(R.id.categoryColor);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		
		CategoryDisplay category = mCategories.get(position);
		holder.categoryName.setText(category.getName());
		holder.categoryColor.setBackgroundResource(category.getColor());
		
		return convertView;
	}
	
	protected static class ViewHolder {
		public TextView categoryName;
		public View categoryColor;
	}

}
