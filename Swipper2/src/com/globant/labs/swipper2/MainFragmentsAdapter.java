package com.globant.labs.swipper2;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.globant.labs.swipper2.fragments.PlacesListFragment;
import com.globant.labs.swipper2.fragments.PlacesMapFragment;
import com.globant.labs.swipper2.provider.PlacesProvider;

public class MainFragmentsAdapter extends FragmentPagerAdapter {

	protected static final int FRAGMENT_COUNT = 2;
	public static final int MAP_FRAGMENT = 0;
	public static final int LIST_FRAGMENT = 1;
	
	protected PlacesMapFragment mMapFragment;
	protected PlacesListFragment mListFragment;
	
	public MainFragmentsAdapter(FragmentManager fm, PlacesProvider placesProvider, MainActivity activity) {
		super(fm);
		
		mMapFragment = new PlacesMapFragment();
		mListFragment = new PlacesListFragment();
		mListFragment.setListAdapter(new PlacesAdapter(placesProvider, activity));
	}

	@Override
	public Fragment getItem(int position) {
		switch(position) {
			case MAP_FRAGMENT: return mMapFragment;
			case LIST_FRAGMENT: return mListFragment;
			default: return null;
		}
	}

	@Override
	public int getCount() {
		return FRAGMENT_COUNT;
	}
	
	public PlacesMapFragment getMapFragment() {
		return mMapFragment;
	}
	
	public PlacesListFragment getListFragment() {
		return mListFragment;
	}

}
