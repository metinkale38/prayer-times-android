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
package com.metinkale.prayer.compass

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuItemCompat
import com.metinkale.prayer.BaseActivity
import com.metinkale.prayer.Preferences
import com.metinkale.prayer.compass.magnetic.MagneticCompass
import com.metinkale.prayer.times.times.Times
import com.metinkale.prayer.utils.PermissionUtils

@SuppressLint("MissingPermission")
class MainFragment : BaseActivity.MainFragment(), LocationListener {
    private lateinit var refresh: MenuItem
    private lateinit var switch: MenuItem
    private lateinit var selCity: TextView
    private var mode: Mode? = null

    var location: Location? = null
        private set
    private var qiblaAngle = 0.0
    private var qiblaDistance = 0f
    private var list: QiblaListener? = null
    private var onlyNew = false

    private enum class Mode {
        Compass, Map
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_main, container, false)
        PermissionUtils.get(requireActivity()).needLocation(requireActivity())
        selCity = v.findViewById(R.id.selcity)
        selCity.setOnClickListener { view: View? ->
            val builder = AlertDialog.Builder(requireActivity())
            builder.setTitle(R.string.cities)
            val times = Times.current.filter { it.lat != 0.0 && it.lng != 0.0 }
            val array = times.map { it.name + " (" + it.source.name + ")" }
            builder.setItems(array.toTypedArray()) { _: DialogInterface?, index: Int ->
                Preferences.COMPASS_LAT = times[index].lat.toFloat()
                Preferences.COMPASS_LNG = times[index].lng.toFloat()

                val loc = Location("custom")
                loc.latitude = Preferences.COMPASS_LAT.toDouble()
                loc.longitude = Preferences.COMPASS_LNG.toDouble()
                calcQiblaAngle(loc)
            }
            builder.show()
        }
        updateFrag(Mode.Compass)
        setHasOptionsMenu(true)
        return v
    }

    override fun onResume() {
        super.onResume()
        if (PermissionUtils.get(requireActivity()).pLocation && false) {
            val locMan =
                requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val providers = locMan.getProviders(true)
            for (provider in providers) {
                locMan.requestLocationUpdates(provider!!, 0, 0f, this)
                val lastKnownLocation = locMan.getLastKnownLocation(provider)
                lastKnownLocation?.let { calcQiblaAngle(it) }
            }
        }
        if (Preferences.COMPASS_LAT != 0f) {
            val loc = Location("custom")
            loc.latitude = Preferences.COMPASS_LAT.toDouble()
            loc.longitude = Preferences.COMPASS_LNG.toDouble()
            calcQiblaAngle(loc)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (Preferences.SHOW_COMPASS_NOTE) {
            val root = view.rootView as ViewGroup
            val toast =
                LayoutInflater.from(activity).inflate(R.layout.compass_toast_menu, root, false)
            root.addView(toast)
            toast.setOnClickListener { v: View? -> root.removeView(toast) }
            toast.postDelayed({ if (toast.rootView === root) root.removeView(toast) }, 10000)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (refresh === item) {
            onlyNew = true
            if (PermissionUtils.get(requireActivity()).pLocation) {
                val locMan =
                    requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
                locMan.removeUpdates(this)
                val providers = locMan.getProviders(true)
                for (provider in providers) {
                    locMan.requestLocationUpdates(provider!!, 0, 0f, this)
                }
            }
        } else if (switch === item) {
            Preferences.SHOW_COMPASS_NOTE = false
            // compass >> time >> map
            if (mode == Mode.Map) {
                updateFrag(Mode.Compass)
                switch.setIcon(R.drawable.ic_action_compass)
            } else if (mode == Mode.Compass) {
                updateFrag(Mode.Map)
                switch.setIcon(R.drawable.ic_action_map)
            } else {
                Toast.makeText(activity, R.string.permissionNotGranted, Toast.LENGTH_LONG).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        refresh = menu.add(Menu.NONE, Menu.NONE, 1, R.string.refresh)
        switch = menu.add(Menu.NONE, Menu.NONE, 0, R.string.switchCompass)
        MenuItemCompat.setShowAsAction(refresh, MenuItemCompat.SHOW_AS_ACTION_NEVER)
        MenuItemCompat.setShowAsAction(switch, MenuItemCompat.SHOW_AS_ACTION_IF_ROOM)
        switch.setIcon(R.drawable.ic_action_clock)
    }

    private fun updateFrag(mode: Mode) {
        if (!isAdded) return
        if (this.mode != mode) {
            val fragmentManager = childFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            if (mode == Mode.Compass || mode == Mode.Map && BuildConfig.FLAVOR != "play") {
                val frag = MagneticCompass()
                list = frag
                fragmentTransaction.replace(R.id.frag, frag, "compass")
            } else if (mode == Mode.Map) {
                val frag = FragMap()
                list = null
                fragmentTransaction.replace(R.id.frag, frag, "map")
            }
            fragmentTransaction.commit()
        }
        this.mode = mode
        notifyListener()
    }

    override fun onLocationChanged(location: Location) {
        if (activity != null && System.currentTimeMillis() - location.time < (if (onlyNew) 1000 * 60 else 1000 * 60 * 60 * 24)) {
            val locMan =
                requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
            locMan.removeUpdates(this)
        }
    }

    private fun calcQiblaAngle(location: Location) {
        this.location = location
        if ("custom" != location.provider) {
            selCity.visibility = View.GONE
        }
        val lat1 = location.latitude // Latitude of User Location
        val lng1 = location.longitude // Longitude of User Location
        val lat2 = 21.42247 // Latitude of Qaaba (+21.45° north of Equator)
        val lng2 = 39.826198 // Longitude of Qaaba (-39.75° east of Prime Meridian)
        val q = -getDirection(lat1, lng1, lat2, lng2)
        val loc = Location(location)
        loc.latitude = lat2
        loc.longitude = lng2
        qiblaAngle = q
        qiblaDistance = location.distanceTo(loc) / 1000
        notifyListener()
    }

    private fun notifyListener() {
        if (list != null && location != null) {
            list!!.setQiblaAngle(qiblaAngle)
            list!!.setQiblaDistance(qiblaDistance.toDouble())
            list!!.setUserLocation(location!!.latitude, location!!.longitude, location!!.altitude)
        }
    }

    private fun getDirection(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val dLng = lng1 - lng2
        return Math.toDegrees(
            getDirectionRad(
                Math.toRadians(lat1),
                Math.toRadians(lat2),
                Math.toRadians(dLng)
            )
        )
    }

    private fun getDirectionRad(lat1: Double, lat2: Double, dLng: Double): Double {
        return Math.atan2(
            Math.sin(dLng),
            Math.cos(lat1) * Math.tan(lat2) - Math.sin(lat1) * Math.cos(dLng)
        )
    }

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}
}