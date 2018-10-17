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

package com.metinkale.prayer.times.alarm;


import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.collection.ArraySet;

import com.metinkale.prayer.App;
import com.metinkale.prayer.times.R;
import com.metinkale.prayer.times.sounds.Sound;
import com.metinkale.prayer.times.times.Times;
import com.metinkale.prayer.times.times.Vakit;
import com.metinkale.prayer.utils.UUID;
import com.metinkale.prayer.utils.VibrationPreference;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode(of = "id")
public class Alarm implements Comparable<Alarm> {
    private static final Set<Vakit> ALL_TIMES = Collections.unmodifiableSet(new ArraySet<>(Arrays.asList(Vakit.IMSAK, Vakit.OGLE, Vakit.IKINDI, Vakit.AKSAM, Vakit.YATSI)));
    public static final Collection<Integer> ALL_WEEKDAYS = Collections.unmodifiableCollection(Arrays.asList(Calendar.MONDAY, Calendar.TUESDAY,
            Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY));
    public static final int VOLUME_MODE_RINGTONE = -1;
    public static final int VOLUME_MODE_NOTIFICATION = -2;
    public static final int VOLUME_MODE_ALARM = -3;
    public static final int VOLUME_MODE_MEDIA = -4;

    @Getter @Setter
    private int id;
    @Getter @Setter
    private boolean enabled = true;
    @Getter @Setter
    private Set<Integer> weekdays = new ArraySet<>(ALL_WEEKDAYS);
    @Getter @Setter
    private List<Sound> sounds = new ArrayList<>();
    @Getter @Setter
    private Set<Vakit> times = new ArraySet<>(ALL_TIMES);
    @Getter @Setter
    private int mins;
    @Getter @Setter
    private boolean vibrate;
    @Getter @Setter
    private boolean removeNotification;
    @Getter @Setter
    private int silenter;
    @Getter @Setter
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

    public LocalDateTime getNextAlarm() {

        Times city = getCity();

        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        for (int i = 0; i <= 7; i++) {
            LocalDate date = today.plusDays(i);
            if (!weekdays.contains(date.getDayOfWeek())) continue;

            for (Vakit vakit : Vakit.values()) {
                if (!times.contains(vakit)) continue;
                LocalDateTime time = city.getLocalDateTime(date, vakit.index);
                if (time.isAfter(now)) return time;
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

    public String getTitle() {

        boolean eachDay = getWeekdays().size() == 7;
        int strRes1 = eachDay ? R.string.noti_eachDay : R.string.noti_weekday;

        String days = null;
        if (!eachDay) {
            StringBuilder daysBuilder = new StringBuilder();
            String[] namesOfDays = DateFormatSymbols.getInstance().getShortWeekdays();
            for (int i : getWeekdays()) {
                daysBuilder.append("/").append(namesOfDays[i]);
            }
            days = daysBuilder.toString().substring(1);
        }


        boolean eachPrayerTime = ALL_TIMES.equals(getTimes());
        int strRes2;
        if (getMins() < 0) {
            strRes2 = eachPrayerTime ? R.string.noti_beforeAll : R.string.noti_beforeTime;
        } else if (getMins() > 0) {
            strRes2 = eachPrayerTime ? R.string.noti_afterAll : R.string.noti_afterAll;
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
        if (comp != 0) return comp;
        comp = getMins() - o.getMins();
        if (comp != 0) return comp;
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
                return VOLUME_MODE_RINGTONE;

        }
    }


    public void vibrate(Context c) {
        if (!isVibrate()) return;
        Vibrator v = (Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createWaveform(VibrationPreference.getPattern(c, "vibration"), -1));
        } else {
            v.vibrate(VibrationPreference.getPattern(c, "vibration"), -1);
        }


    }
}
