package com.globant.labs.swipper.comps;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.SearchView;
import android.util.AttributeSet;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

public class ArrayAdapterSearchView extends SearchView {

	private SearchView.SearchAutoComplete mSearchAutoComplete;

	public ArrayAdapterSearchView(Context context) {
		super(context);
		initialize();
	}

	public ArrayAdapterSearchView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize();
	}

	public void initialize() {
		mSearchAutoComplete = (SearchAutoComplete) findViewById(android.support.v7.appcompat.R.id.search_src_text);
		this.setAdapter(null);
		this.setOnItemClickListener(null);
		this.mSearchAutoComplete.setTextColor(Color.parseColor("#017d6c"));
	}

	@Override
	public void setSuggestionsAdapter(CursorAdapter adapter) {
		// don't let anyone touch this
	}

	public void setOnItemClickListener(OnItemClickListener listener) {
		mSearchAutoComplete.setOnItemClickListener(listener);
	}
	
	public void setText(String text){
		this.mSearchAutoComplete.setText(text);
	}

	public void setAdapter(ArrayAdapter<?> adapter) {
		mSearchAutoComplete.setAdapter(adapter);
	}
	
	public ListAdapter getAdapter(){
		return mSearchAutoComplete.getAdapter();
	}

}