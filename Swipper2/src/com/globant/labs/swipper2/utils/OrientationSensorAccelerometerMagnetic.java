package com.globant.labs.swipper2.utils;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.view.Surface;

import com.globant.labs.swipper2.MonocleActivity.OrientationProvider;
import com.globant.labs.swipper2.utils.OrientationSensorRotationVector.OnAzimuthChangeListener;

public class OrientationSensorAccelerometerMagnetic
		implements
			SensorEventListener,
			OrientationProvider {

	private int mScreenRotation;

	// onSensorChanged cached values for performance, not all needed to be
	// declared here.
	private float[] mGravity = new float[3];
	private float[] mGeomagnetic = new float[3];
	private boolean mGravityUsed;
	private boolean mGeomagneticUsed;

	// View to draw a compass 2D represents North
	private float mAzimuth;
	// used to show id the device is horizontal
	private float mPitch;
	private float mRoll;
	// Magnetic north and real North
	private float mInclination;
	// low pass filter factor
	private float mAlpha = 0.09f;
	// set to true if you have a GUI implementation of compass!
	private boolean mUseLowPassFilter = false;

	private float mOrientation[];// = new float[3];

	private int i = 0;
	private int iLimit = 1;

	private SensorManager mSensorManager;

	public OrientationSensorAccelerometerMagnetic(SensorManager sensorManager) {
		mSensorManager = sensorManager;
		setScreenRotation(Surface.ROTATION_90);
	}

	public void setScreenRotation(int screenRotation) {
		mScreenRotation = screenRotation;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {

		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			mGravityUsed = false;
			// apply a low pass filter: output = alpha*input +
			// (1-alpha)*previous output;
			if (!mUseLowPassFilter) {
				mGravity[0] = mAlpha * event.values[0] + (1f - mAlpha)
						* mGravity[0];
				mGravity[1] = mAlpha * event.values[1] + (1f - mAlpha)
						* mGravity[1];
				mGravity[2] = mAlpha * event.values[2] + (1f - mAlpha)
						* mGravity[2];
			} else {
				mGravity = event.values.clone();
			}
		}
		if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			mGeomagneticUsed = false;
			// apply a low pass filter: output = alpha*input +
			// (1-alpha)*previous output;
			if (mUseLowPassFilter) {
				mGeomagnetic[0] = mAlpha * event.values[0] + (1f - mAlpha)
						* mGeomagnetic[0];
				mGeomagnetic[1] = mAlpha * event.values[1] + (1f - mAlpha)
						* mGeomagnetic[1];
				mGeomagnetic[2] = mAlpha * event.values[2] + (1f - mAlpha)
						* mGeomagnetic[2];
			} else {
				mGeomagnetic = event.values.clone();
			}

		}

		if (!mGravityUsed && !mGeomagneticUsed) {
			float R[] = new float[9];
			// X (product of Y and Z) and roughly points East
			// Y: points to Magnetic NORTH and tangential to ground
			// Z: points to SKY and perpendicular to ground
			float I[] = new float[9];

			// see axis_device.png
			boolean success = SensorManager.getRotationMatrix(R, I, mGravity,
					mGeomagnetic);

			if (success) {

				mOrientation = new float[3];

				SensorManager.getOrientation(R, mOrientation);
				mInclination = SensorManager.getInclination(I);

				mGravityUsed = true;
				mGeomagneticUsed = true;

				i++;
			}
		}

		if (i == iLimit) {
			i = 0;

			switch (mScreenRotation) {
				case Surface.ROTATION_0 :

					fixRotation0(mOrientation);
					break;

				case Surface.ROTATION_90 :

					fixRotation90(mOrientation);
					break;

				case Surface.ROTATION_180 :

					fixRotation180(mOrientation);
					break;

				case Surface.ROTATION_270 :

					fixRotation270(mOrientation);
					break;

				default :
					break;
			}

			mAzimuth = mOrientation[0];
			mPitch = mOrientation[1];
			mRoll = mOrientation[2];
		}
	}

	public static final void fixRotation0(float[] orientation) {
		orientation[1] = -orientation[1];// pitch = -pitch
	}

	public static final void fixRotation90(float[] orientation) {
		orientation[0] += Math.PI / 2f; // offset
		float tmpOldPitch = orientation[1];
		orientation[1] = -orientation[2]; // pitch = -roll
		orientation[2] = -tmpOldPitch; // roll = -pitch
	}

	public static final void fixRotation180(float[] orientation) {
		orientation[0] = (float) (orientation[0] > 0f
				? (orientation[0] - Math.PI)
				: (orientation[0] + Math.PI));// offset
		orientation[2] = -orientation[2];// roll = -roll
	}

	public static final void fixRotation270(float[] orientation) {
		orientation[0] -= Math.PI / 2;// offset
		float tmpOldPitch = orientation[1];
		orientation[1] = orientation[2]; // pitch = roll
		orientation[2] = tmpOldPitch; // roll = pitch
	}

	@Override
	public void onLocationChanged(Location location) {
	}

	@Override
	public boolean Register(OnAzimuthChangeListener[] listeners, int sensorSpeed) {
		Sensor accelerometer = mSensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		Sensor magnetometer = mSensorManager
				.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		if ((accelerometer != null) && (magnetometer != null)) {
			mSensorManager.registerListener(this, accelerometer, sensorSpeed);
			mSensorManager.registerListener(this, magnetometer, sensorSpeed);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void Unregister() {
		mSensorManager.unregisterListener(this);
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

	public double getInclinationRadians() {
		return mInclination;
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

	public double getInclinationDegrees() {
		return Math.toDegrees(mInclination);
	}
}
