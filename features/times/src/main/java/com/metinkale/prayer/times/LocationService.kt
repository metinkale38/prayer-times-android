/*
 * Copyright (c) 2013-2023 Metin Kale
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
package com.metinkale.prayer.times

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import com.metinkale.prayer.App
import com.metinkale.prayer.receiver.AppEventManager
import com.metinkale.prayer.receiver.OnScreenOffListener
import com.metinkale.prayer.receiver.OnScreenOnListener
import com.metinkale.prayer.receiver.OnStartListener
import com.metinkale.prayer.times.times.DayTimesWebProvider
import com.metinkale.prayer.times.times.Times
import com.metinkale.prayer.times.times.setAlarms
import com.metinkale.prayer.times.utils.NotificationUtils
import dev.metinkale.prayertimes.calc.PrayTimes
import dev.metinkale.prayertimes.core.Entry
import dev.metinkale.prayertimes.core.sources.Source
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take

@SuppressLint("MissingPermission")
class LocationService : LifecycleService(), Observer<List<Entry>>, LocationListener,
    OnScreenOffListener, OnScreenOnListener {


    private val searchApi = OpenPrayerTimesSearchEndpoint()
    private lateinit var locationManager: LocationManager
    private lateinit var location: Location
    private var lastSearch: Long = 0
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(
                NotificationUtils.getDummyNotificationId(),
                NotificationUtils.createDummyNotification(this)
            )
        }
        return super.onStartCommand(intent, flags, startId)
    }


    override fun onLocationChanged(location: Location) {
        val time = System.currentTimeMillis()
        if (!this::location.isInitialized || (location.distanceTo(this.location) > MIN_LOCATION_DISTANCE && time - lastSearch > MIN_LOCATION_AGE)) {
            this.location = location
            lastSearch = time
            val lat = location.latitude
            val lng = location.longitude
            searchApi.search(lat, lng)
        }
    }


    override fun onChanged(result: List<Entry>) {
        val lat = location.latitude
        val lng = location.longitude
        val elv = location.altitude

        for (t in Times.current) {
            if (t.autoLocation) {
                for (e in result) {
                    if (t.source === e.source) {
                        if (e.source === Source.Calc) {
                            Times.getTimesById(t.id).update {
                                it?.copy(name = e.localizedName(), key = it.key?.let {
                                    PrayTimes.deserialize(it)
                                        .copy(latitude = lat, longitude = lng, elevation = elv)
                                        .serialize()
                                })
                            }
                        } else {
                            Times.getTimesById(t.id).update {
                                t.copy(
                                    name = e.localizedName(),
                                    key = e.id,
                                    lat = e.lat ?: 0.0,
                                    lng = e.lng ?: 0.0
                                ).also { (it.dayTimes as? DayTimesWebProvider)?.syncAsync() }
                            }
                        }
                    }
                }
            }
        }

        if (Times.current.any { it.autoLocation }) {
            Times.setAlarms()
            AppEventManager.sendTimeTick()
        }
    }

    override fun onCreate() {
        super.onCreate()
        AppEventManager.register(this)
        Times.filter { it.none { it.autoLocation } }.take(1).asLiveData()
            .observe({ lifecycle }) { stopSelf() }

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager


        onScreenOn()

        Times.map { it.any { it.autoLocation } }.distinctUntilChanged().filter { !it }
            .asLiveData().observe({ lifecycle }) {
                stopSelf()
            }

        searchApi.observe({ lifecycle }, this)
    }

    override fun onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            val notMan =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notMan.cancel(NotificationUtils.getDummyNotificationId())
        }
        AppEventManager.unregister(this)
        onScreenOff()
        super.onDestroy()
    }


    override fun onScreenOff() {
        locationManager.removeUpdates(this)
    }


    override fun onScreenOn() {
        locationManager.requestLocationUpdates(
            LocationManager.PASSIVE_PROVIDER,
            MIN_LOCATION_AGE,
            MIN_LOCATION_DISTANCE.toFloat(),
            this
        )
        locationManager.requestLocationUpdates(
            LocationManager.NETWORK_PROVIDER,
            MIN_LOCATION_AGE,
            MIN_LOCATION_DISTANCE.toFloat(),
            this
        )
    }


    companion object : OnStartListener {
        private const val MIN_LOCATION_AGE = 1000L * 60L * 30
        private const val MIN_LOCATION_DISTANCE = 5000

        private fun hasPermission(c: Context = App.get()): Boolean {
            return ActivityCompat.checkSelfPermission(
                c, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                c, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        }

        private fun useAutoLocation(): Boolean = Times.current.any { it.autoLocation }

        override fun onStart() {
            Times.map { it.any { it.autoLocation } }.distinctUntilChanged().filter { it }
                .asLiveData()
                .observeForever { start() }
        }

        fun start() {
            if (!hasPermission() || !useAutoLocation()) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                App.get().startForegroundService(Intent(App.get(), LocationService::class.java))
            } else {
                App.get().startService(Intent(App.get(), LocationService::class.java))
            }
        }
    }


}