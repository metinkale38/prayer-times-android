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

package com.metinkale.prayer.service;

import android.app.ForegroundServiceStartNotAllowedException;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.collection.ArraySet;
import androidx.core.app.NotificationCompat;

import com.metinkale.prayer.base.R;

import java.util.Set;

/**
 * this class holds a foreground service as long there are any needies.
 * <p>
 * If there are no needies (ongoing notifications or widgets), this service will not stay awake
 */
public class ForegroundService extends Service {

    private static final String ACTION_ADD_NEEDY = "addNeedy";
    private static final String ACTION_REMOVE_NEEDY = "removeNeedy";
    private static final String EXTRA_NEEDY = "needy";
    private static final String EXTRA_NOTIFICATION = "notification";
    private static final String EXTRA_NOTIFICATION_ID = "notificationId";
    private final Set<String> mForegroundNeedy = new ArraySet<>();
    private Notification mNotification;
    private int mNotificationId;
    private String mNotificationNeedy;


    public static void addNeedy(Context c, String needy, Notification not, int notId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                Intent intent = new Intent(c, ForegroundService.class);
                intent.setAction(ACTION_ADD_NEEDY);
                intent.putExtra(EXTRA_NEEDY, needy);
                intent.putExtra(EXTRA_NOTIFICATION, not);
                intent.putExtra(EXTRA_NOTIFICATION_ID, notId);
                c.startForegroundService(intent);
            } catch (Exception e) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || !(e instanceof ForegroundServiceStartNotAllowedException)) {
                    throw e;
                }
            }
        }
    }

    public static void addNeedy(Context c, String needy) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                Intent intent = new Intent(c, ForegroundService.class);
                intent.setAction(ACTION_ADD_NEEDY);
                intent.putExtra(EXTRA_NEEDY, needy);
                c.startForegroundService(intent);
            } catch (Exception e) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || !(e instanceof ForegroundServiceStartNotAllowedException)) {
                    throw e;
                }
            }
        }
    }


    public static void removeNeedy(Context c, String needy) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                Intent intent = new Intent(c, ForegroundService.class);
                intent.setAction(ACTION_REMOVE_NEEDY);
                intent.putExtra(EXTRA_NEEDY, needy);
                c.startForegroundService(intent);
            } catch (Exception e) {
                // ForegroundServiceStartNotAllowedException is harmless, if we are not in ForegroundState, removeNeedy is useless
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || !(e instanceof ForegroundServiceStartNotAllowedException)) {
                    throw e;
                }
            }

        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (mNotification != null) {
                startForeground(mNotificationId, mNotification);
            } else {
                startForeground(getNotificationId(), createNotification(this));
            }

            String action;
            if (intent != null && (action = intent.getAction()) != null) {
                switch (action) {
                    case ACTION_ADD_NEEDY: {
                        String needy = intent.getStringExtra(EXTRA_NEEDY);
                        mForegroundNeedy.add(needy);
                        if (intent.hasExtra(EXTRA_NOTIFICATION)) {
                            mNotification = intent.getParcelableExtra(EXTRA_NOTIFICATION);
                            mNotificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0);
                            mNotificationNeedy = needy;

                            if (mNotification != null) {
                                //stopForeground(true);
                                startForeground(mNotificationId, mNotification);
                            }
                        }
                        break;
                    }
                    case ACTION_REMOVE_NEEDY: {
                        String needy = intent.getStringExtra(EXTRA_NEEDY);
                        mForegroundNeedy.remove(needy);
                        if (needy.equals(mNotificationNeedy)) {
                            mNotification = null;
                            mNotificationNeedy = null;
                            mNotificationId = 0;

                            if (!mForegroundNeedy.isEmpty()) {
                                //stopForeground(true);
                                startForeground(getNotificationId(), createNotification(this));
                            }
                        }
                        break;
                    }
                }
            }
        }


        if (mForegroundNeedy.isEmpty()) {
            stopForeground(true);
            stopSelf();
            return START_NOT_STICKY;
        }


        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(getNotificationId(), createNotification(this));
        }
    }

    @Override
    public void onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true);
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static Notification createNotification(Context c) {
        NotificationManager nm = c.getSystemService(NotificationManager.class);

        String channelId = "foreground";
        if (nm.getNotificationChannel(channelId) == null) {
            nm.createNotificationChannel(new NotificationChannel(channelId, c.getString(R.string.appName), NotificationManager.IMPORTANCE_MIN));
        }

        Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, c.getPackageName())
                .putExtra(Settings.EXTRA_CHANNEL_ID, channelId);
        PendingIntent pendingIntent = PendingIntent.getActivity(c, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(c, channelId);
        builder.setContentIntent(pendingIntent);
        builder.setSmallIcon(R.drawable.ic_abicon);
        builder.setContentText(c.getString(R.string.clickToDisableNotification));
        builder.setPriority(NotificationCompat.PRIORITY_MIN);
        builder.setWhen(0); //show as last
        return builder.build();
    }


    public int getNotificationId() {
        return 571;
    }

}
