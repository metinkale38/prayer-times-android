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

package com.metinkale.prayer.service;

import android.Manifest;
import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.BaseColumns;
import android.provider.CalendarContract;
import android.provider.ContactsContract;
import android.provider.SyncStateContract;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.metinkale.prayer.App;
import com.metinkale.prayer.base.R;
import com.metinkale.prayer.date.HijriDate;
import com.metinkale.prayer.Preferences;
import com.metinkale.prayer.utils.LocaleUtils;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.ReadableInstant;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.util.Pair;

public class CalendarIntegrationService extends IntentService {


    private static String ACCOUNT_NAME = "Prayer Times";
    private static String ACCOUNT_TYPE = "com.metinkale.prayer.calendar";
    private static String CALENDAR_COLUMN_NAME = "prayertimes_hijriadapter";

    public CalendarIntegrationService() {
        super("CalendarIntegrationService");
    }


    public static void startCalendarIntegration(@NonNull Context context) {
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
            startForeground(getClass().hashCode(), ForegroundService.createNotification(this));
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
        Context context = App.get();
        try {
            ContentResolver cr = context.getContentResolver();


            cr.delete(CalendarContract.Events.CONTENT_URI, CalendarContract.Events.DESCRIPTION + "=\"com.metinkale.prayer\"", null);

            Uri calenderUri = CalendarContract.Calendars.CONTENT_URI.buildUpon().appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                    .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, ACCOUNT_NAME)
                    .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, ACCOUNT_TYPE).build();
            cr.delete(calenderUri,
                    CalendarContract.Calendars.ACCOUNT_NAME + " = ? AND " + CalendarContract.Calendars.ACCOUNT_TYPE + " = ?",
                    new String[]{ACCOUNT_NAME, ACCOUNT_TYPE});

            String id = Preferences.CALENDAR_INTEGRATION.get();

            if ("-1".equals(id)) {
                return;
            }

            List<ContentValues> events = new ArrayList<>();
            long calendarId = getCalendar(context);
            for (int year = HijriDate.getMinGregYear(); year <= HijriDate.getMaxGregYear(); year++) {


                for (Pair<HijriDate, Integer> date : HijriDate.getHolydaysForGregYear(year)) {
                    if (date == null || date.second <= 0)
                        continue;
                    ContentValues event = new ContentValues();

                    event.put(CalendarContract.Events.CALENDAR_ID, calendarId);
                    event.put(CalendarContract.Events.TITLE, LocaleUtils.getHolyday(date.second));
                    event.put(CalendarContract.Events.DESCRIPTION, "com.metinkale.prayer");
                    LocalDate ld = date.first.getLocalDate();
                    DateTime cal = ld.toDateTimeAtStartOfDay(DateTimeZone.UTC);

                    long dtstart = cal.getMillis();
                    long dtend = cal.plusDays(1).getMillis();

                    event.put(CalendarContract.Events.DTSTART, dtstart);
                    event.put(CalendarContract.Events.DTEND, dtend);
                    event.put(CalendarContract.Events.EVENT_TIMEZONE, Time.TIMEZONE_UTC);
                    event.put(CalendarContract.Events.STATUS, CalendarContract.Events.STATUS_CONFIRMED);
                    event.put(CalendarContract.Events.ALL_DAY, 1);
                    event.put(CalendarContract.Events.HAS_ALARM, 0);
                    event.put(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_FREE);
                    event.put(CalendarContract.Events.CUSTOM_APP_PACKAGE, getPackageName());
                    event.put(CalendarContract.Events.CUSTOM_APP_URI, "https://prayerapp.page.link/calendar");


                    events.add(event);
                }
            }
            cr.bulkInsert(CalendarContract.Events.CONTENT_URI, events.toArray(new ContentValues[0]));
        } catch (Exception e) {
            Preferences.CALENDAR_INTEGRATION.set("-1");
            Crashlytics.logException(e);
        }

    }


    private static long getCalendar(Context context) {

        ContentResolver contentResolver = context.getContentResolver();

        // Find the calendar if we've got one
        Uri calenderUri = CalendarContract.Calendars.CONTENT_URI.buildUpon().appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, ACCOUNT_NAME)
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, ACCOUNT_TYPE).build();


        Cursor cursor = contentResolver.query(calenderUri, new String[]{BaseColumns._ID},
                CalendarContract.Calendars.ACCOUNT_NAME + " = ? AND " + CalendarContract.Calendars.ACCOUNT_TYPE + " = ?",
                new String[]{ACCOUNT_NAME, ACCOUNT_TYPE}, null);

        try {
            if (cursor != null && cursor.moveToNext()) {
                return cursor.getLong(0);
            } else {
                ArrayList<ContentProviderOperation> operationList = new ArrayList<>();

                ContentProviderOperation.Builder builder = ContentProviderOperation
                        .newInsert(calenderUri);
                builder.withValue(CalendarContract.Calendars.ACCOUNT_NAME, ACCOUNT_NAME);
                builder.withValue(CalendarContract.Calendars.ACCOUNT_TYPE, ACCOUNT_TYPE);
                builder.withValue(CalendarContract.Calendars.NAME, CALENDAR_COLUMN_NAME);
                builder.withValue(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
                        context.getString(R.string.appName));
                builder.withValue(CalendarContract.Calendars.CALENDAR_COLOR, context.getResources().getColor(R.color.colorPrimary));
                builder.withValue(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_READ);
                builder.withValue(CalendarContract.Calendars.OWNER_ACCOUNT, ACCOUNT_NAME);
                builder.withValue(CalendarContract.Calendars.SYNC_EVENTS, 1);
                builder.withValue(CalendarContract.Calendars.VISIBLE, 1);

                operationList.add(builder.build());
                try {
                    contentResolver.applyBatch(CalendarContract.AUTHORITY, operationList);
                } catch (Exception e) {
                    e.printStackTrace();
                    return -1;
                }
                return getCalendar(context);
            }
        } finally {
            if (cursor != null && !cursor.isClosed())
                cursor.close();
        }
    }
}
