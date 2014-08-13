package com.globant.labs.swipper.drawercomps;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.globant.labs.swipper.R;

public class CustomDrawerAdapter extends ArrayAdapter<DrawerItem> {

	private Context context;
	private List<DrawerItem> drawerItemList;
	private int layoutResID;
	
	public CustomDrawerAdapter(Context context, int layoutResourceID,
			List<DrawerItem> listItems) {
		super(context, layoutResourceID, listItems);
		this.context = context;
		this.drawerItemList = listItems;
		this.layoutResID = layoutResourceID;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub

		DrawerItemHolder drawerHolder;
		View view = convertView;

		if (view == null) {
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			drawerHolder = new DrawerItemHolder();

			view = inflater.inflate(layoutResID, parent, false);
			drawerHolder.ItemName = (TextView) view
					.findViewById(R.id.drawer_itemName);
			drawerHolder.icon = (ImageView) view.findViewById(R.id.drawer_icon);

			drawerHolder.itemLayout = (LinearLayout) view
					.findViewById(R.id.itemLayout);

			view.setTag(drawerHolder);

		} else {
			drawerHolder = (DrawerItemHolder) view.getTag();

		}

		DrawerItem dItem = (DrawerItem) this.drawerItemList.get(position);

		if (dItem.isSpinner()) {
			/*
			 * drawerHolder.headerLayout.setVisibility(LinearLayout.INVISIBLE);
			 * drawerHolder.itemLayout.setVisibility(LinearLayout.INVISIBLE);
			 * drawerHolder.spinnerLayout.setVisibility(LinearLayout.VISIBLE);
			 * 
			 * List<SpinnerItem> userList = new ArrayList<SpinnerItem>();
			 * 
			 * userList.add(new SpinnerItem(R.drawable.user1, "Ahamed Ishak",
			 * "ishak@gmail.com")); userList.add(new
			 * SpinnerItem(R.drawable.user2, "Brain Jekob",
			 * "brain.j@gmail.com"));
			 * 
			 * CustomSpinnerAdapter adapter = new CustomSpinnerAdapter(context,
			 * R.layout.custom_spinner_item, userList);
			 * 
			 * drawerHolder.spinner.setAdapter(adapter);
			 * 
			 * drawerHolder.spinner .setOnItemSelectedListener(new
			 * OnItemSelectedListener() {
			 * 
			 * @Override public void onItemSelected(AdapterView<?> arg0, View
			 * arg1, int arg2, long arg3) { // TODO Auto-generated method stub
			 * Toast.makeText(context, "User Changed",
			 * Toast.LENGTH_SHORT).show(); }
			 * 
			 * @Override public void onNothingSelected(AdapterView<?> arg0) { //
			 * TODO Auto-generated method stub
			 * 
			 * } });
			 */

		} else if (dItem.getTitle() != null) {
			drawerHolder.itemLayout.setVisibility(LinearLayout.INVISIBLE);
			Log.d("Getview", "Passed4");
		} else {

			drawerHolder.itemLayout.setVisibility(LinearLayout.VISIBLE);

			drawerHolder.icon.setImageDrawable(view.getResources().getDrawable(
					dItem.getImgResID()));
			drawerHolder.ItemName.setText(dItem.getItemName());
			Log.d("Getview", "Passed5");
		}
		
		//when we wanted icons we could add it
		//drawerHolder.icon.setVisibility(View.GONE);
		return view;
	}

	private static class DrawerItemHolder {
		TextView ItemName;
		ImageView icon;
		LinearLayout  itemLayout;
	}
}