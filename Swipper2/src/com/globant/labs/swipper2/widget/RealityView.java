package com.globant.labs.swipper2.widget;

import java.text.DecimalFormat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import com.globant.labs.swipper2.MonocleActivity;
import com.globant.labs.swipper2.PlaceDetailActivity;
import com.globant.labs.swipper2.R;
import com.globant.labs.swipper2.models.Place;
import com.globant.labs.swipper2.utils.GeoUtils;
import com.globant.labs.swipper2.utils.GeometryUtils;
import com.globant.labs.swipper2.utils.OrientationSensor.OnAzimuthChangeListener;
import com.google.android.gms.maps.model.LatLngBounds;

public class RealityView extends MonocleComponentViewGroup
		implements
			OnAzimuthChangeListener {

	private static DecimalFormat DF = new DecimalFormat("0.00");
	private static final double X_FOV_MULTIPLIER = 4;

	private boolean mIsInLayout = false;

	public RealityView(Context context) {
		this(context, null);
	}

	public RealityView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RealityView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void addPlaceView(Place place) {
		RealityPlaceView placeView = (RealityPlaceView) getInflater().inflate(
				R.layout.reality_info_window, this, false);

		// cannot use switch e.e
		/*
		 * if (place.getCategory().equals(getLodgingString())) {
		 * placeView.setBackgroundDrawable(getLodgingDrawable()); } else if
		 * (place.getCategory().equals(getTaxiString())) {
		 * placeView.setBackgroundDrawable(getTaxiDrawable()); } else if
		 * (place.getCategory().equals(getGasString())) {
		 * placeView.setBackgroundDrawable(getGasDrawable()); } else if
		 * (place.getCategory().equals(getCarRentalString())) {
		 * placeView.setBackgroundDrawable(getCarRentalDrawable()); } else if
		 * (place.getCategory().equals(getFoodString())) {
		 * placeView.setBackgroundDrawable(getFoodDrawable()); } else {
		 * Toast.makeText(getContext(), "da fuq?", Toast.LENGTH_SHORT).show(); }
		 */
		SwipperTextView placeNameView = (SwipperTextView) placeView
				.findViewById(R.id.placeName_monocle);
		placeNameView.setText(place.getName());

		SwipperTextView placeDistanceView = (SwipperTextView) placeView
				.findViewById(R.id.distance_monocle);
		placeDistanceView.setText(DF.format(GeoUtils.getDistance(
				place.getLocation(), getActivity().getCurrentLatLng())));

		placeView.setTag(place.getId());

		double distanceRatio = GeoUtils.getDistance(getActivity()
				.getCurrentLatLng(), place.getLocation())
				* 1000 / getActivity().getRadius(1);

		// ((e^(2-x*2))/2)
		float scale = (float) (1 + (Math.pow(Math.E, (2 - distanceRatio * 2)) / 4));

		placeView.setScale(scale);

		placeView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Place p = getPlaces().get(v.getTag());
				Intent intent = new Intent(getActivity(),
						PlaceDetailActivity.class);
				intent.putExtra(PlaceDetailActivity.PLACE_ID_EXTRA, p.getId());
				intent.putExtra(PlaceDetailActivity.PLACE_NAME_EXTRA,
						p.getName());
				intent.putExtra(PlaceDetailActivity.PLACE_CATEGORY_EXTRA,
						p.getCategory());
				intent.putExtra(PlaceDetailActivity.PLACE_DISTANCE_EXTRA,
						GeoUtils.getDistance(p.getLocation(), getActivity()
								.getCurrentLatLng()));
				getActivity().startActivity(intent);
			}
		});

		placeView.setOnTouchListener(new OnTouchListener() {
			private RealityPlaceView realityPlaceView;
			private boolean mPerformingClic = false;

			@Override
			public boolean onTouch(final View view, final MotionEvent event) {
				// final FrameLayout.LayoutParams par =
				// (FrameLayout.LayoutParams) v.getLayoutParams();

				realityPlaceView = (RealityPlaceView) view;

				switch (event.getAction()) {
					case MotionEvent.ACTION_MOVE : {
						mPerformingClic = false;
						realityPlaceView.setPositionY(event.getRawY());
						return true;
					}
					case MotionEvent.ACTION_UP : {
						if (mPerformingClic) {
							mPerformingClic = false;
							view.performClick();
						}
						realityPlaceView.setPositionY(event.getRawY());
						return true;
					}
					case MotionEvent.ACTION_DOWN : {
						mPerformingClic = true;
						realityPlaceView.bringToFront();
						realityPlaceView.setPositionY(event.getRawY());
						return true;
					}
					default :
						return false;
				}
			}
		});

		addView(placeView);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		// here we must tell each view where to position itself.
		// for this case, we want to put them in a way such that when the user
		// is looking with the camera straight to a place, it is positioned
		// exactly in the middle of the screen. if it's just about to become
		// visible, or just about to go out of the FOV, it should be displayed
		// halfway. if the place is out of the FOV, it shouldn't be in the
		// screen altogether.
		if (getActivity().getCurrentLocation() != null && !mIsInLayout) {
			mIsInLayout = true;
			LatLngBounds latLngBounds = getActivity().getBounds(
					2 * MonocleActivity.BASE_COEFICIENT);
			int size_x = right - left;
			int size_y = bottom - top;
			for (int i = 0; i < getChildCount(); i++) {
				RealityPlaceView v = (RealityPlaceView) getChildAt(i);
				if (v.getPositionY() == -1)
					v.setPositionY(size_y / 2);

				Point point = GeometryUtils.locationToRealityPoint(getPlaces()
						.get((String) v.getTag()), latLngBounds, size_x,
						X_FOV_MULTIPLIER, v.getPositionY(), getActivity()
								.getAzimuthDegrees());

				int halfwidth = (int) ((v.getMeasuredWidth() * v.getScale()) / 2);
				int halfheight = (int) ((v.getMeasuredHeight() * v.getScale()) / 2);
				
				ImageView arrow = (ImageView) v.findViewById(R.id.arrow_info_window_monocle);
				// float angle = (float) getActivity().getAzimuthDegrees() + 90;
				float half_canvas = size_x / 2;
				float displacement = point.x - half_canvas;
				float angle = (45 * displacement / half_canvas) - 90;
				
				rotateArrow(arrow, angle, arrow.getWidth() / 2);

				v.layout((int) (point.x - halfwidth),
						(int) (point.y - halfheight),
						(int) (point.x + halfwidth),
						(int) (point.y + halfheight));
			}
			mIsInLayout = false;
		}
	}
	
	private void rotateArrow(ImageView arrow, float angle, float center) {
		RotateAnimation animation = new RotateAnimation(angle, angle,
				center, center);
		animation.setFillAfter(true);
		arrow.startAnimation(animation);
	}

	@Override
	protected void setUpBackgroundDrawables() {
		// we do not use custom drawables for each category, so do nothing here
		/*
		 * setLodgingDrawable(getResources().getDrawable(R.drawable.
		 * reality_item_lodging));
		 * setTaxiDrawable(getResources().getDrawable(R.drawable
		 * .reality_item_taxi));
		 * setGasDrawable(getResources().getDrawable(R.drawable
		 * .reality_item_gas));
		 * setCarRentalDrawable(getResources().getDrawable(R
		 * .drawable.reality_item_car_rental));
		 * setFoodDrawable(getResources().getDrawable
		 * (R.drawable.reality_item_food));
		 */
	}

	@Override
	public void onAzimuthChanged(double azimuthDegrees) {
		// TODO Auto-generated method stub
		
	}
}
