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
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import com.metinkale.prayer.App
import com.metinkale.prayer.receiver.InternalBroadcastReceiver
import com.metinkale.prayer.times.LocationReceiver
import com.metinkale.prayer.times.times.DayTimesWebProvider
import com.metinkale.prayer.times.times.Times
import com.metinkale.prayer.times.times.setAlarms
import dev.metinkale.prayertimes.calc.PrayTimes
import dev.metinkale.prayertimes.core.Entry
import dev.metinkale.prayertimes.core.sources.Source

class LocationReceiver : BroadcastReceiver(), Observer<List<Entry>> {

    private val searchApi = OpenPrayerTimesSearchEndpoint().also { it.observeForever(this) }
    private lateinit var location: Location


    override fun onReceive(context: Context, intent: Intent) {
        location = intent.getParcelableExtra(LocationManager.KEY_LOCATION_CHANGED) ?: run {
            triggerUpdate(context)
            return
        }
        sLastLocationUpdate = System.currentTimeMillis()
        val lat = location.latitude
        val lng = location.longitude

        searchApi.search(lat, lng)
    }


    override fun onChanged(result: List<Entry>) {
        val lat = location.latitude
        val lng = location.longitude
        val elv = location.altitude

        for (t in Times.value) {
            if (t.isAutoLocation) {
                for (e in result) {
                    if (t.source === e.source) {
                        if (e.source === Source.Calc) {
                            Times.getTimesById(t.ID).update {
                                it?.copy(name = e.localizedName(), id = it.id?.let {
                                    PrayTimes.deserialize(it)
                                        .copy(latitude = lat, longitude = lng, elevation = elv)
                                        .serialize()
                                })
                            }
                        } else {
                            Times.getTimesById(t.ID).update {
                                t.copy(
                                    name = e.localizedName(), id = e.id
                                ).also { (it.dayTimes as? DayTimesWebProvider)?.syncAsync() }
                            }
                        }
                    }
                }
            }
        }
        if (Times.value.any { it.isAutoLocation }) {
            Times.setAlarms()
            InternalBroadcastReceiver.sender(App.get()).sendTimeTick()
        } else {
            val lm = App.get().getSystemService(Context.LOCATION_SERVICE) as LocationManager
            lm.removeUpdates(getPendingIntent(App.get()))
        }
    }


    protected fun finalize() {
        searchApi.removeObserver(this)
    }

    companion object {
        private var sLastLocationUpdate: Long = 0
        private fun hasPermission(c: Context): Boolean {
            return ActivityCompat.checkSelfPermission(
                c, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                c, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        }

        private fun useAutoLocation(): Boolean = Times.value.any { it.isAutoLocation }


        @JvmStatic
        fun start(c: Context) {
            if (!hasPermission(c) || !useAutoLocation()) return
            val lm = c.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            lm.requestLocationUpdates(
                LocationManager.PASSIVE_PROVIDER,
                (1000 * 60 * 30).toLong(),
                5000f,
                getPendingIntent(c)
            )
            lm.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                (1000 * 60 * 30).toLong(),
                5000f,
                getPendingIntent(c)
            )
        }

        private fun getPendingIntent(c: Context): PendingIntent {
            val i = Intent(c, LocationReceiver::class.java)
            return PendingIntent.getBroadcast(c, 0, i, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        @JvmStatic
        fun triggerUpdate(c: Context) {
            if (!hasPermission(c) || !useAutoLocation()) return
            if (System.currentTimeMillis() - sLastLocationUpdate > 60 * 60 * 1000) {
                val lm = c.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                lm.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, getPendingIntent(c))
            }
        }
    }
}