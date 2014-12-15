package com.globant.labs.swipper2.utils;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Build;
import android.util.Log;
import android.view.Surface;

import com.globant.labs.swipper2.MonocleActivity.OrientationProvider;
import com.globant.labs.swipper2.utils.OrientationSensorRotationVector.OnAzimuthChangeListener;

public class OrientationSensorSamsungDevice
		implements
			SensorEventListener,
			OrientationProvider {

	private static final String TAG = "GpsTestActivity";

	// Holds sensor data
	private static float[] mRotationMatrix = new float[16];

	private static float[] mValues = new float[3];

	private static float[] mTruncatedRotationVector = new float[4];

	private static float[] mRemappedMatrix = new float[16];

	private static boolean mTruncateVector = false;

	private double mOrientation = 0f;
	private double tilt = 0f;

	boolean mStarted;

	boolean mFaceTrueNorth;

	private SensorManager mSensorManager;

	public OrientationSensorSamsungDevice(SensorManager sm) {
		mSensorManager = sm;
	}

	public boolean Register(OnAzimuthChangeListener[] listeners, int sensorSpeed) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			// Use the modern rotation vector sensors
			Sensor vectorSensor = mSensorManager
					.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
			mSensorManager.registerListener(this, vectorSensor, sensorSpeed); // ~60hz
		} else {
			// Use the legacy orientation sensors
			Sensor sensor = mSensorManager
					.getDefaultSensor(Sensor.TYPE_ORIENTATION);
			if (sensor != null) {
				mSensorManager.registerListener(this, sensor, sensorSpeed);
			}
		}
		return true;
	}

	public void Unregister() {
		mSensorManager.unregisterListener(this);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {

		switch (event.sensor.getType()) {
			case Sensor.TYPE_ROTATION_VECTOR :
				// Modern rotation vector sensors
				if (!mTruncateVector) {
					try {
						SensorManager.getRotationMatrixFromVector(
								mRotationMatrix, event.values);
					} catch (IllegalArgumentException e) {
						// On some Samsung devices, an exception is thrown if
						// this vector > 4 (see #39)
						// Truncate the array, since we can deal with only the
						// first four values
						Log.e(TAG,
								"Samsung device error? Will truncate vectors - "
										+ e);
						mTruncateVector = true;
						// Do the truncation here the first time the exception
						// occurs
						getRotationMatrixFromTruncatedVector(event.values);
					}
				} else {
					// Truncate the array to avoid the exception on some devices
					// (see #39)
					getRotationMatrixFromTruncatedVector(event.values);
				}

				// int rot =
				// getWindowManager().getDefaultDisplay().getRotation();
				int rot = Surface.ROTATION_90;
				switch (rot) {
					case Surface.ROTATION_0 :
						// No orientation change, use default coordinate system
						SensorManager.getOrientation(mRotationMatrix, mValues);
						// Log.d(TAG, "Rotation-0");
						break;
					case Surface.ROTATION_90 :
						// Log.d(TAG, "Rotation-90");
						SensorManager.remapCoordinateSystem(mRotationMatrix,
								SensorManager.AXIS_Y,
								SensorManager.AXIS_MINUS_X, mRemappedMatrix);
						SensorManager.getOrientation(mRemappedMatrix, mValues);
						break;
					case Surface.ROTATION_180 :
						// Log.d(TAG, "Rotation-180");
						SensorManager.remapCoordinateSystem(mRotationMatrix,
								SensorManager.AXIS_MINUS_X,
								SensorManager.AXIS_MINUS_Y, mRemappedMatrix);
						SensorManager.getOrientation(mRemappedMatrix, mValues);
						break;
					case Surface.ROTATION_270 :
						// Log.d(TAG, "Rotation-270");
						SensorManager.remapCoordinateSystem(mRotationMatrix,
								SensorManager.AXIS_MINUS_Y,
								SensorManager.AXIS_X, mRemappedMatrix);
						SensorManager.getOrientation(mRemappedMatrix, mValues);
						break;
					default :
						// This shouldn't happen - assume default orientation
						SensorManager.getOrientation(mRotationMatrix, mValues);
						// Log.d(TAG, "Rotation-Unknown");
						break;
				}
				mOrientation = mValues[0]; // azimuth
				tilt = mValues[1];
				break;
			case Sensor.TYPE_ORIENTATION :
				// Legacy orientation sensors
				mOrientation = event.values[0];
				break;
			default :
				// A sensor we're not using, so return
				return;
		}

		mOrientation = (3 * Math.PI / 2) - mOrientation;
		// Log.i(TAG, "mValues[0]: " + mValues[0] + ", mValues[1]: " + mValues[1] + ", mValues[2]: " + mValues[2]);
	}

	private void getRotationMatrixFromTruncatedVector(float[] vector) {
		System.arraycopy(vector, 0, mTruncatedRotationVector, 0, 4);
		SensorManager.getRotationMatrixFromVector(mRotationMatrix,
				mTruncatedRotationVector);
	}

	public double getAzimuthRadians() {
		return mOrientation;
	}

	public double getAzimuthDegrees() {
		return Math.toDegrees(mOrientation);
	}

	@Override
	public void onLocationChanged(Location arg0) {

	}

}
