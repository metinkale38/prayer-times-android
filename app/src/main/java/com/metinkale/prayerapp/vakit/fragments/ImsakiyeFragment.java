package com.metinkale.prayerapp.vakit.fragments;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.metinkale.prayer.R;
import com.metinkale.prayerapp.Utils;
import com.metinkale.prayerapp.settings.Prefs;
import com.metinkale.prayerapp.vakit.times.Times;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by Metin on 21.07.2015.
 */
public class ImsakiyeFragment extends Fragment {
    private ListView mLV;
    private ImsakiyeAdapter mAdapter;
    private Times mTimes;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bdl) {
        mLV = new ListView(getActivity());
        mAdapter = new ImsakiyeAdapter(getActivity());
        mLV.setAdapter(mAdapter);
        setTimes(mTimes);
        TextView addMore = new TextView(getActivity());
        addMore.setText("\n" + getString(R.string.showMore) + "\n");
        addMore.setGravity(Gravity.CENTER);
        addMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAdapter.daysInMonth += 7;
                mAdapter.notifyDataSetInvalidated();
            }
        });
        mLV.addFooterView(addMore);
        return mLV;
    }

    public void setTimes(Times t) {
        mTimes = t;
        if (mAdapter != null) {
            mAdapter.times = mTimes;
            mAdapter.notifyDataSetChanged();
            mAdapter.daysInMonth = ((Calendar) mAdapter.getItem(1)).getActualMaximum(Calendar.DAY_OF_MONTH);

        }
    }

    public class ImsakiyeAdapter extends BaseAdapter {
        private Times times;
        private int daysInMonth;
        private int month;
        private int year;
        private Calendar calendar;
        private LayoutInflater inflater;
        private int today;

        public ImsakiyeAdapter(Context context) {
            calendar = new GregorianCalendar();
            today = calendar.get(Calendar.DAY_OF_MONTH);
            daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            month = calendar.get(Calendar.MONTH);
            year = calendar.get(Calendar.YEAR);

            inflater = LayoutInflater.from(context);
        }

        @Override
        public long getItemId(int position) {
            return position + (times == null ? 0 : times.getID());
        }

        @Override
        public Object getItem(int position) {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, position);
            return calendar;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.vakit_imsakiye, parent, false);
            }
            ViewGroup v = (ViewGroup) convertView;
            v = (ViewGroup) v.getChildAt(0);
            String[] a;
            if (position == 0) {
                a = new String[]{getString(R.string.date), getString(R.string.imsak), getString(R.string.gunes), getString(R.string.ogle), getString(R.string.ikindi), getString(R.string.aksam), getString(R.string.yatsi)};
            } else if (times == null) {
                a = new String[]{"00:00", "00:00", "00:00", "00:00", "00:00", "00:00", "00:00"};
            } else {
                Calendar cal = (Calendar) getItem(position);

                String[] daytimes = {times.getTime(cal, 0), times.getTime(cal, 1), times.getTime(cal, 2), times.getTime(cal, 3), times.getTime(cal, 4), times.getTime(cal, 5)};

                a = new String[]{Utils.az(cal.get(Calendar.DAY_OF_MONTH)) + "." + Utils.az(cal.get(Calendar.MONTH) + 1), daytimes[0], daytimes[1], daytimes[2], daytimes[3], daytimes[4], daytimes[5]};
            }

            for (int i = 0; i < 7; i++) {
                TextView tv = (TextView) v.getChildAt(i);
                if (i == 0 || !Prefs.use12H() || position == 0) tv.setText(a[i]);
                else tv.setText(Utils.fixTimeForHTML(a[i]));
            }

            if (position == today) {
                v.setBackgroundResource(R.color.colorPrimary);
            } else if (position == 0) {
                v.setBackgroundResource(R.color.indicator);
            } else if (position % 2 == 0) {
                v.setBackgroundResource(R.color.colorPrimaryLight);
            } else {
                v.setBackgroundColor(Color.WHITE);
            }

            return (View) v.getParent();
        }

        @Override
        public int getCount() {
            return daysInMonth + 1;
        }

    }
}
