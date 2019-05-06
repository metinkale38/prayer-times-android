/*
 * Copyright (c) 2013-2019 Metin Kale
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

package com.metinkale.prayer.compass;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.metinkale.prayer.utils.PermissionUtils;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

@SuppressWarnings("MissingPermission")
public class FragMap extends Fragment
        implements OnMapReadyCallback, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    private FloatingActionButton mFab;
    private final LatLng mKaabePos = new LatLng(21.42247, 39.826198);
    @Nullable
    private Polyline mLine;
    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    @Nullable
    private Marker mMarker;
    @Nullable
    private Circle mCircle;
    private boolean mRequestedLocation;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.compass_map, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mFab = v.findViewById(R.id.myLocationButton);

        if (!PermissionUtils.get(getActivity()).pLocation) {
            PermissionUtils.get(getActivity()).needLocation(getActivity());
        }
        return v;
    }


    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {
            buildGoogleApiClient();
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onPause() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        if (mMarker != null)
            mMarker.remove();
        if (mCircle != null)
            mCircle.remove();
        if (mLine != null)
            mLine.remove();
        mMarker = null;
        mCircle = null;
        mLine = null;
        super.onPause();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getContext()).addConnectionCallbacks(this).addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    @Override
    public void onConnected(Bundle bundle) {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (mGoogleApiClient.isConnected() && PermissionUtils.get(getActivity()).pLocation) {
            mRequestedLocation = true;
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }


    @SuppressWarnings("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setBuildingsEnabled(true);
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        mMap.addMarker(new MarkerOptions().position(mKaabePos).anchor(0.5f, 0.5f).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_kaabe)));

        mFab.setOnClickListener(view -> {
            if (mLocation != null) {
                if (!mRequestedLocation) onConnected(null); //start location updates
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()), 15));
            }
        });


    }


    @Override
    public void onLocationChanged(@NonNull Location location) {
        mLocation = location;
        if (!isAdded() || isDetached() || mMap == null)
            return;
        LatLng pos = new LatLng(location.getLatitude(), location.getLongitude());
        if (mLine != null) {
            ArrayList<LatLng> points = new ArrayList<>();
            points.add(pos);
            points.add(mKaabePos);
            mLine.setPoints(points);
            mMarker.setPosition(pos);
            mCircle.setCenter(pos);
            mCircle.setRadius(location.getAccuracy());
            mMap.animateCamera(CameraUpdateFactory.newLatLng(pos));
        } else {
            //zoom first time
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));


            mLine = mMap.addPolyline(
                    new PolylineOptions().add(pos).add(mKaabePos).geodesic(true).color(Color.parseColor("#3bb2d0")).width(3).zIndex(1));

            mMarker = mMap.addMarker(
                    new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_mylocation)).anchor(0.5f, 0.5f).position(pos)
                            .zIndex(2));

            mCircle = mMap.addCircle(
                    new CircleOptions().center(pos).fillColor(0xAA4FC3F7).strokeColor(getResources().getColor(R.color.colorPrimary)).strokeWidth(2)
                            .radius(mLocation.getAccuracy()));
        }


    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}

