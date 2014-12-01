package com.globant.labs.swipper2.fragments;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.globant.labs.swipper2.R;
import com.globant.labs.swipper2.widget.TouchImageView;
import com.globant.labs.swipper2.widget.ZoomableViewPager;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class ImagePagerFragment extends Fragment implements OnClickListener, OnPageChangeListener {

	public static final int INDEX = 2;
	public static final String PHOTOS_URLS_EXTRA = "photos-urls-extra";
	public static final String PHOTO_INDEX_EXTRA = "photo-index-extra";
	public static final String PLACE_NAME_EXTRA = "place-name-extra";
	private static final String LAST_PHOTO_LOCATION_PREF = "last-photo-location";
	private static final String ERROR_NO_DIRECTORY = "io-exception-directory";
	private static final String ERROR_NO_FILE = "io-exception-file";

	private int mCurrentPage;

	protected String[] mImageUrls;

	DisplayImageOptions options;

	private LinearLayout mTitleBar;
	private AlphaAnimation mTitleFade;
	private static final int TITLE_FADE_ANIMATION_DURATION = 500;
	private static final int TITLE_FADE_ANIMATION_OFFSET = 2500;

	private ZoomableViewPager mPager;
	private ImageView mShareImage;
	private Bitmap[] mBitmaps;
	private File mSharedFile;
	private String mSwipperFolderPath;
	private ProgressBar mLoadingView;

	private boolean mSharing = false;

	@Override
	public void onAttach(Activity activity) {
		// let's recover our photos' url as soon as we can
		mImageUrls = getArguments().getStringArray(PHOTOS_URLS_EXTRA);
		mSwipperFolderPath = Environment.getExternalStorageDirectory().getPath() + File.separator
				+ "Swipper";

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

		mBitmaps = new Bitmap[mImageUrls.length];
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_image_pager, container, false);
		mTitleBar = (LinearLayout) rootView.findViewById(R.id.titleBar_Gallery);
		mPager = (ZoomableViewPager) rootView.findViewById(R.id.gallery_pager);
		mPager.setAdapter(new TouchImageAdapter());
		mPager.setOnPageChangeListener(this);

		// go to the photo we've done clic long ago in the place detail activity
		mPager.setCurrentItem(getArguments().getInt(PHOTO_INDEX_EXTRA, 0));

		// as always, let's preload the other views
		mPager.setOffscreenPageLimit(3);

		// title bar
		TextView title = (TextView) rootView.findViewById(R.id.placeName_Gallery);
		title.setText(getArguments().getString(PLACE_NAME_EXTRA));

		// share image
		mShareImage = (ImageView) rootView.findViewById(R.id.shareImage_Gallery);
		mShareImage.setOnClickListener(this);

		mLoadingView = (ProgressBar) rootView.findViewById(R.id.progressBar_Gallery);

		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		onScreenTouched();
		deleteLastSharedPhoto();
	}

	private void deleteLastSharedPhoto() {
		String path = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(
				LAST_PHOTO_LOCATION_PREF, null);
		if (path != null) {
			File file = new File(path);
			if (file.exists() && file.delete()) {
				PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
						.remove(LAST_PHOTO_LOCATION_PREF).commit();
			}
		}
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
		if (!mSharing) {
			mTitleFade.cancel();
			mTitleFade.reset();
			mTitleBar.startAnimation(mTitleFade);
		}
	}

	private void hideTitleBar() {
		mTitleFade.cancel();
		mTitleBar.setVisibility(View.GONE);
		mTitleFade.reset();
	}

	private void onLoadingStart() {
		mSharing = true;
		hideTitleBar();
		mLoadingView.setVisibility(View.VISIBLE);
	}

	private void onLoadingFinish() {
		mLoadingView.setVisibility(View.GONE);
		mSharing = false;
	}

	@Override
	public void onClick(View shareImage) {
		shareImage();
	}

	private void shareImage() {
		Bitmap image = mBitmaps[mCurrentPage];
		if (image != null) {
			// as the write of the bitmap to the new file is a task that could
			// take a couple of seconds, let's do it in an AsyncTask.
			new ShareImageTask().execute(image);
		} else {
			Toast.makeText(getActivity(), R.string.toastGallery_waitForImageLoad,
					Toast.LENGTH_SHORT).show();
		}
	}

	private class ShareImageTask extends AsyncTask<Bitmap, Void, String> {

		@Override
		protected void onPreExecute() {
			onLoadingStart();
		}

		@Override
		protected String doInBackground(Bitmap... params) {
			// here are the core actions that, added up, may take a couple of
			// seconds (we don't want to block the UI thread for longer than
			// 16ms :P)
			File directory = new File(mSwipperFolderPath);
			directory.mkdirs();

			if (directory.isDirectory()) {

				String path = mSwipperFolderPath + File.separator
						+ Integer.toHexString(params[0].hashCode()) + ".jpg";
				mSharedFile = new File(path);

				ByteArrayOutputStream bytes = new ByteArrayOutputStream();
				params[0].compress(Bitmap.CompressFormat.JPEG, 100, bytes);

				try {
					mSharedFile.createNewFile();
					FileOutputStream fo = new FileOutputStream(mSharedFile);
					fo.write(bytes.toByteArray());
					fo.close();
				} catch (IOException e) {
					return ERROR_NO_FILE;
				}

				return path;
			}
			return ERROR_NO_DIRECTORY;
		}

		@Override
		protected void onPostExecute(String path) {

			// cannot use switch :(
			if (ERROR_NO_DIRECTORY == path) {
				Toast.makeText(getActivity(), R.string.toastGallery_errorCreatingDir,
						Toast.LENGTH_SHORT).show();
				return;
			}
			if (ERROR_NO_FILE == path) {
				Toast.makeText(getActivity(), R.string.toastGallery_errorCreatingFile,
						Toast.LENGTH_SHORT).show();
				return;
			}

			// just in case, preserve the last shared photo, so that we try to
			// remove it the next time the fragment resumes (to avoid filling
			// the external storage with temporary files)
			PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
					.putString(LAST_PHOTO_LOCATION_PREF, path).commit();

			// aaand do what we came to do
			Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
			shareIntent.setType("image/jpg");
			Uri uri = Uri.parse("file://" + path);
			shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
			startActivity(shareIntent);

			onLoadingFinish();
		}
	}

	@Override
	public void onPageScrollStateChanged(int state) {
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
	}

	@Override
	public void onPageSelected(int position) {
		mCurrentPage = position;
	}

	// mostly demo code below. modified some bits to adapt to our scenario
	private class TouchImageAdapter extends PagerAdapter {

		private LayoutInflater inflater;

		TouchImageAdapter() {
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
		public Object instantiateItem(ViewGroup view, final int position) {
			View imageLayout = inflater.inflate(R.layout.item_pager_image, view, false);
			TouchImageView imageView = (TouchImageView) imageLayout
					.findViewById(R.id.image_gallery);
			final ProgressBar spinner = (ProgressBar) imageLayout
					.findViewById(R.id.loading_gallery);

			ImageLoader.getInstance().displayImage(mImageUrls[position], imageView, options,
					new SimpleImageLoadingListener() {
						@Override
						public void onLoadingStarted(String imageUri, View view) {
							spinner.setVisibility(View.VISIBLE);
							// we need to cause an update to the view later on
							view.setVisibility(View.GONE);
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

							// make the view displayed to wrap the error image
							LayoutParams lp = view.getLayoutParams();
							lp.width = LayoutParams.WRAP_CONTENT;
							lp.height = LayoutParams.WRAP_CONTENT;
							// and disable the "zoom" functionality for this
							// view
							view.setEnabled(false);
							view.setVisibility(View.VISIBLE);
						}

						@Override
						public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
							spinner.setVisibility(View.GONE);
							// we need to cause an update to the view now
							view.setVisibility(View.VISIBLE);
							mBitmaps[position] = loadedImage;
							Log.i("onLoadingComplete",
									imageUri + "view width: " + view.getWidth() + ", view height: "
											+ view.getHeight() + ", bitmap width: "
											+ loadedImage.getWidth() + ", bitmap height: "
											+ loadedImage.getHeight());
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
