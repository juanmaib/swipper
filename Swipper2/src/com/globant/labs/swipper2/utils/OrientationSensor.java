package com.globant.labs.swipper2.utils;

import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.util.Log;

import com.globant.labs.swipper2.MonocleActivity;

public class OrientationSensor implements SensorEventListener {

	public static final int SENSOR_UNAVAILABLE = -1;
	public static final float ZERO_RADIANS = 0;
	public static final float QUARTER_CIRCLE_RADIAN = (float) (Math.PI / 2);
	public static final float HALF_CIRCLE_RADIAN = (float) (Math.PI);
	public static final float THREE_HALVES_CIRCLE_RADIAN = (float) ((3 * Math.PI) / 2);
	public static final float WHOLE_CIRCLE_RADIAN = (float) (2 * Math.PI);
	private static final float RADTODEG = (float) (180 / Math.PI);

	public static final int ORIENTATION_UNKNOWN = -1;
	public static final int ORIENTATION_PORTRAIT = 0;
	public static final int ORIENTATION_LANDSCAPE_LEFT = 1;
	public static final int ORIENTATION_UPSIDE_DOWN = 2;
	public static final int ORIENTATION_LANDSCAPE_RIGHT = 3;
	private int mOrientationInt = ORIENTATION_UNKNOWN;
	private int mOrientationRaw;
	private float mAngle;

	// references to other objects
	private SensorManager mSensorManager;

	// non-null if this class should call its parent after onSensorChanged(...)
	// and onAccuracyChanged(...) notifications
	private SensorEventListener mParent;

	// current activity for call to
	// getWindowManager().getDefaultDisplay().getRotation()
	// private Activity mActivity;

	// raw inputs from Android sensors length of raw gravity vector received in
	// onSensorChanged(...). NB: should be about 10
	private float mNormGravity;

	// Normalised gravity vector, (i.e. length of this vector is 1), which
	// points straight up into space
	private float[] mNormGravityVector;

	// length of raw magnetic field vector received in onSensorChanged(...).
	private float mNormMagField;

	// Normalised magnetic field vector, (i.e. length of this vector is 1)
	private float[] mNormMagFieldValues;

	// accuracy specifications. SENSOR_UNAVAILABLE if unknown, otherwise
	// SensorManager.SENSOR_STATUS_UNRELIABLE, SENSOR_STATUS_ACCURACY_LOW,
	// SENSOR_STATUS_ACCURACY_MEDIUM or SENSOR_STATUS_ACCURACY_HIGH accuracy of
	// gravity sensor
	private int mGravityAccuracy;

	// accuracy of magnetic field sensor
	private int mMagneticFieldAccuracy;

	// values calculated once gravity and magnetic field vectors are available
	// normalised cross product of raw gravity vector with magnetic field
	// values, points east
	private float[] mNormEastVector;
	// Normalised vector pointing to magnetic north
	private float[] mNormNorthVector;

	// set true if m_azimuth_radians and m_pitch_radians have successfully been
	// calculated following a call to onSensorChanged(...)
	private boolean mOrientationOK;

	// angle of the device from magnetic north
	private float mAzimuthRadians;

	// tilt angle of the device from the horizontal. m_pitch_radians = 0 if the
	// device if flat, m_pitch_radians = Math.PI/2 means the device is upright.
	private float mPitchRadians;

	// angle which defines the axis for the rotation m_pitch_radians
	private float mPitchAxisRadians;

	private OnAzimuthChangeListener mListener;

	public OrientationSensor(SensorManager sm, SensorEventListener parent) {
		mSensorManager = sm;
		mParent = parent;
		// mActivity = null;
		mNormGravityVector = mNormMagFieldValues = null;
		mNormEastVector = new float[3];
		mNormNorthVector = new float[3];
		mOrientationOK = false;
	}

	public int Register(int sensorSpeed, OnAzimuthChangeListener listener, MonocleActivity activity) {
		// current activity required for call to
		// getWindowManager().getDefaultDisplay().getRotation()
		mMonocleActivity = activity;
		mListener = listener;
		mNormGravityVector = new float[3];
		mNormMagFieldValues = new float[3];
		mOrientationOK = false;
		int count = 0;
		Sensor SensorGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
		if (SensorGravity != null) {
			mSensorManager.registerListener(this, SensorGravity, sensorSpeed);
			mGravityAccuracy = SensorManager.SENSOR_STATUS_ACCURACY_HIGH;
			count++;
		} else {
			mGravityAccuracy = SENSOR_UNAVAILABLE;
		}
		Sensor SensorMagField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		if (SensorMagField != null) {
			mSensorManager.registerListener(this, SensorMagField, sensorSpeed);
			mMagneticFieldAccuracy = SensorManager.SENSOR_STATUS_ACCURACY_HIGH;
			count++;
		} else {
			mMagneticFieldAccuracy = SENSOR_UNAVAILABLE;
		}
		Sensor SensorRotationVector = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
		if (SensorRotationVector != null) {
			Log.i("SensorRotationVector", "SensorRotationVector available");
			mSensorManager.registerListener(this, SensorRotationVector, sensorSpeed);
		} else {
			Log.i("SensorRotationVector", "SensorRotationVector not available");
		}
		return count;
	}

	public void Unregister() {
		// mActivity = null;
		mNormGravityVector = mNormMagFieldValues = null;
		mOrientationOK = false;
		mSensorManager.unregisterListener(this);
	}

	@Override
	public void onSensorChanged(SensorEvent event) {

		// try {

		updateOrientation(event);
		anotherAzimuthCalculator(event);

		int SensorType = event.sensor.getType();
		switch (SensorType) {
		case Sensor.TYPE_GRAVITY:
			if (mNormGravityVector == null)
				mNormGravityVector = new float[3];
			System.arraycopy(event.values, 0, mNormGravityVector, 0, mNormGravityVector.length);
			mNormGravity = (float) Math.sqrt(mNormGravityVector[0] * mNormGravityVector[0]
					+ mNormGravityVector[1] * mNormGravityVector[1] + mNormGravityVector[2]
					* mNormGravityVector[2]);
			for (int i = 0; i < mNormGravityVector.length; i++)
				mNormGravityVector[i] /= mNormGravity;
			break;
		case Sensor.TYPE_MAGNETIC_FIELD:
			if (mNormMagFieldValues == null)
				mNormMagFieldValues = new float[3];
			System.arraycopy(event.values, 0, mNormMagFieldValues, 0, mNormMagFieldValues.length);
			mNormMagField = (float) Math.sqrt(mNormMagFieldValues[0] * mNormMagFieldValues[0]
					+ mNormMagFieldValues[1] * mNormMagFieldValues[1] + mNormMagFieldValues[2]
					* mNormMagFieldValues[2]);
			for (int i = 0; i < mNormMagFieldValues.length; i++)
				mNormMagFieldValues[i] /= mNormMagField;
			break;
		}
		if (mNormGravityVector != null && mNormMagFieldValues != null) {
			// first calculate the horizontal vector that points due east
			float East_x = mNormMagFieldValues[1] * mNormGravityVector[2] - mNormMagFieldValues[2]
					* mNormGravityVector[1];
			float East_y = mNormMagFieldValues[2] * mNormGravityVector[0] - mNormMagFieldValues[0]
					* mNormGravityVector[2];
			float East_z = mNormMagFieldValues[0] * mNormGravityVector[1] - mNormMagFieldValues[1]
					* mNormGravityVector[0];
			float norm_East = (float) Math
					.sqrt(East_x * East_x + East_y * East_y + East_z * East_z);

			// Typical values are > 100
			if (mNormGravity * mNormMagField * norm_East < 0.1f) {
				// device is close to free fall (or in space?), or close to
				// magnetic north pole
				mOrientationOK = false;
			} else {
				mNormEastVector[0] = East_x / norm_East;
				mNormEastVector[1] = East_y / norm_East;
				mNormEastVector[2] = East_z / norm_East;

				// next calculate the horizontal vector that points due
				// north
				float M_dot_G = (mNormGravityVector[0] * mNormMagFieldValues[0]
						+ mNormGravityVector[1] * mNormMagFieldValues[1] + mNormGravityVector[2]
						* mNormMagFieldValues[2]);
				float North_x = mNormMagFieldValues[0] - mNormGravityVector[0] * M_dot_G;
				float North_y = mNormMagFieldValues[1] - mNormGravityVector[1] * M_dot_G;
				float North_z = mNormMagFieldValues[2] - mNormGravityVector[2] * M_dot_G;
				float norm_North = (float) Math.sqrt(North_x * North_x + North_y * North_y
						+ North_z * North_z);
				mNormNorthVector[0] = North_x / norm_North;
				mNormNorthVector[1] = North_y / norm_North;
				mNormNorthVector[2] = North_z / norm_North;

				// take account of screen rotation away from its natural
				// rotation
				// int rotation =
				// mActivity.getWindowManager().getDefaultDisplay().getRotation();
				int orientation = getOrientation();

				// assume no rotation by default
				float screen_adjustment = QUARTER_CIRCLE_RADIAN;

				switch (orientation) {
				case ORIENTATION_PORTRAIT:
					screen_adjustment = ZERO_RADIANS;
					break;
				case ORIENTATION_LANDSCAPE_LEFT:
					screen_adjustment = THREE_HALVES_CIRCLE_RADIAN;
					break;
				case ORIENTATION_UPSIDE_DOWN:
					screen_adjustment = HALF_CIRCLE_RADIAN;
					break;
				case ORIENTATION_LANDSCAPE_RIGHT:
					screen_adjustment = QUARTER_CIRCLE_RADIAN;
					break;
				}

				// NB: the rotation matrix has now effectively been
				// calculated.
				// It consists of the three vectors m_NormEastVector[],
				// m_NormNorthVector[] and m_NormGravityVector[]

				// calculate all the required angles from the rotation
				// matrix
				// NB: see
				// http://math.stackexchange.com/questions/381649/whats-the-best-3d-angular-co-ordinate-system-for-working-with-smartfone-apps
				float sin = mNormEastVector[1] - mNormNorthVector[0], cos = mNormEastVector[0]
						+ mNormNorthVector[1];
				// stabilize the value
				mAzimuthRadians = (float) ((mAzimuthRadians * 0.8) + ((sin != 0 && cos != 0 ? Math
						.atan2(sin, cos) : 0) * 0.2));
				mPitchRadians = (float) Math.acos(mNormGravityVector[2]);
				sin = -mNormEastVector[1] - mNormNorthVector[0];
				cos = mNormEastVector[0] - mNormNorthVector[1];
				float aximuth_plus_two_pitch_axis_radians = (float) (sin != 0 && cos != 0 ? Math
						.atan2(sin, cos) : 0);
				mPitchAxisRadians = (float) (aximuth_plus_two_pitch_axis_radians - mAzimuthRadians) / 2;
				mAzimuthRadians += screen_adjustment;
				mPitchAxisRadians += screen_adjustment;
				mOrientationOK = true;
				mAzimuthRadians = (float) ((mAzimuthRadians + 2 * WHOLE_CIRCLE_RADIAN) % WHOLE_CIRCLE_RADIAN);
			}
		}
		if (mParent != null)
			mParent.onSensorChanged(event);

		if (mListener != null) {
			// mListener.onAzimuthChanged(mAzimuthRadians);
			mListener.onAzimuthChanged(currentOrientation);
		}
		// } catch (NullPointerException npe) {
		// // Due to the amount of times this method is called, it may happen
		// // that while the parent is being destroyed, we try to reference it.
		// // In those cases, it doesn't really matter if this method finalizes
		// // it's execution, because we're exiting
		// Log.i("OrientationSensor",
		// "onSensorChanged. this exception can be safely ignored", npe);
		// }

	}

	private float[] rotationMatrix;
	private float[] outRotationMatrix;
	private float[] orientationValues;
	private float currentOrientation;
	private float eyeLevelInclination;
	private float deviceOrientation;
	private GeomagneticField mGeomagneticField;
	private MonocleActivity mMonocleActivity;

	private void anotherAzimuthCalculator(SensorEvent event) {
		switch (event.sensor.getType()) {

		case Sensor.TYPE_ROTATION_VECTOR:
			rotationMatrix = new float[16];
			outRotationMatrix = new float[16];
			SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);

			Location location = mMonocleActivity.getCurrentLocation();
			if (location != null) {
				mGeomagneticField = new GeomagneticField((float) location.getLatitude(),
						(float) location.getLongitude(), (float) location.getAltitude(),
						System.currentTimeMillis());

				orientationValues = event.values;
				// Remap coordinate System to compensate for the landscape
				// position of device
				SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_X,
						SensorManager.AXIS_Z, outRotationMatrix);
				SensorManager.getOrientation(outRotationMatrix, orientationValues);

				// Azimuth; (Degrees)
				/*
				 * currentOrientation = (float)
				 * (Math.toDegrees(orientationValues[0]) + mGeomagneticField
				 * .getDeclination());
				 */
				currentOrientation = (float) (orientationValues[0] + Math
						.toRadians(mGeomagneticField.getDeclination()));
				// Pitch; (Degrees); down is 90 , up is -90.
				eyeLevelInclination = (float) Math.toDegrees(orientationValues[1]);
				// Roll;
				deviceOrientation = (float) Math.toDegrees(orientationValues[2]);

				if (mListener != null) {
					mListener.onAnotherAzimuthChanged(currentOrientation);
				}
			}
		}

		// sendSensorBroadcast(); // Let other classes know of update to sensor
		// data.
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		int SensorType = sensor.getType();
		switch (SensorType) {
		case Sensor.TYPE_GRAVITY:
			mGravityAccuracy = accuracy;
			break;
		case Sensor.TYPE_MAGNETIC_FIELD:
			mMagneticFieldAccuracy = accuracy;
			break;
		}
		if (mParent != null)
			mParent.onAccuracyChanged(sensor, accuracy);
	}

	public interface OnAzimuthChangeListener {
		void onAzimuthChanged(double azimuthRadians);

		void onAnotherAzimuthChanged(float azimuthDegrees);
	}

	private void updateOrientation(SensorEvent event) {
		float[] values = event.values;

		float X = -values[0]; // DATA X
		float Y = -values[1]; // DATA Y
		float Z = -values[2]; // DATA Z

		// float magnitude = X * X + Y * Y;
		// Don't trust the angle if the magnitude is small compared to the y
		// value
		if ((X * X + Y * Y) * 4 >= Z * Z) {
			mAngle = (float) Math.atan2(-Y, X) * RADTODEG;
			// 810 = 2 * 360 + 90
			mOrientationRaw = (810 - (int) Math.round(mAngle)) % 360;

			// figure out actual orientation
			if (mOrientationRaw <= 45 || mOrientationRaw > 315) {
				// round to 0
				mOrientationInt = ORIENTATION_PORTRAIT;
			} else if (mOrientationRaw > 45 && mOrientationRaw <= 135) {
				// round to 90
				mOrientationInt = ORIENTATION_LANDSCAPE_LEFT;
			} else if (mOrientationRaw > 135 && mOrientationRaw <= 225) {
				// round to 180
				mOrientationInt = ORIENTATION_UPSIDE_DOWN;
			} else if (mOrientationRaw > 225 && mOrientationRaw <= 315) {
				// round to 270
				mOrientationInt = ORIENTATION_LANDSCAPE_RIGHT;
			}
		}
	}

	/** Should be used with OrientationSensor.ORIENTATION_* constants */
	public int getOrientation() {
		return mOrientationInt;
	}
}
