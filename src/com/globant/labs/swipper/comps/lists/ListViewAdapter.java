package com.globant.labs.swipper.comps.lists;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.globant.labs.swipper.R;
import com.globant.labs.swipper.fragments.sections.EsculturaItem;

public class ListViewAdapter extends BaseAdapter {
    
    private Activity activity;
    private ArrayList<HashMap<String, EsculturaItem>> data;
    private static LayoutInflater inflater=null;
    
	    
    public ListViewAdapter(Activity a, ArrayList<HashMap<String, EsculturaItem>> d) {
        activity = a;
        data=d;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return data.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }
    
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView==null)
            vi = inflater.inflate(R.layout.fragment_escultura, null);
        
        HashMap<String, EsculturaItem> item = new HashMap<String, EsculturaItem>();
        item = data.get(position);
        
        //Setting all values in listview
        //name.setText(item.get("name"));
        /*.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				Intent intent = new Intent(
						android.content.Intent.ACTION_VIEW,
						Uri.parse("http://maps.google.com/maps?&daddr="
								+ lat + "," + lon));
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.addCategory(Intent.CATEGORY_LAUNCHER);
				intent.setClassName("com.google.android.apps.maps",
						"com.google.android.maps.MapsActivity");
				startActivity(intent);
			}
		});*/
        return vi;
    }
}