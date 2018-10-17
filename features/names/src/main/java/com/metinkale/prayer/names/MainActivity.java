package com.metinkale.prayer.names;

import com.metinkale.prayer.BaseActivity;

public class MainActivity extends BaseActivity {
    public MainActivity() {
        super(R.string.names, R.mipmap.ic_names, new NamesFragment());
    }
}
