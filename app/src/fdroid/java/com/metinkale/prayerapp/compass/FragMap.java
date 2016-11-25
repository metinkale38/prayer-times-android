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

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mapbox.mapboxsdk.MapboxAccountManager;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationServices;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.metinkale.prayer.R;

@SuppressWarnings("MissingPermission")
public class FragMap extends Fragment implements Main.MyCompassListener, MapboxMap.OnMyLocationChangeListener {

    private MapView mMapView;
    private LocationServices mLocService;
    private MapboxMap mMap;
    private final LatLng mKaabePos = new LatLng(21.42247, 39.826198);
    private Polyline mLine;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        MapboxAccountManager.start(getActivity(), "pk.eyJ1IjoibWV0aW5rYWxlMzgiLCJhIjoiY2lyaHF1dW1uMDAzemlnbmtlN3plbXN3ZiJ9.t2c0KnfotdunLL--tj0NCA");
        return inflater.inflate(R.layout.compass_map, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mMapView = (MapView) view.findViewById(R.id.mapview);


        mLocService = LocationServices.getLocationServices(getActivity());

        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                mMap = mapboxMap;
                mMap.setOnMyLocationChangeListener(FragMap.this);
                mMap.setMyLocationEnabled(true);
                if (mMap.getMyLocation() != null)
                    onMyLocationChange(mMap.getMyLocation());
                mMap.addMarker(new MarkerOptions().position(mKaabePos).setIcon(IconFactory.getInstance(getActivity()).fromResource(R.drawable.ic_kaabe)));

                FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.myLocationButton);
                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (mMap.getMyLocation() != null) {
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mMap.getMyLocation().getLatitude(), mMap.getMyLocation().getLongitude()), 15));
                        }
                    }
                });
            }
        });


    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        new AlertDialog.Builder(getActivity()).setTitle("Attention")
                .setMessage("This map is displayed with the MapBox Api, which does not support Geodesic Polylines, so the direction on the map might not be accurate (especially at lower latitudes), because it does not take the earth curvature into account." +
                        "\n\n" +
                        "Therefore this Map is not suggested to use for an accurate determination of the prayer direction!" +
                        "\n\n" +
                        "In the Play Store Version of this Prayer App, Google Maps is used instead, which support Geodesic Lines, and is accurate due to that." +
                        "\n\n" +
                        "Since F-Droid permits the usage of the Google Maps Api, this Map will not be updated anymore!")    
                .setPositiveButton("Ok", null)
                .show();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }


    @Override
    public void onUpdateDirection() {

    }

    @Override
    public void onUpdateSensors(float[] rot) {

    }

    @Override
    public void onMyLocationChange(@Nullable Location location) {
        LatLng pos = new LatLng(location.getLatitude(), location.getLongitude());
        if (mLine != null) {
            mMap.removePolyline(mLine);
            mMap.animateCamera(CameraUpdateFactory.newLatLng(pos));
        } else {
            //zoom first time
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));
        }


        mLine = mMap.addPolyline(new PolylineOptions()
                .add(pos)
                .add(mKaabePos)
                .color(Color.parseColor("#3bb2d0"))
                .width(3));


    }
}
