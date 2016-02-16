package com.metinkale.prayerapp.calendar;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.net.Uri;
import android.preference.ListPreference;
import android.provider.CalendarContract;
import android.util.AttributeSet;
import com.metinkale.prayer.R;
import com.metinkale.prayerapp.PermissionUtils;

import java.util.ArrayList;
import java.util.List;

public class CalendarPreference extends ListPreference {


    public CalendarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();

    }

    private Activity getActivity() {
        Context c = getContext();
        while (c instanceof ContextWrapper && !(c instanceof Activity)) {
            c = ((ContextWrapper) c).getBaseContext();
        }
        if (c instanceof Activity) return (Activity) c;
        return null;
    }

    @Override
    protected void onClick() {
        init();
        if (PermissionUtils.get(getActivity()).pCalendar) super.onClick();
        else PermissionUtils.get(getActivity()).needCalendar(getActivity(), true);
    }

    private void init() {
        List<String> names = new ArrayList<>();
        List<String> ids = new ArrayList<>();

        if (PermissionUtils.get(getActivity()).pCalendar) getCalendars(names, ids);

        names.add(0, getContext().getString(R.string.off));
        ids.add(0, "-1");

        this.setEntries(names.toArray(new String[names.size()]));
        this.setEntryValues(ids.toArray(new String[ids.size()]));
    }

    void getCalendars(List<String> names, List<String> ids) {

        String projection[] = {CalendarContract.Calendars._ID, CalendarContract.Calendars.CALENDAR_DISPLAY_NAME};
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
