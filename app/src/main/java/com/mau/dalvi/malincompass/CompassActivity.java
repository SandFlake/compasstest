package com.mau.dalvi.malincompass;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Map;
import java.util.Set;

public class CompassActivity extends AppCompatActivity implements SensorEventListener {
    private static final String TAG = "MainActivity";
    private DatabaseHelper db;
    private SensorManager sensorManager;
    private Sensor mAccelerometerSensor, mMagnetometerSensor, mOrientationSensor, mStepCounterSensor;
    public boolean mBound;
    private ImageView compass;
    private TextView tvacc, tvmag, tvori, tvcounter, tvuser, tvStepsPerSecond;
    private Button btnHistory, btnReset;
    private long lastUpdateTime;
    private MyServiceConnection connection;
    public StepsService mService;
    private float current = 0f;
    private float mCurrentDegree = 0;
    private float[] mRotationMatrix, mLastAccelerometer, mLastMagnetometer, mOrientation;

    private boolean isSensorPresent, useOrientationAPI, mLastAccelerometerSet, mLastMagnetometerSet, isFirstValue;
    private String userName;

    private float acc, mag, ori;

    private float count = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);
        db = new DatabaseHelper(this, null, null);

        connection = new MyServiceConnection(this);
        Intent stepsIntent = new Intent(this, StepsService.class);
        bindService(stepsIntent, connection, Context.BIND_AUTO_CREATE);
        Log.d(TAG, "service bound");

        compass = findViewById(R.id.compass);
        tvacc = findViewById(R.id.tvacc);
        tvmag = findViewById(R.id.tvmag);
        tvori = findViewById(R.id.tvori);
        tvuser = findViewById(R.id.tvUserName);
        tvStepsPerSecond = findViewById(R.id.tvStepsPerSecond);
        tvcounter = findViewById(R.id.tvcounter);
        btnHistory = findViewById(R.id.historybtn);
        btnHistory.setOnClickListener(new ButtonListener());


        btnReset = (Button) findViewById(R.id.btnReset);
        btnReset.setOnClickListener(new ButtonListener());

        userName = getIntent().getStringExtra("Username");
        tvuser.setText(userName);
        tvcounter.setText("Steps since reset: 0");


        setAccSensor();
        setMagSensor();
        setOriSensor();
        setStepCounterSensor();

        mLastAccelerometer = new float[3];
        mLastMagnetometer = new float[3];
        mOrientation = new float[9];
        mRotationMatrix = new float[9];
        useOrientationAPI = true;
    }

    protected void onResume() {
        super.onResume();
        SharedPreferences sharedpref = this.getSharedPreferences("count", Activity.MODE_PRIVATE);
        count = sharedpref.getFloat("count", 0);
        tvcounter.setText("Steps since reset: " + count);
        if (useOrientationAPI) {
            sensorManager.registerListener(this, mAccelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
            sensorManager.registerListener(this, mMagnetometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
            sensorManager.registerListener(this, mStepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            sensorManager.registerListener(this, mOrientationSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    private void saveCount() {
        SharedPreferences sharedPref = this.getSharedPreferences("count", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putFloat("count", count);
        editor.apply();
    }


    protected void onPause() {
        super.onPause();
        saveCount();
        if (useOrientationAPI) {
            sensorManager.unregisterListener(this, mAccelerometerSensor);
            sensorManager.unregisterListener(this, mMagnetometerSensor);
            sensorManager.unregisterListener(this, mStepCounterSensor);
            sensorManager.unregisterListener(this, mOrientationSensor);
        }
    }

    @Override
    protected void onDestroy() {
        mAccelerometerSensor = null;
        mMagnetometerSensor = null;
        mOrientationSensor = null;
        mStepCounterSensor = null;
        sensorManager = null;
        count = 0;
        if (mBound) {
            unbindService(connection);
            mBound = false;
        }
        Toast.makeText(this, "Step Detector Service unbound!", Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }

    private void setAccSensor() {
        Log.d(TAG, "setting Accelerator sensor");
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager.getDefaultSensor(1) != null) {
            mAccelerometerSensor = sensorManager.getDefaultSensor(1);
            Log.d(TAG, "Accelerator sensor found!");
        }

        if (sensorManager.getDefaultSensor(1) == null) {
            toastMessage("Accelerator sensor is null");
            Log.d(TAG, "Accelerator sensor is null");
        }

        sensorManager.registerListener(this, mAccelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        toastMessage("listener registered");
        Log.d(TAG, "Accelerator sensor listener registered");
    }

    private void setMagSensor() {
        Log.d(TAG, "setting Magnetic sensor");
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager.getDefaultSensor(2) != null) {
            mMagnetometerSensor = sensorManager.getDefaultSensor(2);
            Log.d(TAG, "Magnetic sensor found!");
        }

        if (sensorManager.getDefaultSensor(2) == null) {
            toastMessage("Magnetic sensor is null");
            Log.d(TAG, "Magnetic sensor is null");
        }

        sensorManager.registerListener(this, mMagnetometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        toastMessage("listener registered");
        Log.d(TAG, "Magnetic sensor listener registered");
    }

    private void setOriSensor() {
        Log.d(TAG, "setting Orientation sensor");
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager.getDefaultSensor(3) != null) {
            mOrientationSensor = sensorManager.getDefaultSensor(3);
        }

        if (sensorManager.getDefaultSensor(3) == null) {
            toastMessage("Orientation sensor is null");
            Log.d(TAG, "Orientation sensor is null");
        }

        sensorManager.registerListener(this, mOrientationSensor, SensorManager.SENSOR_DELAY_NORMAL);
        toastMessage("listener registered");
    }

    private void toastMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void setStepCounterSensor() {
        Log.d(TAG, "setting Step Counter sensor");
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager.getDefaultSensor(19) != null) {
            mStepCounterSensor = sensorManager.getDefaultSensor(19);
            Log.d(TAG, "Step counter sensor found!");
            isSensorPresent = true;
        }

        if (sensorManager.getDefaultSensor(19) == null) {
            toastMessage("Step counter sensor is null");
            Log.d(TAG, "Step counter sensor is null");
            isSensorPresent = false;
        }

        sensorManager.registerListener(this, mStepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL);
        toastMessage("listener registered");
        Log.d(TAG, "Step counter sensor listener registered");
    }




    @Override
    public void onSensorChanged(SensorEvent event) {

        Sensor sensor = event.sensor;

        if (sensor.getType() == 1) {
            acc = event.values[0];
            tvacc.setText("Accelerator: " + String.valueOf(acc));
     //       Log.d(TAG, "Accelerator setting...");

        } else if (sensor.getType() == 2) {
            mag = event.values[0];
            tvmag.setText("Magnetic Field: " + String.valueOf(mag));
     //       Log.d(TAG, "Magnetic field setting...");

        } else if (sensor.getType() == 3) {
            ori = event.values[0];
   //         Log.d(TAG, "Orientation field setting...");
            rotateUsingOrientationSensor(ori);

        }else if (sensor.getType() == 19) {
            count++;
            tvcounter.setText("Steps since reset: " + String.valueOf(count));
            tvStepsPerSecond.setText("Steps per second: " + String.valueOf(calculateStepsPerSecond(event.timestamp)));

        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    public void rotateUsingOrientationSensor(Float angle) {
        tvori.setText("Orientation: " + String.valueOf(ori));//only 4 times in 1 second
        lastUpdateTime = System.currentTimeMillis();
        RotateAnimation ra = new RotateAnimation(
                current,
                -angle,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);

        ra.setDuration(210);

        ra.setFillAfter(true);

        compass.startAnimation(ra);
        current = -angle;
    }


    public void updateSteps() {
        if (count == 0) {
            tvcounter.setText("Steps since reset: 0");
            tvStepsPerSecond.setText("Steps per second: 0");
        }
    }

    private double calculateStepsPerSecond(double timestamp) {
        double timeActive = timestamp - db.getUserStartTime(userName, getUsername());
        if (timeActive != 0) {
            double stepsPerSecond = db.getUserSteps(userName, getDate()) / timeActive;
            Log.d(TAG, "STEPS PER SECOND" + stepsPerSecond);
            return stepsPerSecond;


        } else {
            return 0;
        }
    }

    private String getDate() {
        Calendar cal = Calendar.getInstance();
        String date = cal.get(Calendar.DAY_OF_MONTH) + "/" + cal.get(Calendar.MONTH) + "/" + cal.get(Calendar.YEAR);
        Log.d(TAG, "Today's date: " + date);
        return date;
    }

    public String getUsername() {
        return userName;
    }

    private class ButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            switch(v.getId()) {
                case R.id.historybtn:
                    Intent intent = new Intent(CompassActivity.this, StepLog.class);
                    intent.putExtra("userName", userName);
                    startActivity(intent);
                    break;
                case R.id.btnReset:
                    count = 0;
                    updateSteps();
                    break;
            }


        }
    }
}

