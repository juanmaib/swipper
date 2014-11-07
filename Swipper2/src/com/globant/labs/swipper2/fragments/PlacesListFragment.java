package com.globant.labs.swipper2.fragments;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.globant.labs.swipper2.PlaceDetailActivity;
import com.globant.labs.swipper2.R;
import com.globant.labs.swipper2.models.Place;
import com.globant.labs.swipper2.widget.SwipperTextView;

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
	public void onStart() {
		super.onStart();
		setUpEmptyListView();
	}

	private void setUpEmptyListView() {
		// Can't use a compound drawable here because we can't access
		// layout_gravity of TextView. Let's do this the old fashion

		// Set up a linear layout container
		LinearLayout emptyListLayout = new LinearLayout(getActivity());
		emptyListLayout.setOrientation(LinearLayout.VERTICAL);
		emptyListLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
		emptyListLayout.setGravity(Gravity.CENTER_HORIZONTAL);
		emptyListLayout.setPadding(0, dpToPixels(60), 0, 0);
		emptyListLayout.setBackgroundColor(getResources().getColor(android.R.color.white));
		emptyListLayout.setVisibility(View.GONE);

		// Set up the image used
		ImageView imageEmptyList = new ImageView(getActivity());
		imageEmptyList.setImageResource(R.drawable.info_icon);
		imageEmptyList.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));

		// Then the text displayed
		SwipperTextView textEmptyList = new SwipperTextView(getActivity());
		textEmptyList.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		textEmptyList.setGravity(Gravity.CENTER_HORIZONTAL);
		textEmptyList.setTextColor(getResources().getColor(R.color.button_separator));
		textEmptyList.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
		textEmptyList.setTypeface(Typeface.createFromAsset(getActivity().getAssets(),
				"fonts/roboto_light_italic.ttf"));
		textEmptyList.setPadding(0, dpToPixels(20), 0, 0);
		textEmptyList.setText(R.string.status_no_places_dual_line);

		// Add both views to the linear layout
		emptyListLayout.addView(imageEmptyList);
		emptyListLayout.addView(textEmptyList);

		// And link everything as the view to be displayed when the list is
		// empty
		((ViewGroup) getListView().getParent()).addView(emptyListLayout);
		getListView().setEmptyView(emptyListLayout);
	}

	public int dpToPixels(int dp) {
		float scale = getResources().getDisplayMetrics().density;
		int dpAsPixels = (int) (dp * scale + 0.5f);
		return dpAsPixels;
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

	/*
	 * public void setSelectionFromTop(int index, int top) {
	 * getListView().setSelectionFromTop(index, top); }
	 */
}
