package com.metinkale.prayerapp.vakit.times;

import android.content.SharedPreferences;
import com.crashlytics.android.Crashlytics;
import com.metinkale.prayerapp.App;
import com.metinkale.prayerapp.Utils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.metinkale.prayerapp.vakit.times.Times.getIds;
import static com.metinkale.prayerapp.vakit.times.Times.getTimes;

/**
 * Created by metin on 03.04.2016.
 */
public class AbstractTimesStorage {

    private final long id;
    private JSONObject map;
    private boolean deleted = false;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public void delete() {
        deleted = true;

        editor.remove("id" + id);
        apply();

        getTimes().remove(this);
    }

    private void apply() {
        App.aHandler.removeCallbacks(mApplyPrefs);
        App.aHandler.post(mApplyPrefs);
    }

    public AbstractTimesStorage(long id) {
        this.id = id;
        prefs = App.getContext().getSharedPreferences("cities", 0);
        editor = prefs.edit();

        String json = prefs.getString("id" + id, null);

        try {
            map = new JSONObject(json);
        } catch (Exception e) {
            map = new JSONObject();
        }
    }

    private void save() {
        if (deleted) return;
        if (prefs.contains("id" + id)) {
            editor.putString("id" + id, map.toString());
            apply();
        } else {
            editor.putString("id" + id, map.toString());
            apply();
            Times.clearTimes();
        }
    }

    public boolean deleted() {
        return deleted;
    }

    public static void drop(int from, int to) {
        List<Long> keys = getIds();
        Long key = keys.get(from);
        keys.remove(key);
        keys.add(to, key);
        for (Long i : keys) {
            getTimes(i).setSortId(keys.indexOf(i));
        }

        Times.sort();
    }


    String _getTime(int d, int m, int y, int time) {
        return getString(y + "-" + Utils.az(m) + "-" + Utils.az(d) + "-" + time, "00:00");
    }

    void setTime(int d, int m, int y, int time, String value) {
        set(y + "-" + Utils.az(m) + "-" + Utils.az(d) + "-" + time, value);
    }

    void setTimes(int d, int m, int y, String[] value) {
        for (int i = 0; i < value.length; i++)
            setTime(d, m, y, i, value[i]);
    }

    public long getID() {
        return id;
    }

    public void set(String key, String value) {
        try {
            map.put(key, value);
            save();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void set(String key, int[] value) {
        try {
            List<Integer> v = new ArrayList<>();
            for (int i : value)
                v.add(i);
            map.put(key, new JSONArray(v));
            save();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void set(String key, int value) {
        try {
            map.put(key, value);
            save();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void set(String key, double value) {
        try {
            map.put(key, value);
            save();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void set(String key, boolean value) {
        set(key, value ? 1 : 0);
    }

    public String getString(String key) {
        return getString(key, null);
    }

    public String getString(String key, String def) {
        try {
            return map.getString(key);
        } catch (JSONException e) {
        }
        return def;
    }

    public int getInt(String key) {
        return getInt(key, 0);
    }

    public int getInt(String key, int def) {
        try {
            return map.getInt(key);
        } catch (JSONException e) {
        }
        return def;
    }

    public double getDouble(String key) {
        return getDouble(key, 0);
    }


    public double getDouble(String key, double def) {
        try {
            return map.getDouble(key);
        } catch (JSONException e) {
        }
        return def;
    }

    public boolean is(String key) {
        return getInt(key, 0) == 1;
    }

    public int[] getIntArray(String key, int[] def) {
        if (!map.has(key)) return def;
        try {
            JSONArray array = map.getJSONArray(key);
            int len = array.length();
            int[] ret = new int[len];
            for (int i = 0; i < len; i++) {
                ret[i] = array.getInt(i);
            }
            return ret;
        } catch (Exception e1) {
            try {
                String[] smins = getString(key).split(",");
                int[] mins = new int[def.length];
                for (int i = 0; i < mins.length; i++) {
                    try {
                        mins[i] = Integer.parseInt(smins[i]);
                    } catch (Exception ignore) {
                        Crashlytics.logException(ignore);
                    }
                }
                return mins;
            } catch (Exception e2) {
            }
        }
        return def;
    }

    private Runnable mApplyPrefs = new Runnable() {
        @Override
        public void run() {
            editor.apply();
            editor = prefs.edit();
        }
    };
}
