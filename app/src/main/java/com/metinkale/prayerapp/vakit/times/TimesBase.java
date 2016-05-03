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

import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapterFactory;
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
public class TimesBase {
    private static final Gson GSON;

    static {
        GsonBuilder b = new GsonBuilder();
        BooleanSerializer booleanSerializer = new BooleanSerializer();


        TypeAdapterFactory subTypeFactory = RuntimeTypeAdapterFactory
                .of(Times.class, "source")
                .registerSubtype(IGMGTimes.class, "IGMG")
                .registerSubtype(CalcTimes.class, "Calc")
                .registerSubtype(DiyanetTimes.class, "Diyanet")
                .registerSubtype(FaziletTimes.class, "Fazilet")
                .registerSubtype(SemerkandTimes.class, "Semerkand")
                .registerSubtype(NVCTimes.class, "NVC");


        b.registerTypeAdapterFactory(subTypeFactory);
        b.registerTypeAdapter(Boolean.class, booleanSerializer);
        b.registerTypeAdapter(boolean.class, booleanSerializer);

        GSON = b.create();
    }

    transient long ID;
    private String name, source, cuma_sound;
    private boolean deleted, cuma_vibration, sabah_afterImsak, cuma, ongoing;
    private double timezone, lng, lat;
    private int sortId, cuma_silenter, cuma_time, sabah_time;
    private int[] minuteAdj = new int[6];
    private String IMSAK_dua, SABAH_dua, GUNES_DUA, OGLE_dua, IKINDI_dua, AKSAM_dua, YATSI_dua;
    private int IMSAK_silenter, SABAH_silenter, GUNES_silenter, OGLE_silenter, IKINDI_silenter, AKSAM_silenter, YATSI_silenter;
    private String IMSAK_sound, SABAH_sound, GUNES_sound, OGLE_sound, IKINDI_sound, AKSAM_sound, YATSI_sound;
    private String pre_IMSAK_sound, pre_SABAH_sound, pre_GUNES_sound, pre_OGLE_sound, pre_IKINDI_sound, pre_AKSAM_sound, pre_YATSI_sound;
    private boolean pre_IMSAK_vibration, pre_SABAH_vibration, pre_GUNES_vibration, pre_OGLE_vibration, pre_IKINDI_vibration, pre_AKSAM_vibration, pre_YATSI_vibration;
    private boolean IMSAK_vibration, SABAH_vibration, GUNES_vibration, OGLE_vibration, IKINDI_vibration, AKSAM_vibration, YATSI_vibration;
    private boolean IMSAK, SABAH, GUNES, OGLE, IKINDI, AKSAM, YATSI;
    private boolean pre_IMSAK, pre_SABAH, pre_GUNES, pre_OGLE, pre_IKINDI, pre_AKSAM, pre_YATSI;
    private int pre_IMSAK_silenter, pre_SABAH_silenter, pre_GUNES_silenter, pre_OGLE_silenter, pre_IKINDI_silenter, pre_AKSAM_silenter, pre_YATSI_silenter;
    private int pre_IMSAK_time, pre_SABAH_time, pre_GUNES_time, pre_OGLE_time, pre_IKINDI_time, pre_AKSAM_time, pre_YATSI_time;

    private transient SharedPreferences prefs;
    private transient SharedPreferences.Editor editor;
    private transient Runnable mApplyPrefs = new Runnable() {
        @Override
        public void run() {
            synchronized (TimesBase.this) {
                String json = GSON.toJson(TimesBase.this);
                editor.putString("id" + ID, json);
                editor.apply();
            }
        }
    };

    public TimesBase(long id) {
        this();
        ID = id;
        source = getSource().name();
    }

    public TimesBase() {
        prefs = App.getContext().getSharedPreferences("cities", 0);
        editor = prefs.edit();
        source = getSource().name();
    }

    public static Times from(long id) {
        String json = App.getContext().getSharedPreferences("cities", 0).getString("id" + id, null);
        try {
            Times t = GSON.fromJson(json, Times.class);
            t.ID = id;
            return t;
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


    public void delete() {
        deleted = true;

        editor.remove("id" + ID);
        editor.apply();

        getTimes().remove(this);
    }

    private void apply() {
        App.aHandler.removeCallbacks(mApplyPrefs);
        App.aHandler.post(mApplyPrefs);
    }

    protected void save() {
        if (deleted) return;
        apply();
        if (!prefs.contains("id" + ID)) Times.clearTimes();

    }


    public boolean deleted() {
        return deleted;
    }


    public long getID() {
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

    public synchronized void setSource(Source source) {
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


    public synchronized int[] getMinuteAdj() {
        return minuteAdj;
    }

    public synchronized void setMinuteAdj(int[] adj) {
        if (adj.length != 6)
            throw new RuntimeException("setMinuteAdj(double[] adj) can only be called with adj of size 6");
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
        if (s == null) return "silent";
        return s;

    }

    public synchronized void setCumaSound(String value) {
        cuma_sound = value;
        save();
    }

    public synchronized int getCumaTime() {
        int t = cuma_time;
        if (t == 0) return 15;
        return t;
    }

    public synchronized void setCumaTime(int value) {
        cuma_time = value;
        save();
    }

    public synchronized String getDua(Vakit v) {
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
        if (dua == null) return "silent";
        return dua;
    }

    public synchronized int getEarlySilenterDuration(Vakit v) {
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


    public synchronized String getEarlySound(Vakit v) {
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
        if (sound == null) return "silent";
        return sound;
    }

    public synchronized int getEarlyTime(Vakit v) {
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
        if (time == 0) return 15;
        return time;
    }

    public synchronized int getSabahTime() {
        int t = sabah_time;
        if (t == 0) return 30;
        return t;
    }

    public synchronized void setSabahTime(int time) {
        sabah_time = time;
    }

    public synchronized int getSilenterDuration(Vakit v) {
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

    public synchronized String getSound(Vakit v) {
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
        if (sound == null) return "silent";
        return sound;
    }

    public synchronized boolean hasCumaVibration() {
        return cuma_vibration;
    }

    public synchronized boolean hasEarlyVibration(Vakit v) {
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

    public synchronized boolean hasVibration(Vakit v) {
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
    }

    public synchronized boolean isCumaActive() {
        return cuma;
    }

    public synchronized void setCumaActive(boolean value) {
        cuma = value;
        save();
    }

    public synchronized boolean isEarlyNotificationActive(Vakit v) {
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

    public synchronized boolean isNotificationActive(Vakit v) {
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
        WidgetService.updateOngoing();
    }

    public synchronized void setCumaVibration(boolean value) {
        cuma_vibration = value;
    }

    public synchronized void setDua(Vakit v, String value) {
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

    public synchronized void setEarlyNotificationActive(Vakit v, boolean value) {
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

    public synchronized void setEarlySilenterDuration(Vakit v, int value) {
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

    public synchronized void setEarlySound(Vakit v, String value) {
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

    public synchronized void setEarlyTime(Vakit v, int value) {
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

    public synchronized void setEarlyVibration(Vakit v, boolean value) {
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

    public synchronized void setNotificationActive(Vakit v, boolean value) {
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

    public synchronized void setSilenterDuration(Vakit v, int value) {

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

    public synchronized void setSound(Vakit v, String value) {
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

    public synchronized void setVibration(Vakit v, boolean value) {
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

}
