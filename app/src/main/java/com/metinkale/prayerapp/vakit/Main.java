package com.metinkale.prayerapp.vakit;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import com.metinkale.prayer.R;
import com.metinkale.prayerapp.App;
import com.metinkale.prayerapp.BaseActivity;
import com.metinkale.prayerapp.Date;
import com.metinkale.prayerapp.custom.FloatingActionButton;
import com.metinkale.prayerapp.custom.LockableViewPager;
import com.metinkale.prayerapp.vakit.fragments.ImsakiyeFragment;
import com.metinkale.prayerapp.vakit.fragments.MainFragment;
import com.metinkale.prayerapp.vakit.fragments.SettingsFragment;
import com.metinkale.prayerapp.vakit.times.MainHelper;
import com.metinkale.prayerapp.vakit.times.Times;

public class Main extends BaseActivity implements OnPageChangeListener, View.OnClickListener {

    public static boolean isRunning = false;
    public MyAdapter mAdapter;
    public FloatingActionButton mAddCityFab;
    private LockableViewPager mPager;
    private int mStartPos = 1;
    private SettingsFragment mSettingsFrag;
    private ImsakiyeFragment mImsakiyeFrag;
    public MyPaneView mPaneView;
    private TextView mFooterText;

    public static PendingIntent getPendingIntent(Times t) {
        if (t == null) return null;
        Context context = App.getContext();
        Intent intent = new Intent(context, Main.class);
        intent.putExtra("startCity", MainHelper.getIds().indexOf(t.getID()));
        return PendingIntent.getActivity(context, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.vakit_main);


        mStartPos = getIntent().getIntExtra("startCity", -1) + 1;
        if (mStartPos <= 0) {
            mStartPos = 1;
        }
        mFooterText = (TextView) findViewById(R.id.footerText);
        mPager = (LockableViewPager) findViewById(R.id.pager);
        mPaneView = (MyPaneView) findViewById(R.id.pane);
        mAdapter = new MyAdapter(getSupportFragmentManager());
        mPager.setAdapter(mAdapter);


        String holyday = Date.isHolyday();
        if (holyday != null) {
            TextView tv = (TextView) findViewById(R.id.holyday);
            tv.setVisibility(View.VISIBLE);
            tv.setText(holyday);
        }

        mPager.setCurrentItem(mStartPos);
        mPaneView.setEnabled(mStartPos != 0);
        mPager.setOnPageChangeListener(this);

        mSettingsFrag = (SettingsFragment) getFragmentManager().findFragmentByTag("settings");
        mImsakiyeFrag = (ImsakiyeFragment) getFragmentManager().findFragmentByTag("imsakiye");

        mPaneView.setOnTopOpen(new Runnable() {
            @Override
            public void run() {
                int position = mPager.getCurrentItem();
                if (position != 0) {
                    Times t = MainHelper.getTimes(mAdapter.getItemId(position));
                    mSettingsFrag.setTimes(t);
                }
            }
        });

        mPaneView.setOnBottomOpen(new Runnable() {
            @Override
            public void run() {
                int position = mPager.getCurrentItem();
                if (position != 0) {
                    Times t = MainHelper.getTimes(mAdapter.getItemId(position));
                    t.loadAllTimes();
                    mImsakiyeFrag.setTimes(t);
                }
            }
        });

        mAddCityFab = new FloatingActionButton.Builder(this).withDrawable(getResources().getDrawable(R.drawable.ic_action_add)).withButtonColor(Color.RED).withGravity(Gravity.BOTTOM | Gravity.RIGHT).withMargins(0, 0, 16, 5).hide().create();
        mAddCityFab.setOnClickListener(this);
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
        MainHelper.addListener(mAdapter);


    }

    public void onPause() {
        super.onPause();
        isRunning = false;
        MainHelper.removeListener(mAdapter);

    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Fragment frag = getSupportFragmentManager().findFragmentByTag("notPrefs");
            if (frag != null) {
                setFooterText(getString(R.string.imsakiye), true);
                getSupportFragmentManager().beginTransaction().remove(frag).commit();
                return true;
            } else if (mPaneView.isOpen()) {
                mPaneView.close();
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean setNavBar() {
        setNavBarResource(R.color.colorPrimary);
        return true;
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (mAddCityFab != null) if (position == 0 && positionOffset == 0) mAddCityFab.showFloatingActionButton();
        else mAddCityFab.hideFloatingActionButton();


    }

    public void setFooterText(String txt, boolean enablePane) {
        mFooterText.setText(txt);
        mPaneView.setEnabled(enablePane);
        mPager.setSwipeLocked(!enablePane);

    }

    @Override
    public void onPageSelected(int pos) {
        mPaneView.setEnabled(pos != 0);
        mFooterText.setText(pos == 0 ? R.string.cities : R.string.imsakiye);
    }

    public class MyAdapter extends FragmentPagerAdapter implements OnItemClickListener, MainHelper.MainHelperListener {

        public MyAdapter(FragmentManager fm) {
            super(fm);

        }

        public void drop(int from, int to) {
            MainHelper.drop(from, to);
        }

        public void remove(final int which) {
            final Times times = MainHelper.getTimesAt(which);

            AlertDialog dialog = new AlertDialog.Builder(Main.this).create();
            dialog.setTitle(R.string.delete);
            dialog.setMessage(getString(R.string.delConfirm, times.getName()));
            dialog.setCancelable(false);
            dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int buttonId) {
                    times.delete();
                }
            });
            dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int buttonId) {
                    dialog.cancel();
                }
            });
            dialog.setIcon(R.drawable.ic_delete);
            dialog.show();

        }


        @Override
        public int getCount() {
            return MainHelper.getCount() + 1;
        }

        @Override
        public long getItemId(int position) {
            if (position == 0) return 0;

            return MainHelper.getTimesAt(position - 1).getID();
        }

        @Override
        public int getItemPosition(Object object) {
            if (object instanceof SortFragment) {
                return 0;
            } else {
                MainFragment frag = (MainFragment) object;
                int pos = MainHelper.getTimes().indexOf(frag.getTimes());
                if (pos >= 0) return pos + 1;
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

        @Override
        public void onItemClick(AdapterView<?> a, View v, int pos, long index) {
            mPager.setCurrentItem(pos + 1, true);

        }

    }


}
