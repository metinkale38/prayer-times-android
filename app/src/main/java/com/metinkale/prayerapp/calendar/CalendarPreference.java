/*
 * Copyright (c) 2016 Metin Kale
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
 *
 */

package com.metinkale.prayerapp.calendar;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.net.Uri;
import android.preference.ListPreference;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.util.AttributeSet;

import com.metinkale.prayer.R;
import com.metinkale.prayerapp.utils.PermissionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CalendarPreference extends ListPreference {


    public CalendarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();

    }

    private Activity getActivity() {
        Context c = getContext();
        while ((c instanceof ContextWrapper) && !(c instanceof Activity)) {
            c = ((ContextWrapper) c).getBaseContext();
        }
        if (c instanceof Activity) {
            return (Activity) c;
        }
        return null;
    }

    @Override
    protected void onClick() {
        init();
        if (PermissionUtils.get(getActivity()).pCalendar) {
            super.onClick();
        } else {
            PermissionUtils.get(getActivity()).needCalendar(getActivity(), true);
        }
    }

    private void init() {
        List<String> names = new ArrayList<>();
        List<String> ids = new ArrayList<>();

        if (PermissionUtils.get(getActivity()).pCalendar) {
            getCalendars(names, ids);
        }

        names.add(0, getContext().getString(R.string.off));
        ids.add(0, "-1");

        setEntries(names.toArray(new String[names.size()]));
        setEntryValues(ids.toArray(new String[ids.size()]));
    }

    private void getCalendars(@NonNull Collection<String> names, @NonNull Collection<String> ids) {

        String[] projection = {CalendarContract.Calendars._ID, CalendarContract.Calendars.CALENDAR_DISPLAY_NAME};
        Uri calendars;
        calendars = Uri.parse("content://com.android.calendar/calendars");

        ContentResolver contentResolver = getContext().getContentResolver();
        Cursor managedCursor = contentResolver.query(calendars, projection, null, null, null);


        if (managedCursor == null) {
            setEnabled(false);
            return;
        }
        if (managedCursor.moveToFirst()) {
            String calName;
            String calID;
            int nameCol = managedCursor.getColumnIndex(projection[1]);
            int idCol = managedCursor.getColumnIndex(projection[0]);
            do {
                calName = managedCursor.getString(nameCol);
                calID = managedCursor.getString(idCol);
                names.add(calName);
                ids.add(calID);
            } while (managedCursor.moveToNext());
            managedCursor.close();
        }

    }

}
