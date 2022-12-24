/*
 * Copyright (c) 2013-2019 Metin Kale
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.metinkale.prayer.App;
import com.metinkale.prayer.BaseActivity;
import com.metinkale.prayer.Module;
import com.metinkale.prayer.Preferences;
import com.metinkale.prayer.base.BuildConfig;
import com.metinkale.prayer.times.times.Times;
import com.metinkale.prayer.times.times.sources.CalcTimes;
import com.metinkale.prayer.times.utils.RTLViewPager;
import com.metinkale.prayer.utils.LocaleUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Locale;

/**
 * Created by metin on 17.07.2017.
 */

public class MainActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener, View.OnClickListener {
    private RTLViewPager mPager;
    private final int[] mColors = {App.get().getResources().getColor(R.color.colorPrimary), App.get().getResources().getColor(R.color.accent),
            App.get().getResources().getColor(R.color.colorPrimaryDark), 0xFF3F51B5, 0xFF00BCD4};
    private final Class[] mFragClz = new Class[]{LanguageFragment.class, ChangelogFragment.class, MenuIntroFragment.class, PagerIntroFragment.class, ConfigIntroFragment.class};
    private IntroFragment[] mFragments;
    private MyAdapter mAdapter;
    private View mMain;
    private Button mForward;
    private Button mBack;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocaleUtils.init(this);
        createTemproraryTimes();
        setContentView(R.layout.intro_main);
        mPager = findViewById(R.id.pager);
        mMain = findViewById(R.id.main);
        mBack = findViewById(R.id.back);
        mForward = findViewById(R.id.forward);
        mPager.setOffscreenPageLimit(10);
        mBack.setOnClickListener(this);
        mForward.setOnClickListener(this);

        mFragments = new IntroFragment[mFragClz.length];
        for (int i = 0; i < mFragClz.length; i++) {
            try {
                mFragments[i] = (IntroFragment) mFragClz[i].newInstance();
            } catch (IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
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
        mPager.setRTLSupportAdapter(getSupportFragmentManager(), mAdapter);
        mPager.addOnPageChangeListener(this);


    }

    private void createTemproraryTimes() {
        if (Times.getCount() < 2) {
            if (Locale.getDefault().getLanguage().equals(new Locale("tr").getLanguage())) {
                CalcTimes.buildTemporaryTimes("Mekke", 21.4260221, 39.8296538, -1)
                        .setTimezone(java.util.TimeZone.getTimeZone("Asia/Riyadh"));
                CalcTimes.buildTemporaryTimes("Kayseri", 38.7333333333333, 35.4833333333333, -2)
                        .setTimezone(java.util.TimeZone.getTimeZone("Turkey"));
            } else if (Locale.getDefault().getLanguage().equals(new Locale("de").getLanguage())) {
                CalcTimes.buildTemporaryTimes("Mekka", 21.4260221, 39.8296538, -1)
                        .setTimezone(java.util.TimeZone.getTimeZone("Asia/Riyadh"));
                CalcTimes.buildTemporaryTimes("Braunschweig", 52.2666666666667, 10.5166666666667, -2)
                        .setTimezone(java.util.TimeZone.getTimeZone("Europe/Berlin"));
            } else if (Locale.getDefault().getLanguage().equals(new Locale("fr").getLanguage())) {
                CalcTimes.buildTemporaryTimes("Mecque", 21.4260221, 39.8296538, -1)
                        .setTimezone(java.util.TimeZone.getTimeZone("Asia/Riyadh"));
                CalcTimes.buildTemporaryTimes("Paris", 48.8566101, 2.3514992, -2)
                        .setTimezone(java.util.TimeZone.getTimeZone("Europe/Paris"));
            } else {
                CalcTimes.buildTemporaryTimes("Mecca", 21.4260221, 39.8296538, -1)
                        .setTimezone(java.util.TimeZone.getTimeZone("Asia/Riyadh"));
                CalcTimes.buildTemporaryTimes("London", 51.5073219, -0.1276473, -2)
                        .setTimezone(java.util.TimeZone.getTimeZone("Europe/London"));
            }
        }
    }

    @Override
    public void recreate() {
        try {
            mPager.setAdapter(mAdapter);//force reinflating of fragments
        } catch (Exception e) {
            super.recreate();
        }
    }

    public static int blendColors(int color1, int color2, float ratio) {
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
                Preferences.CHANGELOG_VERSION.set(BuildConfig.CHANGELOG_VERSION);
                Preferences.SHOW_INTRO.set(false);

                Bundle bdl = new Bundle();
                if (Times.getCount() == 0) {
                    bdl.putBoolean("openCitySearch", true);
                }
                Module.TIMES.launch(this, bdl);

                String appName = "appName";
                String lang = Preferences.LANGUAGE.get();
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
        } catch (IllegalAccessException | NoSuchFieldException e) {
            return def;
        }
    }

    public int getBackgroundColor(@NonNull Class<? extends IntroFragment> clz) {
        return mColors[Arrays.asList(mFragClz).indexOf(clz)];
    }


    private class MyAdapter extends FragmentPagerAdapter {
        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @NonNull
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
