package com.metinkale.prayerapp.vakit.times;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.PowerManager;
import com.crashlytics.android.Crashlytics;
import com.metinkale.prayerapp.App;
import com.metinkale.prayerapp.vakit.Main;
import org.joda.time.LocalDate;


public class WebTimes extends Times {
    private static final String _ID = "id";
    private static final String _LASTSYNC = "lastSync";
    private Context mContext;

    private String mPrefsId;
    private Thread mThread;

    private Handler mHandler = new Handler();
    private boolean mSyncing;

    WebTimes(long id) {
        super(id);

        mContext = App.getContext();


    }

    @Override
    public void delete() {
        super.delete();
        mHandler.removeCallbacks(mCheckSync);
        if ((mThread != null) && mThread.isAlive()) mThread.interrupt();
    }

    @Override
    public void refresh() {
        if ((getId() == null) || !App.isOnline() || mSyncing) return;

        if (Thread.currentThread() != mThread) {
            Thread t = new Thread() {
                @Override
                public void run() {
                    refresh();

                    if (mThread == this) mThread = null;
                }
            };
            if (t.getState() == Thread.State.NEW) t.start();

            mThread = t;
            return;
        }


        boolean ret;
        mSyncing = true;
        try {
            ret = syncTimes();
        } catch (Exception e) {
            if (e instanceof ArrayIndexOutOfBoundsException) {
                try {
                    Crashlytics.setString("city", getName());
                    Crashlytics.setString("path", getId());
                    Crashlytics.setString("source", getSource().toString());
                } catch (Exception ee) {
                    Crashlytics.logException(ee);
                }
                Crashlytics.logException(e);
            }
            ret = false;
        }
        mSyncing = false;


        setLastSync(System.currentTimeMillis());

    }


    String getId() {
        return getString(_ID);
    }

    void setId(String id) {
        set(_ID, id);
    }


    long getLastSync() {
        return getInt(_LASTSYNC) * 1000L;
    }

    void setLastSync(long lastSync) {
        set(_LASTSYNC, (int) (lastSync / 1000));
    }

    @Override
    public String getTime(LocalDate date, int time) {
        mHandler.post(mCheckSync);

        return super.getTime(date, time);
    }

    protected boolean syncTimes() throws Exception {
        return false;
    }


    private Runnable mCheckSync = new Runnable() {
        @Override
        public void run() {
            mHandler.removeCallbacks(mCheckSync);
            LocalDate ld = LocalDate.now();
            long lastSync = getLastSync();

            if ((System.currentTimeMillis() - lastSync) > (1000 * 60 * 60 * 24)) {
                // always if +15 days does not exist
                ld = ld.plusDays(15);
                if ("00:00".equals(getTime(ld, 1))) {
                    refresh();
                    return;
                }


                ConnectivityManager connManager = (ConnectivityManager) App.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                int reasons = 0;
                if (wifi.isConnected()) reasons++;

                try {
                    IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                    Intent batteryStatus = App.getContext().registerReceiver(null, ifilter);
                    int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                    if ((status == BatteryManager.BATTERY_STATUS_CHARGING) || (status == BatteryManager.BATTERY_STATUS_FULL))
                        reasons++;
                } catch (Exception ignore) {
                    Crashlytics.logException(ignore);
                }
                if (((PowerManager) App.getContext().getSystemService(Context.POWER_SERVICE)).isScreenOn()) reasons++;

                if (Main.isRunning) reasons++;

                ld = ld.plusDays(reasons * 3);
                // if +15+reasons*3 days does not exist
                if ("00:00".equals(getTime(ld, 1))) {
                    refresh();
                    return;
                }

                // always if last sync was earlier than before (60-reasons*5) days
                if ((System.currentTimeMillis() - lastSync) > (1000 * 60 * 60 * 24 * (60 - (reasons * 5)))) {
                    refresh();
                    return;
                }
            }

            //otherwise we dont need to sync
            return;


        }
    };


    public static void add(Source source, String city, String id, double lat, double lng) {
        long _id = System.currentTimeMillis();
        WebTimes t = new WebTimes(_id);
        t.setSource(source);
        t.setName(city);
        t.setLat(lat);
        t.setLng(lng);
        t.setId(id);
        if (source == Source.IGMG) t.set("fixedIGMG", true);

    }


    protected String extractLine(String str) {
        str = str.substring(str.indexOf(">") + 1);
        str = str.substring(0, str.indexOf("</"));
        return str;
    }


    protected String az(int i) {
        if (i < 10) return "0" + i;
        else return i + "";
    }

    protected String az(String i) {
        if (i.length() == 1) return "0" + i;
        else return i + "";
    }
}
