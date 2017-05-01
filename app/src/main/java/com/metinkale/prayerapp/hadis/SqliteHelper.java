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
 *
 */

package com.metinkale.prayerapp.hadis;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.metinkale.prayerapp.App;
import com.metinkale.prayerapp.settings.Prefs;

import java.io.File;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

public class SqliteHelper extends SQLiteOpenHelper {
    private static SqliteHelper mInstance;
    @Nullable
    private static File FILE;
    @Nullable
    private SQLiteDatabase mDB;
    @NonNull
    private List<String> categories = new ArrayList<>();
    private AtomicInteger mOpenCounter = new AtomicInteger();

    private SqliteHelper(@NonNull Context context) {
        super(context, "hadis.db", null, 1);
        FILE = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        mInstance = this;
    }

    private static String normalize(CharSequence str) {
        String string = Normalizer.normalize(str, Normalizer.Form.NFD);
        string = string.replaceAll("[^\\p{ASCII}]", "_");
        return string.toLowerCase(Locale.ENGLISH);
    }

    public static SqliteHelper get() {
        if (mInstance == null) {
            new SqliteHelper(App.get());

        }
        return mInstance;
    }

    public int getCount() {
        openDatabase();
        Cursor c = mDB.query("HADIS", null, null, null, null, null, null);
        int ret = c.getCount();
        c.close();
        closeDatabase();
        return ret;
    }

    @NonNull
    public List<Integer> search(CharSequence query) {
        openDatabase();
        List<Integer> list = new ArrayList<>();
        try {
            Cursor c = mDB.query("HADIS", null, null, null, null, null, null);
            c.moveToFirst();
            if (c.isAfterLast()) {
                return list;
            }
            do {
                String txt = c.getString(c.getColumnIndex("KONU"));
                txt += c.getString(c.getColumnIndex("DETAY"));
                txt += c.getString(c.getColumnIndex("HADIS"));
                txt += c.getString(c.getColumnIndex("KAYNAK"));
                if (normalize(txt).contains(normalize(query))) {
                    list.add(c.getInt(c.getColumnIndex("ID")));
                }
            } while (c.moveToNext());
            c.close();
        } finally {
            closeDatabase();

        }
        return list;
    }

    @NonNull
    public Collection<Integer> get(String cat) {
        openDatabase();
        Collection<Integer> list = new ArrayList<>();
        try {
            Cursor c = mDB.query("HADIS", new String[]{"ID"}, "KONU = \"" + cat + "\"", null, null, null, null);
            c.moveToFirst();
            if (c.isAfterLast()) {
                return list;
            }
            do {
                list.add(c.getInt(c.getColumnIndex("ID")));
            } while (c.moveToNext());
            c.close();
        } finally {
            closeDatabase();

        }
        return list;
    }

    @Nullable
    public Hadis get(int id) {
        openDatabase();
        try {
            Cursor c = mDB.query("HADIS", null, "ID=" + id, null, null, null, null, "1");
            c.moveToFirst();
            if (c.isAfterLast()) {
                return null;
            }
            Hadis h = new Hadis();
            h.id = id;
            h.konu = c.getString(c.getColumnIndex("KONU"));
            h.detay = c.getString(c.getColumnIndex("DETAY"));
            h.hadis = c.getString(c.getColumnIndex("HADIS"));
            h.kaynak = c.getString(c.getColumnIndex("KAYNAK"));
            c.close();
            return h;
        } finally {
            closeDatabase();
        }
    }

    @NonNull
    List<String> getCategories() {
        if (!categories.isEmpty()) {
            return categories;
        }
        openDatabase();
        try {
            Cursor c = mDB.query("HADIS", new String[]{"KONU"}, null, null, "KONU", null, "ID");
            c.moveToFirst();
            do {
                String cat = c.getString(c.getColumnIndex("KONU"));

                if (!categories.contains(cat)) {
                    categories.add(cat);
                }
            } while (c.moveToNext());
            // Collections.sort(categories);
            c.close();
            return categories;
        } finally {
            closeDatabase();
        }
    }


    private synchronized void openDatabase() throws SQLException {
        mOpenCounter.incrementAndGet();
        if (mDB == null) {
            String lang = Prefs.getLanguage("en", "de", "tr");
            mDB = SQLiteDatabase.openDatabase(FILE.getAbsolutePath() + "/" + lang + "/hadis.db", null, SQLiteDatabase.OPEN_READWRITE);
        }

    }

    private synchronized void closeDatabase() throws SQLException {
        if (mOpenCounter.decrementAndGet() == 0 && mDB != null) {
            mDB.close();
            mDB = null;
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        FILE.delete();
    }

    static class Hadis {
        int id;
        String konu;
        String detay;
        String hadis;
        String kaynak;
    }

}