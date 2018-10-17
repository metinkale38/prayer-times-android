/*
 * Copyright (c) 2013-2017 Metin Kale
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
 */

package com.metinkale.prayer.intro;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.metinkale.prayer.App;
import com.metinkale.prayer.BaseActivity;
import com.metinkale.prayer.Prefs;
import com.metinkale.prayer.base.BuildConfig;
import com.metinkale.prayer.times.times.Times;
import com.metinkale.prayer.times.times.sources.CalcTimes;

import java.lang.reflect.Field;
import java.util.Locale;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

/**
 * Created by metin on 17.07.2017.
 */

public class MainActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener, View.OnClickListener {
    private ViewPager mPager;
    private int[] mColors = {App.get().getResources().getColor(R.color.colorPrimary), App.get().getResources().getColor(R.color.indicator),
            App.get().getResources().getColor(R.color.colorPrimaryDark), 0xFF3F51B5, 0xFF00BCD4};
    private IntroFragment[] mFragments =
            {new LanguageFragment(), new ChangelogFragment(), new MenuIntroFragment(), new PagerIntroFragment(), new ConfigIntroFragment()};
    private MyAdapter mAdapter;
    private View mMain;
    private Button mForward;
    private Button mBack;
    
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro_main);
        mPager = findViewById(R.id.pager);
        mMain = findViewById(R.id.main);
        mBack = findViewById(R.id.back);
        mForward = findViewById(R.id.forward);
        mPager.setOffscreenPageLimit(10);
        mBack.setOnClickListener(this);
        mForward.setOnClickListener(this);
        
        if (Times.getCount() < 2) {
            if (Locale.getDefault().getLanguage().equals(new Locale("tr").getLanguage())) {
                CalcTimes.buildTemporaryTimes("Mekke", 21.4260221, 39.8296538, -1).getPrayTimes()
                        .setTimezone(java.util.TimeZone.getTimeZone("Asia/Riyadh"));
                CalcTimes.buildTemporaryTimes("Kayseri", 38.7333333333333, 35.4833333333333, -2).getPrayTimes()
                        .setTimezone(java.util.TimeZone.getTimeZone("Turkey"));
            } else if (Locale.getDefault().getLanguage().equals(new Locale("de").getLanguage())) {
                CalcTimes.buildTemporaryTimes("Mekka", 21.4260221, 39.8296538, -1).getPrayTimes()
                        .setTimezone(java.util.TimeZone.getTimeZone("Asia/Riyadh"));
                CalcTimes.buildTemporaryTimes("Braunschweig", 52.2666666666667, 10.5166666666667, -2).getPrayTimes()
                        .setTimezone(java.util.TimeZone.getTimeZone("Europe/Berlin"));
            } else if (Locale.getDefault().getLanguage().equals(new Locale("fr").getLanguage())) {
                CalcTimes.buildTemporaryTimes("Mecque", 21.4260221, 39.8296538, -1).getPrayTimes()
                        .setTimezone(java.util.TimeZone.getTimeZone("Asia/Riyadh"));
                CalcTimes.buildTemporaryTimes("Paris", 48.8566101, 2.3514992, -2).getPrayTimes()
                        .setTimezone(java.util.TimeZone.getTimeZone("Europe/Paris"));
            } else {
                CalcTimes.buildTemporaryTimes("Mecca", 21.4260221, 39.8296538, -1).getPrayTimes()
                        .setTimezone(java.util.TimeZone.getTimeZone("Asia/Riyadh"));
                CalcTimes.buildTemporaryTimes("London", 51.5073219, -0.1276473, -2).getPrayTimes()
                        .setTimezone(java.util.TimeZone.getTimeZone("Europe/London"));
            }
        }
        
        int doNotShow = 0;
        for (int i = 0; i < mFragments.length; i++) {
            if (!mFragments[i].shouldShow()) {
                mFragments[i] = null;
                doNotShow++;
            }
        }
        
        IntroFragment[] newArray = new IntroFragment[mFragments.length - doNotShow + 1];
        if (newArray.length == 1)
            startActivity(new Intent(this, BaseActivity.class));
        int i = 0;
        for (IntroFragment frag : mFragments) {
            if (frag != null) {
                newArray[i++] = frag;
            }
        }
        newArray[i] = new LastFragment();
        mFragments = newArray;
        
        mAdapter = new MyAdapter(getSupportFragmentManager());
        mPager.setAdapter(mAdapter);
        mPager.addOnPageChangeListener(this);
        
        
    }
    
    @Override
    public void recreate() {
        try {
            mPager.setAdapter(mAdapter);//force reinflating of fragments
        } catch (Exception e) {
            super.recreate();
        }
    }
    
    private static int blendColors(int color1, int color2, float ratio) {
        final float inverseRation = 1f - ratio;
        float r = (Color.red(color1) * ratio) + (Color.red(color2) * inverseRation);
        float g = (Color.green(color1) * ratio) + (Color.green(color2) * inverseRation);
        float b = (Color.blue(color1) * ratio) + (Color.blue(color2) * inverseRation);
        return Color.rgb((int) r, (int) g, (int) b);
    }
    
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        int color1 = mColors[position % mColors.length];
        int color2 = mColors[(position + 1) % mColors.length];
        mMain.setBackgroundColor(blendColors(color1, color2, 1 - positionOffset));
        
        if (position > 0 && positionOffset == 0)
            mFragments[position].setPagerPosition(1);
        mFragments[position].setPagerPosition(positionOffset);
        if (mFragments.length > position + 1)
            mFragments[position + 1].setPagerPosition(1 - positionOffset);
    }
    
    @Override
    public void onPageSelected(int position) {
        mBack.setVisibility(position == 0 ? View.GONE : View.VISIBLE);
        mForward.setText(position == mAdapter.getCount() - 1 ? R.string.finish : R.string.continue_);
    }
    
    @Override
    public void onPageScrollStateChanged(int state) {
    
    }
    
    @Override
    public void onClick(View view) {
        int i1 = view.getId();
        if (i1 == R.id.back) {
            int pos = mPager.getCurrentItem();
            if (pos > 0)
                mPager.setCurrentItem(pos - 1, true);
        } else if (i1 == R.id.forward) {
            int pos = mPager.getCurrentItem();
            if (pos < mAdapter.getCount() - 1)
                mPager.setCurrentItem(pos + 1, true);
            else {
                Times.clearTemporaryTimes();
                finish();
                Prefs.setChangelogVersion(BuildConfig.CHANGELOG_VERSION);
                Prefs.setShowIntro(false);
                
                Bundle bdl = new Bundle();
                bdl.putBoolean("openCitySearch", true);
                BaseActivity.launch(this, "times", bdl);
                
                String appName = "appName";
                String lang = Prefs.getLanguage();
                if (!lang.isEmpty() && !lang.equals("system")) {
                    appName += Character.toUpperCase(lang.charAt(0)) + lang.substring(1);
                }
                
                Intent.ShortcutIconResource icon = Intent.ShortcutIconResource.fromContext(this, R.mipmap.ic_launcher);
                Intent intent = new Intent();
                Intent launchIntent = new Intent(this, BaseActivity.class);
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                launchIntent.putExtra("duplicate", false);
                intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launchIntent);
                intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(getStringResId(appName, R.string.appName)));
                intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
                intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
                sendBroadcast(intent);
            }
        }
    }
    
    private int getStringResId(String resName, int def) {
        try {
            Field f = R.string.class.getDeclaredField(resName);
            return f.getInt(null);
        } catch (IllegalAccessException e) {
            return def;
        } catch (NoSuchFieldException e) {
            return def;
        }
    }
    
    
    private class MyAdapter extends FragmentPagerAdapter {
        public MyAdapter(FragmentManager fm) {
            super(fm);
        }
        
        @Override
        public Fragment getItem(int position) {
            return mFragments[position];
        }
        
        @Override
        public int getCount() {
            return mFragments.length;
        }
    }
}
