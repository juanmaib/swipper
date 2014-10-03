package com.globant.labs.swipper2;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.globant.labs.swipper2.models.GoogleReview;

public class ReviewsAdapter extends BaseAdapter {

	protected ArrayList<GoogleReview> mList;
	protected LayoutInflater mInflater;

	public ReviewsAdapter(Context context) {
		mInflater = LayoutInflater.from(context);
	}
	
	@Override
	public int getCount() {
		if(mList != null) {
			return mList.size();
		}else{
			return 0;
		}
	}

	@Override
	public GoogleReview getItem(int position) {
		return mList.get(position);
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
			convertView = mInflater.inflate(R.layout.review_item, null);
			holder = new ViewHolder();
			holder.text = (TextView) convertView.findViewById(R.id.reviewText);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		
		GoogleReview review = getItem(position);		
		holder.text.setText("\"" + review.getText() + "\"");
		
		return convertView;
	}

	protected static class ViewHolder {
		public TextView text;
	}
	
	public void setReviews(ArrayList<GoogleReview> reviews) {
		mList = reviews;
		notifyDataSetChanged();
	}
}
