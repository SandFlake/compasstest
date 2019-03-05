package com.mau.dalvi.malincompass;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;


public class StepLog extends AppCompatActivity {
    private static final String TAG = "STEPLOG";
    private String userName;
    private DatabaseHelper db;
    private ListView listView;
    private ArrayList<DateStepsModel> mStepCountList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_steplog);
        listView = findViewById(R.id.listView);

        userName = getIntent().getStringExtra("userName");
        Log.d(TAG, "User is " + userName);

        getDataForList();

        final ListAdapter listAdapter = new com.mau.dalvi.malincompass.ListAdapter(mStepCountList, this);
        listView.setAdapter(listAdapter);

    }


    public void getDataForList() {
        db = new DatabaseHelper(this, null, null);
        mStepCountList = db.readStepsEntries(userName);
        Log.d(TAG, mStepCountList.toString());
    }

}
