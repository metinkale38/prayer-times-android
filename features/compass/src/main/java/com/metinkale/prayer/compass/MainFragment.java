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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.metinkale.prayer.App;
import com.metinkale.prayer.BaseActivity;
import com.metinkale.prayer.Preferences;
import com.metinkale.prayer.compass.magnetic.MagneticCompass;
import com.metinkale.prayer.compass.time.FragQiblaTime;
import com.metinkale.prayer.utils.PermissionUtils;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

@SuppressLint("MissingPermission")
public class MainFragment extends BaseActivity.MainFragment implements LocationListener {
    private MenuItem mRefresh;
    private MenuItem mSwitch;
    private Mode mMode;

    private TextView mSelCity;

    public Location getLocation() {
        return mLocation;
    }

    private Location mLocation;
    private double mQiblaAngle;
    private float mQiblaDistance;

    private QiblaListener mList;

    private boolean mOnlyNew;

    private enum Mode {
        Compass, Map, Time
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main, container, false);
        PermissionUtils.get(getActivity()).needLocation(getActivity());
        mSelCity = v.findViewById(R.id.selcity);
        mSelCity.setOnClickListener(view -> {
            if (App.isOnline()) {
                startActivity(new Intent(getActivity(), LocationPicker.class));
            } else {
                Toast.makeText(getActivity(), R.string.noConnection, Toast.LENGTH_LONG).show();
            }
        });
        updateFrag(Mode.Compass);
        setHasOptionsMenu(true);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (PermissionUtils.get(getActivity()).pLocation) {
            LocationManager locMan = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

            List<String> providers = locMan.getProviders(true);
            for (String provider : providers) {
                locMan.requestLocationUpdates(provider, 0, 0, this);
                Location lastKnownLocation = locMan.getLastKnownLocation(provider);
                if (lastKnownLocation != null) {
                    calcQiblaAngle(lastKnownLocation);
                }
            }
        }

        if (Preferences.COMPASS_LAT.get() != 0) {
            Location loc = new Location("custom");
            loc.setLatitude(Preferences.COMPASS_LAT.get());
            loc.setLongitude(Preferences.COMPASS_LNG.get());
            calcQiblaAngle(loc);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (Preferences.SHOW_COMPASS_NOTE.get()) {

            final ViewGroup root = (ViewGroup) (view.getRootView());
            final View toast = LayoutInflater.from(getActivity()).inflate(R.layout.compass_toast_menu, root, false);
            root.addView(toast);

            toast.setOnClickListener(v -> root.removeView(toast));
            toast.postDelayed(() -> {
                if (toast.getRootView() == root)
                    root.removeView(toast);
            }, 10000);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (mRefresh == item) {
            mOnlyNew = true;
            if (PermissionUtils.get(getActivity()).pLocation) {
                LocationManager locMan = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

                locMan.removeUpdates(this);
                List<String> providers = locMan.getProviders(true);
                for (String provider : providers) {
                    locMan.requestLocationUpdates(provider, 0, 0, this);
                }
            }
        } else if (mSwitch == item) {
            Preferences.SHOW_COMPASS_NOTE.set(false);
            // compass >> time >> map
            if (mMode == Mode.Map) {
                updateFrag(Mode.Compass);
                mSwitch.setIcon(MaterialDrawableBuilder.with(getActivity()).setIcon(MaterialDrawableBuilder.IconValue.CLOCK).setColor(Color.WHITE)
                        .setToActionbarSize().build());
            } else if (mMode == Mode.Compass) {
                updateFrag(Mode.Time);
                mSwitch.setIcon(MaterialDrawableBuilder.with(getActivity()).setIcon(MaterialDrawableBuilder.IconValue.MAP).setColor(Color.WHITE)
                        .setToActionbarSize().build());
            } else if (mMode == Mode.Time) {
                updateFrag(Mode.Map);
                mSwitch.setIcon(
                        MaterialDrawableBuilder.with(getActivity()).setIcon(MaterialDrawableBuilder.IconValue.COMPASS_OUTLINE).setColor(Color.WHITE)
                                .setToActionbarSize().build());
            } else {
                Toast.makeText(getActivity(), R.string.permissionNotGranted, Toast.LENGTH_LONG).show();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        mRefresh = menu.add(Menu.NONE, Menu.NONE, 1, R.string.refresh);
        mSwitch = menu.add(Menu.NONE, Menu.NONE, 0, R.string.switchCompass);
        MenuItemCompat.setShowAsAction(mRefresh, MenuItemCompat.SHOW_AS_ACTION_NEVER);
        MenuItemCompat.setShowAsAction(mSwitch, MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);

        mSwitch.setIcon(MaterialDrawableBuilder.with(getActivity()).setIcon(MaterialDrawableBuilder.IconValue.CLOCK).setColor(Color.WHITE)
                .setToActionbarSize().build());
    }

    private void updateFrag(Mode mode) {
        if (!isAdded())
            return;
        if (mMode != mode) {
            FragmentManager fragmentManager = getChildFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            //noinspection ConstantConditions
            if (mode == Mode.Compass || (mode == Mode.Map && !BuildConfig.FLAVOR.equals("play"))) {
                MagneticCompass frag = new MagneticCompass();
                mList = frag;
                fragmentTransaction.replace(R.id.frag, frag, "compass");
                mode = Mode.Compass;
            } else if (mode == Mode.Map) {
                FragMap frag = new FragMap();
                mList = null;
                fragmentTransaction.replace(R.id.frag, frag, "map");
            } else if (mode == Mode.Time) {
                FragQiblaTime frag = new FragQiblaTime();
                mList = frag;
                fragmentTransaction.replace(R.id.frag, frag, "time");
            }
            fragmentTransaction.commit();
        }
        mMode = mode;
        notifyListener();
    }

    @Override
    public boolean onlyPortrait() {
        return true;
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (getActivity() != null && (System.currentTimeMillis() - location.getTime()) < (mOnlyNew ? (1000 * 60) : (1000 * 60 * 60 * 24))) {
            LocationManager locMan = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            locMan.removeUpdates(this);
        }

    }

    private void calcQiblaAngle(@NonNull Location location) {
        mLocation = location;
        if (!"custom".equals(location.getProvider())) {
            mSelCity.setVisibility(View.GONE);
        }
        double lat1 = location.getLatitude();// Latitude of User Location
        double lng1 = location.getLongitude();// Longitude of User Location
        double lat2 = 21.42247;// Latitude of Qaaba (+21.45° north of Equator)
        double lng2 = 39.826198;// Longitude of Qaaba (-39.75° east of Prime Meridian)

        double q = -getDirection(lat1, lng1, lat2, lng2);

        Location loc = new Location(location);
        loc.setLatitude(lat2);
        loc.setLongitude(lng2);
        mQiblaAngle = q;
        mQiblaDistance = location.distanceTo(loc) / 1000;

        notifyListener();

    }

    private void notifyListener() {
        if (mList != null && mLocation != null) {
            mList.setQiblaAngle(mQiblaAngle);
            mList.setQiblaDistance(mQiblaDistance);
            mList.setUserLocation(mLocation.getLatitude(), mLocation.getLongitude(), mLocation.getAltitude());
        }
    }

    private double getDirection(double lat1, double lng1, double lat2, double lng2) {
        double dLng = lng1 - lng2;
        return Math.toDegrees(getDirectionRad(Math.toRadians(lat1), Math.toRadians(lat2), Math.toRadians(dLng)));
    }

    private double getDirectionRad(double lat1, double lat2, double dLng) {
        return Math.atan2(Math.sin(dLng), (Math.cos(lat1) * Math.tan(lat2)) - (Math.sin(lat1) * Math.cos(dLng)));
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
}
