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

package com.metinkale.prayer.times.alarm;


import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.collection.ArraySet;

import com.metinkale.prayer.App;
import com.metinkale.prayer.times.R;
import com.metinkale.prayer.times.alarm.sounds.Sound;
import com.metinkale.prayer.times.times.Times;
import com.metinkale.prayer.times.times.Vakit;
import com.metinkale.prayer.utils.UUID;
import com.metinkale.prayer.utils.Utils;

import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.Period;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class Alarm implements Comparable<Alarm> {
    public static final Set<Vakit> ALL_TIMES =
            Collections.unmodifiableSet(new ArraySet<>(Arrays.asList(Vakit.FAJR, Vakit.DHUHR, Vakit.ASR, Vakit.MAGHRIB, Vakit.ISHAA)));
    public static final Collection<Integer> ALL_WEEKDAYS = Collections.unmodifiableCollection(
            Arrays.asList(Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY,
                    Calendar.SUNDAY));
    public static final int VOLUME_MODE_RINGTONE = -1;
    public static final int VOLUME_MODE_NOTIFICATION = -2;
    public static final int VOLUME_MODE_ALARM = -3;
    public static final int VOLUME_MODE_MEDIA = -4;

    private int id;

    private boolean enabled = false;
    private ArraySet<Integer> weekdays = new ArraySet<>(ALL_WEEKDAYS);
    private ArrayList<Sound> sounds = new ArrayList<>();
    private ArraySet<Vakit> times = new ArraySet<>(ALL_TIMES);
    private int mins;
    private boolean vibrate;
    private boolean randomsound;
    private boolean removeNotification;
    private int silenter;
    private int volume = getLegacyVolumeMode(App.get());

    private long cityId;

    public static Alarm fromId(int id) {
        for (Times t : Times.getTimes()) {
            for (Alarm a : t.getUserAlarms()) {
                if (a.getId() == id) {
                    return a;
                }
            }
        }
        return null;
    }

    private static int jodaWDToJavaWD(int wd) {
        switch (wd) {
            case DateTimeConstants.MONDAY:
                return Calendar.MONDAY;
            case DateTimeConstants.TUESDAY:
                return Calendar.TUESDAY;
            case DateTimeConstants.WEDNESDAY:
                return Calendar.WEDNESDAY;
            case DateTimeConstants.THURSDAY:
                return Calendar.THURSDAY;
            case DateTimeConstants.FRIDAY:
                return Calendar.FRIDAY;
            case DateTimeConstants.SATURDAY:
                return Calendar.SATURDAY;
            case DateTimeConstants.SUNDAY:
                return Calendar.SUNDAY;
        }
        return 0;
    }

    public LocalDateTime getNextAlarm() {

        Times city = getCity();

        if (city == null) return null;
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        for (int i = 0; i <= 7; i++) {
            LocalDate date = today.plusDays(i);
            if (!weekdays.contains(jodaWDToJavaWD(date.getDayOfWeek())))
                continue;

            for (Vakit vakit : Vakit.values()) {
                if (!times.contains(vakit))
                    continue;
                LocalDateTime time = city.getTime(date, vakit.ordinal()).plusMinutes(mins);
                if (time.isAfter(now))
                    return time;
            }
        }

        return null;
    }

    public Alarm() {
        id = UUID.asInt();
    }

    public void setCity(Times times) {
        cityId = times.getID();
    }

    public Times getCity() {
        return Times.getTimes(cityId);
    }


    public String getCurrentTitle() {
        Times city = getCity();

        int time = city.getCurrentTime();

        while(!times.isEmpty() && !times.contains(Vakit.getByIndex(time))){
            time++;
        }

        int minutes = new Period(city.getTime(LocalDate.now(), time), LocalDateTime.now()).toStandardMinutes().getMinutes();

        if(minutes > 0 && times.contains(Vakit.getByIndex(time+1))){
            int minutesToNext = new Period(city.getTime(LocalDate.now(), time + 1), LocalDateTime.now()).toStandardMinutes().getMinutes();
            if(minutes > Math.abs(minutesToNext)){
                time++;
                minutes = minutesToNext;
            }
        }

        // avoid messages like 1 minute before/after Maghtib for minimal deviations
       int minutesThreshold=2;
       if(Math.abs(minutes) < minutesThreshold){
           minutes = 0;
       }

        int strRes2;
        if (minutes < 0) {
            strRes2 = R.string.noti_beforeTime;
        } else if (minutes > 0) {
            strRes2 = R.string.noti_afterTime;
        } else {
            strRes2 = R.string.noti_exactTime;
        }

        Context ctx = App.get();
        return ctx.getString(strRes2, Math.abs(minutes), Vakit.getByIndex(time).getString());
    }


    public String getTitle() {

        boolean eachDay = getWeekdays().size() == 7;
        int strRes1 = eachDay ? R.string.noti_eachDay : R.string.noti_weekday;

        String days = null;
        if (!eachDay) {
            StringBuilder daysBuilder = new StringBuilder();
            String[] namesOfDays =
                    getWeekdays().size() == 1 ? DateFormatSymbols.getInstance().getWeekdays() : DateFormatSymbols.getInstance().getShortWeekdays();
            for (int i : getWeekdays()) {
                daysBuilder.append("/").append(namesOfDays[i]);
            }
            days = daysBuilder.toString();
            if (days.length() > 0) days = days.substring(1);
        }


        boolean eachPrayerTime = ALL_TIMES.equals(getTimes());
        int strRes2;
        if (getMins() < 0) {
            strRes2 = eachPrayerTime ? R.string.noti_beforeAll : R.string.noti_beforeTime;
        } else if (getMins() > 0) {
            strRes2 = eachPrayerTime ? R.string.noti_afterAll : R.string.noti_afterTime;
        } else {
            strRes2 = eachPrayerTime ? R.string.noti_exactAll : R.string.noti_exactTime;
        }

        String times = null;
        if (!eachPrayerTime) {
            StringBuilder timesBuilder = new StringBuilder();
            for (Vakit vakit : getTimes()) {
                timesBuilder.append("/").append(vakit.getString());
            }
            if (timesBuilder.length() > 0)
                times = timesBuilder.toString().substring(1);
        }


        Context ctx = App.get();
        return ctx.getString(strRes1, days, ctx.getString(strRes2, Math.abs(getMins()), times));

    }

    @Override
    public int compareTo(@NonNull Alarm o) {
        int comp;
        comp = Collections.min(getTimes()).ordinal() - Collections.min(o.getTimes()).ordinal();
        if (comp != 0)
            return comp;
        comp = getMins() - o.getMins();
        if (comp != 0)
            return comp;
        comp = Collections.min(getWeekdays()) - Collections.min(o.getWeekdays());
        return comp;
    }

    private static int getLegacyVolumeMode(Context c) {
        String ezanvolume = PreferenceManager.getDefaultSharedPreferences(c).getString("ezanvolume", "noti");
        switch (ezanvolume) {
            case "alarm":
                return VOLUME_MODE_ALARM;
            case "media":
                return VOLUME_MODE_MEDIA;
            case "noti":
            default:
                AudioManager am = (AudioManager) c.getSystemService(Context.AUDIO_SERVICE);
                return am.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 2;

        }
    }


    public void vibrate(Context c) {
        if (!isVibrate())
            return;
        Vibrator v = (Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createWaveform(Utils.getVibrationPattern(c, "vibration"), -1));
        } else {
            v.vibrate(Utils.getVibrationPattern(c, "vibration"), -1);
        }


    }

    public int getId() {
        return id;
    }


    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Set<Integer> getWeekdays() {
        return weekdays;
    }

    public void setWeekdays(Set<Integer> weekdays) {
        this.weekdays = new ArraySet<>(weekdays);
    }

    public List<Sound> getSounds() {
        return sounds;
    }
    public List<Sound> getRandomSound() {
        return Collections.singletonList(sounds.get(ThreadLocalRandom.current().nextInt(sounds.size())));
    }

    public void setSounds(List<Sound> sounds) {
        this.sounds = new ArrayList<>(sounds);
    }

    public Set<Vakit> getTimes() {
        return times;
    }

    public void setTimes(Set<Vakit> times) {
        this.times = new ArraySet<>(times);
    }

    public int getMins() {
        return mins;
    }

    public void setMins(int mins) {
        this.mins = mins;
    }

    public boolean isVibrate() {
        return vibrate;
    }

    public void setVibrate(boolean vibrate) {
        this.vibrate = vibrate;
    }

    public boolean isRandomSound() {
        return randomsound;
    }

    public void setRandomSound(boolean randomsound) {
        this.randomsound = randomsound;
    }

    public boolean isRemoveNotification() {
        return removeNotification;
    }

    public void setRemoveNotification(boolean removeNotification) {
        this.removeNotification = removeNotification;
    }

    public int getSilenter() {
        return silenter;
    }

    public void setSilenter(int silenter) {
        this.silenter = Math.max(0, silenter);
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Alarm alarm = (Alarm) o;
        return id == alarm.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
