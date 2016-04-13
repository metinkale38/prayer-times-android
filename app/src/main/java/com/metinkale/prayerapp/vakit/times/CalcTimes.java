package com.metinkale.prayerapp.vakit.times;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import com.metinkale.prayer.R;
import com.metinkale.prayerapp.vakit.times.PrayTime.AdjMethod;
import com.metinkale.prayerapp.vakit.times.PrayTime.Juristic;
import com.metinkale.prayerapp.vakit.times.PrayTime.Method;
import org.joda.time.LocalDate;

import java.util.List;
import java.util.TimeZone;

public class CalcTimes extends Times {

    private Double mLat, mLng;

    private PrayTime mPrayTime;


    private static final String _METHOD = "method";
    private static final String _ADJMETHOD = "adjMethod";
    private static final String _JURISTIC = "juristic";

    public Method getMethod() {
        return Method.valueOf(getString(_METHOD));
    }

    public Juristic getJuristic() {
        return Juristic.valueOf(getString(_JURISTIC));
    }

    public AdjMethod getAdjMethod() {
        return AdjMethod.valueOf(getString(_ADJMETHOD));
    }

    public void setMethod(Method value) {
        set(_METHOD, value.name());
    }

    public void setJuristic(Juristic value) {
        set(_JURISTIC, value.name());
    }

    public void setAdjMethod(AdjMethod value) {
        set(_ADJMETHOD, value.name());
    }

    CalcTimes(long id) {
        super(id);
        mPrayTime = new PrayTime();
        mLat = getLat();
        mLng = getLng();
        try {
            mPrayTime.setCalcMethod(getMethod());
            mPrayTime.setAsrJuristic(getJuristic());
            mPrayTime.setAdjustHighLats(getAdjMethod());
        } catch (NullPointerException e) {
            delete();
        }
        mPrayTime.setTimeZone(TimeZone.getDefault().getOffset(System.currentTimeMillis()) / (1000d * 60d * 60d));
    }

    CalcTimes(long id, boolean placeholder) {
        super(id);
        //this class is just used when creating a CalcTimes below
    }

    @SuppressLint("InflateParams")
    public static void add(final Activity c, final Bundle bdl) {
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        LayoutInflater inflater = LayoutInflater.from(c);
        View view = inflater.inflate(R.layout.calcmethod_dialog, null);
        ListView lv = (ListView) view.findViewById(R.id.listView);
        final Spinner sp = (Spinner) view.findViewById(R.id.spinner);
        final Spinner sp2 = (Spinner) view.findViewById(R.id.spinner2);

        lv.setAdapter(new ArrayAdapter<Method>(c, R.layout.calcmethod_dlgitem, R.id.legacySwitch, Method.values()) {
            @Override
            public View getView(int pos, View convertView, ViewGroup parent) {
                ViewGroup vg = (ViewGroup) super.getView(pos, convertView, parent);
                ((TextView) vg.getChildAt(0)).setText(getItem(pos).title);
                ((TextView) vg.getChildAt(1)).setText(getItem(pos).desc);
                return vg;
            }

        });


        sp.setAdapter(new ArrayAdapter<>(c, android.R.layout.simple_spinner_dropdown_item, c.getResources().getStringArray(R.array.adjMethod)));
        sp2.setAdapter(new ArrayAdapter<>(c, android.R.layout.simple_spinner_dropdown_item, c.getResources().getStringArray(R.array.juristicMethod)));


        builder.setTitle(R.string.calcMethod);
        builder.setView(view);
        final AlertDialog dlg = builder.create();
        lv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long index) {

                CalcTimes t = new CalcTimes(System.currentTimeMillis(), true);
                t.setSource(Source.Calc);
                t.setName(bdl.getString("city"));
                t.setLat(bdl.getDouble("lat"));
                t.setLng(bdl.getDouble("lng"));
                t.setMethod(Method.values()[pos]);
                t.setJuristic(Juristic.values()[sp2.getSelectedItemPosition()]);
                t.setAdjMethod(AdjMethod.values()[sp.getSelectedItemPosition()]);
                dlg.cancel();
                c.finish();

            }

        });

        dlg.show();

    }

    @Override
    public Source getSource() {
        return Source.Calc;
    }


    private LocalDate mLast;
    private List<String> mLastTimes;

    @Override
    public String _getTime(LocalDate date, int time) {
        if (mLast.equals(date)) return mLastTimes.get(time);

        List<String> times = mPrayTime.getDatePrayerTimes(date.getDayOfMonth(), date.getMonthOfYear(), date.getYear(), mLat, mLng);
        times.remove(4);
        mLast=date;
        mLastTimes = times;
        return times.get(time);
    }


}
