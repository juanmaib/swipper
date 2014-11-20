package com.globant.labs.swipper2.utils;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.Surface;

public class OrientationSensor implements SensorEventListener {

	public final static int SENSOR_UNAVAILABLE = -1;

	// references to other objects
	SensorManager mSensorManager;
	SensorEventListener mParent; // non-null if this class should call its
									// parent after onSensorChanged(...) and
									// onAccuracyChanged(...) notifications
	Activity mActivity; // current activity for call to
						// getWindowManager().getDefaultDisplay().getRotation()

	// raw inputs from Android sensors
	float mNormGravity; // length of raw gravity vector received in
						// onSensorChanged(...). NB: should be about 10
	float[] mNormGravityVector; // Normalised gravity vector, (i.e. length of
								// this vector is 1), which points straight
								// up into space
	float mNormMagField; // length of raw magnetic field vector received in
							// onSensorChanged(...).
	float[] mNormMagFieldValues; // Normalised magnetic field vector, (i.e.
									// length of this vector is 1)

	// accuracy specifications. SENSOR_UNAVAILABLE if unknown, otherwise
	// SensorManager.SENSOR_STATUS_UNRELIABLE, SENSOR_STATUS_ACCURACY_LOW,
	// SENSOR_STATUS_ACCURACY_MEDIUM or SENSOR_STATUS_ACCURACY_HIGH
	int mGravityAccuracy; // accuracy of gravity sensor
	int mMagneticFieldAccuracy; // accuracy of magnetic field sensor

	// values calculated once gravity and magnetic field vectors are available
	float[] mNormEastVector; // normalised cross product of raw gravity vector
								// with magnetic field values, points east
	float[] mNormNorthVector; // Normalised vector pointing to magnetic north
	boolean mOrientationOK; // set true if m_azimuth_radians and
							// m_pitch_radians have successfully been
							// calculated following a call to
							// onSensorChanged(...)
	float mAzimuthRadians; // angle of the device from magnetic north
	float mPitchRadians; // tilt angle of the device from the horizontal.
							// m_pitch_radians = 0 if the device if flat,
							// m_pitch_radians = Math.PI/2 means the device is
							// upright.
	float mPitchAxisRadians; // angle which defines the axis for the rotation
								// m_pitch_radians

	OnAzimuthChangeListener mListener;

	public OrientationSensor(SensorManager sm, SensorEventListener parent) {
		mSensorManager = sm;
		mParent = parent;
		mActivity = null;
		mNormGravityVector = mNormMagFieldValues = null;
		mNormEastVector = new float[3];
		mNormNorthVector = new float[3];
		mOrientationOK = false;
	}

	public int Register(Activity activity, int sensorSpeed, OnAzimuthChangeListener listener) {
		mActivity = activity; // current activity required for call to
								// getWindowManager().getDefaultDisplay().getRotation()
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
		return count;
	}

	public void Unregister() {
		mActivity = null;
		mNormGravityVector = mNormMagFieldValues = null;
		mOrientationOK = false;
		mSensorManager.unregisterListener(this);
	}

	@Override
	public void onSensorChanged(SensorEvent evnt) {

		int SensorType = evnt.sensor.getType();
		switch (SensorType) {
		case Sensor.TYPE_GRAVITY:
			if (mNormGravityVector == null)
				mNormGravityVector = new float[3];
			System.arraycopy(evnt.values, 0, mNormGravityVector, 0, mNormGravityVector.length);
			mNormGravity = (float) Math.sqrt(mNormGravityVector[0] * mNormGravityVector[0]
					+ mNormGravityVector[1] * mNormGravityVector[1] + mNormGravityVector[2]
					* mNormGravityVector[2]);
			for (int i = 0; i < mNormGravityVector.length; i++)
				mNormGravityVector[i] /= mNormGravity;
			break;
		case Sensor.TYPE_MAGNETIC_FIELD:
			if (mNormMagFieldValues == null)
				mNormMagFieldValues = new float[3];
			System.arraycopy(evnt.values, 0, mNormMagFieldValues, 0, mNormMagFieldValues.length);
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
			if (mNormGravity * mNormMagField * norm_East < 0.1f) { // Typical
																	// values
																	// are
																	// >
																	// 100.
				mOrientationOK = false; // device is close to free fall (or
										// in
										// space?), or close to magnetic
										// north pole.
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
				int rotation = mActivity.getWindowManager().getDefaultDisplay().getRotation();
				float screen_adjustment = 0;
				switch (rotation) {
				case Surface.ROTATION_0:
					screen_adjustment = 0;
					break;
				case Surface.ROTATION_90:
					screen_adjustment = (float) Math.PI / 2;
					break;
				case Surface.ROTATION_180:
					screen_adjustment = (float) Math.PI;
					break;
				case Surface.ROTATION_270:
					screen_adjustment = 3 * (float) Math.PI / 2;
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
				mAzimuthRadians = (float) (sin != 0 && cos != 0 ? Math.atan2(sin, cos) : 0);
				mPitchRadians = (float) Math.acos(mNormGravityVector[2]);
				sin = -mNormEastVector[1] - mNormNorthVector[0];
				cos = mNormEastVector[0] - mNormNorthVector[1];
				float aximuth_plus_two_pitch_axis_radians = (float) (sin != 0 && cos != 0 ? Math
						.atan2(sin, cos) : 0);
				mPitchAxisRadians = (float) (aximuth_plus_two_pitch_axis_radians - mAzimuthRadians) / 2;
				mAzimuthRadians += screen_adjustment;
				mPitchAxisRadians += screen_adjustment;
				mOrientationOK = true;
			}
		}
		if (mParent != null)
			mParent.onSensorChanged(evnt);

		if (mListener != null) {
			mListener.onAzimuthChanged(mAzimuthRadians);
		}

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
	}
}
