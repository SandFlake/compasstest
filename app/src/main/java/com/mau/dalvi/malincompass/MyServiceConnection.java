package com.mau.dalvi.malincompass;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

public class MyServiceConnection implements ServiceConnection {
    private CompassActivity activity;

    public MyServiceConnection(CompassActivity activity) {
        this.activity = activity;
    }

    @Override
    public void onServiceConnected(ComponentName className, IBinder service) {
        StepsService.LocalBinder binder = (StepsService.LocalBinder) service;

        activity.mService = binder.getService();
        activity.mBound = true;
        activity.mService.setListenerActivity(activity);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

        activity.mBound = false;
    }


}
