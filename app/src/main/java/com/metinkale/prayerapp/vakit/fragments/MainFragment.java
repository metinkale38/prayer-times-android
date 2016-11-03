/*
 * Copyright (c) 2016 Metin Kale
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

package com.metinkale.prayerapp.vakit.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;
import com.metinkale.prayer.R;
import com.metinkale.prayerapp.HicriDate;
import com.metinkale.prayerapp.Utils;
import com.metinkale.prayerapp.settings.Prefs;
import com.metinkale.prayerapp.vakit.Main;
import com.metinkale.prayerapp.vakit.times.Times;
import com.metinkale.prayerapp.vakit.times.WebTimes;
import com.metinkale.prayerapp.vakit.times.other.Vakit;
import org.joda.time.LocalDate;

@SuppressLint("ClickableViewAccessibility")
public class MainFragment extends Fragment implements Times.OnTimesUpdatedListener {

    private static final int[] ids = {R.id.imsaktime, R.id.gunestime, R.id.ogletime, R.id.ikinditime, R.id.aksamtime, R.id.yatsitime};
    private static final int[] idsNames = {R.id.imsak, R.id.gunes, R.id.ogle, R.id.ikindi, R.id.aksam, R.id.yatsi};
    private static Handler mHandler = new Handler();
    private View mView;
    private Times mTimes;
    private long mCity;
    private TextView mCountdown;
    private TextView mKerahat;
    private TextView mTitle;
    private TextView mHicri;
    private TextView mDate;
    private Runnable onSecond = new Runnable() {

        @Override
        public void run() {

            if ((mTimes != null) && !mTimes.deleted()) {
                checkKerahat();

                int next = mTimes.getNext();
                if (Prefs.getVakitIndicator().equals("next")) next++;
                for (int i = 0; i < 6; i++) {
                    TextView time = (TextView) mView.findViewById(ids[i]);
                    ViewGroup parent = (ViewGroup) time.getParent();
                    if (i == (next - 1)) {
                        time.setBackgroundResource(R.color.indicator);
                        parent.getChildAt(parent.indexOfChild(time) - 1).setBackgroundResource(R.color.indicator);
                    } else {
                        time.setBackgroundColor(Color.TRANSPARENT);
                        parent.getChildAt(parent.indexOfChild(time) - 1).setBackgroundColor(Color.TRANSPARENT);
                    }
                }

                String left = mTimes.getLeft(next);
                mCountdown.setText(left);

            }
            mHandler.postDelayed(this, 1000);

        }


    };

    @Override
    public void onCreate(Bundle bdl) {
        super.onCreate(bdl);
        mCity = getArguments().getLong("city");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bdl) {
        mView = inflater.inflate(R.layout.vakit_fragment, container, false);
        setHasOptionsMenu(true);
        if (mCity == 0) {
            return mView;
        }

        mCountdown = (TextView) mView.findViewById(R.id.countdown);
        mTitle = (TextView) mView.findViewById(R.id.city);
        mDate = (TextView) mView.findViewById(R.id.date);
        mHicri = (TextView) mView.findViewById(R.id.hicri);


        mKerahat = (TextView) mView.findViewById(R.id.kerahat);

        mTimes = Times.getTimes(mCity);

        if (mTimes == null) {
            return new View(getActivity());
        }
        ImageView source1 = (ImageView) mView.findViewById(R.id.source1);
        ImageView source2 = (ImageView) mView.findViewById(R.id.source2);
        if (mTimes.getSource().resId != 0) {
            source1.setImageResource(mTimes.getSource().resId);
            source2.setImageResource(mTimes.getSource().resId);
        }


        if (Prefs.useArabic()) {
            for (int i = 0; i < idsNames.length; i++) {
                TextView tv = (TextView) mView.findViewById(idsNames[i]);
                tv.setGravity(Gravity.LEFT);
                tv.setText(Vakit.getByIndex(i).getString());
            }
        }

        return mView;
    }

    public void update() {
        if ((mTimes == null) || (mView == null)) {
            return;
        }

        mTitle.setText(mTimes.getName());

        LocalDate greg = LocalDate.now();
        HicriDate hijr = new HicriDate(greg);

        mHicri.setText(Utils.format(hijr));
        mDate.setText(Utils.format(greg));

        String[] daytimes = {mTimes.getTime(greg, 0), mTimes.getTime(greg, 1), mTimes.getTime(greg, 2), mTimes.getTime(greg, 3), mTimes.getTime(greg, 4), mTimes.getTime(greg, 5)};

        for (int i = 0; i < 6; i++) {

            TextView time = (TextView) mView.findViewById(ids[i]);
            time.setText(Utils.fixTimeForHTML(daytimes[i]));
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.vakit, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.notification:
                Fragment frag = getActivity().getSupportFragmentManager().findFragmentByTag("notPrefs");
                if (frag == null) {
                    ((Main) getActivity()).setFooterText("", false);
                    getActivity().getSupportFragmentManager().beginTransaction().add(R.id.fragContainer, NotificationPrefs.create(mTimes), "notPrefs").commit();
                } else {
                    ((Main) getActivity()).setFooterText(getString(R.string.monthly), true);
                    getActivity().getSupportFragmentManager().beginTransaction().remove(frag).commit();

                }

            case R.id.refresh:
                if (mTimes instanceof WebTimes) {
                    ((WebTimes) mTimes).syncTimes();
                }
                break;

            case R.id.share:
                String txt = getString(R.string.shareTimes, mTimes.getName()) + ":";
                LocalDate date = LocalDate.now();
                String[] times = {mTimes.getTime(date, 0), mTimes.getTime(date, 1), mTimes.getTime(date, 2), mTimes.getTime(date, 3), mTimes.getTime(date, 4), mTimes.getTime(date, 5)};
                for (int i = 0; i < times.length; i++) {
                    txt += "\n   " + Vakit.getByIndex(i).getString() + ": " + times[i];
                }

                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.appName));
                sharingIntent.putExtra(Intent.EXTRA_TEXT, txt);
                startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share)));

        }
        return super.onOptionsItemSelected(item);
    }

    public Times getTimes() {
        return mTimes;
    }

    @Override
    public void onResume() {
        super.onResume();
        mHandler.removeCallbacks(onSecond);
        mHandler.post(onSecond);
        if (mTimes != null)
            mTimes.addOnTimesUpdatedListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeCallbacks(onSecond);
        if (mTimes != null) mTimes.removeOnTimesUpdatedListener(this);
    }

    void checkKerahat() {
        if (mTimes == null) return;
        boolean k = mTimes.isKerahat();
        mKerahat.setVisibility(k ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onTimesUpdated(Times t) {
        update();
    }
}
