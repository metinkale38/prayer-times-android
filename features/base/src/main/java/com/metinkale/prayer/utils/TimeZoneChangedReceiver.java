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

package com.metinkale.prayer.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.joda.time.DateTimeZone;

import java.util.TimeZone;

import androidx.annotation.NonNull;

/**
 * Listens for android.intent.action.TIMEZONE_CHANGED and adjusts
 * default DateTimeZone as necessary.
 */
public class TimeZoneChangedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, @NonNull Intent intent) {
        String tzId = intent.getStringExtra("time-zone");

        try {
            DateTimeZone newDefault = DateTimeZone.forTimeZone(TimeZone.getDefault());
            DateTimeZone.setDefault(newDefault);
            Log.d("prayer-times-android", "TIMEZONE_CHANGED received, changed default timezone to \"" + tzId + "\"");
        } catch (IllegalArgumentException e) {
            Log.d("prayer-times-android", "Could not recognize timezone id \"" + tzId + "\"", e);
        }
    }

}
