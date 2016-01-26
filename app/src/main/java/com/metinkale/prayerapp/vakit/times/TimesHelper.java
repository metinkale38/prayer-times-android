package com.metinkale.prayerapp.vakit.times;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.metinkale.prayerapp.App;
import com.metinkale.prayerapp.custom.WeakValueHashMap;

public class TimesHelper extends SQLiteOpenHelper {

    static final String DATABASE_NAME = "times.sqlite";
    static final int DATABASE_VERSION = 2;
    static final String TABLE_NAME = "cities";

    static final String KEY__ID = "_id";
    static final String KEY_ID = "id";
    static final String KEY_JURISTIC = "juristic";
    static final String KEY_LAT = "lat";
    static final String KEY_LNG = "lng";
    static final String KEY_METHOD = "method";
    static final String KEY_MINUTEADJ = "minuteAdj";
    static final String KEY_NAME = "city";
    static final String KEY_SORTID = "sortId";
    static final String KEY_SOURCE = "source";
    static final String KEY_TIMEZONE = "timezone";
    static final String KEY_ADJMETHOD = "adjMethod";

    static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " (" + KEY__ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_SOURCE + " TEXT, " + KEY_NAME + " TEXT, " + KEY_ID + " TEXT, " + KEY_LAT + " DOUBLE, " + KEY_LNG + " DOUBLE," + KEY_METHOD + " TEXT, " + KEY_JURISTIC + " TEXT, " + KEY_ADJMETHOD + " TEXT, " + KEY_SORTID + " INTEGER, " + KEY_TIMEZONE + " double," + KEY_MINUTEADJ + " text);";

    private static volatile TimesHelper mInstance;
    private volatile SQLiteDatabase mDatabase;
    private volatile int mOpenCounter;

    private WeakValueHashMap<Integer, Times> mTimes = new WeakValueHashMap<>();

    private TimesHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    public static synchronized TimesHelper getInstance() {
        if (mInstance == null) {
            synchronized (TimesHelper.class) {
                if (mInstance == null) {
                    mInstance = new TimesHelper(App.getContext());
                }
            }
        }

        return mInstance;
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
        if (mOpenCounter == 0 && mDatabase != null) {
            mDatabase.close();

        }
    }

    @Override
    public synchronized void onCreate(SQLiteDatabase db) {
        mDatabase = db;
        mOpenCounter++;
        db.execSQL(TABLE_CREATE);


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        mDatabase = db;
        mOpenCounter++;
        if (oldVersion < 2) {
            db.compileStatement("ALTER TABLE " + TABLE_NAME + " add " + KEY_TIMEZONE + " double").execute();
            db.compileStatement("ALTER TABLE " + TABLE_NAME + " add " + KEY_MINUTEADJ + " text").execute();
        }
        closeDB();
    }


}