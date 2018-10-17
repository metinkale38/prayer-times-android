package com.metinkale.prayer.about;

import com.metinkale.prayer.BaseActivity;

public class MainActivity extends BaseActivity {


    public MainActivity() {
        super(R.string.about, R.mipmap.ic_launcher, new AboutFragment());
    }
}
