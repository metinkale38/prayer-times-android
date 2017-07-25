/*
 * Copyright (c) 2013-2017 Metin Kale
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

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.metinkale.prayerapp.App;
import com.metinkale.prayerapp.vakit.WidgetService;
import com.metinkale.prayerapp.vakit.times.gson.BooleanSerializer;
import com.metinkale.prayerapp.vakit.times.gson.RuntimeTypeAdapterFactory;

import java.util.List;

import static com.metinkale.prayerapp.vakit.times.Times.getIds;
import static com.metinkale.prayerapp.vakit.times.Times.getTimes;


/**
 * Created by metin on 03.04.2016.
 */
class TimesBase {
    @NonNull
    private static final Gson GSON;

    static {
        GsonBuilder b = new GsonBuilder();
        BooleanSerializer booleanSerializer = new BooleanSerializer();


        RuntimeTypeAdapterFactory<Times> subTypeFactory = RuntimeTypeAdapterFactory
                .of(Times.class, "source");
        for (Source source : Source.values()) {
            subTypeFactory = subTypeFactory.registerSubtype(source.clz, source.name());
        }

        b.registerTypeAdapterFactory(subTypeFactory);
        b.registerTypeAdapter(Boolean.class, booleanSerializer);
        b.registerTypeAdapter(boolean.class, booleanSerializer);

        GSON = b.create();
    }

    private transient final SharedPreferences prefs;
    private transient long ID;
    private transient final Runnable mApplyPrefs = new Runnable() {
        @Override
        public void run() {
            synchronized (TimesBase.this) {
                String json = GSON.toJson(TimesBase.this);
                prefs.edit().putString("id" + ID, json).apply();
            }
        }
    };
    private String name;
    private String source;
    private boolean deleted;
    private boolean ongoing;
    private double timezone;
    private double lng;
    private double lat;
    private int sortId = Integer.MAX_VALUE;
    private int[] minuteAdj = new int[6];

    //cuma
    private boolean cuma;
    private String cuma_sound;
    private boolean cuma_vibration;
    private boolean sabah_afterImsak;
    private int cuma_silenter;
    private int cuma_time;

    //sabah
    private int sabah_time;

    //vakit
    private boolean IMSAK_vibration;
    private boolean SABAH_vibration;
    private boolean GUNES_vibration;
    private boolean OGLE_vibration;
    private boolean IKINDI_vibration;
    private boolean AKSAM_vibration;
    private boolean YATSI_vibration;
    private boolean IMSAK;
    private boolean SABAH;
    private boolean GUNES;
    private boolean OGLE;
    private boolean IKINDI;
    private boolean AKSAM;
    private boolean YATSI;
    private String IMSAK_dua;
    private String SABAH_dua;
    private String GUNES_DUA;
    private String OGLE_dua;
    private String IKINDI_dua;
    private String AKSAM_dua;
    private String YATSI_dua;
    private int IMSAK_silenter;
    private int SABAH_silenter;
    private int GUNES_silenter;
    private int OGLE_silenter;
    private int IKINDI_silenter;
    private int AKSAM_silenter;
    private int YATSI_silenter;
    private String IMSAK_sound;
    private String SABAH_sound;
    private String GUNES_sound;
    private String OGLE_sound;
    private String IKINDI_sound;
    private String AKSAM_sound;
    private String YATSI_sound;

    //pre
    private String pre_IMSAK_sound;
    private String pre_SABAH_sound;
    private String pre_GUNES_sound;
    private String pre_OGLE_sound;
    private String pre_IKINDI_sound;
    private String pre_AKSAM_sound;
    private String pre_YATSI_sound;
    private boolean pre_IMSAK_vibration;
    private boolean pre_SABAH_vibration;
    private boolean pre_GUNES_vibration;
    private boolean pre_OGLE_vibration;
    private boolean pre_IKINDI_vibration;
    private boolean pre_AKSAM_vibration;
    private boolean pre_YATSI_vibration;
    private boolean pre_IMSAK;
    private boolean pre_SABAH;
    private boolean pre_GUNES;
    private boolean pre_OGLE;
    private boolean pre_IKINDI;
    private boolean pre_AKSAM;
    private boolean pre_YATSI;
    private int pre_IMSAK_silenter;
    private int pre_SABAH_silenter;
    private int pre_GUNES_silenter;
    private int pre_OGLE_silenter;
    private int pre_IKINDI_silenter;
    private int pre_AKSAM_silenter;
    private int pre_YATSI_silenter;
    private int pre_IMSAK_time;
    private int pre_SABAH_time;
    private int pre_GUNES_time;
    private int pre_OGLE_time;
    private int pre_IKINDI_time;
    private int pre_AKSAM_time;
    private int pre_YATSI_time;

    /**
     * idea
     * class Alarm {
     * boolean active;
     * Vakit vakit;
     * int time;
     * int silenter;
     * boolean vibration;
     * String sound;
     * String dua;
     * }
     */

    TimesBase(long id) {
        this();
        ID = id;
        source = getSource().name();
    }

    TimesBase() {
        prefs = App.get().getSharedPreferences("cities", 0);
        source = getSource().name();
    }

    public static Times from(long id) {
        String json = App.get().getSharedPreferences("cities", 0).getString("id" + id, null);
        try {
            TimesBase t = GSON.fromJson(json, Times.class);
            t.setID(id);
            return (Times) t;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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


    public synchronized void delete() {
        deleted = true;

        prefs.edit().remove("id" + ID).apply();

        //noinspection SuspiciousMethodCalls
        getTimes().remove(this);
    }

    protected void save() {
        if (ID < 0 || deleted) {
            return;
        }
        App.get().getHandler().removeCallbacks(mApplyPrefs);
        App.get().getHandler().post(mApplyPrefs);
    }


    public synchronized boolean deleted() {
        return deleted;
    }


    public synchronized long getID() {
        return ID;
    }

    public synchronized int getSortId() {
        return sortId;
    }

    public synchronized void setSortId(int sortId) {
        this.sortId = sortId;
        save();
    }

    public synchronized Source getSource() {
        return Source.valueOf(source);
    }

    public synchronized void setSource(@NonNull Source source) {
        this.source = source.name();
        save();
    }

    public synchronized double getLng() {
        return lng;
    }

    public synchronized void setLng(double value) {
        lng = value;
        save();
    }

    public synchronized double getLat() {
        return lat;
    }

    public synchronized void setLat(double value) {
        lat = value;
        save();
    }


    @NonNull
    public synchronized int[] getMinuteAdj() {
        return minuteAdj;
    }

    public synchronized void setMinuteAdj(@NonNull int[] adj) {
        if (adj.length != 6) {
            throw new RuntimeException("setMinuteAdj(double[] adj) can only be called with adj of size 6");
        }
        minuteAdj = adj;
        save();
    }

    public synchronized String getName() {
        return name;
    }

    public synchronized void setName(String name) {
        this.name = name;
        save();
    }

    public synchronized double getTZFix() {
        return timezone;
    }

    public synchronized void setTZFix(double tz) {
        timezone = tz;
        save();
    }

    public synchronized int getCumaSilenterDuration() {
        return cuma_silenter;
    }

    public synchronized void setCumaSilenterDuration(int value) {
        cuma_silenter = value;
        save();
    }

    public synchronized String getCumaSound() {
        String s = cuma_sound;
        if (s == null) {
            return "silent";
        }
        return s;

    }

    public synchronized void setCumaSound(String value) {
        cuma_sound = value;
        save();
    }

    public synchronized int getCumaTime() {
        int t = cuma_time;
        if (t == 0) {
            return 15;
        }
        return t;
    }

    public synchronized void setCumaTime(int value) {
        cuma_time = value;
        save();
    }

    @Nullable
    public synchronized String getDua(@NonNull Vakit v) {
        String dua = null;
        switch (v) {
            case IMSAK:
                dua = IMSAK_dua;
                break;
            case GUNES:
                dua = GUNES_DUA;
                break;
            case SABAH:
                dua = SABAH_dua;
                break;
            case OGLE:
                dua = OGLE_dua;
                break;
            case IKINDI:
                dua = IKINDI_dua;
                break;
            case AKSAM:
                dua = AKSAM_dua;
                break;
            case YATSI:
                dua = YATSI_dua;
                break;


        }
        if (dua == null) {
            return "silent";
        }
        return dua;
    }

    public synchronized int getEarlySilenterDuration(@NonNull Vakit v) {
        switch (v) {
            case IMSAK:
                return pre_IMSAK_silenter;
            case GUNES:
                return pre_GUNES_silenter;
            case SABAH:
                return pre_SABAH_silenter;
            case OGLE:
                return pre_OGLE_silenter;
            case IKINDI:
                return pre_IKINDI_silenter;
            case AKSAM:
                return pre_AKSAM_silenter;
            case YATSI:
                return pre_YATSI_silenter;
        }
        return 0;
    }


    @Nullable
    public synchronized String getEarlySound(@NonNull Vakit v) {
        String sound = null;
        switch (v) {
            case IMSAK:
                sound = pre_IMSAK_sound;
                break;
            case GUNES:
                sound = pre_GUNES_sound;
                break;
            case SABAH:
                sound = pre_SABAH_sound;
                break;
            case OGLE:
                sound = pre_OGLE_sound;
                break;
            case IKINDI:
                sound = pre_IKINDI_sound;
                break;
            case AKSAM:
                sound = pre_AKSAM_sound;
                break;
            case YATSI:
                sound = pre_YATSI_sound;
                break;
        }
        if (sound == null) {
            return "silent";
        }
        return sound;
    }

    public synchronized int getEarlyTime(@NonNull Vakit v) {
        int time = 0;
        switch (v) {
            case IMSAK:
                time = pre_IMSAK_time;
                break;
            case GUNES:
                time = pre_GUNES_time;
                break;
            case SABAH:
                time = pre_SABAH_time;
                break;
            case OGLE:
                time = pre_OGLE_time;
                break;
            case IKINDI:
                time = pre_IKINDI_time;
                break;
            case AKSAM:
                time = pre_AKSAM_time;
                break;
            case YATSI:
                time = pre_YATSI_time;
                break;
        }
        if (time == 0) {
            return 15;
        }
        return time;
    }

    public synchronized int getSabahTime() {
        int t = sabah_time;
        if (t == 0) {
            return 30;
        }
        return t;
    }

    public synchronized void setSabahTime(int time) {
        sabah_time = time;
        save();
    }

    public synchronized int getSilenterDuration(@NonNull Vakit v) {
        switch (v) {
            case IMSAK:
                return IMSAK_silenter;
            case GUNES:
                return GUNES_silenter;
            case SABAH:
                return SABAH_silenter;
            case OGLE:
                return OGLE_silenter;
            case IKINDI:
                return IKINDI_silenter;
            case AKSAM:
                return AKSAM_silenter;
            case YATSI:
                return YATSI_silenter;
        }
        return 0;
    }

    @Nullable
    public synchronized String getSound(@NonNull Vakit v) {
        String sound = null;
        switch (v) {
            case IMSAK:
                sound = IMSAK_sound;
                break;
            case GUNES:
                sound = GUNES_sound;
                break;
            case SABAH:
                sound = SABAH_sound;
                break;
            case OGLE:
                sound = OGLE_sound;
                break;
            case IKINDI:
                sound = IKINDI_sound;
                break;
            case AKSAM:
                sound = AKSAM_sound;
                break;
            case YATSI:
                sound = YATSI_sound;
                break;
        }
        if (sound == null) {
            return "silent";
        }
        return sound;
    }

    public synchronized boolean hasCumaVibration() {
        return cuma_vibration;
    }

    public synchronized boolean hasEarlyVibration(@NonNull Vakit v) {
        switch (v) {
            case IMSAK:
                return pre_IMSAK_vibration;
            case GUNES:
                return pre_GUNES_vibration;
            case SABAH:
                return pre_SABAH_vibration;
            case OGLE:
                return pre_OGLE_vibration;
            case IKINDI:
                return pre_IKINDI_vibration;
            case AKSAM:
                return pre_AKSAM_vibration;
            case YATSI:
                return pre_YATSI_vibration;
        }
        return false;
    }

    public synchronized boolean hasVibration(@NonNull Vakit v) {
        switch (v) {
            case IMSAK:
                return IMSAK_vibration;
            case GUNES:
                return GUNES_vibration;
            case SABAH:
                return SABAH_vibration;
            case OGLE:
                return OGLE_vibration;
            case IKINDI:
                return IKINDI_vibration;
            case AKSAM:
                return AKSAM_vibration;
            case YATSI:
                return YATSI_vibration;
        }
        return false;
    }

    public synchronized boolean isAfterImsak() {
        return sabah_afterImsak;

    }

    public synchronized void setAfterImsak(boolean value) {
        sabah_afterImsak = value;
        save();
    }

    public synchronized boolean isCumaActive() {
        return cuma;
    }

    public synchronized void setCumaActive(boolean value) {
        cuma = value;
        save();
    }

    public synchronized boolean isEarlyNotificationActive(@NonNull Vakit v) {
        switch (v) {
            case IMSAK:
                return pre_IMSAK;
            case GUNES:
                return pre_GUNES;
            case SABAH:
                return pre_SABAH;
            case OGLE:
                return pre_OGLE;
            case IKINDI:
                return pre_IKINDI;
            case AKSAM:
                return pre_AKSAM;
            case YATSI:
                return pre_YATSI;
        }
        return false;

    }

    public synchronized boolean isNotificationActive(@NonNull Vakit v) {
        switch (v) {
            case IMSAK:
                return IMSAK;
            case GUNES:
                return GUNES;
            case SABAH:
                return SABAH;
            case OGLE:
                return OGLE;
            case IKINDI:
                return IKINDI;
            case AKSAM:
                return AKSAM;
            case YATSI:
                return YATSI;
        }
        return false;

    }

    public synchronized boolean isOngoingNotificationActive() {
        return ongoing;
    }

    public synchronized void setOngoingNotificationActive(boolean value) {
        ongoing = value;
        save();
        WidgetService.start(App.get());
    }

    public synchronized void setCumaVibration(boolean value) {
        cuma_vibration = value;
        save();
    }

    public synchronized void setDua(@NonNull Vakit v, String value) {
        switch (v) {
            case IMSAK:
                IMSAK_dua = value;
                break;
            case GUNES:
                GUNES_DUA = value;
                break;
            case SABAH:
                SABAH_dua = value;
                break;
            case OGLE:
                OGLE_dua = value;
                break;
            case IKINDI:
                IKINDI_dua = value;
                break;
            case AKSAM:
                AKSAM_dua = value;
                break;
            case YATSI:
                YATSI_dua = value;
                break;
        }
        save();
    }

    public synchronized void setEarlyNotificationActive(@NonNull Vakit v, boolean value) {
        switch (v) {
            case IMSAK:
                pre_IMSAK = value;
                break;
            case GUNES:
                pre_GUNES = value;
                break;
            case SABAH:
                pre_SABAH = value;
                break;
            case OGLE:
                pre_OGLE = value;
                break;
            case IKINDI:
                pre_IKINDI = value;
                break;
            case AKSAM:
                pre_AKSAM = value;
                break;
            case YATSI:
                pre_YATSI = value;
                break;
        }
        save();
    }

    public synchronized void setEarlySilenterDuration(@NonNull Vakit v, int value) {
        switch (v) {
            case IMSAK:
                pre_IMSAK_silenter = value;
                break;
            case GUNES:
                pre_GUNES_silenter = value;
                break;
            case SABAH:
                pre_SABAH_silenter = value;
                break;
            case OGLE:
                pre_OGLE_silenter = value;
                break;
            case IKINDI:
                pre_IKINDI_silenter = value;
                break;
            case AKSAM:
                pre_AKSAM_silenter = value;
                break;
            case YATSI:
                pre_YATSI_silenter = value;
                break;
        }
        save();
    }

    public synchronized void setEarlySound(@NonNull Vakit v, String value) {
        switch (v) {
            case IMSAK:
                pre_IMSAK_sound = value;
                break;
            case GUNES:
                pre_GUNES_sound = value;
                break;
            case SABAH:
                pre_SABAH_sound = value;
                break;
            case OGLE:
                pre_OGLE_sound = value;
                break;
            case IKINDI:
                pre_IKINDI_sound = value;
                break;
            case AKSAM:
                pre_AKSAM_sound = value;
                break;
            case YATSI:
                pre_YATSI_sound = value;
                break;
        }
        save();
    }

    public synchronized void setEarlyTime(@NonNull Vakit v, int value) {
        switch (v) {
            case IMSAK:
                pre_IMSAK_time = value;
                break;
            case GUNES:
                pre_GUNES_time = value;
                break;
            case SABAH:
                pre_SABAH_time = value;
                break;
            case OGLE:
                pre_OGLE_time = value;
                break;
            case IKINDI:
                pre_IKINDI_time = value;
                break;
            case AKSAM:
                pre_AKSAM_time = value;
                break;
            case YATSI:
                pre_YATSI_time = value;
                break;
        }
        save();
    }

    public synchronized void setEarlyVibration(@NonNull Vakit v, boolean value) {
        switch (v) {
            case IMSAK:
                pre_IMSAK_vibration = value;
                break;
            case GUNES:
                pre_GUNES_vibration = value;
                break;
            case SABAH:
                pre_SABAH_vibration = value;
                break;
            case OGLE:
                pre_OGLE_vibration = value;
                break;
            case IKINDI:
                pre_IKINDI_vibration = value;
                break;
            case AKSAM:
                pre_AKSAM_vibration = value;
                break;
            case YATSI:
                pre_YATSI_vibration = value;
                break;
        }
        save();

    }

    public synchronized void setNotificationActive(@NonNull Vakit v, boolean value) {
        switch (v) {
            case IMSAK:
                IMSAK = value;
                break;
            case GUNES:
                GUNES = value;
                break;
            case SABAH:
                SABAH = value;
                break;
            case OGLE:
                OGLE = value;
                break;
            case IKINDI:
                IKINDI = value;
                break;
            case AKSAM:
                AKSAM = value;
                break;
            case YATSI:
                YATSI = value;
                break;
        }
        save();
    }

    public synchronized void setSilenterDuration(@NonNull Vakit v, int value) {

        switch (v) {
            case IMSAK:
                IMSAK_silenter = value;
                break;
            case GUNES:
                GUNES_silenter = value;
                break;
            case SABAH:
                SABAH_silenter = value;
                break;
            case OGLE:
                OGLE_silenter = value;
                break;
            case IKINDI:
                IKINDI_silenter = value;
                break;
            case AKSAM:
                AKSAM_silenter = value;
                break;
            case YATSI:
                YATSI_silenter = value;
                break;
        }
        save();

    }

    public synchronized void setSound(@NonNull Vakit v, String value) {
        switch (v) {
            case IMSAK:
                IMSAK_sound = value;
                break;
            case GUNES:
                GUNES_sound = value;
                break;
            case SABAH:
                SABAH_sound = value;
                break;
            case OGLE:
                OGLE_sound = value;
                break;
            case IKINDI:
                IKINDI_sound = value;
                break;
            case AKSAM:
                AKSAM_sound = value;
                break;
            case YATSI:
                YATSI_sound = value;
                break;
        }
        save();
    }

    public synchronized void setVibration(@NonNull Vakit v, boolean value) {
        switch (v) {
            case IMSAK:
                IMSAK_vibration = value;
                break;
            case GUNES:
                GUNES_vibration = value;
                break;
            case SABAH:
                SABAH_vibration = value;
                break;
            case OGLE:
                OGLE_vibration = value;
                break;
            case IKINDI:
                IKINDI_vibration = value;
                break;
            case AKSAM:
                AKSAM_vibration = value;
                break;
            case YATSI:
                YATSI_vibration = value;
                break;
        }
        save();
    }

    private void setID(long ID) {
        this.ID = ID;
    }
}
