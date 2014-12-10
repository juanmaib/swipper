package com.globant.labs.swipper2.utils;

import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;

import com.globant.labs.swipper2.MonocleActivity.OrientationProvider;
import com.google.android.gms.location.LocationListener;

public class OrientationSensorRotationVector
		implements
			SensorEventListener,
			LocationListener,
			OrientationProvider {

	private float[] mRotationMatrix;
	private float[] mOutRotationMatrix;
	/** [-PI, PI], with 0 = due north */
	private double mAzimuth = 0;
	/** down [PI/2 , -PI/2] up */
	private double mPitch = 0;
	private double mRoll = 0;
	private GeomagneticField mGeomagneticField;
	private OnAzimuthChangeListener mAzimuthListeners[];
	private SensorManager mSensorManager;
	private Location mLocation;

	public OrientationSensorRotationVector(SensorManager sm) {
		mSensorManager = sm;
	}

	public boolean Register(OnAzimuthChangeListener[] listeners, int sensorSpeed) {

		mAzimuthListeners = listeners;
		mRotationMatrix = new float[16];
		mOutRotationMatrix = new float[16];

		Sensor sensorRotationVector = mSensorManager
				.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
		if (sensorRotationVector != null) {
			mSensorManager.registerListener(this, sensorRotationVector,
					sensorSpeed);
			return true;
		} else {
			return false;
		}
	}

	public void Unregister() {
		mSensorManager.unregisterListener(this);
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (mLocation != null) {
			if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
				SensorManager.getRotationMatrixFromVector(mRotationMatrix,
						event.values);

				mGeomagneticField = new GeomagneticField(
						(float) mLocation.getLatitude(),
						(float) mLocation.getLongitude(),
						(float) mLocation.getAltitude(),
						System.currentTimeMillis());

				// Remap coordinate System to compensate for the landscape
				// position of device
				SensorManager.remapCoordinateSystem(mRotationMatrix,
						SensorManager.AXIS_X, SensorManager.AXIS_Z,
						mOutRotationMatrix);
				SensorManager.getOrientation(mOutRotationMatrix, event.values);

				mAzimuth = event.values[0]
						+ Math.toRadians(mGeomagneticField.getDeclination());
				mPitch = event.values[1];
				mRoll = event.values[2];

				if (mAzimuthListeners != null) {
					for (OnAzimuthChangeListener listener : mAzimuthListeners) {
						listener.onAzimuthChanged(mAzimuth);
					}
				}
			}
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	public interface OnAzimuthChangeListener {
		void onAzimuthChanged(double azimuthDegrees);
	}

	@Override
	public void onLocationChanged(Location location) {
		mLocation = location;
	}

	public double getAzimuthRadians() {
		return mAzimuth;
	}

	public double getPitchRadians() {
		return mPitch;
	}

	public double getRollRadians() {
		return mRoll;
	}

	public double getAzimuthDegrees() {
		return Math.toDegrees(mAzimuth);
	}

	public double getPitchDegrees() {
		return Math.toDegrees(mPitch);
	}

	public double getRollDegrees() {
		return Math.toDegrees(mRoll);
	}
}
