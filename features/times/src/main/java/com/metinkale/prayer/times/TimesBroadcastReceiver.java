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

package com.metinkale.prayer.times;

import com.crashlytics.android.Crashlytics;
import com.evernote.android.job.JobManager;
import com.metinkale.prayer.App;
import com.metinkale.prayer.receiver.InternalBroadcastReceiver;
import com.metinkale.prayer.times.times.Times;

import androidx.annotation.NonNull;


public class TimesBroadcastReceiver extends InternalBroadcastReceiver
        implements InternalBroadcastReceiver.OnStartListener, InternalBroadcastReceiver.OnPrefsChangedListener {


    @Override
    public void onPrefsChanged(@NonNull String key) {
        switch (key) {
            case "useAlarm":
                Times.setAlarms();
                break;
            case "use12h":
            case "ongoingIcon":
            case "ongoingNumber":
            case "SHOW_ALT_WIDGET_HIGHLIGHT":
            case "widget_countdown":
            case "alternativeOngoing":
            case "showLegacyWidgets":
                InternalBroadcastReceiver.sender(App.get()).sendTimeTick();
                break;
        }
    }

    @Override
    public void onStart() {
        JobManager.create(App.get()).addJobCreator(new SyncJobCreator());

        try {
            Times.getTimes();
        } catch (Exception e) {
            Crashlytics.logException(e);
        }

        InternalBroadcastReceiver.sender(App.get()).sendTimeTick();
        LocationReceiver.start(App.get());
        Times.setAlarms();
    }
}
