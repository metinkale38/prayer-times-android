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

package com.metinkale.prayerapp.compass;

import android.content.Context;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.metinkale.prayer.R;

public class FragMap extends Fragment implements LocationListener, OnMapReadyCallback, Main.MyCompassListener, LocationSource {

    private GoogleMap mMap;
    private Location mLocation;
    private LocationManager mLocMan;
    private OnLocationChangedListener mLocListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.compass_map, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        mLocMan = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

    }

    @Override
    public void onResume() {
        super.onResume();

        mLocMan.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000L, 10F, this);
        mLocMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000L, 10F, this);

    }

    @Override
    public void onPause() {
        mLocMan.removeUpdates(this);
        super.onPause();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.setLocationSource(this);
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        onLocationChanged(mLocMan.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
    }

    @Override
    public void onUpdateDirection() {

    }

    @Override
    public void onUpdateSensors(float[] rot) {

    }


    @Override
    public void onLocationChanged(Location location) {
        if (location.getProvider() == LocationManager.GPS_PROVIDER) {
            mLocMan.removeUpdates(this);
            mLocMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000L, 10F, this);
        }
        mLocation = location;
        if (mLocListener != null)
            mLocListener.onLocationChanged(location);

        LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.clear();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 15));
        LatLng kaaba = new LatLng(21.42247, 39.826198);
        mMap.addMarker(new MarkerOptions().position(kaaba));
        mMap.addPolyline(new PolylineOptions().color(Color.RED).add(latlng).add(kaaba));

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mLocListener = onLocationChangedListener;
        if (mLocation != null)
            mLocListener.onLocationChanged(mLocation);
    }

    @Override
    public void deactivate() {
        mLocListener = null;
    }
}
