package com.metinkale.prayer.missedprayers;

import com.metinkale.prayer.BaseActivity;

public class MainActivity extends BaseActivity {
    public MainActivity() {
        super(R.string.missedPrayers, R.mipmap.ic_launcher, new MissedPrayersFragment());
    }
}
