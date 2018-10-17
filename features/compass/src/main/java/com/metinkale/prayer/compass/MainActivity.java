package com.metinkale.prayer.compass;

import com.metinkale.prayer.BaseActivity;

public class MainActivity extends BaseActivity {

    public MainActivity() {
        super(R.string.compass, R.mipmap.ic_compass, new CompassFragment());
    }
}
