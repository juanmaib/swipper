package com.globant.labs.swipper2.fragments;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.globant.labs.swipper2.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class ImagePagerFragment extends Fragment {

	public static final int INDEX = 2;
	public static final String PHOTOS_URLS_EXTRA = "photos-urls-extra";
	public static final String PHOTO_INDEX_EXTRA = "photo-index-extra";
	public static final String PLACE_NAME_EXTRA = "place-name-extra";

	protected String[] mImageUrls;

	DisplayImageOptions options;

	private LinearLayout mTitleBar;
	private AlphaAnimation mTitleFade;
	private static final int TITLE_FADE_ANIMATION_DURATION = 500;
	private static final int TITLE_FADE_ANIMATION_OFFSET = 2500;

	@Override
	public void onAttach(Activity activity) {
		// let's recover our photos' url as soon as we can
		mImageUrls = getArguments().getStringArray(PHOTOS_URLS_EXTRA);

		setUpTitleAnimation();

		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		options = new DisplayImageOptions.Builder().showImageForEmptyUri(R.drawable.ic_empty)
				.showImageOnFail(R.drawable.ic_error).resetViewBeforeLoading(true)
				.cacheOnDisk(true).imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
				.bitmapConfig(Bitmap.Config.RGB_565).considerExifParams(true)
				.displayer(new FadeInBitmapDisplayer(300)).build();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_image_pager, container, false);
		mTitleBar = (LinearLayout) rootView.findViewById(R.id.titleBar_Gallery);
		ViewPager pager = (ViewPager) rootView.findViewById(R.id.pager);
		pager.setAdapter(new ImageAdapter());

		// go to the photo we've done clic long ago in the place detail activity
		pager.setCurrentItem(getArguments().getInt(PHOTO_INDEX_EXTRA, 0));

		// as always, let's preload the other views
		pager.setOffscreenPageLimit(3);

		// title bar
		TextView title = (TextView) rootView.findViewById(R.id.placeName_Gallery);
		title.setText(getArguments().getString(PLACE_NAME_EXTRA));

		return rootView;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		onScreenTouched();
	}

	private void setUpTitleAnimation() {
		mTitleFade = new AlphaAnimation(1.0f, 0.0f);
		mTitleFade.setDuration(TITLE_FADE_ANIMATION_DURATION);
		mTitleFade.setStartOffset(TITLE_FADE_ANIMATION_OFFSET);
		mTitleFade.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				mTitleBar.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				mTitleBar.setVisibility(View.GONE);
			}
		});
	}
	
	public void onScreenTouched() {
		mTitleFade.cancel();
		mTitleFade.reset();
		mTitleBar.startAnimation(mTitleFade);
	}

	// mostly demo code below. modified some bits to adapt to our scenario
	private class ImageAdapter extends PagerAdapter {

		private LayoutInflater inflater;

		ImageAdapter() {
			inflater = LayoutInflater.from(getActivity());
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		@Override
		public int getCount() {
			return mImageUrls.length;
		}

		@Override
		public Object instantiateItem(ViewGroup view, int position) {
			View imageLayout = inflater.inflate(R.layout.item_pager_image, view, false);
			assert imageLayout != null;
			ImageView imageView = (ImageView) imageLayout.findViewById(R.id.image);
			final ProgressBar spinner = (ProgressBar) imageLayout.findViewById(R.id.loading);

			ImageLoader.getInstance().displayImage(mImageUrls[position], imageView, options,
					new SimpleImageLoadingListener() {
						@Override
						public void onLoadingStarted(String imageUri, View view) {
							spinner.setVisibility(View.VISIBLE);
						}

						@Override
						public void onLoadingFailed(String imageUri, View view,
								FailReason failReason) {
							String message = null;
							switch (failReason.getType()) {
							case IO_ERROR:
								message = "Input/Output error";
								break;
							case DECODING_ERROR:
								message = "Image can't be decoded";
								break;
							case NETWORK_DENIED:
								message = "Downloads are denied";
								break;
							case OUT_OF_MEMORY:
								message = "Out Of Memory error";
								break;
							case UNKNOWN:
								message = "Unknown error";
								break;
							}
							Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();

							spinner.setVisibility(View.GONE);
						}

						@Override
						public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
							spinner.setVisibility(View.GONE);
						}
					});

			view.addView(imageLayout, 0);
			return imageLayout;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view.equals(object);
		}

		@Override
		public void restoreState(Parcelable state, ClassLoader loader) {
		}

		@Override
		public Parcelable saveState() {
			return null;
		}
	}
}