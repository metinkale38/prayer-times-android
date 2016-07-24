/*
 * Copyright (c) 2016 Metin Kale
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.metinkale.prayerapp.vakit.times;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.metinkale.prayerapp.App;
import com.metinkale.prayerapp.settings.Prefs;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static android.database.Cursor.*;

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
    private static final String[] _TIME = {"time0", "time1", "time2", "time3", "time4", "time5"};
    private static final String TIMES_TABLE = "times";
    private static final String TIMES_CREATE = "CREATE TABLE " + TIMES_TABLE + " (" + _ID + " INTEGER, " + _DATE + " TEXT," + _TIME[0] + " TEXT, " + _TIME[1] + " TEXT, " + _TIME[2] + " TEXT, " + _TIME[3] + " TEXT, " + _TIME[4] + " TEXT, " + _TIME[5] + " TEXT, PRIMARY KEY (" + _ID + ", " + _DATE + "))";


    public static void copy() {
        if (Prefs.getMigratedFromSqlite()) {
            return;
        }
        File dbFile = App.getContext().getDatabasePath(DATABASE_NAME);
        if (dbFile.exists()) {
            MainHelper mainHelper = new MainHelper();

            Map<Long, JSONObject> map = new HashMap<>();
            SQLiteDatabase db = mainHelper.getWritableDatabase();
            Cursor c = db.query(CITIES_TABLE, null, null, null, null, null, null);
            c.moveToFirst();
            if (!c.isAfterLast()) {
                do {
                    long id = c.getLong(c.getColumnIndex(_ID));
                    String key = c.getString(c.getColumnIndex(_KEY));
                    int column = c.getColumnIndex(_VALUE);
                    int type = c.getType(column);
                    JSONObject times;
                    if (map.containsKey(id)) {
                        times = map.get(id);
                    } else {
                        times = new JSONObject();
                        map.put(id, times);
                    }
                    try {
                        switch (type) {
                            case FIELD_TYPE_INTEGER:
                                times.put(key, c.getInt(column));
                                break;
                            case FIELD_TYPE_STRING:
                                times.put(key, c.getString(column));
                                break;
                            case FIELD_TYPE_FLOAT:
                                times.put(key, c.getDouble(column));
                                break;
                        }
                    } catch (Exception e) {
                    }

                } while (c.moveToNext());
            }
            c.close();


            for (long id : map.keySet()) {
                App.getContext().getSharedPreferences("cities", 0).edit().putString("id" + id, map.get(id).toString()).apply();
            }
            db.delete(CITIES_TABLE, null, null);
            db.delete(TIMES_TABLE, null, null);

            db.close();
            mainHelper.close();

            if (!dbFile.delete()) {
                dbFile.deleteOnExit();
            }
        }

        for (Times t : Times.getTimes()) {
            t.refresh();
        }
        Prefs.setMigratedFromSqlite(true);
    }

    private MainHelper() {
        super(App.getContext(), DATABASE_NAME, null, DATABASE_VERSION);

    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CITIES_CREATE);
        db.execSQL(TIMES_CREATE);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    // @formatter:off
    /*


    public interface TimesListener {
        void notifyDataSetChanged();
    }

    protected class _TimesBase {
        private long id;
        private boolean deleted;

        public _TimesBase(long id) {
            this.id = id;
            if (id == 0) throw new RuntimeException("can't create _TimesBase with i=0");
            if (is("deleted")) throw new RuntimeException("can't create AbstractTimesBasics with deleted id(" + id + ")");
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

            SQLiteDatabase db = openDB();
            db.insertWithOnConflict(TIMES_TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            closeDB();
        }

        String _getTime(int d, int m, int y, int time) {
            if (deleted) return null;
            String date = y + "-" + Utils.az(m) + "-" + Utils.az(d);

            Object cached = data.get(id + date + time);
            if (cached instanceof String) return (String) cached;
            SQLiteDatabase db = openDB();
            Cursor c = db.query(TIMES_TABLE, null, _ID + " = " + id + " AND " + _DATE + " = '" + date + "'", null, null, null, null);
            c.moveToFirst();
            try {
                if (c.isAfterLast()) {
                    c.close();
                    return "00:00";
                }

                String ret = c.getString(c.getColumnIndex(_TIME[time]));
                for (int i = 0; i < _TIME.length; i++) {
                    data.put(id + date + i, c.getString(c.getColumnIndex(_TIME[i])));
                }
                return ret;
            } finally {
                c.close();
                closeDB();
            }
        }

        public void loadAllTimes() {
            String date = y + "-" + Utils.az(Calendar.getInstance().get(Calendar.MONTH) + 1) + "-01";

            SQLiteDatabase db = openDB();
            Cursor c = db.query(TIMES_TABLE, null, _ID + " = " + id + " AND " + _DATE + " >= '" + date + "'", null, null, null, null);
            c.moveToFirst();

            c.moveToFirst();
            if (!c.isAfterLast()) {
                do {
                    String d = c.getString(c.getColumnIndex(_DATE));
                    for (int i = 0; i < _TIME.length; i++) {
                        data.put(id + d + i, c.getString(c.getColumnIndex(_TIME[i])));
                    }

                } while (c.moveToNext());
            }

            c.close();
            closeDB();
        }

        public void delete() {
            if (deleted) return;
            SQLiteDatabase db = openDB();
            db.delete(CITIES_TABLE, _ID + " = " + id, null);
            db.delete(TIMES_TABLE, _ID + " = " + id, null);
            set("deleted", true);
            closeDB();

            deleted = true;
            id = 0;
            loadTimes();

        }

        public void clearTimes() {
            SQLiteDatabase db = openDB();
            db.delete(TIMES_TABLE, _ID + " = " + id, null);
            closeDB();
        }

        public final long getID() {
            if (deleted) return 0;
            return id;
        }


        public void set(String key, String value) {
            if (deleted) return;
            ContentValues values = new ContentValues();
            values.put(_ID, id);
            values.put(_KEY, key);
            values.put(_VALUE, value);
            SQLiteDatabase db = openDB();
            db.insertWithOnConflict(CITIES_TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            closeDB();
            data.put(id + key, value);
        }


        public void set(String key, int value) {
            if (deleted) return;

            ContentValues values = new ContentValues();
            values.put(_ID, id);
            values.put(_KEY, key);
            values.put(_VALUE, value);
            SQLiteDatabase db = openDB();
            db.insertWithOnConflict(CITIES_TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            closeDB();
            data.put(id + key, value);

        }


        public void set(String key, boolean value) {
            set(key, value ? 1 : 0);
        }


        public void set(String key, double value) {
            if (deleted) return;

            ContentValues values = new ContentValues();
            values.put(_ID, id);
            values.put(_KEY, key);
            values.put(_VALUE, value);
            SQLiteDatabase db = openDB();
            db.insertWithOnConflict(CITIES_TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            closeDB();
            data.put(id + key, value);

        }


        public void set(String key, byte[] value) {
            if (deleted) return;

            ContentValues values = new ContentValues();
            values.put(_ID, id);
            values.put(_KEY, key);
            values.put(_VALUE, value);
            SQLiteDatabase db = openDB();
            db.insertWithOnConflict(CITIES_TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            closeDB();
            data.put(id + key, value);

        }


        public double getDouble(String key) {
            return getDouble(key, 0);
        }

        public double getDouble(String key, double def) {
            if (deleted) return def;
            Object cached = data.get(id + key);
            if (cached instanceof Double) return (Double) cached;
            return def;
        }

        public String getString(String key) {
            return getString(key, null);
        }

        public String getString(String key, String def) {
            if (deleted) return def;
            Object cached = data.get(id + key);
            if (cached instanceof String) return (String) cached;
            return def;
        }

        public boolean deleted() {
            return deleted;
        }

        public int getInt(String key) {
            return getInt(key, -1);
        }

        public int getInt(String key, int def) {
            if (!"deleted".equals(key)) if (deleted) return def;
            Object cached = data.get(id + key);

            if (cached instanceof Integer) return (Integer) cached;
            return def;
        }


        public boolean is(String key) {
            return getInt(key, 0) == 1;
        }

    }*/
// @formatter:on
}
