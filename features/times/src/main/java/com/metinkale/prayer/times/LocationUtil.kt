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

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.metinkale.prayer.App
import com.metinkale.prayer.receiver.AppEventManager
import com.metinkale.prayer.times.times.DayTimesWebProvider
import com.metinkale.prayer.times.times.Times
import com.metinkale.prayer.times.times.setAlarms
import com.metinkale.prayer.utils.PermissionUtils
import dev.metinkale.prayertimes.calc.PrayTimes
import dev.metinkale.prayertimes.providers.SearchEntry
import dev.metinkale.prayertimes.providers.sources.Source
import kotlinx.coroutines.launch


class LocationUtil : DefaultLifecycleObserver, LocationListener {


    private lateinit var locationManager: LocationManager
    private var location: Location? = null
    private var lifecycleOwner: LifecycleOwner? = null
    val running = MutableLiveData(false)

    @SuppressLint("MissingPermission")
    override fun onStart(owner: LifecycleOwner) {
        lifecycleOwner = owner
        if (useAutoLocation() && System.currentTimeMillis() - lastLocation > MIN_LOCATION_AGE) {
            locationManager =
                App.get().getSystemService(Context.LOCATION_SERVICE) as LocationManager

            running.value = true

            val lastLocation = locationManager.getProviders(true).mapNotNull {
                locationManager.getLastKnownLocation(it)
            }.maxByOrNull { it.time }

            if (lastLocation != null && System.currentTimeMillis() - lastLocation.time < MIN_LOCATION_AGE) {
                updateAsync(lastLocation)
            } else {

                locationManager.allProviders.forEach {
                    locationManager.requestLocationUpdates(
                        it,
                        MIN_LOCATION_AGE,
                        MIN_LOCATION_DISTANCE, this
                    )
                }
            }
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        running.value = false
        if (this::locationManager.isInitialized) locationManager.removeUpdates(this)
        lifecycleOwner = null
    }

    override fun onLocationChanged(it: Location) {
        if (System.currentTimeMillis() - it.time < MIN_LOCATION_AGE) {
            if (location == null || location!!.distanceTo(it) >= MIN_LOCATION_DISTANCE) {
                updateAsync(it)
            } else {
                lifecycleOwner?.let { onStop(it) }
            }
        }
    }

    private fun updateAsync(it: Location) {
        lifecycleOwner?.lifecycleScope?.launch {
            location = it
            lastLocation = it.time
            update(it)
            lifecycleOwner?.let { onStop(it) }
        }
    }

    private suspend fun update(location: Location) {
        val result = SearchEntry.search(location.latitude, location.longitude)

        val lat = location.latitude
        val lng = location.longitude
        val elv = location.altitude

        if (Times.current.any { it.autoLocation }) {
            for (t in Times.current) {
                if (t.autoLocation) {
                    for (e in result) {
                        if (t.source === e.source) {
                            if (e.source === Source.Calc) {
                                Times.getTimesById(t.id).update {
                                    it?.copy(name = e.localizedName, key = it.key?.let {
                                        PrayTimes.deserialize(it)
                                            .copy(latitude = lat, longitude = lng, elevation = elv)
                                            .serialize()
                                    })
                                }
                            } else {
                                if (App.isOnline()) {
                                    Times.getTimesById(t.id).update {
                                        t.copy(
                                            name = e.localizedName,
                                            key = e.id,
                                            lat = e.lat ?: 0.0,
                                            lng = e.lng ?: 0.0
                                        )
                                            .also { (it.dayTimes as? DayTimesWebProvider)?.syncAsync() }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Times.setAlarms()
            AppEventManager.sendTimeTick()
        }
    }

    override fun onProviderEnabled(provider: String) {}

    override fun onProviderDisabled(provider: String) {}

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}


    companion object {
        private val MIN_LOCATION_AGE = if (BuildConfig.DEBUG) 1000L * 10L else 1000L * 60L * 30
        private const val MIN_LOCATION_DISTANCE = 5000f
        private var lastLocation: Long = 0

        private fun useAutoLocation(): Boolean =
            Times.current.any { it.autoLocation } && PermissionUtils.get(App.get()).pLocation


    }


}