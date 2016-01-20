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

import java.util.*;

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

    private List<Times> mTimes = new ArrayList<Times>();
    private LruCache<String, Object> cache = new LruCache<String, Object>(500);
    private List<MainHelperListener> mListeners = new ArrayList<>();

    private MainHelper() {
        super(App.getContext(), DATABASE_NAME, null, DATABASE_VERSION);

    }

    public static void addListener(MainHelperListener listener) {
        get().mListeners.add(listener);
        listener.notifyDataSetChanged();
    }

    public static void removeListener(MainHelperListener listener) {
        get().mListeners.remove(listener);
    }

    static MainHelper get() {
        if (sInstance == null) {
            sInstance = new MainHelper();
            sInstance.loadTimes();
        }

        return sInstance;
    }

    public static Times getTimesAt(int index) {
        return get().mTimes.get(index);
    }

    public static Times getTimes(long id) {
        for (Times t : get().mTimes) {
            if (t.getID() == id)
                return t;
        }

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
                case Semerkand:
                    return new SemerkandTimes(id);
                case Calc:
                    return new CalcTimes(id);
            }

        } catch (Exception e) {
        }
        return null;
    }

    public static List<Times> getTimes() {
        return get().mTimes;
    }

    public static List<Long> getIds() {
        List<Long> ids = new ArrayList<>();
        List<Times> times = getTimes();
        for (Times t : times) {
            if (t != null)
                ids.add(t.getID());
        }
        return ids;
    }

    public static int getCount() {
        return get().mTimes.size();
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
        get().loadTimes();
    }

    private void notifyOnDataSetChanged() {
        for (MainHelperListener list : mListeners)
            list.notifyDataSetChanged();
    }

    public SQLiteDatabase getDB() {
        if (mDB == null)
            mDB = getWritableDatabase();
        return mDB;
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
                        edit.putLong(key, newids.get(all.get(key)));
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

    protected void loadTimes() {
        List<Long> ids = new ArrayList<>();
        List<Times> times = new ArrayList<>(mTimes);
        for (Times t : times) {
            if (t.isDeleted()) {
                mTimes.remove(t);
            } else {
                ids.add(t.getID());
            }
        }

        Cursor c = get().getDB().query(CITIES_TABLE, new String[]{_ID}, "key != 'deleted'", null, _ID, null, null);
        c.moveToFirst();
        if (!c.isAfterLast()) {
            do {
                long id = c.getLong(0);
                if (!ids.contains(id)) {
                    Times t = MainHelper.getTimes(id);
                    if (t != null)
                        mTimes.add(t);
                }
            } while (c.moveToNext());
        }
        c.close();

        Collections.sort(mTimes, new Comparator<Times>() {
            @Override
            public int compare(Times t1, Times t2) {
                try {
                    return t1.getSortId() - t2.getSortId();
                } catch (RuntimeException e) {
                    return 0;
                }
            }
        });

        notifyOnDataSetChanged();
    }


    public interface MainHelperListener {
        void notifyDataSetChanged();
    }

    protected class _TimesBase {
        private long id;
        private boolean deleted = false;

        public _TimesBase(long id) {
            this.id = id;
            if (id == 0) throw new RuntimeException("can't create _TimesBase with i=0");
            if (is("deleted"))
                throw new RuntimeException("can't create TimesBase with deleted id(" + id + ")");
        }

        boolean isDeleted() {
            return deleted || is("deleted");
        }

        public void setTimes(int d, int m, int y, String[] time) {
            if (deleted) return;
            ContentValues values = new ContentValues();
            values.put(_ID, id);
            values.put(_DATE, y + "-" + Utils.az(m) + "-" + Utils.az(d));
            for (int i = 0; i < Math.min(time.length, _TIME.length); i++)
                values.put(_TIME[i], time[i]);

            getDB().insertWithOnConflict(TIMES_TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);

        }

        String _getTime(int d, int m, int y, int time) {
            if (deleted) return null;
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
            if (deleted) return;
            getDB().delete(CITIES_TABLE, _ID + " = " + id, null);
            getDB().delete(TIMES_TABLE, _ID + " = " + id, null);
            set("deleted", true);
            deleted = true;
            id = 0;
            loadTimes();
        }

        public void clearTimes() {
            getDB().delete(TIMES_TABLE, _ID + " = " + id, null);
        }

        public final long getID() {
            if (deleted) return 0;
            return id;
        }


        public void set(String key, String value) {
            if (deleted) return;
            cache.put(id + key, value);
            ContentValues values = new ContentValues();
            values.put(_ID, id);
            values.put(_KEY, key);
            values.put(_VALUE, value);
            getDB().insertWithOnConflict(CITIES_TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);

        }


        public void set(String key, int value) {
            if (deleted) return;
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
            if (deleted) return;
            cache.put(id + key, value);

            ContentValues values = new ContentValues();
            values.put(_ID, id);
            values.put(_KEY, key);
            values.put(_VALUE, value);
            getDB().insertWithOnConflict(CITIES_TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);

        }


        public void set(String key, long value) {
            if (deleted) return;
            cache.put(id + key, value);

            ContentValues values = new ContentValues();
            values.put(_ID, id);
            values.put(_KEY, key);
            values.put(_VALUE, value);
            getDB().insertWithOnConflict(CITIES_TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);

        }


        public void set(String key, byte[] value) {
            if (deleted) return;
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
            if (deleted) return def;
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
            if (deleted) return def;
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
            if (deleted) return def;
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
            if (!key.equals("deleted"))
                if (deleted) return def;
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
