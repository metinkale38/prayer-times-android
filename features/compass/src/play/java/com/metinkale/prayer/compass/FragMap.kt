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

import android.graphics.Color
import android.location.Location
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.maps.GoogleMap
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.SupportMapFragment
import com.metinkale.prayer.utils.PermissionUtils
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.model.*
import java.util.ArrayList

class FragMap : Fragment(), OnMapReadyCallback, LocationListener,
    GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private var map: GoogleMap? = null
    private var fab: FloatingActionButton? = null
    private val qaabaPos = LatLng(21.42247, 39.826198)
    private var line: Polyline? = null
    private var googleApiClient: GoogleApiClient? = null
    private var location: Location? = null
    private var marker: Marker? = null
    private var circle: Circle? = null
    private var requestedLocation = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.compass_map, container, false)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
        fab = v.findViewById(R.id.myLocationButton)
        if (!PermissionUtils.get(requireActivity()).pLocation) {
            PermissionUtils.get(requireActivity()).needLocation(requireActivity())
        }
        return v
    }

    override fun onResume() {
        super.onResume()
        if (googleApiClient == null || !googleApiClient!!.isConnected) {
            buildGoogleApiClient()
            googleApiClient!!.connect()
        }
    }

    override fun onPause() {
        if (googleApiClient != null && googleApiClient!!.isConnected) {
            googleApiClient!!.disconnect()
        }
        if (marker != null) marker!!.remove()
        if (circle != null) circle!!.remove()
        if (line != null) line!!.remove()
        marker = null
        circle = null
        line = null
        super.onPause()
    }

    @Synchronized
    protected fun buildGoogleApiClient() {
        googleApiClient = GoogleApiClient.Builder(requireContext()).addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API).build()
    }

    override fun onConnected(bundle: Bundle?) {
        val locationRequest = LocationRequest()
        locationRequest.interval = 1000
        locationRequest.fastestInterval = 1000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        if (googleApiClient!!.isConnected && PermissionUtils.get(requireActivity()).pLocation) {
            requestedLocation = true
            LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient!!,
                locationRequest,
                this
            )
        }
    }

    override fun onConnectionSuspended(i: Int) {}
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map!!.isBuildingsEnabled = true
        map!!.mapType = GoogleMap.MAP_TYPE_HYBRID
        map!!.addMarker(
            MarkerOptions().position(qaabaPos).anchor(0.5f, 0.5f)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_kaabe))
        )
        fab!!.setOnClickListener { view: View? ->
            if (location != null) {
                if (!requestedLocation) onConnected(null) //start location updates
                map!!.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            location!!.latitude,
                            location!!.longitude
                        ), 15f
                    )
                )
            }
        }
    }

    override fun onLocationChanged(location: Location) {
        this.location = location
        if (!isAdded || isDetached || map == null) return
        val pos = LatLng(location.latitude, location.longitude)
        if (line != null) {
            val points = ArrayList<LatLng>()
            points.add(pos)
            points.add(qaabaPos)
            line!!.points = points
            marker!!.position = pos
            circle!!.center = pos
            circle!!.radius = location.accuracy.toDouble()
            map!!.animateCamera(CameraUpdateFactory.newLatLng(pos))
        } else {
            //zoom first time
            map!!.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        location.latitude,
                        location.longitude
                    ), 15f
                )
            )
            line = map!!.addPolyline(
                PolylineOptions().add(pos).add(qaabaPos).geodesic(true)
                    .color(Color.parseColor("#3bb2d0")).width(3f).zIndex(1f)
            )
            marker = map!!.addMarker(
                MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_mylocation))
                    .anchor(0.5f, 0.5f).position(pos)
                    .zIndex(2f)
            )
            circle = map!!.addCircle(
                CircleOptions().center(pos).fillColor(-0x55b03c09)
                    .strokeColor(resources.getColor(R.color.colorPrimary)).strokeWidth(2f)
                    .radius(this.location!!.accuracy.toDouble())
            )
        }
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {}
}