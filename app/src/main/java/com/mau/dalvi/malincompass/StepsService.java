package com.mau.dalvi.malincompass;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;

public class StepsService extends Service implements SensorEventListener{
    private static final String TAG = "StepService";
	private LocalBinder mBinder;
	private CompassActivity mListener;
	private SensorManager mSensorManager;
	private Sensor mStepDetectorSensor;
	private DatabaseHelper db;
	private boolean startTimeSet;


	public StepsService() {
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mBinder = new LocalBinder();
		mSensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
		if (mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) != null) {
			mStepDetectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
			Log.v("Step detector sensor", "Registered!");
		} else {
			Toast.makeText(this, "Step Detector Sensor not available!", Toast.LENGTH_SHORT).show();
		}
		db = new DatabaseHelper(this, null, null);
	}

	public void setListenerActivity(CompassActivity activity) {
		this.mListener = activity;
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		double timestampSeconds = event.timestamp / 1000000000;
		String date = getDate();
		if (!startTimeSet) {
			db.setStartTime(mListener.getUsername(), date, timestampSeconds);
			startTimeSet = true;
		} else {
			if (!db.checkForDate(date)) {
				Log.d(TAG, date + " does not exist");
				Log.d(TAG, "Step registered");
			db.addUserSteps(mListener.getUsername(), timestampSeconds);
			Log.d(TAG, "steps added to " + date);
			}else {
				Log.d(TAG, date + " exists!");
				Log.d(TAG, "Step registered");
				db.updateUserSteps(mListener.getUsername(), getDate());
				Log.d(TAG, "steps updated on " + date);
			}
		}
	}

	private String getDate() {
		Calendar cal = Calendar.getInstance();
		String date = cal.get(Calendar.DAY_OF_MONTH) + "/" + cal.get(Calendar.MONTH) + "/" + cal.get(Calendar.YEAR);
		Log.d(TAG, "Today's date: " + date);
		return date;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

	public void resetStartTime() {
		startTimeSet = false;
	}

	@Override
	public IBinder onBind(Intent intent) {
		mSensorManager.registerListener(this, mStepDetectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
		return mBinder;
	}


	public class LocalBinder extends Binder {
		StepsService getService() {
			return StepsService.this;
		}
	}

}
