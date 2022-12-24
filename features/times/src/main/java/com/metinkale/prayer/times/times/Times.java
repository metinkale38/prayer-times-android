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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import com.metinkale.prayer.App;
import com.metinkale.prayer.Preferences;
import com.metinkale.prayer.times.alarm.Alarm;
import com.metinkale.prayer.times.alarm.AlarmService;
import com.metinkale.prayer.times.times.sources.FaziletTimes;
import com.metinkale.prayer.utils.livedata.LiveDataAwareList;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public abstract class Times extends TimesBase {


    @NonNull
    private final static LiveDataAwareList<Times> sTimes = new LiveDataAwareList<>();


    protected Times(long id) {
        super(id);

        if (!sTimes.contains(this)) {
            sTimes.add(this);
            createDefaultAlarms();
        }
    }

    protected Times() {
        super();
    }


    public static Times getTimesAt(int index) {
        return getTimes().get(index);
    }

    @Nullable
    public static Times getTimes(long id) {
        for (Times t : getTimes()) {
            if (t != null) {
                if (t.getID() == id) {
                    return t;
                }
            }
        }
        return null;
    }

    @NonNull
    public static LiveDataAwareList<Times> getTimes() {
        if (sTimes.isEmpty()) {
            SharedPreferences prefs = App.get().getSharedPreferences("cities", 0);

            Set<String> keys = prefs.getAll().keySet();
            for (String key : keys) {
                if (key.startsWith("id")) {
                    Times t = TimesBase.from(Long.parseLong(key.substring(2)));
                    if (t instanceof FaziletTimes) {
                        prefs.edit().remove(key).apply();
                    } else if (t != null) {
                        sTimes.add(t);
                    }
                }
            }


            if (!sTimes.isEmpty()) {
                clearTemporaryTimes();
                sort();
            }

        }

        return sTimes;

    }

    public static void sort() {
        Collections.sort(getTimes(), Comparator.comparingInt(TimesBase::getSortId));
    }

    @NonNull
    public static List<Long> getIds() {
        List<Long> ids = new ArrayList<>();
        List<Times> times = getTimes();
        for (Times t : times) {
            if (t != null) {
                ids.add(t.getID());
            }
        }
        return ids;
    }

    public static int getCount() {
        return getTimes().size();
    }


    public static void setAlarms() {
        Pair<Alarm, LocalDateTime> nextAlarm = getNextAlarm();
        if (nextAlarm != null && nextAlarm.first != null && nextAlarm.second != null)
            AlarmService.setAlarm(App.get(), nextAlarm);
    }


    @Nullable
    private static Pair<Alarm, LocalDateTime> getNextAlarm(Times t) {
        Alarm alarm = null;
        LocalDateTime time = null;
        for (Alarm a : t.getUserAlarms()) {
            if (!a.isEnabled()) continue;
            LocalDateTime nextAlarm = a.getNextAlarm();
            if (nextAlarm == null) continue;
            if (time == null || time.isAfter(nextAlarm)) {
                alarm = a;
                time = nextAlarm;
            }
        }
        if (alarm == null)
            return null;
        alarm.setCity(t);
        return new Pair<>(alarm, time);
    }

    @Nullable
    private static Pair<Alarm, LocalDateTime> getNextAlarm() {
        Pair<Alarm, LocalDateTime> pair = null;
        for (Times t : Times.getTimes()) {
            Pair<Alarm, LocalDateTime> nextAlarm = getNextAlarm(t);
            if (pair == null || pair.second == null || (nextAlarm != null && nextAlarm.second != null && pair.second.isAfter(nextAlarm.second))) {
                pair = nextAlarm;
            }
        }
        return pair;
    }

    @NonNull
    public LocalDateTime getSabahTime(@NonNull LocalDate date) {
        return parseTime(date, getSabah(date));
    }

    @NonNull
    public LocalDateTime getAsrThaniTime(@NonNull LocalDate date) {
        return parseTime(date, getAsrThani(date));
    }

    @NonNull
    public LocalDateTime getTime(@NonNull LocalDate date, int time) {
        while (time < 0) {
            date = date.minusDays(1);
            time += Vakit.LENGTH;
        }

        while (time >= Vakit.LENGTH) {
            date = date.plusDays(1);
            time -= Vakit.LENGTH;
        }
        LocalDateTime dt = parseTime(date, getStrTime(date, Vakit.getByIndex(time))).plusMinutes(getMinuteAdj()[time]);


        int h = dt.getHourOfDay();
        if ((time >= Vakit.DHUHR.ordinal()) && (h < 5)) {
            dt = dt.plusDays(1);
        }
        return dt;
    }

    private LocalDateTime parseTime(@NonNull LocalDate date, String str) {
        if (str == null || str.equals("00:00")) {
            return LocalDate.now().toLocalDateTime(LocalTime.MIDNIGHT);
        }

        if (str.startsWith("24")) {
            str = "00" + str.substring(2);
        }

        LocalDateTime timeCal = date.toLocalDateTime(new LocalTime(str));

        // add timezone drift
        double drift = getTZFix();
        timeCal = timeCal.plusMinutes((int) Math.round(drift * 60));


        return timeCal;
    }


    public int getCurrentTime() {
        return getNextTime() - 1;
    }

    public int getNextTime() {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        int vakit = Vakit.FAJR.ordinal();
        while (!getTime(today, vakit).isAfter(now)) {
            vakit++;
        }
        return vakit;
    }

    public boolean isKerahat() {
        LocalDateTime now = LocalDateTime.now();

        int untilSun = new Period(getTime(now.toLocalDate(), Vakit.SUN.ordinal()), now, PeriodType.minutes()).getMinutes();
        if (untilSun >= 0 && untilSun < Preferences.KERAHAT_SUNRISE.get()) {
            return true;
        }

        int untilDhuhr = new Period(now, getTime(now.toLocalDate(), Vakit.DHUHR.ordinal()), PeriodType.minutes()).getMinutes();
        if ((untilDhuhr >= 0) && (untilDhuhr < (Preferences.KERAHAT_ISTIWA.get()))) {
            return true;
        }

        int untilMaghrib = new Period(now, getTime(now.toLocalDate(), Vakit.MAGHRIB.ordinal()), PeriodType.minutes()).getMinutes();
        return (untilMaghrib >= 0) && (untilMaghrib < (Preferences.KERAHAT_SUNSET.get()));
    }

    @NonNull
    @Override
    public String toString() {
        return "times_id_" + getID();
    }


    public static void clearTemporaryTimes() {
        List<Times> times = getTimes();
        for (int i = times.size() - 1; i >= 0; i--) {
            Times t = times.get(i);
            if (t != null && t.getID() < 0)
                t.delete();
        }
    }


    /**
     * @param date Date
     * @param time Time
     * @return time in formatDate "HH:mm"
     */
    @Nullable
    protected abstract String getStrTime(LocalDate date, Vakit time);


    /**
     * if the Times source has seperate Imsak/Fajr times, this can be used to provide the extra Fajr value
     *
     * @param date Date
     * @return time in formatDate "HH:mm"
     */
    @Nullable
    protected String getSabah(LocalDate date) {
        return null;
    }

    /**
     * if the Times source has an extra time for Asr (which is not the default) it can be provided with that one
     *
     * @param date Date
     * @return time in formatDate "HH:mm"
     */
    @Nullable
    protected String getAsrThani(LocalDate date) {
        return null;
    }

}
