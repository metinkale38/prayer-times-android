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

package com.metinkale.prayer.times;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;

import com.metinkale.prayer.App;
import com.metinkale.prayer.InternalBroadcastReceiver;
import com.metinkale.prayer.times.times.Cities;
import com.metinkale.prayer.times.times.Entry;
import com.metinkale.prayer.times.times.Source;
import com.metinkale.prayer.times.times.Times;
import com.metinkale.prayer.times.times.Vakit;
import com.metinkale.prayer.times.times.sources.CalcTimes;
import com.metinkale.prayer.times.times.sources.WebTimes;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.util.List;

import androidx.core.app.ActivityCompat;

import static android.location.LocationManager.KEY_LOCATION_CHANGED;

@SuppressWarnings("MissingPermission")
public class LocationReceiver extends BroadcastReceiver {
    private static long sLastLocationUpdate;

    public LocationReceiver() {
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean hasPermission(Context c) {
        return ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean useAutoLocation() {
        List<Times> times = Times.getTimes();
        for (Times t : times) {
            if (t.isAutoLocation()) {
                return true;
            }
        }
        return false;
    }

    public static void start(Context c) {
        if (!hasPermission(c) || !useAutoLocation())
            return;

        LocationManager lm = (LocationManager) c.getSystemService(Context.LOCATION_SERVICE);
        lm.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 1000 * 60 * 30, 5000, getPendingIntent(c));
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000 * 60 * 30, 5000, getPendingIntent(c));
    }

    private static PendingIntent getPendingIntent(Context c) {
        Intent i = new Intent(c, LocationReceiver.class);
        return PendingIntent.getBroadcast(c, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static void triggerUpdate(Context c) {
        if (!hasPermission(c) || !useAutoLocation())
            return;
        if (System.currentTimeMillis() - sLastLocationUpdate > 60 * 60 * 1000) {
            LocationManager lm = (LocationManager) c.getSystemService(Context.LOCATION_SERVICE);
            lm.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, getPendingIntent(c));
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Location location = intent.getParcelableExtra(KEY_LOCATION_CHANGED);
        sLastLocationUpdate = System.currentTimeMillis();
        final double lat = location.getLatitude();
        final double lng = location.getLongitude();
        final double elv = location.getAltitude();

        Cities.get().search(lat, lng, new Cities.Callback<List<Entry>>() {
            @Override
            public void onResult(List<Entry> result) {
                List<Times> times = Times.getTimes();
                boolean updated = false;
                for (Times t : times) {
                    if (t.isAutoLocation()) {
                        for (Entry e : result) {
                            if (t.getSource() == e.getSource()) {
                                if (e.getSource() == Source.Calc) {


                                    LocalDate today = LocalDate.now();
                                    LocalDateTime[] oldtimes =
                                            {t.getTime(today, Vakit.FAJR.ordinal()),
                                                    t.getTime(today, Vakit.SUN.ordinal()),
                                                    t.getTime(today, Vakit.DHUHR.ordinal()),
                                                    t.getTime(today, Vakit.ASR.ordinal()),
                                                    t.getTime(today, Vakit.MAGHRIB.ordinal()),
                                                    t.getTime(today, Vakit.ISHAA.ordinal())};
                                    ((CalcTimes) t).getPrayTimes().setCoordinates(lat, lng, elv);
                                    t.setName(e.getName());
                                    for (Vakit v : Vakit.values()) {
                                        if (oldtimes[v.ordinal()].equals(t.getTime(today, v.ordinal()))) {
                                            updated = true;
                                            break;
                                        }
                                    }


                                } else {
                                    t.setName(e.getName());
                                    if (!((WebTimes) t).getId().equals(e.getKey())) {
                                        ((WebTimes) t).setId(e.getKey());
                                        updated = true;
                                        ((WebTimes) t).syncAsync();
                                    }
                                }
                            }
                        }
                    }
                }
                if (updated) {
                    Times.setAlarms();
                    InternalBroadcastReceiver.sender(App.get()).sendTimeTick();
                } else {
                    LocationManager lm = (LocationManager) App.get().getSystemService(Context.LOCATION_SERVICE);
                    lm.removeUpdates(getPendingIntent(App.get()));
                }
            }
        });
    }


}
