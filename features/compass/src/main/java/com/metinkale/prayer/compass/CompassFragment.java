/*
 * Copyright (c) 2013-2017 Metin Kale
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

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.metinkale.prayer.App;
import com.metinkale.prayer.BaseActivity;
import com.metinkale.prayer.Preferences;
import com.metinkale.prayer.compass._2D.Frag2D;
import com.metinkale.prayer.compass._3D.Frag3D;
import com.metinkale.prayer.compass.classes.OrientationCalculator;
import com.metinkale.prayer.compass.classes.OrientationCalculatorImpl;
import com.metinkale.prayer.compass.classes.math.Matrix4;
import com.metinkale.prayer.compass.classes.rotation.MagAccelListener;
import com.metinkale.prayer.compass.classes.rotation.RotationUpdateDelegate;
import com.metinkale.prayer.compass.time.FragQiblaTime;
import com.metinkale.prayer.utils.PermissionUtils;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

@SuppressWarnings("MissingPermission")
public class CompassFragment extends BaseActivity.MainFragment implements LocationListener, RotationUpdateDelegate {
    
    private double mQAngle;
    private float mDist;
    public MagAccelListener mMagAccel;
    @NonNull
    private Matrix4 mRotationMatrix = new Matrix4();
    private int mDisplayRotation;
    private SensorManager mSensorManager;
    private TextView mSelCity;
    private MenuItem mRefresh;
    private MenuItem mSwitch;
    private boolean mOnlyNew;
    private MyCompassListener mList;
    @NonNull
    private OrientationCalculator mOrientationCalculator = new OrientationCalculatorImpl();
    @NonNull
    private float[] mDerivedDeviceOrientation = {0, 0, 0};
    private Frag2D mFrag2D;
    private Frag3D mFrag3D;
    private FragQiblaTime mFragTime;
    private FragMap mFragMap;
    private Mode mMode;
    
    public Location getLocation() {
        return mLocation;
    }
    
    private Location mLocation;
    
    enum Mode {
        TwoDim, ThreeDim, Map, Time
    }
    
    public float getDistance() {
        return mDist;
    }
    
    public double getQiblaAngle() {
        return mQAngle;
    }
    
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.compass_main, container, false);
        PermissionUtils.get(getActivity()).needLocation(getActivity());
        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        
        
        Display display = ((WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        mDisplayRotation = display.getRotation();
        
        // sensor listeners
        mMagAccel = new MagAccelListener(this);
        
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        mFrag2D = new Frag2D();
        mList = mFrag2D;
        fragmentTransaction.add(R.id.frag2D, mFrag2D, "2d");
        fragmentTransaction.commit();
        
        return v;
    }
    
    private void updateFrag(Mode mode) {
        if (!isAdded())
            return;
        if (mMode != mode) {
            FragmentManager fragmentManager = getChildFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            
            if ((mode == Mode.TwoDim) && mFrag2D.mHidden) {
                fragmentTransaction.remove((Fragment) mList);
                mList = mFrag2D;
                mFrag2D.show();
            } else if (mode == Mode.ThreeDim) {
                
                if (PermissionUtils.get(getActivity()).pCamera) {
                    
                    if (mFrag3D == null) {
                        mFrag3D = new Frag3D();
                    }
                    
                    if (mList != mFrag3D) {
                        fragmentTransaction.replace(R.id.frag, mFrag3D, "3d");
                        
                        mList = mFrag3D;
                        mFrag2D.hide();
                    }
                } else {
                    PermissionUtils.get(getActivity()).needCamera(getActivity());
                }
                
            } else if (mode == Mode.Map) {
                
                
                if (mFragMap == null) {
                    mFragMap = new FragMap();
                }
                
                if (mList != mFragMap) {
                    fragmentTransaction.replace(R.id.frag, mFragMap, "map");
                    
                    mList = mFragMap;
                }
                
                
            } else if (mode == Mode.Time) {
                
                
                if (mFragTime == null) {
                    mFragTime = new FragQiblaTime();
                }
                
                if (mList != mFragTime) {
                    fragmentTransaction.replace(R.id.frag, mFragTime, "time");
                    mList = mFragTime;
                    mFrag2D.hide();
                }
                
                
            }
            fragmentTransaction.commit();
        }
        mMode = mode;
    }
    
    private Handler mHandler = new Handler();
    
    private Toast makeToast() {
        if (getActivity() == null)
            return null;
        Toast t = Toast.makeText(getActivity(), "", Toast.LENGTH_LONG);
        t.setView(getLayoutInflater().inflate(R.layout.compass_toast_menu, null, false));
        t.setGravity(Gravity.TOP | Gravity.RIGHT | Gravity.END, 0, 0);
        t.show();
        return t;
    }
    
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (Preferences.SHOW_COMPASS_NOTE.get()) {
            final Toast t = makeToast();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    t.cancel();
                    final Toast t2 = makeToast();
                    if (t2 != null)
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                t2.cancel();
                                makeToast();
                            }
                        }, 1000);
                }
            }, 1000);
        }
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        //super.onSaveInstanceState(outState);
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
            if (mMode == Mode.Map) {
                mSensorManager
                        .registerListener(mMagAccel, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
                mSensorManager
                        .registerListener(mMagAccel, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_GAME);
                
                updateFrag(Mode.TwoDim);
                
                mSwitch.setIcon(MaterialDrawableBuilder.with(getActivity()).setIcon(MaterialDrawableBuilder.IconValue.CLOCK).setColor(Color.WHITE)
                        .setToActionbarSize().build());
            } else if (mMode != Mode.Time && PermissionUtils.get(getActivity()).pLocation) {
                mSensorManager.unregisterListener(mMagAccel);
                updateFrag(Mode.Time);
                mSwitch.setIcon(MaterialDrawableBuilder.with(getActivity()).setIcon(MaterialDrawableBuilder.IconValue.MAP).setColor(Color.WHITE)
                        .setToActionbarSize().build());
            } else if (mMode == Mode.Time) {
                mSensorManager.unregisterListener(mMagAccel);
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
    
    @Override
    public void onResume() {
        super.onResume();
        mSensorManager.unregisterListener(mMagAccel);
        
        mSensorManager.registerListener(mMagAccel, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(mMagAccel, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_GAME);
        
        if (mSelCity == null) {
            mSelCity = getView().findViewById(R.id.selcity);
            mSelCity.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (App.isOnline()) {
                        startActivity(new Intent(getActivity(), LocationPicker.class));
                    } else {
                        Toast.makeText(getActivity(), R.string.noConnection, Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
        
        if (PermissionUtils.get(getActivity()).pLocation) {
            LocationManager locMan = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            
            List<String> providers = locMan.getProviders(true);
            for (String provider : providers) {
                locMan.requestLocationUpdates(provider, 0, 0, this);
                Location lastKnownLocation = locMan.getLastKnownLocation(provider);
                if (lastKnownLocation != null) {
                    calcQiblaAngel(lastKnownLocation);
                }
            }
        }
        
        if (Preferences.COMPASS_LAT.get() != 0) {
            Location loc = new Location("custom");
            loc.setLatitude(Preferences.COMPASS_LAT.get());
            loc.setLongitude(Preferences.COMPASS_LNG.get());
            calcQiblaAngel(loc);
        }
    }
    
    @Override
    public void onPause() {
        mSensorManager.unregisterListener(mMagAccel);
        LocationManager locMan = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        locMan.removeUpdates(this);
        super.onPause();
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (PermissionUtils.get(getActivity()).pCamera) {
            mMode = Mode.TwoDim;
        }
    }
    
    // RotationUpdateDelegate methods
    @Override
    public void onRotationUpdate(@NonNull float[] newMatrix) {
        if (mMode == Mode.Map || mMode == Mode.Time) {
            return;
        }
        // remap matrix values according to display rotation, as in
        // SensorManager documentation.
        switch (mDisplayRotation) {
            case Surface.ROTATION_0:
            case Surface.ROTATION_180:
                break;
            case Surface.ROTATION_90:
                SensorManager.remapCoordinateSystem(newMatrix, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, newMatrix);
                break;
            case Surface.ROTATION_270:
                SensorManager.remapCoordinateSystem(newMatrix, SensorManager.AXIS_MINUS_Y, SensorManager.AXIS_X, newMatrix);
                break;
            default:
                break;
        }
        mRotationMatrix.set(newMatrix);
        mOrientationCalculator.getOrientation(mRotationMatrix, mDisplayRotation, mDerivedDeviceOrientation);
        
        updateFrag((mDerivedDeviceOrientation[1] > -55f) ? Mode.ThreeDim : Mode.TwoDim);
        
        mList.onUpdateSensors(mDerivedDeviceOrientation);
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
    
    private void calcQiblaAngel(@NonNull Location location) {
        mLocation = location;
        if (!"custom".equals(location.getProvider())) {
            mSelCity.setVisibility(View.GONE);
        }
        double lat1 = location.getLatitude();// Latitude of Desired Location
        double lng1 = location.getLongitude();// Longitude of Desired Location
        double lat2 = 21.42247;// Latitude of Mecca (+21.45° north of Equator)
        double lng2 = 39.826198;// Longitude of Mecca (-39.75° east of Prime
        // Meridian)
        
        double q = -getDirection(lat1, lng1, lat2, lng2);
        
        Location loc = new Location(location);
        loc.setLatitude(lat2);
        loc.setLongitude(lng2);
        mQAngle = q;
        mDist = location.distanceTo(loc) / 1000;
        mList.onUpdateDirection();
        
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
    
    
    public interface MyCompassListener {
        void onUpdateDirection();
        
        void onUpdateSensors(float[] rot);
    }
    
}
