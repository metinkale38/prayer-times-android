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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.metinkale.prayer.Preferences;
import com.metinkale.prayer.times.fragments.AlarmsFragment;
import com.metinkale.prayer.times.fragments.TimesFragment;
import com.metinkale.prayer.times.times.Times;
import com.metinkale.prayer.times.times.Vakit;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;
import net.steamcrafted.materialiconlib.MaterialMenuInflater;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.util.List;

/**
 * Created by metin on 17.07.2017.
 */

public class ConfigIntroFragment extends IntroFragment {

    private View mView;
    private int mTask;
    private Fragment mPrefsFrag;
    private Runnable mDoTask = new Runnable() {
        @Override
        public void run() {
            mView.postDelayed(mDoTask, 2000);
            if (mFragment == null || mFragment.getTopSlider() == null || isDetached() || !isAdded()) {
                return;
            }

            FragmentManager fm = getChildFragmentManager();
            if (fm.isStateSaved()) return;

            switch (mTask % 6) {
                case 0:
                    mFragment.getTopSlider().animateOpen();
                    break;
                case 1:
                    mFragment.getTopSlider().animateClose();
                    break;
                case 2:
                    mFragment.getBottomSlider().animateOpen();
                    break;
                case 3:
                    mFragment.getBottomSlider().animateClose();
                    break;
                case 4:
                    mMenuItem.getIcon().setAlpha(100);
                    mMenuItem.getIcon().invalidateSelf();

                    mFragment.setFooterText("", false);
                    fm.beginTransaction()
                            .replace(R.id.basecontent,
                                    mPrefsFrag).addToBackStack("").commit();
                    break;
                case 5:
                    mMenuItem.getIcon().setAlpha(255);
                    mMenuItem.getIcon().invalidateSelf();
                    mFragment.setFooterText(getString(R.string.monthly), false);
                    getChildFragmentManager().popBackStack();
                    break;

            }
            mTask++;
        }
    };
    private TimesFragment mFragment;
    private MenuItem mMenuItem;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.intro_config, container, false);


        Toolbar toolbar = mView.findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.appName);
        toolbar.setNavigationIcon(MaterialDrawableBuilder.with(getActivity())
                .setIcon(MaterialDrawableBuilder.IconValue.MENU)
                .setColorResource(R.color.white)
                .setToActionbarSize()
                .build());

        try {
            MaterialMenuInflater.with(getActivity(), getActivity().getMenuInflater())
                    .setDefaultColorResource(R.color.white)
                    .inflate(R.menu.vakit, toolbar.getMenu());
            mMenuItem = toolbar.getMenu().getItem(0);
            mMenuItem.setIcon(mMenuItem.getIcon());
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }


        return mView;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Times city = null;
        List<Times> cities = Times.getTimes();
        for (Times time : cities) {
            if (city == null) city = time;
            if (!time.getTime(LocalDate.now().withDayOfMonth(1), Vakit.DHUHR.ordinal()).toLocalTime().equals(LocalTime.MIDNIGHT)) {
                city = time;
                break;
            }
        }
        Bundle bdl = new Bundle();
        bdl.putInt("startCity", cities.indexOf(city));
        mFragment = new TimesFragment();
        mFragment.setArguments(bdl);
        mPrefsFrag = AlarmsFragment.create(city);
        getChildFragmentManager().beginTransaction().replace(R.id.basecontent, mFragment).commit();
    }


    @Override
    protected void onSelect() {
        if (mView != null) {
            mView.removeCallbacks(mDoTask);
            mView.postDelayed(mDoTask, 500);
        }
    }

    @Override
    protected void onEnter() {

    }

    @Override
    protected void onExit() {
        if (mView != null)
            mView.removeCallbacks(mDoTask);

    }

    @Override
    protected boolean shouldShow() {
        return Preferences.SHOW_INTRO.get();
    }
}
