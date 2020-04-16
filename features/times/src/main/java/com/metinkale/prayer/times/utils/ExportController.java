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

package com.metinkale.prayer.times.utils;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;

import com.crashlytics.android.Crashlytics;
import com.metinkale.prayer.times.R;
import com.metinkale.prayer.times.times.Times;
import com.metinkale.prayer.times.times.Vakit;
import com.metinkale.prayer.times.times.sources.WebTimes;
import com.metinkale.prayer.utils.LocaleUtils;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.metinkale.prayer.times.times.Vakit.ASR;
import static com.metinkale.prayer.times.times.Vakit.DHUHR;
import static com.metinkale.prayer.times.times.Vakit.FAJR;
import static com.metinkale.prayer.times.times.Vakit.ISHAA;
import static com.metinkale.prayer.times.times.Vakit.MAGHRIB;
import static com.metinkale.prayer.times.times.Vakit.SUN;

public class ExportController {
    public static void exportPDF(Context ctx, Times times, @NonNull LocalDate from, @NonNull LocalDate to) throws IOException {
        PdfDocument document = new PdfDocument();

        PdfDocument.PageInfo pageInfo;
        int pw = 595;
        int ph = 842;
        pageInfo = new PdfDocument.PageInfo.Builder(pw, ph, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Drawable launcher = Drawable.createFromStream(ctx.getAssets().open("pdf/launcher.png"), null);
        Drawable qr = Drawable.createFromStream(ctx.getAssets().open("pdf/qrcode.png"), null);
        Drawable badge =
                Drawable.createFromStream(ctx.getAssets().open("pdf/badge_" + LocaleUtils.getLanguage("en", "de", "tr", "fr", "ar") + ".png"),
                        null);

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
        canvas.drawText("com.metinkale.prayer", pw - 30 - w / 2f, 30 + (60 / 2f - h / 2f) + h + 10, paint);

        launcher.draw(canvas);
        qr.draw(canvas);
        badge.draw(canvas);

        paint.setARGB(255, 61, 184, 230);
        canvas.drawRect(30, 30 + 60, pw - 30, 30 + 60 + 5, paint);


        if (times.getSource().drawableId != 0) {
            Drawable source = ctx.getResources().getDrawable(times.getSource().drawableId);

            h = 65;
            w = h * source.getIntrinsicWidth() / source.getIntrinsicHeight();
            source.setBounds(30, 30 + 65 + 5, 30 + w, 30 + 65 + 5 + h);
            source.draw(canvas);
        }

        paint.setARGB(255, 0, 0, 0);
        paint.setTextSize(40);
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(ctx.getText(R.string.appName).toString(), 30 + 65 + 5, 30 + 50, paint);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setFakeBoldText(true);
        canvas.drawText(times.getName(), pw / 2.0f, 30 + 65 + 50, paint);

        paint.setTextSize(12);
        int y = 30 + 65 + 5 + 65 + 30;
        int p = 30;
        int cw = (pw - p - p) / 7;
        canvas.drawText(ctx.getString(R.string.date), 30 + (0.5f * cw), y, paint);
        canvas.drawText(FAJR.getString(), 30 + (1.5f * cw), y, paint);
        canvas.drawText(Vakit.SUN.getString(), 30 + (2.5f * cw), y, paint);
        canvas.drawText(Vakit.DHUHR.getString(), 30 + (3.5f * cw), y, paint);
        canvas.drawText(Vakit.ASR.getString(), 30 + (4.5f * cw), y, paint);
        canvas.drawText(Vakit.MAGHRIB.getString(), 30 + (5.5f * cw), y, paint);
        canvas.drawText(Vakit.ISHAA.getString(), 30 + (6.5f * cw), y, paint);
        paint.setFakeBoldText(false);
        do {
            y += 20;
            canvas.drawText((from.toString("dd.MM.yyyy")), 30 + (0.5f * cw), y, paint);
            canvas.drawText(times.getTime(from, FAJR.ordinal()).toLocalTime().toString(), 30 + (1.5f * cw), y, paint);
            canvas.drawText(times.getTime(from, SUN.ordinal()).toLocalTime().toString(), 30 + (2.5f * cw), y, paint);
            canvas.drawText(times.getTime(from, DHUHR.ordinal()).toLocalTime().toString(), 30 + (3.5f * cw), y, paint);
            canvas.drawText(times.getTime(from, ASR.ordinal()).toLocalTime().toString(), 30 + (4.5f * cw), y, paint);
            canvas.drawText(times.getTime(from, MAGHRIB.ordinal()).toLocalTime().toString(), 30 + (5.5f * cw), y, paint);
            canvas.drawText(times.getTime(from, ISHAA.ordinal()).toLocalTime().toString(), 30 + (6.5f * cw), y, paint);
        } while (!(from = from.plusDays(1)).isAfter(to));
        document.finishPage(page);


        File outputDir = ctx.getCacheDir();
        if (!outputDir.exists())
            outputDir.mkdirs();
        File outputFile = new File(outputDir, times.getName().replace(" ", "_") + ".pdf");
        if (outputFile.exists())
            outputFile.delete();
        FileOutputStream outputStream = new FileOutputStream(outputFile);
        document.writeTo(outputStream);
        document.close();

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("application/pdf");

        Uri uri = FileProvider.getUriForFile(ctx, ctx.getString(R.string.FILE_PROVIDER_AUTHORITIES), outputFile);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);

        ctx.startActivity(Intent.createChooser(shareIntent, ctx.getResources().getText(R.string.export)));
    }
    
    public static void exportCSV(Context ctx, Times times, @NonNull LocalDate from, @NonNull LocalDate to) throws IOException {
        File outputDir = ctx.getCacheDir();
        if (!outputDir.exists())
            outputDir.mkdirs();
        File outputFile = new File(outputDir, times.getName().replace(" ", "_") + ".csv");
        if (outputFile.exists())
            outputFile.delete();
        FileOutputStream outputStream;
        
        outputStream = new FileOutputStream(outputFile);
        outputStream.write("HijriDate;Fajr;Shuruq;Dhuhr;Asr;Maghrib;Ishaa\n".getBytes());
        
        do {
            outputStream.write((from.toString("yyyy-MM-dd") + ";").getBytes());
            outputStream.write((times.getTime(from, FAJR.ordinal()).toLocalTime().toString() + ";").getBytes());
            outputStream.write((times.getTime(from, SUN.ordinal()).toLocalTime().toString() + ";").getBytes());
            outputStream.write((times.getTime(from, DHUHR.ordinal()).toLocalTime().toString() + ";").getBytes());
            outputStream.write((times.getTime(from, ASR.ordinal()).toLocalTime().toString() + ";").getBytes());
            outputStream.write((times.getTime(from, MAGHRIB.ordinal()).toLocalTime().toString() + ";").getBytes());
            outputStream.write((times.getTime(from, ISHAA.ordinal()).toLocalTime().toString() + "\n").getBytes());
        } while (!(from = from.plusDays(1)).isAfter(to));
        outputStream.close();
        
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("name/csv");
        
        Uri uri = FileProvider.getUriForFile(ctx, ctx.getString(R.string.FILE_PROVIDER_AUTHORITIES), outputFile);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        
        ctx.startActivity(Intent.createChooser(shareIntent, ctx.getResources().getText(R.string.export)));
    }
    
    
    public static void export(final Context ctx, final Times times) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle(R.string.export).setItems(new CharSequence[]{"CSV", "PDF"}, (dialogInterface, which) -> {
            long minDate = 0;
            long maxDate = Long.MAX_VALUE;
            if (times instanceof WebTimes) {
                minDate = ((WebTimes) times).getFirstSyncedDay().toDateTimeAtCurrentTime().getMillis();
                maxDate = ((WebTimes) times).getLastSyncedDay().toDateTimeAtCurrentTime().getMillis();
            }
            final LocalDate ld = LocalDate.now();
            final long finalMaxDate = maxDate;
            DatePickerDialog dlg = new DatePickerDialog(ctx, (datePicker, y, m, d) -> {
                final LocalDate from = new LocalDate(y, m + 1, d);
                DatePickerDialog dlg1 = new DatePickerDialog(ctx, (datePicker1, y1, m1, d1) -> {
                    final LocalDate to = new LocalDate(y1, m1 + 1, d1);
                    try {
                        if (which == 0) {
                            exportCSV(ctx, times, from, to);
                        } else {
                            exportPDF(ctx, times, from, to);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Crashlytics.logException(e);
                        Toast.makeText(ctx, R.string.error, Toast.LENGTH_SHORT).show();
                    }
                }, ld.getYear(), ld.getMonthOfYear() - 1, ld.getDayOfMonth());
                DateTime startDate = DateTime.now().withDate(y, m + 1, d);
                long start = startDate.getMillis();
                if (which == 1)
                    dlg1.getDatePicker().setMaxDate(Math.min(finalMaxDate, startDate.plusDays(31).getMillis()));
                else
                    dlg1.getDatePicker().setMaxDate(finalMaxDate);

                dlg1.getDatePicker().setMinDate(Math.min(start, dlg1.getDatePicker().getMaxDate() - 1000 * 60 * 60 * 24));
                dlg1.setTitle(R.string.to);
                dlg1.show();

            }, ld.getYear(), ld.getMonthOfYear() - 1, ld.getDayOfMonth());
            dlg.getDatePicker().setMinDate(minDate);
            dlg.getDatePicker().setMaxDate(Math.max(maxDate, minDate));
            dlg.setTitle(R.string.from);
            dlg.show();
        });
        builder.show();
        
    }
}
