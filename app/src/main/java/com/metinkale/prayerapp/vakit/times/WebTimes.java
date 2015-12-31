package com.metinkale.prayerapp.vakit.times;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.PowerManager;

import com.metinkale.prayerapp.App;
import com.metinkale.prayerapp.vakit.Main;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class WebTimes extends Times {
    private static final String _ID = "id";
    private static final String _LASTSYNC = "lastSync";
    private Context mContext;

    private String mPrefsId;
    private Thread mThread;

    private Handler mHandler = new Handler();
    private boolean mSyncing = false;

    WebTimes(long id) {
        super(id);
        mContext = App.getContext();


    }

    @Override
    public void refresh() {
        if (getId() == null || !App.isOnline() || mSyncing)
            return;

        if (Thread.currentThread() != mThread) {
            mThread = new Thread() {
                @Override
                public void run() {
                    refresh();
                }
            };

            mThread.start();
            return;
        }


        boolean ret;
        mSyncing = true;
        try {
            ret = syncTimes();
        } catch (Exception e) {
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
        return getLong(_LASTSYNC);
    }

    void setLastSync(long lastSync) {
        set(_LASTSYNC, lastSync);
    }

    @Override
    public String getTime(int d, int m, int y, int time) {
        mHandler.post(mCheckSync);

        return super.getTime(d, m, y, time);
    }

    boolean syncTimes() throws Exception {

        URL url = new URL(App.API_URL + "/api/getTimes.php?path=" + URLEncoder.encode(getId(), "utf8"));
        URLConnection ucon = url.openConnection();

        InputStream is = ucon.getInputStream();
        BufferedInputStream bis = new BufferedInputStream(is);


        BufferedReader reader = new BufferedReader(new InputStreamReader(bis, "UTF-8"));

        String line;

        int i = 0;
        while ((line = reader.readLine()) != null)
            try {
                String date = line.split("=")[0];
                String data[] = line.split("=")[1].split(" ");


                this.setTimes(Integer.parseInt(date.substring(0, 2)), Integer.parseInt(date.substring(3, 5)), Integer.parseInt(date.substring(6, 10)), data);
                i++;

            } catch (Exception ignore) {
            }


        return i != 0;

    }


    private Runnable mCheckSync = new Runnable() {
        @Override
        public void run() {
            mHandler.removeCallbacks(mCheckSync);
            Calendar cal = new GregorianCalendar();
            cal.add(Calendar.DAY_OF_YEAR, 15);
            long lastSync = getLastSync();
            if (System.currentTimeMillis() - lastSync > 1000 * 60 * 60 * 24) {
                // always if +15 days does not exist
                if (getTime(cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR), 1).equals("00:00")) {
                    refresh();
                    return;
                }


                ConnectivityManager connManager = (ConnectivityManager) App.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                int reasons = 0;
                if (wifi.isConnected())
                    reasons++;

                IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                Intent batteryStatus = App.getContext().registerReceiver(null, ifilter);
                int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                if (status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL)
                    reasons++;

                if (((PowerManager) App.getContext().getSystemService(Context.POWER_SERVICE)).isScreenOn())
                    reasons++;

                if (Main.isRunning)
                    reasons++;

                cal.add(Calendar.DAY_OF_YEAR, reasons * 3);
                // if +15+reasons*3 days does not exist
                if (getTime(cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR), 1).equals("00:00")) {
                    refresh();
                    return;
                }

                // always if last sync was earlier than before (60-reasons*5) days
                if (System.currentTimeMillis() - lastSync > 1000 * 60 * 60 * 24 * (60 - reasons * 5)) {
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
        WebTimes t = source == Source.NVC ? new NVCTimes(_id) : new WebTimes(_id);
        t.setSource(source);
        t.setName(city);
        t.setLat(lat);
        t.setLng(lng);
        t.setId(id);
    }


}
