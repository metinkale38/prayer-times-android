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

package com.metinkale.prayer;

import android.Manifest;
import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.CalendarContract;
import android.text.format.DateUtils;
import android.text.format.Time;

import com.crashlytics.android.Crashlytics;
import com.metinkale.prayer.utils.LocaleUtils;

import org.joda.time.LocalDate;
import org.joda.time.ReadableInstant;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.util.Pair;

public class MainIntentService extends IntentService {
    
    private static final String ACTION_CALENDAR_INTEGRATION = "com.metinkale.prayer.action.CALENDAR_INTEGRATION";
    
    
    public MainIntentService() {
        super("MainIntentService");
    }
    
    
    public static void startCalendarIntegration(@NonNull Context context) {
        Intent intent = new Intent(context, MainIntentService.class);
        intent.setAction(ACTION_CALENDAR_INTEGRATION);
        context.startService(intent);
    }
    
    
    @Override
    protected void onHandleIntent(@NonNull Intent intent) {
        long mills = System.currentTimeMillis();
        String action = intent.getAction();
        switch (action) {
            
            case ACTION_CALENDAR_INTEGRATION:
                handleCalendarIntegration();
                break;
        }
        
        mills -= System.currentTimeMillis();
        if (mills > 1000) {
            Crashlytics.logException(new Exception(action.substring(action.lastIndexOf(".") + 1) + " took " + mills + " ms"));
        }
        
        
    }
    
    
    private void handleCalendarIntegration() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            Prefs.setCalendar("-1");
            return;
        }
        Context context = App.get();
        try {
            ContentResolver cr = context.getContentResolver();
            
            
            cr.delete(CalendarContract.Events.CONTENT_URI, CalendarContract.Events.DESCRIPTION + "=\"com.metinkale.prayer\"", null);
            
            
            String id = Prefs.getCalendar();
            
            if ("-1".equals(id)) {
                return;
            }
            int year = LocalDate.now().getYear();
            List<Pair<HijriDate, Integer>> holidays = new ArrayList<>();
            holidays.addAll(HijriDate.getHolydaysForGregYear(year));
            holidays.addAll(HijriDate.getHolydaysForGregYear(year + 1));
            
            int i = 0;
            ContentValues[] events = new ContentValues[holidays.size()];
            for (Pair<HijriDate, Integer> date : holidays) {
                if (date.second < 0)
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
                
                events[i] = event;
                i++;
            }
            cr.bulkInsert(CalendarContract.Events.CONTENT_URI, events);
        } catch (Exception e) {
            Prefs.setCalendar("-1");
            Crashlytics.logException(e);
        }
    }
}
