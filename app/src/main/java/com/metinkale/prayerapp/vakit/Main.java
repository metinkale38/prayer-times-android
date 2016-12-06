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

package com.metinkale.prayerapp.vakit;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import com.metinkale.prayer.R;
import com.metinkale.prayerapp.App;
import com.metinkale.prayerapp.BaseActivity;
import com.metinkale.prayerapp.HicriDate;
import com.metinkale.prayerapp.Utils;
import com.metinkale.prayerapp.utils.MultipleOrientationSlidingDrawer;
import com.metinkale.prayerapp.utils.RTLViewPager;
import com.metinkale.prayerapp.vakit.fragments.ImsakiyeFragment;
import com.metinkale.prayerapp.vakit.fragments.MainFragment;
import com.metinkale.prayerapp.vakit.fragments.SettingsFragment;
import com.metinkale.prayerapp.vakit.fragments.SortFragment;
import com.metinkale.prayerapp.vakit.times.Times;

public class Main extends BaseActivity implements OnPageChangeListener, View.OnClickListener {

    public static boolean isRunning;
    public MyAdapter mAdapter;
    public FloatingActionButton mAddCityFab;
    private RTLViewPager mPager;
    private int mStartPos = 1;
    private SettingsFragment mSettingsFrag;
    private ImsakiyeFragment mImsakiyeFrag;
    private TextView mFooterText;
    private MultipleOrientationSlidingDrawer mTopSlider;
    private MultipleOrientationSlidingDrawer mBottomSlider;

    public static PendingIntent getPendingIntent(Times t) {
        if (t == null) {
            return null;
        }
        Context context = App.getContext();
        Intent intent = new Intent(context, Main.class);
        intent.putExtra("startCity", Times.getTimes().indexOf(t));
        return PendingIntent.getActivity(context, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.vakit_main);

        mFooterText = (TextView) findViewById(R.id.footerText);
        mPager = (RTLViewPager) findViewById(R.id.pager);
        mAdapter = new MyAdapter(getSupportFragmentManager());

        mSettingsFrag = (SettingsFragment) getFragmentManager().findFragmentByTag("settings");
        mImsakiyeFrag = (ImsakiyeFragment) getFragmentManager().findFragmentByTag("imsakiye");

        mTopSlider = (MultipleOrientationSlidingDrawer) findViewById(R.id.topSlider);
        mBottomSlider = (MultipleOrientationSlidingDrawer) findViewById(R.id.bottomSlider);

        mAddCityFab = (FloatingActionButton) findViewById(R.id.addCity);
        mAddCityFab.setOnClickListener(this);

        mPager.setRTLSupportAdapter(getSupportFragmentManager(), mAdapter);

        int holyday = HicriDate.isHolyday();
        if (holyday != 0) {
            TextView tv = (TextView) findViewById(R.id.holyday);
            tv.setVisibility(View.VISIBLE);
            tv.setText(Utils.getHolyday(holyday - 1));
        }

        onNewIntent(getIntent());
        mPager.addOnPageChangeListener(this);


        mTopSlider.setOnDrawerScrollListener(new MultipleOrientationSlidingDrawer.OnDrawerScrollListener() {
            @Override
            public void onScrollStarted() {
            }

            @Override
            public void onScrolling(int pos) {
                mPager.setTranslationY(pos);
            }

            @Override
            public void onScrollEnded() {

            }
        });

        mTopSlider.setOnDrawerOpenListener(new MultipleOrientationSlidingDrawer.OnDrawerOpenListener() {
            @Override
            public void onDrawerOpened() {
                int position = mPager.getCurrentItem();
                if (position != 0) {
                    Times t = Times.getTimes(mAdapter.getItemId(position));
                    mSettingsFrag.setTimes(t);
                }
                mBottomSlider.lock();
            }
        });

        mTopSlider.setOnDrawerCloseListener(new MultipleOrientationSlidingDrawer.OnDrawerCloseListener() {
            @Override
            public void onDrawerClosed() {
                mBottomSlider.unlock();

                Fragment page = getSupportFragmentManager().findFragmentByTag("android:switcher:" + mPager.getId() + ":" + mAdapter.getItemId(mPager.getCurrentItem()));
                if (page instanceof MainFragment) {
                    ((MainFragment) page).update();
                }

            }
        });


        mBottomSlider.setOnDrawerOpenListener(new MultipleOrientationSlidingDrawer.OnDrawerOpenListener() {
            @Override
            public void onDrawerOpened() {

                mTopSlider.lock();
            }
        });

        mBottomSlider.setOnDrawerCloseListener(new MultipleOrientationSlidingDrawer.OnDrawerCloseListener() {
            @Override
            public void onDrawerClosed() {
                mTopSlider.unlock();
            }
        });
        findViewById(R.id.topSliderCloseHandler).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if ((motionEvent.getAction() == MotionEvent.ACTION_DOWN) && mTopSlider.isOpened()) {
                    mTopSlider.animateClose();
                    return true;
                }
                return false;
            }
        });


        findViewById(R.id.bottomSliderCloseHandler).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if ((motionEvent.getAction() == MotionEvent.ACTION_DOWN) && mBottomSlider.isOpened()) {
                    mBottomSlider.animateClose();
                    return true;
                }
                return false;
            }
        });


    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mStartPos = getIntent().getIntExtra("startCity", -1) + 1;
        if (mStartPos <= 0) {
            mStartPos = 1;
        }
        mStartPos = Math.min(mStartPos, mAdapter.getCount() - 1);

        mPager.setCurrentItem(mStartPos);
        onPageScrolled(mStartPos, 0, 0);
        onPageSelected(mStartPos);
        onPageScrollStateChanged(ViewPager.SCROLL_STATE_IDLE);
    }

    @Override
    public void onClick(View v) {
        if (v == mAddCityFab) {
            startActivity(new Intent(this, AddCity.class));
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        isRunning = true;
        Times.addOnTimesListChangeListener(mAdapter);


    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Fragment frag = getSupportFragmentManager().findFragmentByTag("notPrefs");
            if (frag != null) {
                setFooterText(getString(R.string.monthly), true);
                getSupportFragmentManager().beginTransaction().remove(frag).commit();
                return true;
            } else if (mBottomSlider.isOpened()) {
                mBottomSlider.animateClose();
                return true;
            } else if (mTopSlider.isOpened()) {
                mTopSlider.animateClose();
                return true;
            }


        }
        return super.onKeyUp(keyCode, event);
    }


    @Override
    public void onPageScrollStateChanged(int state) {
        if (state == ViewPager.SCROLL_STATE_IDLE) {
            int pos = mPager.getCurrentItem();
            if (pos != 0) {
                Times t = Times.getTimes(mAdapter.getItemId(pos));
                mImsakiyeFrag.setTimes(t);
            }
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (position == 0) {
            mTopSlider.lock();
            mBottomSlider.lock();
        } else {
            mTopSlider.unlock();
            mBottomSlider.unlock();
        }
        if (mAddCityFab != null) if ((position == 0) && (positionOffset == 0)) {
            mAddCityFab.show();
        } else {
            mAddCityFab.hide();
        }


    }

    public void setFooterText(CharSequence txt, boolean enablePane) {
        mFooterText.setVisibility(txt.toString().isEmpty() ? View.GONE : View.VISIBLE);
        mFooterText.setText(txt);
        mPager.setSwipeLocked(!enablePane);

    }

    @Override
    public void onPageSelected(int pos) {
        mFooterText.setText((pos == 0) ? R.string.cities : R.string.monthly);
    }

    public void onItemClick(int pos) {
        mPager.setCurrentItem(pos + 1, true);

    }

    public class MyAdapter extends FragmentPagerAdapter implements Times.OnTimesListChangeListener {

        public MyAdapter(FragmentManager fm) {
            super(fm);

        }


        @Override
        public int getCount() {
            return Times.getCount() + 1;
        }

        @Override
        public long getItemId(int position) {
            if (position == 0) {
                return 0;
            }

            return Times.getTimesAt(position - 1).getID();
        }

        @Override
        public int getItemPosition(Object object) {
            if (object instanceof SortFragment) {
                return 0;
            } else {
                MainFragment frag = (MainFragment) object;
                int pos = Times.getTimes().indexOf(frag.getTimes());
                if (pos >= 0) {
                    return pos + 1;
                }
            }
            return POSITION_NONE;
        }


        @Override
        public Fragment getItem(int position) {
            if (position > 0) {
                MainFragment frag = new MainFragment();
                Bundle bdl = new Bundle();
                bdl.putLong("city", getItemId(position));
                frag.setArguments(bdl);

                if (position == mStartPos) {
                    mStartPos = 0;
                }
                return frag;
            } else {
                return new SortFragment();
            }
        }


    }
}
