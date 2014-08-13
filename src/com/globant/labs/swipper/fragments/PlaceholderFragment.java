package com.globant.labs.swipper.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.globant.labs.swipper.net.IRequester;
import com.globant.labs.swipper.net.ImageLoader;
import com.globant.labs.swipper.R;

public abstract class PlaceholderFragment extends Fragment implements
		IRequester {

	private int fragmentId;
	protected View rootView;

	protected View mLoginFormView;
	protected View mLoginStatusView;
	
	private ImageLoader imageLoaderService;

	protected String codeName;
	protected Context context;
	
	public PlaceholderFragment(Context act) {
		super();
		this.context = act;
	}

	public int getFragmentId() {
		return fragmentId;
	}

	public void setFragmentId(int fragmentId) {
		this.fragmentId = fragmentId;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (rootView == null) {
			try {
				rootView = inflater.inflate(fragmentId, container, false);
				mLoginFormView = rootView.findViewById(R.id.login_form);
				mLoginStatusView = rootView.findViewById(R.id.login_status);
				showProgress(true);
			} catch (InflateException e) {
				
			}
		} else {
			ViewGroup parent = (ViewGroup) rootView.getParent();
			if (parent != null) {
				parent.removeView(rootView);
			}
		}
		return rootView;
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	protected void showProgress(final boolean show) {
		mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
		mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
	}

	public ImageLoader getImageLoaderService() {
		return imageLoaderService;
	}

	public void setImageLoaderService(ImageLoader imageLoaderService) {
		this.imageLoaderService = imageLoaderService;
	}

}
