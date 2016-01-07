package com.metinkale.prayerapp;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import com.metinkale.prayer.R;
import com.metinkale.prayerapp.settings.Prefs;

/**
 * Created by metin on 14.12.2015.
 */
public class PermissionUtils {
    private static final int REQUEST_CALENDAR = 1;
    private static final int REQUEST_CAMERA = 2;
    private static final int REQUEST_STORAGE = 3;
    private static final int REQUEST_LOCATION = 4;
    public boolean pCalendar, pCamera, pStorage, pLocation;
    private static PermissionUtils mInstance;

    public static PermissionUtils get(Activity act) {
        if (mInstance == null) {
            mInstance = new PermissionUtils(act);
        }
        return mInstance;
    }

    private PermissionUtils(Activity act) {
        checkPermissions(act);
    }


    private void checkPermissions(Activity act) {
        pCalendar = ContextCompat.checkSelfPermission(act,
                Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED;
        pCamera = ContextCompat.checkSelfPermission(act,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        pStorage = ContextCompat.checkSelfPermission(act,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        pLocation = ContextCompat.checkSelfPermission(act,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public void needCamera(final Activity act) {
        if (!pCamera) {


            AlertDialog.Builder builder = new AlertDialog.Builder(act);

            builder.setTitle(R.string.permission_camera_title)
                    .setMessage(R.string.permission_camera_text).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    ActivityCompat.requestPermissions(act,
                            new String[]{Manifest.permission.CAMERA},
                            REQUEST_CAMERA);
                }
            });

            builder.show();

        }
    }


    public void needLocation(final Activity act) {
        if (!pLocation) {

            AlertDialog.Builder builder = new AlertDialog.Builder(act);

            builder.setTitle(R.string.permission_location_title)
                    .setMessage(R.string.permission_location_text).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    ActivityCompat.requestPermissions(act,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_LOCATION);
                }
            });


            builder.show();

        }
    }


    public void needCalendar(final Activity act, boolean force) {
        if (!pCalendar && (!Prefs.getCalendar().equals("-1") || force)) {

            AlertDialog.Builder builder = new AlertDialog.Builder(act);

            builder.setTitle(R.string.permission_calendar_title)
                    .setMessage(R.string.permission_calendar_text).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    ActivityCompat.requestPermissions(act,
                            new String[]{Manifest.permission.WRITE_CALENDAR},
                            REQUEST_CALENDAR);
                }
            });


            builder.show();
        }

    }


    public void needStorage(final Activity act) {
        if (!pStorage) {

            AlertDialog.Builder builder = new AlertDialog.Builder(act);

            builder.setTitle(R.string.permission_storage_title)
                    .setMessage(R.string.permission_storage_text).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    ActivityCompat.requestPermissions(act,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_STORAGE);
                }
            });


            builder.show();

        }
    }


    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pCamera = true;
                } else {
                    pCamera = false;
                }
                return;
            }

            case REQUEST_CALENDAR: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pCalendar = true;
                } else {
                    pCalendar = false;
                }
                return;
            }

            case REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pLocation = true;
                } else {
                    pLocation = false;
                    Prefs.setCalendar("-1");
                }
                return;
            }

            case REQUEST_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pStorage = true;
                } else {
                    pStorage = false;
                }
                return;
            }


        }
    }
}
