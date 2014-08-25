package com.globant.labs.swipper.fragments.sections;

import java.util.ArrayList;
import java.util.Locale;

import com.globant.labs.swipper.LocatorActivity;
import com.globant.labs.swipper.fragments.LocatorFragment;
import com.globant.labs.swipper.fragments.PlaceholderFragment;
import com.globant.labs.swipper.net.ImageLoader;
import com.globant.labs.swipper.R;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class SectionsPagerAdapter extends FragmentPagerAdapter {

	public static final String ARG_SECTION_NUMBER = "section_number";

	private ArrayList<PlaceholderFragment> fragments;
	private Context context;

	private ImageLoader imageLoaderService;

	public SectionsPagerAdapter(FragmentManager fm, Context context,ImageLoader imageLoaderService) {
		super(fm);
		this.context = context;
		fragments = new ArrayList<PlaceholderFragment>(3);
		this.imageLoaderService = imageLoaderService;
	}

	@Override
	public Fragment getItem(int position) {
		PlaceholderFragment p = position < fragments.size() ? fragments
				.get(position) : null;
		if (p == null) {
			switch (position) {
			case 0:
				LocatorFragment f = (LocatorFragment) new HighlightsSectionFragment(context);
				p = f;
				break;
			case 2:
				p = new EsculturasSectionFragment(context);
				break;
			case 1:
				p = new NovedadesSectionFragment(context);
				break;
			}
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, position);
			p.setArguments(args);
			p.setImageLoaderService(imageLoaderService);
			if (fragments.size() > position){
				fragments.add(position, p);
			}
		}
		return p;
	}

	@Override
	public int getCount() {
		// Show 3 total pages.
		return 1;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		Locale l = Locale.getDefault();
		switch (position) {
		case 0:
			return context.getString(R.string.title_section1).toUpperCase(l);
		case 2:
			return context.getString(R.string.title_section2).toUpperCase(l);
		case 1:
			return context.getString(R.string.title_section3).toUpperCase(l);
		}
		return null;
	}

}
