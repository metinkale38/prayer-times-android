package com.metinkale.prayerapp.vakit.times;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.LruCache;

import com.metinkale.prayerapp.App;
import com.metinkale.prayerapp.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Metin on 19.06.2015.
 */
public class MainHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "times";
    private static final int DATABASE_VERSION = 1;


    private static final String _ID = "id";
    private static final String _KEY = "key";
    private static final String _VALUE = "value";
    private static final String CITIES_TABLE = "cities";
    private static final String CITIES_CREATE = "CREATE TABLE " + CITIES_TABLE + " (" + _ID + " INTEGER not null, " + _KEY + " TEXT not null," + _VALUE + " BLOB not null, PRIMARY KEY (" + _ID + ", " + _KEY + "))";


    private static final String _DATE = "date";
    private static final String _TIME[] = new String[]{"time0", "time1", "time2", "time3", "time4", "time5"};
    private static final String TIMES_TABLE = "times";
    private static final String TIMES_CREATE = "CREATE TABLE " + TIMES_TABLE + " (" + _ID + " INTEGER, " + _DATE + " TEXT," + _TIME[0] + " TEXT, " + _TIME[1] + " TEXT, " + _TIME[2] + " TEXT, " + _TIME[3] + " TEXT, " + _TIME[4] + " TEXT, " + _TIME[5] + " TEXT, PRIMARY KEY (" + _ID + ", " + _DATE + "))";


    private static MainHelper sInstance;
    private SQLiteDatabase mDB;

    private MainHelper() {
        super(App.getContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    private LruCache<String, Object> cache = new LruCache<String, Object>(500);

    static MainHelper get() {
        if (sInstance == null)
            sInstance = new MainHelper();

        return sInstance;
    }

    public SQLiteDatabase getDB() {
        if (mDB == null)
            mDB = getWritableDatabase();
        return mDB;
    }

    public static Times getTimes(long id) {

        try {
            TimesBase.Source source = TimesBase.getSource(id);
            switch (source) {
                case Diyanet:
                    return new DiyanetTimes(id);
                case Fazilet:
                    return new FaziletTimes(id);
                case IGMG:
                    return new IGMGTimes(id);
                case NVC:
                    return new NVCTimes(id);
                case Calc:
                    return new CalcTimes(id);
            }

        } catch (Exception e) {
        }
        return null;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        mDB = db;
        mDB.execSQL(CITIES_CREATE);
        mDB.execSQL(TIMES_CREATE);

        HashMap<Integer, Long> newids = new HashMap<Integer, Long>();
        //import TimesHelper&PrefsHelper
        SQLiteDatabase t = TimesHelper.getInstance().openDB();
        SQLiteDatabase p = PrefsHelper.getInstance().openDB();
        Cursor c = t.query(TimesHelper.TABLE_NAME, null, null, null, null, null, null);
        c.moveToFirst();
        List<Long> ids = new ArrayList<>();
        long l = System.currentTimeMillis() / 2;
        if (!c.isAfterLast()) {
            do {
                l++;
                String source = c.getString(c.getColumnIndex(TimesHelper.KEY_SOURCE));
                Times times = null;
                if (source.equals("Calc")) {
                    CalcTimes calc = new CalcTimes(l, true);
                    calc.setSource(TimesBase.Source.Calc);
                    calc.setAdjMethod(PrayTime.AdjMethod.valueOf(c.getString(c.getColumnIndex(TimesHelper.KEY_ADJMETHOD))));
                    calc.setJuristic(PrayTime.Juristic.valueOf(c.getString(c.getColumnIndex(TimesHelper.KEY_JURISTIC))));
                    calc.setMethod(PrayTime.Method.valueOf(c.getString(c.getColumnIndex(TimesHelper.KEY_METHOD))));
                    calc.setLng(c.getLong(c.getColumnIndex(TimesHelper.KEY_LAT)));
                    calc.setLat(c.getLong(c.getColumnIndex(TimesHelper.KEY_LNG)));
                    calc.setName(c.getString(c.getColumnIndex(TimesHelper.KEY_NAME)));
                    times = calc;
                } else {
                    WebTimes web = new WebTimes(l);
                    web.setSource(TimesBase.Source.valueOf(c.getString(c.getColumnIndex(TimesHelper.KEY_SOURCE))));
                    web.setLng(c.getLong(c.getColumnIndex(TimesHelper.KEY_LAT)));
                    web.setLat(c.getLong(c.getColumnIndex(TimesHelper.KEY_LNG)));
                    web.setName(c.getString(c.getColumnIndex(TimesHelper.KEY_NAME)));
                    web.setId(c.getString(c.getColumnIndex(TimesHelper.KEY_ID)));
                    times = web;
                }
                int id = c.getInt(c.getColumnIndex(TimesHelper.KEY__ID));
                newids.put(id, l);
                Cursor cc = null;
                try {
                    cc = p.query("id_" + id, null, null, null, null, null, null);
                    cc.moveToFirst();
                    if (!cc.isAfterLast()) {
                        do {
                            times.set(cc.getString(cc.getColumnIndex(PrefsHelper._KEY)), cc.getString(cc.getColumnIndex(PrefsHelper._VALUE)));
                        } while (cc.moveToNext());
                    }
                    cc.close();

                } catch (SQLiteException ignore) {

                } finally {
                    if (cc != null && !cc.isClosed()) cc.close();
                }


            } while (c.moveToNext());
        }
        c.close();

        t.delete(TimesHelper.TABLE_NAME, null, null);


        //correct WidgetIds
        SharedPreferences widgets = App.getContext().getSharedPreferences("widgets", 0);
        SharedPreferences.Editor edit = widgets.edit();
        Map<String, ?> all = widgets.getAll();
        for (String key : all.keySet()) {
            if (all.get(key) instanceof Integer) {
                if (!key.contains("_")) {
                    try {
                        edit.remove(key);
                        edit.putLong(key, newids.get((Integer) all.get(key)));
                    } catch (Exception ignore) {
                    }
                }
            }
        }

        edit.commit();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        mDB = db;
    }

    public static List<Long> getIds() {
        Cursor c = get().getDB().query(CITIES_TABLE, new String[]{_ID}, "key != 'deleted'", null, _ID, null, null);
        c.moveToFirst();
        List<Long> ids = new ArrayList<>();
        if (!c.isAfterLast()) {
            do {
                ids.add(c.getLong(0));
            } while (c.moveToNext());
        }
        c.close();

        Collections.sort(ids, new Comparator<Long>() {
            @Override
            public int compare(Long l1, Long l2) {
                try {
                    return getTimes(l1).getSortId() - getTimes(l2).getSortId();
                } catch (RuntimeException e) {
                    return 0;
                }
            }
        });
        return ids;
    }


    public static int getCount() {
        Cursor c = get().getDB().query(CITIES_TABLE, new String[]{"count(DISTINCT " + _ID + ")"}, "key != 'deleted'", null, null, null, null);
        try {
            c.moveToFirst();
            if (!c.isAfterLast()) {
                return c.getInt(0);
            }
        } finally {
            c.close();
        }
        return 0;
    }


    public static void drop(int from, int to) {

        List<Long> keys = getIds();
        Long key = keys.get(from);
        keys.remove(key);
        keys.add(to, key);
        get().getDB().beginTransaction();
        for (Long i : keys) {
            getTimes(i).setSortId(keys.indexOf(i));
        }
        get().getDB().setTransactionSuccessful();
        get().getDB().endTransaction();
    }


    protected class _TimesBase {
        private long id;
        private boolean deleted = false;

        _TimesBase(long id) {
            this.id = id;
            if (id == 0) throw new RuntimeException("can't create _TimesBase with i=0");
            if (is("deleted"))
                throw new RuntimeException("can't create TimesBase with deleted id(" + id + ")");
        }

        private void checkDeleted() {
            if (deleted)
                throw new RuntimeException(" _TimesBase; id " + id + " has been deleted, you can't use it");
        }

        public void setTimes(int d, int m, int y, String[] time) {
            checkDeleted();
            ContentValues values = new ContentValues();
            values.put(_ID, id);
            values.put(_DATE, y + "-" + Utils.az(m) + "-" + Utils.az(d));
            for (int i = 0; i < Math.min(time.length, _TIME.length); i++)
                values.put(_TIME[i], time[i]);

            getDB().insertWithOnConflict(TIMES_TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);

        }

        String _getTime(int d, int m, int y, int time) {
            checkDeleted();
            String date = y + "-" + Utils.az(m) + "-" + Utils.az(d);

            Object cached = cache.get(id + date + time);
            if (cached instanceof String)
                return (String) cached;

            Cursor c = getDB().query(TIMES_TABLE, new String[]{_TIME[time]}, _ID + " = " + id + " AND " + _DATE + " = '" + date + "'", null, null, null, null);
            c.moveToFirst();
            if (c.isAfterLast()) {
                c.close();
                return "00:00";
            }
            try {
                String ret = c.getString(0);
                cache.put(id + date + time, ret);
                return ret;
            } finally {
                c.close();
            }
        }

        public void delete() {
            checkDeleted();
            getDB().delete(CITIES_TABLE, _ID + " = " + id, null);
            getDB().delete(TIMES_TABLE, _ID + " = " + id, null);
            set("deleted", true);
            deleted = true;
            id = 0;
        }


        public final long getID() {
            checkDeleted();
            return id;
        }


        public void set(String key, String value) {
            checkDeleted();
            cache.put(id + key, value);
            ContentValues values = new ContentValues();
            values.put(_ID, id);
            values.put(_KEY, key);
            values.put(_VALUE, value);
            getDB().insertWithOnConflict(CITIES_TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);

        }


        public void set(String key, int value) {
            checkDeleted();
            cache.put(id + key, value);

            ContentValues values = new ContentValues();
            values.put(_ID, id);
            values.put(_KEY, key);
            values.put(_VALUE, value);
            getDB().insertWithOnConflict(CITIES_TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);

        }


        public void set(String key, boolean value) {
            set(key, value ? 1 : 0);
        }


        public void set(String key, double value) {
            checkDeleted();
            cache.put(id + key, value);

            ContentValues values = new ContentValues();
            values.put(_ID, id);
            values.put(_KEY, key);
            values.put(_VALUE, value);
            getDB().insertWithOnConflict(CITIES_TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);

        }


        public void set(String key, long value) {
            checkDeleted();
            cache.put(id + key, value);

            ContentValues values = new ContentValues();
            values.put(_ID, id);
            values.put(_KEY, key);
            values.put(_VALUE, value);
            getDB().insertWithOnConflict(CITIES_TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);

        }


        public void set(String key, byte[] value) {
            checkDeleted();
            cache.put(id + key, value);

            ContentValues values = new ContentValues();
            values.put(_ID, id);
            values.put(_KEY, key);
            values.put(_VALUE, value);
            getDB().insertWithOnConflict(CITIES_TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);

        }

        public long getLong(String key) {
            return getLong(key, 0);
        }

        public long getLong(String key, long def) {
            checkDeleted();
            Object cached = cache.get(id + key);
            if (cached instanceof Long) return (Long) cached;
            Cursor c = getDB().query(CITIES_TABLE, new String[]{_VALUE}, _ID + " = " + id + " AND " + _KEY + " = '" + key + "'", null, null, null, null);
            c.moveToFirst();
            if (c.isAfterLast()) {
                c.close();
                return def;
            }
            try {
                long ret = c.getLong(0);
                cache.put(id + key, ret);
                return ret;
            } finally {
                c.close();
            }
        }

        public double getDouble(String key) {
            return getDouble(key, 0);
        }

        public double getDouble(String key, double def) {
            checkDeleted();
            Object cached = cache.get(id + key);
            if (cached instanceof Double) return (Double) cached;
            Cursor c = getDB().query(CITIES_TABLE, new String[]{_VALUE}, _ID + " = " + id + " AND " + _KEY + " = '" + key + "'", null, null, null, null);
            c.moveToFirst();
            if (c.isAfterLast()) {
                c.close();
                return def;
            }
            try {
                double ret = c.getDouble(0);
                cache.put(id + key, ret);
                return ret;
            } finally {
                c.close();
            }
        }

        public String getString(String key) {
            return getString(key, null);
        }

        public String getString(String key, String def) {
            checkDeleted();
            Object cached = cache.get(id + key);
            if (cached instanceof String) return (String) cached;
            Cursor c = getDB().query(CITIES_TABLE, new String[]{_VALUE}, _ID + " = " + id + " AND " + _KEY + " = '" + key + "'", null, null, null, null);
            c.moveToFirst();
            if (c.isAfterLast()) {
                c.close();
                return def;
            }
            try {
                String ret = c.getString(0);
                cache.put(id + key, ret);
                return ret;
            } finally {
                c.close();
            }
        }

        public boolean deleted() {
            return deleted;
        }

        public int getInt(String key) {
            return getInt(key, -1);
        }

        public int getInt(String key, int def) {
            checkDeleted();
            Object cached = cache.get(id + key);
            if (cached instanceof Integer) return (Integer) cached;
            Cursor c = getDB().query(CITIES_TABLE, new String[]{_VALUE}, _ID + " = " + id + " AND " + _KEY + " = '" + key + "'", null, null, null, null);
            c.moveToFirst();
            if (c.isAfterLast()) {
                c.close();
                return def;
            }
            try {
                int ret = c.getInt(0);
                cache.put(id + key, ret);
                return ret;
            } finally {
                c.close();
            }
        }


        public boolean is(String key) {
            return getInt(key, 0) == 1;
        }

    }
}
