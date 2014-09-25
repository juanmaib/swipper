package com.globant.labs.swipper2.fragments;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.Menu;
import android.view.MenuInflater;

import com.globant.labs.swipper2.R;

public class PlacesListFragment extends ListFragment {

	public PlacesListFragment() {
		super();
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	    inflater.inflate(R.menu.list, menu);
	    super.onCreateOptionsMenu(menu,inflater);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		getListView().setDivider(getActivity().getResources().getDrawable(R.drawable.divider));	
		super.onActivityCreated(savedInstanceState);
	}
	
}
