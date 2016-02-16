package com.metinkale.prayerapp.vakit.times;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.metinkale.prayerapp.App;

public class PrefsHelper extends SQLiteOpenHelper {
    static final String _KEY = "key";
    static final String _VALUE = "value";
    private static final String DATABASE_NAME = "timesprefs.sqlite";
    private static final int DATABASE_VERSION = 1;
    private static volatile PrefsHelper mInstance;
    private volatile SQLiteDatabase mDatabase;
    private volatile int mOpenCounter;

    private PrefsHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    synchronized SQLiteDatabase openDB() {
        mOpenCounter++;
        if (mOpenCounter == 1) {
            mDatabase = getWritableDatabase();
        }
        return mDatabase;
    }


    private synchronized void closeDB() {
        mOpenCounter--;
        if (mOpenCounter == 0) {
            mDatabase.close();

        }
    }


    public static synchronized PrefsHelper getInstance() {
        if (mInstance == null) {
            synchronized (PrefsHelper.class) {
                if (mInstance == null) {
                    mInstance = new PrefsHelper(App.getContext());
                }
            }
        }

        return mInstance;
    }


    @SuppressLint("UseSparseArrays")
    @Override
    public void onCreate(SQLiteDatabase db) {

        mDatabase = db;
        mOpenCounter++;

        closeDB();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
        mDatabase = db;
        mOpenCounter++;
        closeDB();


    }


}