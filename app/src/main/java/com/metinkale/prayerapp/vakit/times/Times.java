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
import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.koushikdutta.async.future.FutureCallback;
import com.metinkale.prayerapp.App;
import com.metinkale.prayerapp.Utils;
import com.metinkale.prayerapp.settings.Prefs;
import com.metinkale.prayerapp.vakit.AlarmReceiver;
import com.metinkale.prayerapp.vakit.times.other.Vakit;
import org.joda.time.*;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.util.*;

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

    private static Collection<OnTimesListChangeListener> sListeners = new ArrayList<>();
    private static List<Times> sTimes = new ArrayList<Times>() {
        @Override
        public boolean add(Times object) {
            if (object == null) {
                return false;
            }
            boolean ret = super.add(object);
            notifyDataSetChanged();
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
        if (sListeners == null) return;
        for (OnTimesListChangeListener list : sListeners) {
            try {
                list.notifyDataSetChanged();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void addOnTimesListChangeListener(OnTimesListChangeListener list) {
        if (sListeners == null) sListeners = new ArrayList<>();
        sListeners.add(list);
        list.notifyDataSetChanged();
    }

    public static void removeOnTimesListChangeListener(OnTimesListChangeListener list) {
        if (sListeners == null) return;
        sListeners.remove(list);
    }

    public static Times getTimesAt(int index) {
        return getTimes().get(index);
    }

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

    protected static void clearTimes() {
        sTimes.clear();
    }

    public static List<Times> getTimes() {
        if (sTimes.isEmpty()) {
            SharedPreferences prefs = App.getContext().getSharedPreferences("cities", 0);

            Set<String> keys = prefs.getAll().keySet();
            for (String key : keys) {
                if (key.startsWith("id")) {
                    sTimes.add(TimesBase.from(Long.parseLong(key.substring(2))));
                }
            }

            sort();
        }
        return sTimes;

    }

    public static void sort() {
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

        for (Times t : sTimes) {
            t.setSortId(sTimes.indexOf(t));
        }
    }

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

    public static List<Alarm> getAllAlarms() {
        List<Alarm> alarms = new ArrayList<>();
        List<Long> ids = getIds();
        for (long id : ids) {
            Times t = getTimes(id);
            if (t == null) {
                continue;
            }
            alarms.addAll(t.getAlarms());
        }
        return alarms;
    }

    public static void setAlarms() {
        List<Alarm> alarms = getAllAlarms();

        for (Alarm a : alarms) {
            AlarmReceiver.setAlarm(App.getContext(), a);

        }

    }

    public void addOnTimesUpdatedListener(OnTimesUpdatedListener list) {
        if (mListeners == null) mListeners = new ArrayList<>();
        mListeners.add(list);
        list.onTimesUpdated(this);
    }

    public void removeOnTimesUpdatedListener(OnTimesUpdatedListener list) {
        if (mListeners == null) return;
        mListeners.remove(list);
    }

    protected void notifyOnUpdated() {
        if (mListeners != null)
            for (OnTimesUpdatedListener list : mListeners)
                list.onTimesUpdated(this);
    }

    Collection<Alarm> getAlarms() {
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
                            Alarm a = new Alarm();
                            a.city = getID();
                            a.early = false;
                            a.cuma = false;
                            a.time = mills;
                            a.vakit = v;
                            a.dayOffset = ii;
                            alarms.add(a);
                        }
                    } else {
                        long mills;
                        if (isAfterImsak()) {
                            mills = getTimeCal(cal, 0).toDateTime().getMillis() + getSabahTime() * 60 * 1000;
                        } else {
                            mills = getTimeCal(cal, 1).toDateTime().getMillis() - getSabahTime() * 60 * 1000;
                        }
                        if (System.currentTimeMillis() < mills) {
                            Alarm a = new Alarm();
                            a.city = getID();
                            a.cuma = false;
                            a.early = false;
                            a.time = mills;
                            a.vakit = v;
                            a.dayOffset = ii;
                            alarms.add(a);
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
                            Alarm a = new Alarm();
                            a.city = getID();
                            a.early = true;
                            a.cuma = false;
                            a.time = mills;
                            a.vakit = v;
                            a.dayOffset = ii;
                            alarms.add(a);
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
                Alarm a = new Alarm();
                a.city = getID();
                a.cuma = true;
                a.early = false;
                a.time = mills;
                a.vakit = Vakit.OGLE;
                a.dayOffset = 0;
                alarms.add(a);
            }
        }


        return alarms;
    }

    public LocalDateTime getTimeCal(LocalDate date, int time) {
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

    public String getTime(LocalDate date, int time) {
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
        String ret = adj(_getTime(date, time), time);
        return ret;
    }

    protected String _getTime(LocalDate date, int time) {
        throw new RuntimeException("You must override _getTime()");
    }

    public String getTime(int time) {
        return getTime(null, time);
    }

    private String adj(String time, int t) {
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

    @Override
    public String toString() {
        return "times_id_" + getID();
    }


    public interface OnTimesListChangeListener {
        void notifyDataSetChanged();
    }

    public interface OnTimesUpdatedListener {
        void onTimesUpdated(Times t);
    }


    public static class Alarm {
        public long city;
        public boolean cuma;
        public boolean early;
        public long time;
        public Vakit vakit;
        public int dayOffset;

        public static Alarm fromString(String json) {
            return new Gson().fromJson(json, Alarm.class);
        }

        public String toString() {
            return new Gson().toJson(this);
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

    abstract class CatchedFutureCallback<T> implements FutureCallback<T> {
        @Override
        public void onCompleted(Exception e, T result) {
            if (e != null) {
                e.printStackTrace();
                return;
            }

            try {
                onCompleted(result);
            } catch (Exception ee) {
                Crashlytics.logException(ee);
            }
        }

        abstract void onCompleted(T result);
    }

}
