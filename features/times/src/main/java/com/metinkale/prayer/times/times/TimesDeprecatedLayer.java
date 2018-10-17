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

package com.metinkale.prayer.times.times;

import android.content.Context;
import android.media.AudioManager;
import android.os.Looper;
import android.preference.PreferenceManager;

import com.crashlytics.android.Crashlytics;
import com.metinkale.prayer.App;
import com.metinkale.prayer.times.alarm.Alarm;
import com.metinkale.prayer.times.sounds.Sounds;
import com.metinkale.prayer.utils.livedata.TransientLiveData;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * deprecated member variables moved here to be able to migrate
 */
@Deprecated
public class TimesDeprecatedLayer extends TransientLiveData<Times> {


    TimesDeprecatedLayer() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            setValue((Times) this);
        } else {
            postValue((Times) this);
        }
    }


    public static int getStreamType(Context c) {
        String ezanvolume = PreferenceManager.getDefaultSharedPreferences(c).getString("ezanvolume", "noti");
        switch (ezanvolume) {
            case "alarm":
                return AudioManager.STREAM_ALARM;
            case "media":
                return AudioManager.STREAM_MUSIC;
            default:
                return AudioManager.STREAM_RING;

        }
    }

    private void addSound(Alarm alarm, String sound) {
        if (sound != null && !sound.startsWith("silent")) {

            int volume = 0;
            if (sound.contains("$volume")) {
                volume = Integer.parseInt(sound.substring(sound.indexOf("$volume") + 7));
                sound = sound.substring(0, sound.indexOf("$volume"));
            }

            if (volume > 0) {
                try {
                    AudioManager am = (AudioManager) App.get().getSystemService(Context.AUDIO_SERVICE);
                    float oldmax = am.getStreamMaxVolume(getStreamType(App.get()));
                    float newmax = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC); //music volume should be the finest

                    volume = (int) (volume / oldmax * newmax);
                } catch (Exception e) {
                    Crashlytics.logException(e);
                }
                alarm.setVolume(volume);
            }


            alarm.getSounds().add(Sounds.getSound(sound));
        }
    }

    public List<Alarm> migrateAlarms() {
        List<Alarm> alarms = new ArrayList<>();
        boolean force = false;

        if (cuma || force) {
            Alarm alarm = new Alarm();
            alarm.setCity((Times) this);
            alarm.getTimes().add(Vakit.OGLE);
            alarm.setVibrate(cuma_vibration);
            alarm.setSilenter(cuma_silenter);
            alarm.setMins(-(cuma_time == 0 ? 15 : cuma_time));
            addSound(alarm, cuma_sound);
            alarm.getWeekdays().add(Calendar.FRIDAY);
            alarms.add(alarm);
            cuma = false;
        }

        if (SABAH || force) {
            Alarm alarm = new Alarm();
            alarm.setCity((Times) this);
            alarm.getTimes().add(sabah_afterImsak ? Vakit.IMSAK : Vakit.GUNES);
            alarm.setVibrate(SABAH_vibration);
            alarm.setSilenter(SABAH_silenter);
            if (sabah_time == 0) sabah_time = 30;
            alarm.setMins(sabah_afterImsak ? sabah_time : -sabah_time);
            addSound(alarm, SABAH_sound);
            addSound(alarm, SABAH_dua);
            alarms.add(alarm);
            SABAH = false;
        }

        if (IMSAK || force) {
            Alarm alarm = new Alarm();
            alarm.setCity((Times) this);
            alarm.getTimes().add(Vakit.IMSAK);
            alarm.setVibrate(IMSAK_vibration);
            alarm.setSilenter(IMSAK_silenter);
            addSound(alarm, IMSAK_sound);
            addSound(alarm, IMSAK_dua);
            alarms.add(alarm);
            IMSAK = false;
        }

        if (GUNES || force) {
            Alarm alarm = new Alarm();
            alarm.setCity((Times) this);
            alarm.getTimes().add(Vakit.GUNES);
            alarm.setVibrate(GUNES_vibration);
            alarm.setSilenter(GUNES_silenter);
            addSound(alarm, GUNES_sound);
            addSound(alarm, GUNES_DUA);
            alarms.add(alarm);
            GUNES = false;
        }

        if (OGLE || force) {
            Alarm alarm = new Alarm();
            alarm.setCity((Times) this);
            alarm.getTimes().add(Vakit.OGLE);
            alarm.setVibrate(OGLE_vibration);
            alarm.setSilenter(OGLE_silenter);
            addSound(alarm, OGLE_sound);
            addSound(alarm, OGLE_dua);
            alarms.add(alarm);
            OGLE = false;
        }

        if (IKINDI || force) {
            Alarm alarm = new Alarm();
            alarm.setCity((Times) this);
            alarm.getTimes().add(Vakit.IKINDI);
            alarm.setVibrate(IKINDI_vibration);
            alarm.setSilenter(IKINDI_silenter);
            addSound(alarm, IKINDI_sound);
            addSound(alarm, IKINDI_dua);
            alarms.add(alarm);
            IKINDI = false;
        }


        if (AKSAM || force) {
            Alarm alarm = new Alarm();
            alarm.setCity((Times) this);
            alarm.getTimes().add(Vakit.AKSAM);
            alarm.setVibrate(AKSAM_vibration);
            alarm.setSilenter(AKSAM_silenter);
            addSound(alarm, AKSAM_sound);
            addSound(alarm, AKSAM_dua);
            alarms.add(alarm);
            AKSAM = false;
        }

        if (YATSI || force) {
            Alarm alarm = new Alarm();
            alarm.setCity((Times) this);
            alarm.getTimes().add(Vakit.YATSI);
            alarm.setVibrate(YATSI_vibration);
            alarm.setSilenter(YATSI_silenter);
            addSound(alarm, YATSI_sound);
            addSound(alarm, YATSI_dua);
            alarms.add(alarm);
            YATSI = false;
        }


        if (pre_IMSAK || force) {
            Alarm alarm = new Alarm();
            alarm.setCity((Times) this);
            alarm.getTimes().add(Vakit.IMSAK);
            alarm.setVibrate(pre_IMSAK_vibration);
            alarm.setSilenter(pre_IMSAK_silenter);
            addSound(alarm, pre_IMSAK_sound);
            alarm.setMins(-(pre_IMSAK_time == 0 ? 15 : pre_IMSAK_time));
            alarms.add(alarm);
            pre_IMSAK = false;
        }

        if (pre_GUNES || force) {
            Alarm alarm = new Alarm();
            alarm.setCity((Times) this);
            alarm.getTimes().add(Vakit.GUNES);
            alarm.setVibrate(pre_GUNES_vibration);
            alarm.setSilenter(pre_GUNES_silenter);
            addSound(alarm, pre_GUNES_sound);
            alarm.setMins(-(pre_GUNES_time == 0 ? 15 : pre_GUNES_time));
            alarms.add(alarm);
            pre_GUNES = false;
        }

        if (pre_OGLE || force) {
            Alarm alarm = new Alarm();
            alarm.setCity((Times) this);
            alarm.getTimes().add(Vakit.OGLE);
            alarm.setVibrate(pre_OGLE_vibration);
            alarm.setSilenter(pre_OGLE_silenter);
            addSound(alarm, pre_OGLE_sound);
            alarm.setMins(-(pre_OGLE_time == 0 ? 15 : pre_OGLE_time));
            alarms.add(alarm);
            pre_OGLE = false;
        }

        if (pre_IKINDI || force) {
            Alarm alarm = new Alarm();
            alarm.setCity((Times) this);
            alarm.getTimes().add(Vakit.IKINDI);
            alarm.setVibrate(pre_IKINDI_vibration);
            alarm.setSilenter(pre_IKINDI_silenter);
            addSound(alarm, pre_IKINDI_sound);
            alarm.setMins(-(pre_IKINDI_time == 0 ? 15 : pre_IKINDI_time));
            alarms.add(alarm);
            pre_IKINDI = false;
        }


        if (pre_AKSAM || force) {
            Alarm alarm = new Alarm();
            alarm.setCity((Times) this);
            alarm.getTimes().add(Vakit.AKSAM);
            alarm.setVibrate(pre_AKSAM_vibration);
            alarm.setSilenter(pre_AKSAM_silenter);
            alarm.setMins(-(pre_AKSAM_time == 0 ? 15 : pre_AKSAM_time));
            addSound(alarm, pre_AKSAM_sound);
            alarms.add(alarm);
            pre_AKSAM = false;
        }

        if (pre_YATSI || force) {
            Alarm alarm = new Alarm();
            alarm.setCity((Times) this);
            alarm.getTimes().add(Vakit.YATSI);
            alarm.setVibrate(pre_YATSI_vibration);
            alarm.setSilenter(pre_YATSI_silenter);
            alarm.setMins(-(pre_YATSI_time == 0 ? 15 : pre_YATSI_time));
            addSound(alarm, pre_YATSI_sound);
            alarms.add(alarm);
            pre_YATSI = false;
        }

        return alarms;
    }


    private boolean cuma;
    private String cuma_sound;
    private boolean cuma_vibration;
    private int cuma_silenter;
    private int cuma_time;
    private int sabah_time;
    private boolean sabah_afterImsak;
    private boolean SABAH_vibration;
    private boolean SABAH;
    private String SABAH_dua;
    private int SABAH_silenter;
    private String SABAH_sound;
    private boolean IMSAK_vibration;
    private boolean GUNES_vibration;
    private boolean OGLE_vibration;
    private boolean IKINDI_vibration;
    private boolean AKSAM_vibration;
    private boolean YATSI_vibration;
    private boolean IMSAK;
    private boolean GUNES;
    private boolean OGLE;
    private boolean IKINDI;
    private boolean AKSAM;
    private boolean YATSI;
    private String IMSAK_dua;
    private String GUNES_DUA;
    private String OGLE_dua;
    private String IKINDI_dua;
    private String AKSAM_dua;
    private String YATSI_dua;
    private int IMSAK_silenter;
    private int GUNES_silenter;
    private int OGLE_silenter;
    private int IKINDI_silenter;
    private int AKSAM_silenter;
    private int YATSI_silenter;
    private String IMSAK_sound;
    private String GUNES_sound;
    private String OGLE_sound;
    private String IKINDI_sound;
    private String AKSAM_sound;
    private String YATSI_sound;
    private String pre_IMSAK_sound;
    private String pre_GUNES_sound;
    private String pre_OGLE_sound;
    private String pre_IKINDI_sound;
    private String pre_AKSAM_sound;
    private String pre_YATSI_sound;
    private boolean pre_IMSAK_vibration;
    private boolean pre_GUNES_vibration;
    private boolean pre_OGLE_vibration;
    private boolean pre_IKINDI_vibration;
    private boolean pre_AKSAM_vibration;
    private boolean pre_YATSI_vibration;
    private boolean pre_IMSAK;
    private boolean pre_GUNES;
    private boolean pre_OGLE;
    private boolean pre_IKINDI;
    private boolean pre_AKSAM;
    private boolean pre_YATSI;
    private int pre_IMSAK_silenter;
    private int pre_GUNES_silenter;
    private int pre_OGLE_silenter;
    private int pre_IKINDI_silenter;
    private int pre_AKSAM_silenter;
    private int pre_YATSI_silenter;
    private int pre_IMSAK_time;
    private int pre_GUNES_time;
    private int pre_OGLE_time;
    private int pre_IKINDI_time;
    private int pre_AKSAM_time;
    private int pre_YATSI_time;

}
