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

package com.metinkale.prayer;

import android.Manifest;
import android.app.ActivityManager;
import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.CalendarContract;
import android.text.format.DateUtils;
import android.text.format.Time;

import com.crashlytics.android.Crashlytics;
import com.metinkale.prayer.utils.ForegroundService;
import com.metinkale.prayer.utils.LocaleUtils;

import org.joda.time.LocalDate;
import org.joda.time.ReadableInstant;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.util.Pair;

public class CalendarIntegrationService extends IntentService {


    public CalendarIntegrationService() {
        super("CalendarIntegrationService");
    }


    public static void startCalendarIntegration(@NonNull Context context) {
        ForegroundService.addNeedy(context, "calendarIntegration");
        Intent intent = new Intent(context, CalendarIntegrationService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(123, ForegroundService.createNotification(this));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true);
        }
    }

    @Override
    protected void onHandleIntent(@NonNull Intent intent) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            Preferences.CALENDAR_INTEGRATION.set("-1");
            return;
        }
        ForegroundService.addNeedy(this, "calendarIntegration");
        Context context = App.get();
        try {
            ContentResolver cr = context.getContentResolver();


            cr.delete(CalendarContract.Events.CONTENT_URI, CalendarContract.Events.DESCRIPTION + "=\"com.metinkale.prayer\"", null);


            String id = Preferences.CALENDAR_INTEGRATION.get();

            if ("-1".equals(id)) {
                return;
            }
            int year = LocalDate.now().getYear();
            List<Pair<HijriDate, Integer>> holidays = new ArrayList<>();
            holidays.addAll(HijriDate.getHolydaysForGregYear(year));
            holidays.addAll(HijriDate.getHolydaysForGregYear(year + 1));

            List<ContentValues> events = new ArrayList<>();
            for (Pair<HijriDate, Integer> date : holidays) {
                if (date.second <= 0)
                    continue;
                ContentValues event = new ContentValues();

                event.put(CalendarContract.Events.CALENDAR_ID, id);
                event.put(CalendarContract.Events.TITLE, LocaleUtils.getHolyday(date.second));
                event.put(CalendarContract.Events.DESCRIPTION, "com.metinkale.prayer");
                LocalDate ld = date.first.getLocalDate();
                ReadableInstant cal = ld.toDateTimeAtStartOfDay();

                long dtstart = cal.getMillis();
                long dtend = dtstart + DateUtils.DAY_IN_MILLIS;

                event.put(CalendarContract.Events.DTSTART, dtstart + TimeZone.getDefault().getOffset(dtstart));
                event.put(CalendarContract.Events.DTEND, dtend + TimeZone.getDefault().getOffset(dtend));
                event.put(CalendarContract.Events.EVENT_TIMEZONE, Time.TIMEZONE_UTC);
                event.put(CalendarContract.Events.STATUS, CalendarContract.Events.STATUS_CONFIRMED);
                event.put(CalendarContract.Events.ALL_DAY, 1);

                events.add(event);
            }
            cr.bulkInsert(CalendarContract.Events.CONTENT_URI, events.toArray(new ContentValues[0]));
        } catch (Exception e) {
            Preferences.CALENDAR_INTEGRATION.set("-1");
            Crashlytics.logException(e);
        } finally {
            ForegroundService.removeNeedy(this, "calendarIntegration");
        }
    }
}
