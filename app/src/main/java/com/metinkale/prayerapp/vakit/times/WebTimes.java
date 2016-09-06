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

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.PowerManager;
import com.metinkale.prayerapp.App;
import com.metinkale.prayerapp.vakit.Main;
import com.metinkale.prayerapp.vakit.times.other.Source;
import org.joda.time.LocalDate;

import java.util.HashMap;
import java.util.Map;


public class WebTimes extends Times {


    public Runnable mNotify = new Runnable() {
        @Override
        public void run() {
            notifyOnUpdated();
            App.getHandler().removeCallbacks(this);
        }
    };
    protected Map<String, String> times = new HashMap<>();
    private int lastSyncTime;
    private String id;
    private Runnable mCheckSync = new Runnable() {
        @Override
        public void run() {
            App.getHandler().removeCallbacks(mCheckSync);

            LocalDate ld = LocalDate.now();
            long lastSync = getLastSyncTime();

            if ((System.currentTimeMillis() - lastSync) > (1000 * 60 * 60 * 24)) {
                // always if +15 days does not exist
                ld = ld.plusDays(15);
                if ("00:00".equals(getTime(ld, 1))) {
                    syncTimes();
                    return;
                }


                ConnectivityManager connManager = (ConnectivityManager) App.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                int reasons = 0;
                if (wifi.isConnected()) {
                    reasons++;
                }

                try {
                    IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                    Intent batteryStatus = App.getContext().registerReceiver(null, ifilter);
                    int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                    if ((status == BatteryManager.BATTERY_STATUS_CHARGING) || (status == BatteryManager.BATTERY_STATUS_FULL)) {
                        reasons++;
                    }
                } catch (Exception ignore) {
                }
                if (((PowerManager) App.getContext().getSystemService(Context.POWER_SERVICE)).isScreenOn()) {
                    reasons++;
                }

                if (Main.isRunning) {
                    reasons++;
                }

                ld = ld.plusDays(reasons * 3);
                // if +15+reasons*3 days does not exist
                if ("00:00".equals(getTime(ld, 1))) {
                    syncTimes();
                    return;
                }

                // always if last sync was earlier than before (60-reasons*5) days
                if ((System.currentTimeMillis() - lastSync) > (1000 * 60 * 60 * 24 * (60 - (reasons * 5)))) {
                    syncTimes();
                    //noinspection UnnecessaryReturnStatement
                    return;
                }
            }
        }
    };

    WebTimes(long id) {
        super(id);

    }

    WebTimes() {
        super();
    }


    public static void add(Source source, String city, String id, double lat, double lng) {
        long _id = System.currentTimeMillis();
        WebTimes t = null;
        switch (source) {
            case Diyanet:
                t = new DiyanetTimes(_id);
                break;
            case IGMG:
                t = new IGMGTimes(_id);
                break;
            case Fazilet:
                t = new FaziletTimes(_id);
                break;
            case NVC:
                t = new NVCTimes(_id);
                break;
            case Semerkand:
                t = new SemerkandTimes(_id);
        }
        t.setSource(source);
        t.setName(city);
        t.setLat(lat);
        t.setLng(lng);
        t.setId(id);
        t.setSortId(99);

    }

    @Override
    public void delete() {
        super.delete();
        App.getHandler().removeCallbacks(mCheckSync);
    }

    @Override
    public synchronized String getTime(LocalDate date, int time) {
        App.getHandler().post(mCheckSync);

        return super.getTime(date, time);
    }

    public void syncTimes() {

    }

    protected String extractLine(String str) {
        str = str.substring(str.indexOf(">") + 1);
        str = str.substring(0, str.indexOf("</"));
        return str;
    }

    protected String az(int i) {
        if (i < 10) {
            return "0" + i;
        } else {
            return i + "";
        }
    }

    protected String az(String i) {
        if (i.length() == 1) {
            return "0" + i;
        } else {
            return i + "";
        }
    }

    public long getLastSyncTime() {
        return lastSyncTime * 1000L;
    }

    public void setLastSyncTime(long lastSyncTime) {
        this.lastSyncTime = (int) (lastSyncTime / 1000);
        save();
    }

    @Override
    public synchronized String _getTime(LocalDate date, int time) {
        String str = times.get(date.toString("yyyy-MM-dd") + "-" + time);
        if (str == null) {
            return "00:00";
        }
        return str.replace("*", "");
    }

    public synchronized void setTime(LocalDate date, int time, String value) {
        if (deleted()) return;
        times.put(date.toString("yyyy-MM-dd") + "-" + time, value.replace("*", ""));
        save();

        App.getHandler().post(mNotify);
    }

    public void setTimes(LocalDate date, String[] value) {
        if (deleted()) return;
        for (int i = 0; i < value.length; i++) {
            setTime(date, i, value[i]);
        }
    }

    public synchronized String getId() {
        return id;
    }

    public synchronized void setId(String id) {
        this.id = id;
        save();
    }
}
