package com.metinkale.prayer.dhikr;

import com.metinkale.prayer.BaseActivity;

public class MainActivity extends BaseActivity {
    public MainActivity() {
        super(R.string.dhikr, R.mipmap.ic_dhikr, new DhikrFragment());
    }
}
