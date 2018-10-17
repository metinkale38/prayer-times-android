package com.metinkale.prayer.times;

import android.os.Bundle;

import com.metinkale.prayer.BaseActivity;
import com.metinkale.prayer.times.fragments.SearchCityFragment;
import com.metinkale.prayer.times.fragments.TimesFragment;

public class MainActivity extends BaseActivity {

    public MainActivity() {
        super(R.string.appName, R.mipmap.ic_launcher, new TimesFragment());
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocationService.triggerUpdate(this);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().getBooleanExtra("openCitySearch", false)) {
            moveToFrag(new SearchCityFragment());
        }
    }
}
