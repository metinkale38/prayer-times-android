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

import android.content.SharedPreferences;

import com.crashlytics.android.Crashlytics;
import com.metinkale.prayer.App;
import com.metinkale.prayer.Preferences;
import com.metinkale.prayer.times.alarm.Alarm;
import com.metinkale.prayer.times.alarm.AlarmService;
import com.metinkale.prayer.utils.LocaleUtils;
import com.metinkale.prayer.utils.livedata.LiveDataAwareList;

import org.joda.time.DateTime;
import org.joda.time.DurationFieldType;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArraySet;
import androidx.core.util.Pair;

public abstract class Times extends TimesBase {
    
    
    private static final PeriodFormatter PERIOD_FORMATTER_HMS =
            new PeriodFormatterBuilder().printZeroIfSupported().minimumPrintedDigits(2).appendHours().appendLiteral(":").minimumPrintedDigits(2)
                    .appendMinutes().appendLiteral(":").appendSeconds().toFormatter();
    private static final PeriodFormatter PERIOD_FORMATTER_HM =
            new PeriodFormatterBuilder().printZeroIfSupported().minimumPrintedDigits(2).appendHours().appendLiteral(":").minimumPrintedDigits(2)
                    .appendMinutes().toFormatter();
    
    
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
    
    private void createDefaultAlarms() {
        if (getUserAlarms().isEmpty()) {
            Alarm alarm = new Alarm();
            alarm.setCity(this);
            alarm.setEnabled(false);
            alarm.setMins(0);
            alarm.setRemoveNotification(false);
            alarm.setVibrate(true);
            alarm.setWeekdays(new ArraySet<>(Alarm.ALL_WEEKDAYS));
            alarm.setTimes(new ArraySet<>(Alarm.ALL_TIMES));
            getUserAlarms().add(alarm);
            save();
        }
    }
    
    @NonNull
    public static LiveDataAwareList<Times> getTimes() {
        if (sTimes.isEmpty()) {
            SharedPreferences prefs = App.get().getSharedPreferences("nvc", 0);
            
            Set<String> keys = prefs.getAll().keySet();
            for (String key : keys) {
                if (key.startsWith("id")) {
                    sTimes.add(TimesBase.from(Long.parseLong(key.substring(2))));
                }
            }
            
            
            if (!sTimes.isEmpty()) {
                sort();
                clearTemporaryTimes();
            }
        }
        return sTimes;
        
    }
    
    public static void sort() {
        if (sTimes.isEmpty())
            return;
        Collections.sort(sTimes, new Comparator<Times>() {
            @Override
            public int compare(Times t1, Times t2) {
                try {
                    return t1.getSortId() - t2.getSortId();
                } catch (RuntimeException e) {
                    Crashlytics.logException(e);
                    return 0;
                }
            }
        });
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
            LocalDateTime nextAlarm = a.getNextAlarm();
            if (time == null || time.isAfter(nextAlarm)) {
                alarm = a;
                time = nextAlarm;
            }
        }
        if (alarm == null || time == null)
            return null;
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
    public LocalDateTime getLocalDateTime(@Nullable LocalDate date, Vakit time) {
        return getLocalDateTime(date, time.ordinal());
    }
    
    @NonNull
    public LocalDateTime getLocalDateTime(@Nullable LocalDate date, int time) {
        if (date == null) {
            date = LocalDate.now();
        }
        if ((time < 0) || (time > 5)) {
            while (time >= 6) {
                date = date.plusDays(1);
                time -= 6;
            }
            
            while (time <= -1) {
                date = date.minusDays(1);
                time += 6;
            }
        }
        
        
        LocalDateTime timeCal = date.toLocalDateTime(new LocalTime(getTime(date, time)));
        int h = timeCal.getHourOfDay();
        if ((time >= 3) && (h < 5)) {
            timeCal = timeCal.plusDays(1);
        }
        return timeCal;
    }
    
    @NonNull
    public String getTime(@Nullable LocalDate date, int time) {
        if (date == null) {
            date = LocalDate.now();
        }
        if ((time < 0) || (time > 5)) {
            while (time >= 6) {
                date = date.plusDays(1);
                time -= 6;
            }
            
            while (time == -1) {
                date = date.minusDays(1);
                time += 6;
            }
            
            
        }
        return adj(_getTime(date, time), time);
    }
    
    protected String _getTime(LocalDate date, int time) {
        throw new RuntimeException("You must override _getTime()");
    }
    
    public String getTime(int time) {
        return getTime(null, time);
    }
    
    @NonNull
    String adj(@NonNull String time, int t) {
        try {
            double drift = getTZFix();
            int[] adj = getMinuteAdj();
            if ((drift == 0) && (adj[t] == 0)) {
                return time;
            }
            
            int h = (int) Math.round(drift - 0.5);
            int m = (int) ((drift - h) * 60);
            
            String[] s = time.split(":");
            LocalTime lt = new LocalTime(Integer.parseInt(s[0]), Integer.parseInt(s[1]), 0);
            lt = lt.plusHours(h).plusMinutes(m).plusMinutes(adj[t]);
            time = lt.toString("HH:mm");
            
            
            return time;
        } catch (Exception e) {
            Crashlytics.logException(e);
            return "00:00";
        }
    }
    
    public String getLeft() {
        return getLeft(getNext(), true);
        
    }
    
    public String getLeft(int next) {
        return getLeft(next, true);
    }
    
    public long getMills(int next) {
        DateTime date = getLocalDateTime(null, next).toDateTime();
        return date.getMillis();
    }
    
    public String getLeft(int next, boolean showsecs) {
        LocalDateTime date = getLocalDateTime(null, next);
        Period period = new Period(LocalDateTime.now(), date, PeriodType.dayTime());
        
        if (showsecs) {
            return LocaleUtils.toArabicNrs(PERIOD_FORMATTER_HMS.print(period));
        } else if (Preferences.COUNTDOWN_TYPE.get().equals(Preferences.COUNTDOWN_TYPE_FLOOR)) {
            return LocaleUtils.toArabicNrs(PERIOD_FORMATTER_HM.print(period));
        } else {
            period = period.withFieldAdded(DurationFieldType.minutes(), 1);
            return LocaleUtils.toArabicNrs(PERIOD_FORMATTER_HM.print(period));
        }
        
    }
    
    public int getLeftMinutes(int which) {
        LocalDateTime date = getLocalDateTime(null, which);
        Period period = new Period(LocalDateTime.now(), date, PeriodType.minutes());
        return period.getMinutes();
    }
    
    public int getPassedMinutes() {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        for (int i = 5; i >= -1; i++) {
            LocalDateTime time = getLocalDateTime(today, i);
            if (time.isBefore(now)) {
                return new Period(time, now, PeriodType.minutes()).getMinutes();
            }
        }
        //
        return new Period(getLocalDateTime(today, Vakit.YATSI), now, PeriodType.minutes()).getMinutes();
    }
    
    public int getLeftMinutes() {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < 7; i++) {
            LocalDateTime time = getLocalDateTime(today, i);
            if (time.isAfter(now)) {
                return new Period(now, time, PeriodType.minutes()).getMinutes();
            }
        }
        return new Period(getLocalDateTime(today.plusDays(1), 0), now, PeriodType.minutes()).getMinutes();
    }
    
    public float getPassedPart() {
        int i = getNext();
        LocalDateTime date1 = getLocalDateTime(null, i - 1);
        LocalDateTime date2 = getLocalDateTime(null, i);
        Period period = new Period(date1, date2, PeriodType.minutes());
        float total = period.getMinutes();
        float passed = total - getLeftMinutes(i);
        return passed / total;
    }
    
    public Vakit getTime() {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < 6; i++) {
            if (getLocalDateTime(today, i).isAfter(now)) {
                return Vakit.getByIndex(i - 1);
            }
        }
        return Vakit.YATSI;
    }
    
    public int getNext() {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < 6; i++) {
            if (getLocalDateTime(today, i).isAfter(now)) {
                return i;
            }
        }
        return 6;
    }
    
    public boolean isKerahat() {
        long m = getLeftMinutes(1);
        if ((m <= 0) && (m > (-Preferences.KERAHAT_SUNRISE.get()))) {
            return true;
        }
        
        m = getLeftMinutes(2);
        if ((m >= 0) && (m < (Preferences.KERAHAT_ISTIWA.get()))) {
            return true;
        }
        
        m = getLeftMinutes(4);
        return (m >= 0) && (m < (Preferences.KERAHAT_SUNSET.get()));
        
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
            if (t.getID() < 0)
                t.delete();
        }
    }
    
    
    public String getSabah(LocalDate date) {
        return adj(_getTime(date, 6), 0);
    }
    
    public String getAsrSani(LocalDate date) {
        return adj(_getTime(date, 7), 3);
    }
}
