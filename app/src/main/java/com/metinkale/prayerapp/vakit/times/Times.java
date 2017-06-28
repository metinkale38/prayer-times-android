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

package com.metinkale.prayerapp.vakit.times;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.metinkale.prayerapp.App;
import com.metinkale.prayerapp.settings.Prefs;
import com.metinkale.prayerapp.utils.Utils;
import com.metinkale.prayerapp.vakit.AlarmReceiver;
import com.metinkale.prayerapp.vakit.times.other.Vakit;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DurationFieldType;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public abstract class Times extends TimesBase {


    private static final PeriodFormatter PERIOD_FORMATTER_HMS = new PeriodFormatterBuilder()
            .printZeroIfSupported()
            .minimumPrintedDigits(2)
            .appendHours()
            .appendLiteral(":")
            .minimumPrintedDigits(2)
            .appendMinutes()
            .appendLiteral(":")
            .appendSeconds()
            .toFormatter();
    private static final PeriodFormatter PERIOD_FORMATTER_HM = new PeriodFormatterBuilder()
            .printZeroIfSupported()
            .minimumPrintedDigits(2)
            .appendHours()
            .appendLiteral(":")
            .minimumPrintedDigits(2)
            .appendMinutes()
            .toFormatter();

    private String issue = null;

    @NonNull
    private static Collection<OnTimesListChangeListener> sListeners = new ArrayList<>();
    @NonNull
    private final static List<Times> sTimes = new ArrayList<Times>() {
        @Override
        public boolean add(@Nullable Times object) {
            if (object == null) {
                return false;
            }

            boolean ret = super.add(object);
            Times.sort();
            return ret;
        }

        @Override
        public Times remove(int index) {
            Times ret = super.remove(index);
            notifyDataSetChanged();
            return ret;
        }

        @Override
        public boolean remove(Object object) {
            boolean ret = super.remove(object);
            notifyDataSetChanged();
            return ret;
        }

        @Override
        public void clear() {
            super.clear();
            notifyDataSetChanged();
        }
    };
    private transient Collection<OnTimesUpdatedListener> mListeners;

    Times(long id) {
        super(id);
        if (!sTimes.contains(this)) {
            sTimes.add(this);
        }
    }

    Times() {
        super();
    }

    private static void notifyDataSetChanged() {
        for (OnTimesListChangeListener list : sListeners) {
            try {
                list.notifyDataSetChanged();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void addOnTimesListChangeListener(@NonNull OnTimesListChangeListener list) {
        sListeners.add(list);
        list.notifyDataSetChanged();
    }

    public static void removeOnTimesListChangeListener(OnTimesListChangeListener list) {
        sListeners.remove(list);
    }

    public static Times getTimesAt(int index) {
        return getTimes().get(index);
    }

    @Nullable
    public static Times getTimes(long id) {
        for (Times t : sTimes) {
            if (t != null) {
                if (t.getID() == id) {
                    return t;
                }
            }
        }
        return null;
    }

    @NonNull
    public static List<Times> getTimes() {
        if (sTimes.isEmpty()) {
            SharedPreferences prefs = App.get().getSharedPreferences("cities", 0);

            Set<String> keys = prefs.getAll().keySet();
            for (String key : keys) {
                if (key.startsWith("id")) {
                    sTimes.add(TimesBase.from(Long.parseLong(key.substring(2))));
                }
            }

            if (!sTimes.isEmpty())
                sort();
        }
        return sTimes;

    }

    public static void sort() {
        if (sTimes.isEmpty()) return;
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

        notifyDataSetChanged();
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

    @NonNull
    private static List<Alarm> getAllAlarms() {
        List<Alarm> alarms = new ArrayList<>();
        List<Long> ids = getIds();
        for (long id : ids) {
            Times t = getTimes(id);
            if (t == null) {
                continue;
            }
            alarms.addAll(t.getAlarms());
        }

        Collections.sort(alarms, new Comparator<Alarm>() {
            @Override
            public int compare(Alarm o1, Alarm o2) {
                return (int) (o1.time - o2.time);
            }
        });
        return alarms;
    }

    public static void setAlarms() {
        List<Alarm> alarms = getAllAlarms();
        if (!alarms.isEmpty())
            AlarmReceiver.setAlarm(App.get(), alarms.get(0));
    }

    public void addOnTimesUpdatedListener(@NonNull OnTimesUpdatedListener list) {
        if (mListeners == null) mListeners = new ArrayList<>();
        mListeners.add(list);
        list.onTimesUpdated(this);
    }

    public void removeOnTimesUpdatedListener(OnTimesUpdatedListener list) {
        if (mListeners == null) return;
        mListeners.remove(list);
    }

    void notifyOnUpdated() {
        if (mListeners != null)
            for (OnTimesUpdatedListener list : mListeners)
                list.onTimesUpdated(this);
    }

    @NonNull
    private Collection<Alarm> getAlarms() {
        Collection<Alarm> alarms = new ArrayList<>();


        LocalDate cal = LocalDate.now();
        for (int ii = 0; ii <= 1/* next day */; ii++) {
            for (Vakit v : Vakit.values()) {
                if (isNotificationActive(v)) {
                    if (v != Vakit.SABAH) {
                        int vakit = v.ordinal();
                        if (vakit != 0) {
                            vakit--;
                        }

                        long mills = getTimeCal(cal, vakit).toDateTime().getMillis();
                        if (System.currentTimeMillis() < mills) {
                            alarms.add(new Alarm(getID(), false, false, mills, v, ii));
                        }
                    } else {
                        long mills;
                        if (isAfterImsak()) {
                            mills = getTimeCal(cal, 0).toDateTime().getMillis() + getSabahTime() * 60 * 1000;
                        } else {
                            mills = getTimeCal(cal, 1).toDateTime().getMillis() - getSabahTime() * 60 * 1000;
                        }
                        if (System.currentTimeMillis() < mills) {
                            alarms.add(new Alarm(getID(), false, false, mills, v, ii));
                        }
                    }
                }

                if (isEarlyNotificationActive(v)) {
                    if (v != Vakit.SABAH) {
                        int vakit = v.ordinal();
                        if (vakit != 0) {
                            vakit--;
                        }

                        int early = getEarlyTime(v);
                        long mills = getTimeCal(cal, vakit).toDateTime().getMillis() - early * 60 * 1000;
                        if (System.currentTimeMillis() < mills) {
                            alarms.add(new Alarm(getID(), false, true, mills, v, ii));
                        }
                    }
                }
            }
            cal = cal.plusDays(1);
        }
        if (isCumaActive()) {

            int early = getCumaTime();

            DateTime c = DateTime.now().withDayOfWeek(DateTimeConstants.FRIDAY);
            if ((c.getMillis() + 1000) < System.currentTimeMillis()) {
                c = c.plusWeeks(1);
            }
            long mills = getTimeCal(c.toLocalDate(), 2).toDateTime().getMillis();
            mills -= early * 60 * 1000;
            if (System.currentTimeMillis() < mills) {
                alarms.add(new Alarm(getID(), true, false, mills, Vakit.OGLE, 0));
            }
        }


        return alarms;
    }

    @NonNull
    private LocalDateTime getTimeCal(@Nullable LocalDate date, int time) {
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
        DateTime date = getTimeCal(null, next).toDateTime();
        return date.getMillis();
    }

    public String getLeft(int next, boolean showsecs) {
        LocalDateTime date = getTimeCal(null, next);
        Period period = new Period(LocalDateTime.now(), date, PeriodType.dayTime());

        if (showsecs) {
            return Utils.toArabicNrs(PERIOD_FORMATTER_HMS.print(period));
        } else if (Prefs.isDefaultWidgetMinuteType()) {
            return Utils.toArabicNrs(PERIOD_FORMATTER_HM.print(period));
        } else {
            period = period.withFieldAdded(DurationFieldType.minutes(), 1);
            return Utils.toArabicNrs(PERIOD_FORMATTER_HM.print(period));
        }

    }

    public int getLeftMinutes(int which) {
        LocalDateTime date = getTimeCal(null, which);
        Period period = new Period(LocalDateTime.now(), date, PeriodType.minutes());
        return period.getMinutes();
    }


    public float getPassedPart() {
        int i = getNext();
        LocalDateTime date1 = getTimeCal(null, i - 1);
        LocalDateTime date2 = getTimeCal(null, i);
        Period period = new Period(date1, date2, PeriodType.minutes());
        float total = period.getMinutes();
        float passed = total - getLeftMinutes(i);
        return passed / total;
    }


    public int getNext() {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < 6; i++) {
            if (getTimeCal(today, i).isAfter(now)) {
                return i;
            }
        }
        return 6;
    }

    public boolean isKerahat() {
        long m = getLeftMinutes(1);
        if ((m <= 0) && (m > (-Prefs.getKerahatSunrise()))) {
            return true;
        }

        m = getLeftMinutes(2);
        if ((m >= 0) && (m < (Prefs.getKerahatIstiwa()))) {
            return true;
        }

        m = getLeftMinutes(4);
        return (m >= 0) && (m < (Prefs.getKerahatSunet()));

    }

    @NonNull
    @Override
    public String toString() {
        return "times_id_" + getID();
    }

    public String getIssue() {
        return issue;
    }

    public void setIssue(String issue) {
        this.issue = issue;
    }


    public interface OnTimesListChangeListener {
        void notifyDataSetChanged();
    }

    public interface OnTimesUpdatedListener {
        void onTimesUpdated(Times t);
    }


    public static class Alarm {
        private static final Gson GSON = new Gson();
        public final long city;
        public final boolean cuma;
        public final boolean early;
        public final long time;
        public final Vakit vakit;
        public final int dayOffset;

        public Alarm(long city, boolean cuma, boolean early, long time, Vakit vakit, int dayOffset) {
            this.city = city;
            this.cuma = cuma;
            this.early = early;
            this.time = time;
            this.vakit = vakit;
            this.dayOffset = dayOffset;
        }


        public static Alarm fromJson(String json) {
            return GSON.fromJson(json, Alarm.class);
        }

        public String toJson() {
            return GSON.toJson(this);
        }

        @Override
        public int hashCode() {
            int result = 571;
            result = 37 * result + (int) (city ^ (city >>> 32));
            result = 37 * result + (cuma ? 1 : 0);
            result = 37 * result + (early ? 1 : 0);
            result = 37 * result + vakit.ordinal();
            result = 37 * result + dayOffset;
            return result;
        }


    }


}
