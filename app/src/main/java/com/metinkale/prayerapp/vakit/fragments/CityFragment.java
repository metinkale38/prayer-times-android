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

package com.metinkale.prayerapp.vakit.fragments;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.metinkale.prayer.R;
import com.metinkale.prayerapp.App;
import com.metinkale.prayerapp.MainActivity;
import com.metinkale.prayerapp.HicriDate;
import com.metinkale.prayerapp.settings.Prefs;
import com.metinkale.prayerapp.utils.AppRatingDialog;
import com.metinkale.prayerapp.utils.Utils;
import com.metinkale.prayerapp.vakit.times.Source;
import com.metinkale.prayerapp.vakit.times.Times;
import com.metinkale.prayerapp.vakit.times.sources.WebTimes;
import com.metinkale.prayerapp.vakit.times.Vakit;

import net.steamcrafted.materialiconlib.MaterialMenuInflater;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@SuppressLint("ClickableViewAccessibility")
public class CityFragment extends Fragment implements Times.OnTimesUpdatedListener {

    private static final int[] ids = {R.id.fajrTime, R.id.sunTime, R.id.zuhrTime, R.id.asrTime, R.id.maghribTime, R.id.ishaaTime};
    private static final int[] idsNames = {R.id.fajr, R.id.sun, R.id.zuhr, R.id.asr, R.id.maghrib, R.id.ishaa};
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
    @NonNull
    private Runnable onSecond = new Runnable() {

        @Override
        public void run() {

            if ((mTimes != null) && !mTimes.deleted()) {
                checkKerahatAndIssues();

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
        mTimes = Times.getTimes(getArguments().getLong("city"));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle bdl) {


        if (mTimes == null) {
            return new View(getActivity());
        }

        mView = inflater.inflate(R.layout.vakit_fragment, container, false);
        setHasOptionsMenu(true);


        mCountdown = (TextView) mView.findViewById(R.id.countdown);
        mTitle = (TextView) mView.findViewById(R.id.city);
        mDate = (TextView) mView.findViewById(R.id.date);
        mHicri = (TextView) mView.findViewById(R.id.hicri);


        mKerahat = (TextView) mView.findViewById(R.id.kerahat);

        if ("tr".equals(Prefs.getLanguage())) {
            ((TextView) mView.findViewById(R.id.fajr)).setText(R.string.imsak);
        } else {
            ((TextView) mView.findViewById(R.id.fajr)).setText(R.string.fajr);
        }

        if (mTimes.getID() >= 0) {
            ImageView source1 = (ImageView) mView.findViewById(R.id.source1);
            ImageView source2 = (ImageView) mView.findViewById(R.id.source2);
            if (mTimes.getSource().resId != 0) {
                source1.setImageResource(mTimes.getSource().resId);
                source2.setImageResource(mTimes.getSource().resId);
            }
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

        boolean synced = false;
        for (int i = 0; i < 6; i++) {

            TextView time = (TextView) mView.findViewById(ids[i]);
            time.setText(Utils.fixTimeForHTML(daytimes[i]));
            if (!synced && daytimes[i].equals("00:00") && mTimes instanceof WebTimes && App.isOnline()) {
                ((WebTimes) mTimes).syncAsync();
                synced = true;
            }
        }

        if (Prefs.showExtraTimes() && (mTimes.getSource() == Source.Fazilet
                || mTimes.getSource() == Source.NVC
                || mTimes.getSource() == Source.Calc)) {
            mHandler.removeCallbacks(mAltTimes);
            mHandler.removeCallbacks(mNormalTimes);
            mHandler.postDelayed(mAltTimes, 3000);
        }

    }


    private Runnable mNormalTimes = new Runnable() {
        @Override
        public void run() {
            LocalDate greg = LocalDate.now();
            String imsak = mTimes.getTime(greg, 0);
            String asr = mTimes.getTime(greg, 3);

            TextView time = (TextView) mView.findViewById(ids[0]);
            time.setText(Utils.fixTimeForHTML(imsak));

            time = (TextView) mView.findViewById(ids[3]);
            time.setText(Utils.fixTimeForHTML(asr));

            time = (TextView) mView.findViewById(idsNames[0]);
            time.setText(R.string.imsak);

            time = (TextView) mView.findViewById(idsNames[3]);
            time.setText(Vakit.IKINDI.getString());

            mHandler.postDelayed(mAltTimes, 3000);
        }
    };

    private Runnable mAltTimes = new Runnable() {
        @Override
        public void run() {
            LocalDate greg = LocalDate.now();

            String sabah = mTimes.getSabah(greg);
            String asr = mTimes.getAsrSani(greg);
            if (sabah != null && !sabah.equals("00:00")) {
                TextView time = (TextView) mView.findViewById(ids[0]);
                time.setText(Utils.fixTimeForHTML(sabah));
                time = (TextView) mView.findViewById(idsNames[0]);
                time.setText(R.string.fajr);
            }

            if (asr != null && !asr.equals("00:00")) {
                TextView time = (TextView) mView.findViewById(ids[3]);
                time.setText(Utils.fixTimeForHTML(asr));
                time = (TextView) mView.findViewById(idsNames[3]);
                time.setText(R.string.asrSani);
            }
            mHandler.postDelayed(mNormalTimes, 3000);
        }
    };

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        try {
            MaterialMenuInflater.with(getActivity(), inflater)
                    .setDefaultColor(Color.WHITE)
                    .inflate(R.menu.vakit, menu);
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case R.id.notification:
                Fragment frag = getActivity().getSupportFragmentManager().findFragmentByTag("notPrefs");
                if (frag == null) {
                    ((VakitFragment) getParentFragment()).setFooterText("", false);
                    ((MainActivity.MainFragment) getParentFragment()).moveToFrag(NotificationPrefs.create(mTimes));
                } else {
                    ((VakitFragment) getParentFragment()).setFooterText(getString(R.string.monthly), true);
                    ((MainActivity.MainFragment) getParentFragment()).back();
                }

                AppRatingDialog.addToOpenedMenus("notPrefs");
                break;
            case R.id.export:
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.export)
                        .setItems(new CharSequence[]{"CSV", "PDF"}, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, final int which) {
                                long minDate = 0;
                                long maxDate = Long.MAX_VALUE;
                                if (mTimes instanceof WebTimes) {
                                    minDate = ((WebTimes) mTimes).getFirstSyncedDay().toDateTimeAtCurrentTime().getMillis();
                                    maxDate = ((WebTimes) mTimes).getLastSyncedDay().toDateTimeAtCurrentTime().getMillis();
                                }
                                final LocalDate ld = LocalDate.now();
                                final long finalMaxDate = maxDate;
                                DatePickerDialog dlg = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                                    @Override
                                    public void onDateSet(DatePicker datePicker, int y, int m, int d) {
                                        final LocalDate from = new LocalDate(y, m + 1, d);
                                        DatePickerDialog
                                                dlg1 = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                                            @Override
                                            public void onDateSet(DatePicker datePicker, int y1, int m1, int d1) {
                                                final LocalDate to = new LocalDate(y1, m1 + 1, d1);
                                                try {
                                                    export(which, from, to);
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                    Crashlytics.logException(e);
                                                    Toast.makeText(getActivity(), R.string.error, Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }, ld.getYear(), ld.getMonthOfYear() - 1, ld.getDayOfMonth());
                                        DateTime startDate = DateTime.now().withDate(y, m + 1, d);
                                        long start = startDate.getMillis();
                                        if (which == 1)
                                            dlg1.getDatePicker().setMaxDate(Math.min(finalMaxDate, startDate.plusDays(31).getMillis()));
                                        else
                                            dlg1.getDatePicker().setMaxDate(finalMaxDate);

                                        dlg1.getDatePicker().setMinDate(Math.min(start,
                                                dlg1.getDatePicker().getMaxDate() - 1000 * 60 * 60 * 24));
                                        dlg1.setTitle(R.string.to);
                                        dlg1.show();

                                    }
                                }, ld.getYear(), ld.getMonthOfYear() - 1, ld.getDayOfMonth());
                                dlg.getDatePicker().setMinDate(minDate);
                                dlg.getDatePicker().setMaxDate(Math.max(maxDate, minDate));
                                dlg.setTitle(R.string.from);
                                dlg.show();
                            }
                        });
                builder.show();
                break;
            case R.id.refresh:
                if (mTimes instanceof WebTimes) {
                    ((WebTimes) mTimes).syncAsync();
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
                sharingIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                sharingIntent.putExtra(Intent.EXTRA_TEXT, txt);
                startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share)));

        }
        return super.onOptionsItemSelected(item);
    }

    private void export(int csvpdf, @NonNull LocalDate from, @NonNull LocalDate to) throws IOException {
        File outputDir = getActivity().getCacheDir();
        if (!outputDir.exists()) outputDir.mkdirs();
        File outputFile = new File(outputDir, mTimes.getName().replace(" ", "_") + (csvpdf == 0 ? ".csv" : ".pdf"));
        if (outputDir.exists()) outputFile.delete();
        FileOutputStream outputStream;

        outputStream = new FileOutputStream(outputFile);
        if (csvpdf == 0) {
            outputStream.write("Date;Fajr;Shuruq;Dhuhr;Asr;Maghrib;Ishaa\n".getBytes());

            do {
                outputStream.write((from.toString("yyyy-MM-dd") + ";").getBytes());
                outputStream.write((mTimes.getTime(from, 0) + ";").getBytes());
                outputStream.write((mTimes.getTime(from, 1) + ";").getBytes());
                outputStream.write((mTimes.getTime(from, 2) + ";").getBytes());
                outputStream.write((mTimes.getTime(from, 3) + ";").getBytes());
                outputStream.write((mTimes.getTime(from, 4) + ";").getBytes());
                outputStream.write((mTimes.getTime(from, 5) + "\n").getBytes());
            } while (!(from = from.plusDays(1)).isAfter(to));
            outputStream.close();


        } else {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                PdfDocument document = new PdfDocument();

                PdfDocument.PageInfo pageInfo;
                int pw = 595;
                int ph = 842;
                pageInfo = new PdfDocument.PageInfo.Builder(pw, ph, 1).create();
                PdfDocument.Page page = document.startPage(pageInfo);
                Drawable launcher = Drawable.createFromStream(getActivity().getAssets().open("pdf/launcher.png"), null);
                Drawable qr = Drawable.createFromStream(getActivity().getAssets().open("pdf/qrcode.png"), null);
                Drawable badge = Drawable.createFromStream(getActivity().getAssets().open("pdf/badge_" + Prefs.getLanguage("en", "de", "tr", "fr", "ar") + ".png"), null);

                launcher.setBounds(30, 30, 30 + 65, 30 + 65);
                qr.setBounds(pw - 30 - 65, 30 + 65 + 5, pw - 30, 30 + 65 + 5 + 65);
                int w = 100;
                int h = w * badge.getIntrinsicHeight() / badge.getIntrinsicWidth();
                badge.setBounds(pw - 30 - w, 30 + (60 / 2 - h / 2), pw - 30, 30 + (60 / 2 - h / 2) + h);


                Canvas canvas = page.getCanvas();

                Paint paint = new Paint();
                paint.setARGB(255, 0, 0, 0);
                paint.setTextSize(10);
                paint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText("com.metinkale.prayer", pw - 30 - w / 2, 30 + (60 / 2 - h / 2) + h + 10, paint);

                launcher.draw(canvas);
                qr.draw(canvas);
                badge.draw(canvas);

                paint.setARGB(255, 61, 184, 230);
                canvas.drawRect(30, 30 + 60, pw - 30, 30 + 60 + 5, paint);


                if (mTimes.getSource().resId != 0) {
                    Drawable source = getResources().getDrawable(mTimes.getSource().resId);

                    h = 65;
                    w = h * source.getIntrinsicWidth() / source.getIntrinsicHeight();
                    source.setBounds(30, 30 + 65 + 5, 30 + w, 30 + 65 + 5 + h);
                    source.draw(canvas);
                }

                paint.setARGB(255, 0, 0, 0);
                paint.setTextSize(40);
                paint.setTextAlign(Paint.Align.LEFT);
                canvas.drawText(getText(R.string.appName).toString(), 30 + 65 + 5, 30 + 50, paint);
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setFakeBoldText(true);
                canvas.drawText(mTimes.getName(), pw / 2.0f, 30 + 65 + 50, paint);

                paint.setTextSize(12);
                int y = 30 + 65 + 5 + 65 + 30;
                int p = 30;
                int cw = (pw - p - p) / 7;
                canvas.drawText(getString(R.string.date), 30 + (0.5f * cw), y, paint);
                canvas.drawText(Vakit.IMSAK.getString(), 30 + (1.5f * cw), y, paint);
                canvas.drawText(Vakit.GUNES.getString(), 30 + (2.5f * cw), y, paint);
                canvas.drawText(Vakit.OGLE.getString(), 30 + (3.5f * cw), y, paint);
                canvas.drawText(Vakit.IKINDI.getString(), 30 + (4.5f * cw), y, paint);
                canvas.drawText(Vakit.AKSAM.getString(), 30 + (5.5f * cw), y, paint);
                canvas.drawText(Vakit.YATSI.getString(), 30 + (6.5f * cw), y, paint);
                paint.setFakeBoldText(false);
                do {
                    y += 20;
                    canvas.drawText((from.toString("dd.MM.yyyy")), 30 + (0.5f * cw), y, paint);
                    canvas.drawText((mTimes.getTime(from, 0)), 30 + (1.5f * cw), y, paint);
                    canvas.drawText((mTimes.getTime(from, 1)), 30 + (2.5f * cw), y, paint);
                    canvas.drawText((mTimes.getTime(from, 2)), 30 + (3.5f * cw), y, paint);
                    canvas.drawText((mTimes.getTime(from, 3)), 30 + (4.5f * cw), y, paint);
                    canvas.drawText((mTimes.getTime(from, 4)), 30 + (5.5f * cw), y, paint);
                    canvas.drawText((mTimes.getTime(from, 5)), 30 + (6.5f * cw), y, paint);
                } while (!(from = from.plusDays(1)).isAfter(to));
                document.finishPage(page);


                document.writeTo(outputStream);

                // close the document
                document.close();

            } else {
                Toast.makeText(getActivity(), R.string.versionNotSupported, Toast.LENGTH_LONG).show();
            }
        }

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType(csvpdf == 0 ? "text/csv" : "application/pdf");

        Uri uri = FileProvider.getUriForFile(getActivity(), "com.metinkale.prayer.fileprovider", outputFile);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);

        startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.export)));
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
        if (mTimes != null)
            mTimes.addOnTimesUpdatedListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeCallbacks(onSecond);
        if (mTimes != null) mTimes.removeOnTimesUpdatedListener(this);
    }

    void checkKerahatAndIssues() {
        if (mTimes == null || mTimes.getID() < 0) return;
        String issue = mTimes.getIssue();
        boolean k = mTimes.isKerahat() || issue != null;
        mKerahat.setVisibility(k ? View.VISIBLE : View.GONE);
        if (issue != null) {
            mKerahat.setText(issue);
        } else {
            mKerahat.setText(R.string.kerahatTime);
        }
    }

    @Override
    public void onTimesUpdated(Times t) {
        update();
    }
}
