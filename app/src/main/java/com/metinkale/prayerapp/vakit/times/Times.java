package com.metinkale.prayerapp.vakit.times;

import android.content.SharedPreferences;
import android.os.Bundle;
import com.crashlytics.android.Crashlytics;
import com.metinkale.prayerapp.App;
import com.metinkale.prayerapp.Utils;
import com.metinkale.prayerapp.settings.Prefs;
import com.metinkale.prayerapp.vakit.AlarmReceiver;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.util.*;

public abstract class Times extends AbstractTimesBasics {


    Times(long id) {
        super(id);
    }


    private static List<TimesListener> sListeners = new ArrayList<>();
    private static List<Times> sTimes = new ArrayList<Times>() {
        @Override
        public boolean add(Times object) {
            if (object == null) return false;
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
            } catch (Exception ignore) {
            }
        }
    }

    public static void addListener(TimesListener list) {
        sListeners.add(list);
    }

    public static void removeListener(TimesListener list) {
        sListeners.remove(list);
    }

    public interface TimesListener {
        public void notifyDataSetChanged();
    }


    public static Times getTimesAt(int index) {
        return getTimes().get(index);
    }

    public static Times getTimes(long id) {
        for (Times t : sTimes) {
            if (t != null)
                if (t.getID() == id) return t;
        }

        try {
            Source source = AbstractTimesBasics.getSource(id);
            switch (source) {
                case Diyanet:
                    return new DiyanetTimes(id);
                case Fazilet:
                    return new FaziletTimes(id);
                case IGMG:
                    return new IGMGTimes(id);
                case NVC:
                    return new NVCTimes(id);
                case Semerkand:
                    return new SemerkandTimes(id);
                case Calc:
                    return new CalcTimes(id);
            }

        } catch (Exception ignore) {
        }
        return null;
    }

    protected static void clearTimes() {
        sTimes.clear();
    }

    public static List<Times> getTimes() {
        if (sTimes.isEmpty()) {
            SharedPreferences prefs = App.getContext().getSharedPreferences("cities", 0);

            List<Long> ids = new ArrayList<Long>();
            Set<String> keys = prefs.getAll().keySet();
            for (String key : keys) {
                if (key.startsWith("id")) {
                    sTimes.add(getTimes(Long.parseLong(key.substring(2))));
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
    }

    public static List<Long> getIds() {
        List<Long> ids = new ArrayList<>();
        List<Times> times = getTimes();
        for (Times t : times) {
            if (t != null) ids.add(t.getID());
        }
        return ids;
    }

    public static int getCount() {
        return getTimes().size();
    }

    public static List<Alarm> getAllAlarms() {
        List<Alarm> alarms = new ArrayList<>();
        List<Long> ids = Times.getIds();
        for (long id : ids) {
            Times t = Times.getTimes(id);
            if (t == null) continue;
            alarms.addAll(t.getAlarms());
        }
        return alarms;
    }

    public static void setAlarms() {
        List<Alarm> alarms = getAllAlarms();

        for (Alarm alarm : alarms) {
            AlarmReceiver.setAlarm(App.getContext(), alarm);
        }
    }

    Collection<Alarm> getAlarms() {
        List<Alarm> alarms = new ArrayList<>();


        LocalDate cal = LocalDate.now();

        for (int ii = 0; ii <= 1/* next day */; ii++) {
            for (Vakit v : Vakit.values()) {
                if (isNotificationActive(v)) if (v != Vakit.SABAH) {
                    int vakit = v.ordinal();
                    if (vakit != 0) vakit--;

                    long mills = getTimeCal(cal, vakit).getMillis();
                    if (System.currentTimeMillis() < mills) {
                        Alarm a = new Alarm();
                        a.city = getID();
                        a.dua = getDua(v);
                        a.early = 0;
                        a.name = getName();
                        a.silenter = getSilenterDuration(v);
                        a.sound = getSound(v);
                        a.time = mills;
                        a.vakit = v;
                        a.vibrate = hasVibration(v);
                        a.pref = v.name();

                        alarms.add(a);
                    }
                } else {
                    long mills;
                    if (isAfterImsak()) mills = getTimeCal(cal, 0).getMillis() + (getSabahTime() * 60 * 1000);
                    else mills = getTimeCal(cal, 1).getMillis() - (getSabahTime() * 60 * 1000);
                    if (System.currentTimeMillis() < mills) {
                        Alarm a = new Alarm();
                        a.city = getID();
                        a.dua = getDua(v);
                        a.early = 0;
                        a.name = getName();
                        a.silenter = getSilenterDuration(v);
                        a.sound = getSound(v);
                        a.time = mills;
                        a.vakit = v;
                        a.vibrate = hasVibration(v);
                        a.pref = v.name();
                        alarms.add(a);
                    }
                }

                if (isEarlyNotificationActive(v)) if (v != Vakit.SABAH) {
                    int vakit = v.ordinal();
                    if (vakit != 0) vakit--;

                    int early = getEarlyTime(v);
                    long mills = getTimeCal(cal, vakit).getMillis() - (early * 60 * 1000);
                    if (System.currentTimeMillis() < mills) {
                        Alarm a = new Alarm();
                        a.city = getID();
                        a.dua = "silent";
                        a.early = early;
                        a.name = getName();
                        a.silenter = getEarlySilenterDuration(v);
                        a.sound = getEarlySound(v);
                        a.time = mills;
                        a.vakit = v;
                        a.vibrate = hasEarlyVibration(v);
                        a.pref = "pre_" + v.name();
                        alarms.add(a);
                    }
                }
            }

            if (isCumaActive()) {

                int early = getCumaTime();

                DateTime c = DateTime.now().withDayOfWeek(DateTimeConstants.FRIDAY);
                if (c.getMillis() < System.currentTimeMillis()) c = c.plusDays(1);
                long mills = getTimeCal(c.toLocalDate(), 2).getMillis();
                mills -= early * 60 * 1000;
                if (System.currentTimeMillis() < mills) {
                    Alarm a = new Alarm();
                    a.city = getID();
                    a.dua = "silent";
                    a.early = early;
                    a.name = getName();
                    a.silenter = getCumaSilenterDuration();
                    a.sound = getCumaSound();
                    a.time = mills;
                    a.vakit = Vakit.OGLE;
                    a.vibrate = hasCumaVibration();
                    a.pref = "cuma";
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
        String[] t = getTime(date, time).split(":");

        int h = Integer.parseInt(t[0]);
        int m = Integer.parseInt(t[1]);
        DateTime timeCal = new DateTime(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth(), h, m, 0);

        if ((time >= 3) && (h < 5)) timeCal = timeCal.plusDays(1);
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

        return adj(_getTime(date, time), time);
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
            if ((drift == 0) && (adj[t] == 0)) return time;

            int h = (int) Math.round(drift - 0.5);
            int m = (int) ((drift - h) * 60);

            String[] s = time.split(":");
            LocalTime lt = new LocalTime(Integer.parseInt(s[0]), Integer.parseInt(s[1]), 0);

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

        if (showsecs)
            return left.toString("HH:mm:ss");
        else if (Prefs.isDefaultWidgetMinuteType())
            return left.toString("HH:mm");
        else return Utils.az(left.getHourOfDay()) + ":" + (Utils.az(left.getMinuteOfHour() + 1));

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
            if (mills < getMills(i)) return i;
        }
        return 6;
    }


    private long getTZOffset() {
        return TimeZone.getDefault().getRawOffset();
    }

    public boolean isKerahat() {
        long m = getLeftMills(1) + getTZOffset();
        if ((m <= 0) && (m > (Prefs.getKerahatSunrise() * -60000))) return true;

        m = getLeftMills(2) + getTZOffset();
        if ((m >= 0) && (m < (Prefs.getKerahatIstiwa() * 60000))) return true;

        m = getLeftMills(4) + getTZOffset();
        return (m >= 0) && (m < (Prefs.getKerahatSunet() * 60000));

    }

    public void refresh() {

    }

    @Override
    public String toString() {
        return "times_id_" + getID();
    }


    public static class Alarm {
        public long city;

        public String dua;
        public long early;
        public String name;
        public long silenter;
        public String sound;
        public long time;
        public Vakit vakit;
        public boolean vibrate;
        public String pref;

        public static Alarm fromBundle(Bundle bdl) {
            Alarm a = new Alarm();
            a.city = bdl.getLong("city");
            if (bdl.getString("vakit") != null) a.vakit = Vakit.valueOf(bdl.getString("vakit"));
            a.silenter = bdl.getLong("silenter");
            a.vibrate = bdl.getBoolean("vibrate");
            a.sound = bdl.getString("sound");
            a.time = bdl.getLong("time");
            a.early = bdl.getLong("early");
            a.dua = bdl.getString("dua");
            a.name = bdl.getString("name");
            a.pref = bdl.getString("pref");
            return a;
        }

        public Bundle toBundle() {
            Bundle bdl = new Bundle();
            bdl.putLong("city", city);
            if (vakit != null) bdl.putString("vakit", vakit.name());
            bdl.putLong("silenter", silenter);
            bdl.putBoolean("vibrate", vibrate);
            bdl.putString("sound", sound);
            bdl.putLong("time", time);
            bdl.putLong("early", early);
            bdl.putString("dua", dua);
            bdl.putString("name", name);
            bdl.putString("pref", pref);
            return bdl;
        }

    }

}
