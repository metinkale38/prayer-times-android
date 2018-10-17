package com.metinkale.prayer.settings;

import com.metinkale.prayer.BaseActivity;

public class MainActivity extends BaseActivity {
    public MainActivity() {
        super(R.string.settings, 0, new SettingsFragment());
    }
}
