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
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.metinkale.prayer.R;
import com.metinkale.prayerapp.App;
import com.metinkale.prayerapp.BaseActivity;
import com.metinkale.prayerapp.Date;
import com.metinkale.prayerapp.custom.FloatingActionButton;
import com.metinkale.prayerapp.vakit.fragments.ImsakiyeFragment;
import com.metinkale.prayerapp.vakit.fragments.MainFragment;
import com.metinkale.prayerapp.vakit.fragments.SettingsFragment;
import com.metinkale.prayerapp.vakit.times.MainHelper;
import com.metinkale.prayerapp.vakit.times.Times;

import java.util.ArrayList;
import java.util.List;

public class Main extends BaseActivity implements OnPageChangeListener, View.OnClickListener {

    public static boolean isRunning = false;
    public MyAdapter mAdapter;
    public FloatingActionButton mAddCityFab;
    private ViewPager mPager;
    private int mStartPos = 1;
    private SettingsFragment mSettingsFrag;
    private ImsakiyeFragment mImsakiyeFrag;
    private MyPaneView mPaneView;
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
        mPager = (ViewPager) findViewById(R.id.pager);
        mPaneView = (MyPaneView) findViewById(R.id.pane);
        mAdapter = new MyAdapter(getSupportFragmentManager());
        mPager.setAdapter(mAdapter);
        mPaneView.setEnabled(true);

        String holyday = Date.isHolyday();
        if (holyday != null) {
            TextView tv = (TextView) findViewById(R.id.holyday);
            tv.setVisibility(View.VISIBLE);
            tv.setText(holyday);
        }


        mAdapter.notifyDataSetChanged();
        mPager.setCurrentItem(mStartPos);
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
        mAdapter.notifyDataSetChanged();
        isRunning = true;


    }

    public void onPause() {
        super.onPause();
        isRunning = false;

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
        if (mAddCityFab != null)
            if (position == 0 && positionOffset == 0)
                mAddCityFab.showFloatingActionButton();
            else
                mAddCityFab.hideFloatingActionButton();


    }

    @Override
    public void onPageSelected(int pos) {
        mPaneView.setEnabled(pos != 0);
        mFooterText.setText(pos == 0 ? R.string.cities : R.string.imsakiye);
    }

    public class MyAdapter extends FragmentPagerAdapter implements OnItemClickListener {
        private List<Long> ids = new ArrayList<Long>();

        public MyAdapter(FragmentManager fm) {
            super(fm);
            notifyDataSetChanged();
        }


        public void drop(int from, int to) {
            MainHelper.drop(from, to);
            notifyDataSetChanged();
        }

        public void remove(final int which) {
            if (ids.size() <= which) {
                return;
            }
            long id = ids.get(which);
            final Times times = MainHelper.getTimes(id);

            AlertDialog dialog = new AlertDialog.Builder(Main.this).create();
            dialog.setTitle(R.string.delete);
            dialog.setMessage(getString(R.string.delConfirm, times.getName()));
            dialog.setCancelable(false);
            dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int buttonId) {

                    times.delete();
                    notifyDataSetChanged();
                }
            });
            dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int buttonId) {
                    dialog.cancel();
                    notifyDataSetChanged();
                }
            });
            dialog.setIcon(R.drawable.ic_delete);
            dialog.show();

        }


        @Override
        public void notifyDataSetChanged() {
            ids.clear();
            ids.addAll(MainHelper.getIds());
            ids.add(0, 0L);
            super.notifyDataSetChanged();

            List<Fragment> frags = getSupportFragmentManager().getFragments();
            if (frags != null)
                for (Fragment frag : frags) {
                    if (frag instanceof SortFragment) {
                        ((SortFragment) frag).notifyDataSetChanged();
                    }
                }
        }

        @Override

        public int getCount() {
            return ids.size();
        }

        @Override
        public long getItemId(int position) {
            return ids.get(position);
        }

        @Override
        public Fragment getItem(int position) {
            if (position > 0) {
                MainFragment frag = new MainFragment();
                Bundle bdl = new Bundle();
                bdl.putLong("city", ids.get(position));
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
