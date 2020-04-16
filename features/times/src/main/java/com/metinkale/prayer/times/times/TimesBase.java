/*
 * Copyright (c) 2013-2019 Metin Kale
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

package com.metinkale.prayer.times.times;

import android.content.SharedPreferences;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.collection.ArraySet;

import com.metinkale.prayer.App;
import com.metinkale.prayer.times.LocationReceiver;
import com.metinkale.prayer.times.alarm.Alarm;
import com.metinkale.prayer.times.gson.GSONFactory;
import com.metinkale.prayer.utils.livedata.TransientLiveData;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


/**
 * Created by metin on 03.04.2016.
 */
public abstract class TimesBase extends TransientLiveData<Times> {

    private transient final SharedPreferences prefs;
    private transient long ID;//all ids created since 07.04.2018 fit into int, consider switching to int at sometime
    private transient boolean deleted;

    private String name;
    private String source;
    private boolean ongoing;
    private double timezone;
    private double lng;
    private double lat;
    private double elv;
    private int sortId = Integer.MAX_VALUE;
    private int[] minuteAdj = new int[6];
    private boolean autoLocation = false;


    private Set<Alarm> alarms = new HashSet<>();

    private final transient Runnable mApplyPrefs = new Runnable() {
        @Override
        public void run() {
            String json = GSONFactory.build().toJson(TimesBase.this);
            prefs.edit().putString("id" + ID, json).apply();
            setValue((Times) TimesBase.this);
        }
    };


    public void setAutoLocation(boolean autoLocation) {
        this.autoLocation = autoLocation;
        if (autoLocation)
            LocationReceiver.start(App.get());
        save();
    }

    public boolean isAutoLocation() {
        return autoLocation;
    }


    public synchronized Set<Alarm> getUserAlarms() {
        return alarms;
    }

    protected void createDefaultAlarms() {
        if (alarms.isEmpty()) {
            for (Vakit v : Vakit.values()) {
                Alarm alarm = new Alarm();
                alarm.setCity((Times) this);
                alarm.setEnabled(false);
                alarm.setMins(0);
                alarm.setRemoveNotification(false);
                alarm.setVibrate(false);
                alarm.setWeekdays(new HashSet<>(Alarm.ALL_WEEKDAYS));
                alarm.setTimes(new ArraySet<>(Collections.singletonList(v)));
                alarms.add(alarm);
            }

            save();
        }
    }


    public Alarm getAlarm(int id) {
        for (Alarm alarm : alarms) {
            if (alarm.getId() == id) {
                return alarm;
            }
        }
        return null;
    }


    TimesBase(long id) {
        this();
        ID = id;
        source = getSource().name();
    }

    TimesBase() {
        prefs = App.get().getSharedPreferences("cities", 0);
        source = getSource().name();

        if (Looper.myLooper() == Looper.getMainLooper()) {
            setValue((Times) this);
        } else {
            postValue((Times) this);
        }
    }

    protected static Times from(long id) {
        String json = App.get().getSharedPreferences("cities", 0).getString("id" + id, null);
        try {
            Times t = GSONFactory.build().fromJson(json, Times.class);
            t.setID(id);
            return t;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public synchronized void delete() {
        deleted = true;

        prefs.edit().remove("id" + ID).apply();

        //noinspection SuspiciousMethodCalls
        Times.getTimes().remove(this);
    }

    public synchronized void save() {
        if (ID < 0 || deleted) {
            return;
        }
        App.get().getHandler().removeCallbacks(mApplyPrefs);
        App.get().getHandler().post(mApplyPrefs);
    }


    public boolean isDeleted() {
        return deleted;
    }


    public long getID() {
        return ID;
    }

    public int getIntID() {
        return (int) (ID >= Integer.MAX_VALUE ? ID / 1000 : ID);
    }

    public int getSortId() {
        return sortId;
    }

    public void setSortId(int sortId) {
        this.sortId = sortId;
        save();
    }

    public Source getSource() {
        return Source.valueOf(source);
    }

    public void setSource(@NonNull Source source) {
        this.source = source.name();
        save();
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double value) {
        lng = value;
        save();
    }

    public double getLat() {
        return lat;
    }

    public double getElv() {
        return elv;
    }

    public void setLat(double value) {
        lat = value;
        save();
    }

    public void setElv(double elv) {
        this.elv = elv;
        save();
    }

    @NonNull
    public int[] getMinuteAdj() {
        return minuteAdj;
    }

    public void setMinuteAdj(@NonNull int[] adj) {
        if (adj.length != 6) {
            throw new RuntimeException("setMinuteAdj(double[] adj) can only be called with adj of size 6");
        }
        minuteAdj = adj;
        save();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        save();
    }

    public double getTZFix() {
        return timezone;
    }

    public void setTZFix(double tz) {
        timezone = tz;
        save();
    }


    public boolean isOngoingNotificationActive() {
        return ongoing;
    }

    public void setOngoingNotificationActive(boolean value) {
        ongoing = value;
        save();
    }


    protected void setID(long ID) {
        this.ID = ID;
    }
}
