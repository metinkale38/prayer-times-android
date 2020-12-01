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

package com.metinkale.prayer.times.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.metinkale.prayer.App;
import com.metinkale.prayer.BaseActivity;
import com.metinkale.prayer.Preferences;
import com.metinkale.prayer.date.HijriDate;
import com.metinkale.prayer.times.R;
import com.metinkale.prayer.times.times.Times;
import com.metinkale.prayer.times.times.Vakit;
import com.metinkale.prayer.times.times.sources.WebTimes;
import com.metinkale.prayer.times.utils.ExportController;
import com.metinkale.prayer.utils.LocaleUtils;
import com.metinkale.prayer.utils.Utils;

import net.steamcrafted.materialiconlib.MaterialIconView;
import net.steamcrafted.materialiconlib.MaterialMenuInflater;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import java.util.Locale;

@SuppressLint("ClickableViewAccessibility")
public class CityFragment extends Fragment implements Observer<Times> {

    @NonNull
    private static Handler mHandler = new Handler();
    private View mView;
    @Nullable
    private Times mTimes;
    private TextView mCountdown;
    private TextView mKerahat;
    private TextView mTitle;
    private TextView mHicri;
    private TextView mDate;
    private TextView[] mPrayerTitles;
    private TextView[] mPrayerTimes;
    private MaterialIconView mGPSIcon;
    private int mThreeSecondCounter;
    @NonNull
    private Runnable onSecond = new Runnable() {

        @Override
        public void run() {

            if ((mTimes != null) && !mTimes.isDeleted()) {
                checkKerahat();
                if (mTimes.isAutoLocation())
                    mTitle.setText(mTimes.getName());

                int next = mTimes.getNextTime();
                int indicator = next;
                if (Preferences.VAKIT_INDICATOR_TYPE.get().equals("next"))
                    indicator = indicator + 1;
                for (int i = 0; i < 6; i++) {
                    TextView time = mPrayerTimes[i];
                    ViewGroup parent = (ViewGroup) time.getParent();
                    if (i == indicator - 1) {
                        time.setBackgroundResource(R.color.accent);
                        parent.getChildAt(parent.indexOfChild(time) - 1).setBackgroundResource(R.color.accent);
                    } else {
                        time.setBackgroundResource(R.color.transparent);
                        parent.getChildAt(parent.indexOfChild(time) - 1).setBackgroundResource(R.color.transparent);
                    }
                }


                String left = LocaleUtils.formatPeriod(DateTime.now(), mTimes.getTime(LocalDate.now(), next).toDateTime(), true);
                mCountdown.setText(left);

                if (++mThreeSecondCounter % 3 == 0) {
                    LocalDate date = LocalDate.now();
                    LocalDateTime sabah = mTimes.getSabahTime(date);
                    LocalDateTime asr = mTimes.getAsrThaniTime(date);
                    if (!(sabah.toLocalTime().equals(LocalTime.MIDNIGHT))) {
                        if (mThreeSecondCounter % 6 == 0) {
                            mPrayerTitles[Vakit.FAJR.ordinal()].setText(Vakit.FAJR.getString(0));
                            mPrayerTimes[Vakit.FAJR.ordinal()].setText(LocaleUtils.formatTimeForHTML(mTimes.getTime(date, Vakit.FAJR.ordinal()).toLocalTime()));
                        } else {
                            mPrayerTitles[Vakit.FAJR.ordinal()].setText(Vakit.FAJR.getString(1));
                            mPrayerTimes[Vakit.FAJR.ordinal()].setText(LocaleUtils.formatTimeForHTML(mTimes.getSabahTime(date).toLocalTime()));
                        }
                    }

                    if (!(asr.toLocalTime().equals(LocalTime.MIDNIGHT))) {
                        if (mThreeSecondCounter % 6 == 0) {
                            mPrayerTitles[Vakit.ASR.ordinal()].setText(Vakit.ASR.getString(0));
                            mPrayerTimes[Vakit.ASR.ordinal()].setText(LocaleUtils.formatTimeForHTML(mTimes.getTime(date, Vakit.ASR.ordinal()).toLocalTime()));
                        } else {
                            mPrayerTitles[Vakit.ASR.ordinal()].setText(Vakit.ASR.getString(1));
                            mPrayerTimes[Vakit.ASR.ordinal()].setText(LocaleUtils.formatTimeForHTML(mTimes.getAsrThaniTime(date).toLocalTime()));
                        }
                    }

                }

            }
            mHandler.postDelayed(this, 1000);

        }


    };


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle bdl) {
        mTimes = Times.getTimes(getArguments().getLong("city"));
        if (mTimes == null) {
            return new View(getActivity());
        }

        mView = inflater.inflate(R.layout.vakit_fragment, container, false);
        setHasOptionsMenu(true);


        mCountdown = mView.findViewById(R.id.countdown);
        mTitle = mView.findViewById(R.id.city);
        mDate = mView.findViewById(R.id.date);
        mHicri = mView.findViewById(R.id.hicri);
        mGPSIcon = mView.findViewById(R.id.gps);

        mKerahat = mView.findViewById(R.id.kerahat);


        int[] ids = {R.id.fajrTime, R.id.sunTime, R.id.zuhrTime, R.id.asrTime, R.id.maghribTime, R.id.ishaaTime};
        int[] idsNames = {R.id.fajr, R.id.sun, R.id.zuhr, R.id.asr, R.id.maghrib, R.id.ishaa};
        mPrayerTitles = new TextView[idsNames.length];
        mPrayerTimes = new TextView[ids.length];
        for (int i = 0; i < ids.length; i++) {
            mPrayerTitles[i] = mView.findViewById(idsNames[i]);
            mPrayerTimes[i] = mView.findViewById(ids[i]);
            mPrayerTitles[i].setText(Vakit.getByIndex(i).getString());
        }


        mTimes.observe(this, this);
        onChanged(mTimes);

        if (new Locale("tr").getLanguage().equals(LocaleUtils.getLocale().getLanguage())) {
            ((TextView) mView.findViewById(R.id.fajr)).setText(R.string.imsak);
        } else {
            ((TextView) mView.findViewById(R.id.fajr)).setText(R.string.fajr);
        }

        if (mTimes.getID() >= 0) {
            ImageView source1 = mView.findViewById(R.id.source1);
            ImageView source2 = mView.findViewById(R.id.source2);
            if (mTimes.getSource().drawableId != 0) {
                source1.setImageResource(mTimes.getSource().drawableId);
                source2.setImageResource(mTimes.getSource().drawableId);

                if (Utils.isNightMode(getActivity())) {
                    float[] matrix = {
                            1 / 3f, 1 / 3f, 1 / 3f, 0, 255, //red
                            1 / 3f, 1 / 3f, 1 / 3f, 0, 255, //green
                            1 / 3f, 1 / 3f, 1 / 3f, 0, 255, //blue
                            0, 0, 0, 1.0f, 0 //alpha
                    };
                    ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
                    source1.setColorFilter(filter);
                    source2.setColorFilter(filter);
                }
            }
        }

        if (Preferences.USE_ARABIC.get()) {
            for (int i = 0; i < mPrayerTitles.length; i++) {
                TextView tv = mPrayerTitles[i];
                tv.setGravity(Gravity.START);
                tv.setText(Vakit.getByIndex(i).getString());
            }
        }


        return mView;
    }

    @Override
    public void onChanged(@Nullable Times times) {
        update();
    }


    public void update() {
        if ((mTimes == null) || (mView == null)) {
            return;
        }

        mTitle.setText(mTimes.getName());
        mGPSIcon.setVisibility(mTimes.isAutoLocation() ? View.VISIBLE : View.GONE);


        LocalDate greg = LocalDate.now();
        HijriDate hijr = HijriDate.now();

        mHicri.setText(LocaleUtils.formatDate(hijr));
        mDate.setText(LocaleUtils.formatDate(greg));

        if (!updateTimes() && mTimes instanceof WebTimes && App.isOnline()) {
            ((WebTimes) mTimes).syncAsync();
        }

        mHandler.postDelayed(this::update, 60000);


    }

    private boolean updateTimes() {
        LocalDate greg = LocalDate.now();

        boolean hasTimes = false;
        for (int i = 0; i < 6; i++) {

            TextView time = mPrayerTimes[i];
            time.setText(LocaleUtils.formatTimeForHTML(mTimes.getTime(greg, i).toLocalTime()));
            if (!time.getText().equals("00:00")) {
                hasTimes = true;
            }
        }

        if (!hasTimes) {
            mHandler.postDelayed(this::updateTimes, 1000);
        }

        return hasTimes;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        try {
            MaterialMenuInflater.with(getActivity(), inflater).setDefaultColor(Color.WHITE).inflate(R.menu.vakit, menu);
        } catch (Exception e) {
            e.printStackTrace();
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (mTimes == null) return super.onOptionsItemSelected(item);
        int i1 = item.getItemId();
        if (i1 == R.id.notification) {
            Fragment frag = getActivity().getSupportFragmentManager().findFragmentByTag("notPrefs");
            if (frag == null) {
                ((TimesFragment) getParentFragment()).setFooterText("", false);
                ((BaseActivity.MainFragment) getParentFragment()).moveToFrag(AlarmsFragment.create(mTimes));
            } else {
                ((TimesFragment) getParentFragment()).setFooterText(getString(R.string.monthly), true);
                ((BaseActivity.MainFragment) getParentFragment()).back();
            }

            //AppRatingDialog.addToOpenedMenus("notPrefs");

        } else if (i1 == R.id.export) {
            if (mTimes instanceof WebTimes) {
                ((WebTimes) mTimes).syncAsync();
            }
            ExportController.export(getActivity(), mTimes);
        } else if (i1 == R.id.refresh) {
            if (mTimes instanceof WebTimes) {
                ((WebTimes) mTimes).syncAsync();
            }

        } else if (i1 == R.id.share) {
            StringBuilder txt = new StringBuilder(getString(R.string.shareTimes, mTimes.getName()) + ":");
            LocalDate date = LocalDate.now();

            for (Vakit v : Vakit.values()) {
                txt.append("\n   ").append(v.getString()).append(": ").append(LocaleUtils.formatTime(mTimes.getTime(date, v.ordinal()).toLocalTime()));
            }

            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.appName));
            sharingIntent.putExtra(Intent.EXTRA_TEXT, txt.toString());
            startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share)));
        }
        return super.onOptionsItemSelected(item);
    }

    @Nullable
    public Times getTimes() {
        return mTimes;
    }

    @Override
    public void onResume() {
        super.onResume();
        mHandler.removeCallbacks(onSecond);
        mHandler.post(onSecond);
    }

    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeCallbacks(onSecond);
    }

    private void checkKerahat() {
        if (mTimes == null || mTimes.getID() < 0)
            return;
        mKerahat.setVisibility(mTimes.isKerahat() ? View.VISIBLE : View.GONE);
        mKerahat.setText(R.string.kerahatTime);
    }


}
