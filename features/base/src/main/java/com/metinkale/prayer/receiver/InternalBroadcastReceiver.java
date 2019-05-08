
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

package com.metinkale.prayer.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.metinkale.prayer.App;

public class InternalBroadcastReceiver extends BroadcastReceiver {
    private static final String ACTION_ON_START = "com.metikale.prayer.ON_START";
    private static final String ACTION_PREFSCHANGED = "com.metinkale.prayer.PREFS_CHANGED";
    private static final String ACTION_TIMETICK = "com.metinkale.prayer.TIMETICK";


    public static void loadAll() {
        loadClass("com.metinkale.prayer.times.TimesBroadcastReceiver");
        loadClass("com.metinkale.prayer.times.OngoingNotificationsReceiver");
        loadClass("com.metinkale.prayerapp.vakit.WidgetUtils");
        loadClass("com.metinkale.prayer.AliasManager");
    }

    private static void loadClass(Class<? extends InternalBroadcastReceiver> clz) {
        try {
            InternalBroadcastReceiver receiver = clz.newInstance();
            receiver.register();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void loadClass(String clz) {
        try {
            //noinspection unchecked
            loadClass((Class<? extends InternalBroadcastReceiver>) Class.forName(clz));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    @NonNull
    private Context context;


    public InternalBroadcastReceiver(@NonNull Context context) {
        this.context = context;
    }


    protected InternalBroadcastReceiver() {
        this(App.get());
    }

    public void register() {
        IntentFilter filter = new IntentFilter();
        if (this instanceof OnStartListener) {
            filter.addAction(ACTION_ON_START);
        }
        if (this instanceof OnTimeTickListener) {
            filter.addAction(ACTION_TIMETICK);
        }
        if (this instanceof OnPrefsChangedListener) {
            filter.addAction(ACTION_PREFSCHANGED);
        }

        LocalBroadcastManager.getInstance(context).registerReceiver(this, filter);
    }

    public void unregister() {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
    }

    @NonNull
    protected Context getContext() {
        return context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action;
        if (intent == null || (action = intent.getAction()) == null)
            return;
        switch (action) {
            case ACTION_TIMETICK:
                if (this instanceof OnTimeTickListener) {
                    ((OnTimeTickListener) this).onTimeTick();
                }
                break;
            case ACTION_ON_START:
                if (this instanceof OnStartListener) {
                    ((OnStartListener) this).onStart();
                }
                break;
            case ACTION_PREFSCHANGED:
                if (this instanceof OnPrefsChangedListener) {
                    ((OnPrefsChangedListener) this).onPrefsChanged(intent.getStringExtra("key"));
                }

                break;
        }
    }

    public interface OnPrefsChangedListener {
        void onPrefsChanged(@NonNull String key);
    }

    public interface OnStartListener {
        void onStart();
    }

    public interface OnTimeTickListener {
        void onTimeTick();
    }


    public static Sender sender(Context c) {
        return new Sender(c);
    }


    public static class Sender {
        private final Context context;

        private Sender(Context c) {
            context = c;
        }


        public void sendOnPrefsChanged(@NonNull String key) {
            Intent i = new Intent(ACTION_PREFSCHANGED);
            i.putExtra("key", key);
            LocalBroadcastManager.getInstance(context).sendBroadcast(i);
        }


        public void sendOnStart() {
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ACTION_ON_START));
        }

        public void sendTimeTick() {
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ACTION_TIMETICK));
        }
    }

}
