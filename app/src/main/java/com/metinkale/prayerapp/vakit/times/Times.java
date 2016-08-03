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
import com.metinkale.prayerapp.App;
import com.metinkale.prayerapp.Utils;
import com.metinkale.prayerapp.settings.Prefs;
import com.metinkale.prayerapp.vakit.AlarmReceiver;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.util.*;

public abstract class Times extends TimesBase {


    public transient boolean modified;//simple listener ;)

    Times(long id) {
        super(id);
        if (!sTimes.contains(this)) {
            sTimes.add(this);
        }
    }


    Times() {
    }


    private static Collection<TimesListener> sListeners = new ArrayList<>();
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


    public static void notifyDataSetChanged() {
        for (TimesListener list : sListeners) {
            try {
                list.notifyDataSetChanged();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void addListener(TimesListener list) {
        sListeners.add(list);
        list.notifyDataSetChanged();
    }

    public static void removeListener(TimesListener list) {
        sListeners.remove(list);
    }

    public interface TimesListener {
        void notifyDataSetChanged();
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

                        long mills = getTimeCal(cal, vakit).getMillis();
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
                            mills = getTimeCal(cal, 0).getMillis() + getSabahTime() * 60 * 1000;
                        } else {
                            mills = getTimeCal(cal, 1).getMillis() - getSabahTime() * 60 * 1000;
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
                        long mills = getTimeCal(cal, vakit).getMillis() - early * 60 * 1000;
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

            if (isCumaActive()) {

                int early = getCumaTime();

                DateTime c = DateTime.now().withDayOfWeek(DateTimeConstants.FRIDAY);
                if ((c.getMillis() + 1000) < System.currentTimeMillis()) {
                    c = c.plusWeeks(1);
                }
                long mills = getTimeCal(c.toLocalDate(), 2).getMillis();
                mills -= early * 60 * 1000;
                if (System.currentTimeMillis() < mills) {
                    Alarm a = new Alarm();
                    a.city = getID();
                    a.cuma = true;
                    a.early = false;
                    a.time = mills;
                    a.vakit = Vakit.OGLE;
                    a.dayOffset = ii;
                    alarms.add(a);
                }
            }
            cal = cal.plusDays(1);
        }
        return alarms;
    }


    public DateTime getTimeCal(LocalDate date, int time) {
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


        DateTime timeCal = date.toDateTime(new LocalTime(getTime(date, time)));
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

    public DateTime getTimeCal(int time) {
        return getTimeCal(null, time);
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
        LocalTime left = new LocalTime(getLeftMills(next));

        if (showsecs) {
            return Utils.toArabicNrs(left.toString("HH:mm:ss"));
        } else if (Prefs.isDefaultWidgetMinuteType()) {
            return Utils.toArabicNrs(left.toString("HH:mm"));
        } else {
            return Utils.toArabicNrs(Utils.az(left.getHourOfDay()) + ":" + Utils.az(left.getMinuteOfHour() + 1));
        }

    }


    long getLeftMills(int which) {
        return getMills(which) - (System.currentTimeMillis() + getTZOffset());
    }

    public long getMills(int which) {
        return getTimeCal(null, which).getMillis();
    }

    public int getNext() {
        long mills = System.currentTimeMillis();
        for (int i = 0; i < 6; i++) {
            if (mills < getMills(i)) {
                return i;
            }
        }
        return 6;
    }


    private long getTZOffset() {
        return TimeZone.getDefault().getRawOffset();
    }

    public boolean isKerahat() {
        long m = getLeftMills(1) + getTZOffset();
        if ((m <= 0) && (m > (Prefs.getKerahatSunrise() * -60000))) {
            return true;
        }

        m = getLeftMills(2) + getTZOffset();
        if ((m >= 0) && (m < (Prefs.getKerahatIstiwa() * 60000))) {
            return true;
        }

        m = getLeftMills(4) + getTZOffset();
        return (m >= 0) && (m < (Prefs.getKerahatSunet() * 60000));

    }



    @Override
    public String toString() {
        return "times_id_" + getID();
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

    }

}
