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

package com.metinkale.prayerapp.vakit;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.IntDef;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.metinkale.prayerapp.utils.Geocoder;
import com.metinkale.prayerapp.vakit.times.Cities;
import com.metinkale.prayerapp.vakit.times.Entry;
import com.metinkale.prayerapp.vakit.times.Source;
import com.metinkale.prayerapp.vakit.times.Times;
import com.metinkale.prayerapp.vakit.times.sources.CalcTimes;
import com.metinkale.prayerapp.vakit.times.sources.WebTimes;

import java.util.List;

@SuppressWarnings("MissingPermission")
public class LocationService extends Service implements LocationListener {
    private static final String UPDATE_LOCATION = "UPDATE_LOCATION";
    private LocationManager mLocationManager;
    private boolean mHasWebTime = false;
    private boolean mHasCalcTime = false;
    private long mLastLocationUpdate;

    public LocationService() {
    }

    public static void start(Context c) {
        Intent i = new Intent(c, LocationService.class);
        c.startService(i);
    }

    public static void triggerUpdate(Context c) {
        Intent i = new Intent(c, LocationService.class);
        i.setAction(UPDATE_LOCATION);
        c.startService(i);
    }


    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            stopSelf();
            return;
        }


        List<Times> times = Times.getTimes();
        for (Times t : times) {
            if (t.isAutoLocation()) {
                if (t instanceof CalcTimes)
                    mHasCalcTime = true;
                else if (t instanceof WebTimes)
                    mHasWebTime = true;

                if (mHasCalcTime && mHasWebTime) break;
            }
        }

        if (mHasCalcTime || mHasWebTime)
            initGPS();
        else
            this.stopSelf();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!mHasCalcTime && !mHasWebTime) {
            stopSelf();
            return START_NOT_STICKY;
        }
        if (System.currentTimeMillis() - mLastLocationUpdate > 60 * 60 * 1000 || UPDATE_LOCATION.equals(intent.getAction())) {
            mLocationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, this, Looper.myLooper());
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        mLocationManager.removeUpdates(this);
        super.onDestroy();
    }

    private void initGPS() {
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 1000 * 60 * 30, 5000, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocationUpdate = System.currentTimeMillis();
        final double lat = location.getLatitude();
        final double lng = location.getLongitude();
        final double elv = location.getAltitude();

        Cities.get().search(lat, lng, new Cities.Callback<List<Entry>>() {
            @SuppressWarnings("ConstantConditions")
            @Override
            public void onResult(List<Entry> result) {
                List<Times> times = Times.getTimes();
                mHasCalcTime = false;
                mHasWebTime = false;
                boolean updated = false;
                for (Times t : times) {
                    if (t.isAutoLocation()) {
                        for (Entry e : result) {
                            if (t.getSource() == e.getSource()) {
                                if (e.getSource() == Source.Calc) {
                                    String oldtimes[] = {t.getTime(0), t.getTime(1), t.getTime(2),
                                            t.getTime(3), t.getTime(4), t.getTime(5), t.getTime(6)};
                                    ((CalcTimes) t).getPrayTimes().setCoordinates(lat, lng, elv);
                                    t.setName(e.getName());
                                    for (int i = 0; i < oldtimes.length; i++) {
                                        if (oldtimes[i].equals(t.getTime(i))) {
                                            updated = true;
                                            break;
                                        }
                                    }
                                    mHasCalcTime = true;
                                } else {
                                    t.setName(e.getName());
                                    if (!((WebTimes) t).getId().equals(e.getKey())) {
                                        ((WebTimes) t).setId(e.getKey());
                                        updated = true;
                                        ((WebTimes) t).syncAsync();
                                    }
                                    mHasWebTime = true;
                                }
                            }
                        }
                    }
                }
                if (updated) {
                    Times.notifyDataSetChanged();
                    Times.setAlarms();
                    WidgetService.start(getApplication());
                }
            }
        });
    }


    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
