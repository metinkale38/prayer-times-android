package com.metinkale.prayerapp.compass;

import android.graphics.Color;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.metinkale.prayer.R;

import java.util.ArrayList;

@SuppressWarnings("MissingPermission")
public class FragMap extends Fragment implements Main.MyCompassListener, OnMapReadyCallback, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    private FloatingActionButton mFab;
    private final LatLng mKaabePos = new LatLng(21.42247, 39.826198);
    private Polyline mLine;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    private Marker mMarker;
    private Circle mCircle;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.compass_map, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mFab = (FloatingActionButton) v.findViewById(R.id.myLocationButton);

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
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onPause();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (mGoogleApiClient.isConnected())
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
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

        mMap.addMarker(new MarkerOptions().position(mKaabePos).anchor(0.5f, 0.5f)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_kaabe)));

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mLocation != null) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()), 15));
                }
            }
        });


    }

    @Override
    public void onUpdateDirection() {

    }

    @Override
    public void onUpdateSensors(float[] rot) {

    }

    @Override
    public void onLocationChanged(Location location) {

        mLocation = location;
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
            mMap.animateCamera(CameraUpdateFactory
                    .newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));


            mLine = mMap.addPolyline(new PolylineOptions()
                    .add(pos)
                    .add(mKaabePos)
                    .geodesic(true)
                    .color(Color.parseColor("#3bb2d0"))
                    .width(3));

            mMarker = mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_mylocation))
                    .anchor(0.5f, 0.5f)
                    .position(pos).zIndex(1));

            mCircle = mMap.addCircle(new CircleOptions()
                    .center(pos)
                    .fillColor(0xAA4FC3F7)
                    .strokeColor(getResources().getColor(R.color.colorPrimary))
                    .strokeWidth(2)
                    .radius(mLocation.getAccuracy()));
        }


    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}

