package com.globant.labs.swipper2.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RelativeLayout;
import ca.weixiao.widget.InfiniteScrollListView;
import ca.weixiao.widget.InfiniteScrollListView.LoadingMode;
import ca.weixiao.widget.InfiniteScrollListView.StopPosition;

import com.globant.labs.swipper2.MainActivity;
import com.globant.labs.swipper2.PlaceDetailActivity;
import com.globant.labs.swipper2.R;
import com.globant.labs.swipper2.models.Place;
import com.globant.labs.swipper2.provider.ListPlacesProvider;

public class PlacesListFragment extends Fragment implements OnItemClickListener {

	protected InfiniteScrollListView mListView;
	protected PlacesAdapter mAdapter;
	
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
		super.onActivityCreated(savedInstanceState);
		
		MainActivity activity = (MainActivity) getActivity(); 
		ListPlacesProvider provider = activity.getListMapPlacesProvider();
		mAdapter = new PlacesAdapter(provider, activity);
		mListView.setAdapter(mAdapter);
		provider.loadPlaces();
	}
	
	@Override
	@SuppressLint("InflateParams")
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.fragment_places_list,
				container, false);
		
		mListView = (InfiniteScrollListView) layout.findViewById(R.id.listView);
		mListView.setDivider(getActivity().getResources().getDrawable(R.drawable.divider));	
		mListView.setOnItemClickListener(this);
		mListView.setLoadingMode(LoadingMode.SCROLL_TO_BOTTOM);
		mListView.setStopPosition(StopPosition.REMAIN_UNCHANGED);
		mListView.setLoadingView(inflater.inflate(R.layout.list_loading_view, null));
		
		return layout;
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
		Place p = (Place) adapter.getItemAtPosition(position);
		Intent intent = new Intent(getActivity(), PlaceDetailActivity.class);
		intent.putExtra(PlaceDetailActivity.PLACE_ID_EXTRA, p.getId());
		intent.putExtra(PlaceDetailActivity.PLACE_NAME_EXTRA, p.getName());
		intent.putExtra(PlaceDetailActivity.PLACE_CATEGORY_EXTRA, p.getCategory());
		intent.putExtra(PlaceDetailActivity.PLACE_DISTANCE_EXTRA, mAdapter.getProvider().getDistanceTo(p));
		startActivity(intent);
	}
	
}
