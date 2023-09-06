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
package com.metinkale.prayer.times

import com.metinkale.prayer.App
import com.metinkale.prayer.receiver.AppEventManager
import com.metinkale.prayer.receiver.OnPrefsChangedListener
import com.metinkale.prayer.receiver.OnStartListener
import com.metinkale.prayer.times.times.SyncTimesWorker
import com.metinkale.prayer.times.times.Times
import com.metinkale.prayer.times.times.setAlarms

object TimesBroadcastReceiver : OnStartListener, OnPrefsChangedListener {
    override fun onPrefsChanged(key: String) {
        when (key) {
            "useAlarm" -> Times.setAlarms()
            "use12h", "ongoingIcon", "ongoingNumber", "SHOW_ALT_WIDGET_HIGHLIGHT", "widget_countdown", "alternativeOngoing", "showLegacyWidgets" ->
                AppEventManager.sendTimeTick()
        }
    }

    override fun onStart() {
        AppEventManager.sendTimeTick()
        Times.setAlarms()
        SyncTimesWorker.scheduleWorker(App.get())
    }
}