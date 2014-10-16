package com.globant.labs.swipper2;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;

import com.globant.labs.swipper2.fragments.ImagePagerFragment;
import com.globant.labs.swipper2.models.Place;

public class GalleryActivity extends FragmentActivity {

	public static final String PLACE_NAME_EXTRA = "place-name-extra";
	public static final String PHOTO_INDEX_EXTRA = "photo-index-extra";
	public static final String PHOTOS_URLS_EXTRA = "photos-urls-extra";
	protected Place mPlace;
	protected PagerAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		String tag = ImagePagerFragment.class.getSimpleName();
		Fragment fr = getSupportFragmentManager().findFragmentByTag(tag);
		if (fr == null) {
			fr = new ImagePagerFragment();
			// let's recover the info we've got from place detail activity
			Bundle extras = getIntent().getExtras();
			// and throw it again to a more appropiate receiver
			Bundle bundle = new Bundle();
			bundle.putStringArray(ImagePagerFragment.PHOTOS_URLS_EXTRA,
					extras.getStringArray(PHOTOS_URLS_EXTRA));
			bundle.putInt(ImagePagerFragment.PHOTO_INDEX_EXTRA, extras.getInt(PHOTO_INDEX_EXTRA));
			bundle.putString(ImagePagerFragment.PLACE_NAME_EXTRA, extras.getString(PLACE_NAME_EXTRA));
			fr.setArguments(bundle);
			// now that we have all we want, let's dismiss current view, and use the better one
			getSupportFragmentManager().beginTransaction().replace(android.R.id.content, fr, tag)
					.commit();
		}
	}
}