package com.globant.labs.swipper2.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.globant.labs.swipper2.PlaceDetailActivity;
import com.globant.labs.swipper2.R;
import com.globant.labs.swipper2.models.Place;

public class PlacesListFragment extends ListFragment implements OnItemClickListener {

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
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		getListView().setDivider(getActivity().getResources().getDrawable(R.drawable.divider));
		getListView().setOnItemClickListener(this);
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
		/*
		 * int index = getListView().getFirstVisiblePosition(); View v =
		 * getListView().getChildAt(0); int top = (v == null) ? 0 : v.getTop();
		 * ((MainActivity) getActivity()).setSelectedPlacePosition(index, top);
		 */

		Place p = (Place) adapter.getItemAtPosition(position);
		Intent intent = new Intent(getActivity(), PlaceDetailActivity.class);
		intent.putExtra(PlaceDetailActivity.PLACE_ID_EXTRA, p.getId());
		intent.putExtra(PlaceDetailActivity.PLACE_NAME_EXTRA, p.getName());
		intent.putExtra(PlaceDetailActivity.PLACE_CATEGORY_EXTRA, p.getCategory());
		intent.putExtra(PlaceDetailActivity.PLACE_DISTANCE_EXTRA,
				((PlacesAdapter) getListAdapter()).getProvider().getDistanceTo(p));
		startActivity(intent);
	}

	public void setSelectionFromTop(int index, int top) {
		getListView().setSelectionFromTop(index, top);
	}

}
