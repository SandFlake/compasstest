package com.mau.dalvi.malincompass;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DATABASEHELPER";
    private static final String DATABASE_NAME = "userInfo.db";

    public static final String table = "login";
    public static final String id = "id";
    public static final String name = "name";
    public static final String password = "password";
    public static final String steps = "steps";
    public static final String starttime = "starttime";
    public static final String date = "date";

    private static final String CREATE_table = "CREATE TABLE " + table + "( " + id + " INTEGER PRIMARY KEY AUTOINCREMENT, " + name + " TEXT, " + password + " TEXT, " + steps + " INTEGER, " +
            starttime + " DOUBLE, " +  date + " TEXT " + ");";

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, DATABASE_NAME, factory, 6);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_table);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + table);
        onCreate(db);
    }

    public void addNewUser(User userInfo) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(name, userInfo.get_name());
        values.put(password, userInfo.get_password());
        values.put(steps, 0);
        values.put(starttime, 1);
        values.put(date, getDate());
        db.insert(table, null, values);
        db.close();
    }

    public void setStartTime(String username, String checkDate, double startTime) {
//        long startTimeStamp = startTime;
        double startTimeStamp = startTime;
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(starttime, startTimeStamp);
        db.update(table, values, name + " = '" + username + "' AND " + date + " = '" + checkDate + "'", null);
    }

    public void addUserSteps(String username, double start) {
        int msteps = 1;
        String mdate = getDate();
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(starttime, start);
        values.put(steps, msteps);
        values.put(date, mdate);
        db.update(table, values, name + " = '" + username + "'", null);
    }

    private String getDate() {
        Calendar cal = Calendar.getInstance();
        String date = cal.get(Calendar.DAY_OF_MONTH) + "/" + cal.get(Calendar.MONTH) + "/" + cal.get(Calendar.YEAR);
        return date;
    }

    public void resetUserSteps(String username) {
        int msteps = 0;
        double time = 1;
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(steps, msteps);
        values.put(starttime, time);
        db.update(table, values, name + " = '" + username + "'", null);
    }


    public boolean checkUserNameTaken(String username) {
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + table + " WHERE " + name + " = '" + username + "'";
        //Cursor point to a location in your results
        Cursor c = db.rawQuery(query, null);
        //Move to the first row in your results
        c.moveToFirst();
        boolean nameExists = c.getCount() > 0;
        db.close();
        return nameExists;
    }

    public boolean checkForDate(String dateCheck) {
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + table + " WHERE " + date + " = '" + dateCheck + "'";
        //Cursor point to a location in your results
        Cursor c = db.rawQuery(query, null);
        //Move to the first row in your results
        c.moveToFirst();
        boolean dateExists = c.getCount() > 0;
        db.close();
        return dateExists;
    }

    public String getUserPassword(String username) {
        String dbString = "";
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + table + " WHERE " + name + " = '" + username + "'";
        //Cursor point to a location in your results
        Cursor c = db.rawQuery(query, null);
        //Move to the first row in your results
        c.moveToFirst();

        dbString = c.getString(c.getColumnIndex(password));
        db.close();
        return dbString;
    }

    public int getUserSteps(String username, String mdate) {
        int msteps;
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + table + " WHERE " + name + " = '" + username + "' AND " + date + " = '" + mdate + "'";
        //Cursor point to a location in your results
        Cursor c = db.rawQuery(query, null);
        //Move to the first row in your results
        c.moveToFirst();
        msteps = c.getInt(c.getColumnIndex(steps));
        db.close();
        return msteps;
    }

    public double getUserStartTime(String username, String checkDate) {
        double startTime = 0;
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT starttime FROM " + table + " WHERE " + name + " = '" + username + "' AND " + date + " = '" + checkDate + "'";
        //Cursor point to a location in your results
        Cursor c = db.rawQuery(query, null);
        //Move to the first row in your results
        c.moveToFirst();
        try {
            startTime = c.getDouble(c.getColumnIndex(starttime));
        }catch(CursorIndexOutOfBoundsException e) {
            Log.d(TAG, e.getMessage());
        }
        db.close();
        return startTime;
    }

    public ArrayList<DateStepsModel> readStepsEntries(String username) {

        ArrayList<DateStepsModel> mStepCountList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + table + " WHERE " + name + " = '" + username + "'";
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor c = db.rawQuery(selectQuery, null);
            if (c.moveToFirst()) {
                do {
                    DateStepsModel mDateStepsModel = new DateStepsModel();
                    mDateStepsModel.mDate = c.getString((c.getColumnIndex(date)));
                    mDateStepsModel.mStepCount = c.getInt((c.getColumnIndex(steps)));
                    mStepCountList.add(mDateStepsModel);
                } while (c.moveToNext());
            }
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mStepCountList;
    }

    public void updateUserSteps(String username, String checkdate) {
        int msteps = getUserSteps(username, checkdate) + 1;
        String mdate = getDate();
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(steps, msteps);
        Log.d(TAG, "number of steps: " + msteps);
        db.update(table, values, name + " = '" + username + "' AND " + date + " = '" + mdate + "'", null);
    }
}