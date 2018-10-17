package com.metinkale.prayer.calendar;


import com.metinkale.prayer.BaseActivity;

public class MainActivity extends BaseActivity {
    public MainActivity() {
        super(R.string.calendar, R.mipmap.ic_calendar, new CalendarFragment());
    }
}
