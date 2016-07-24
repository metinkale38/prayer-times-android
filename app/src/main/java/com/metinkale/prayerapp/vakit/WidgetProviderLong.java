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

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.RemoteViews;
import com.crashlytics.android.Crashlytics;
import com.metinkale.prayer.R;
import com.metinkale.prayerapp.Utils;
import com.metinkale.prayerapp.settings.Prefs;
import com.metinkale.prayerapp.vakit.times.Times;
import com.metinkale.prayerapp.vakit.times.Vakit;
import org.joda.time.LocalDate;

public class WidgetProviderLong extends AppWidgetProvider {
    private static float mDP;

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int widgetId) {

        if (mDP == 0) {
            Resources r = context.getResources();
            mDP = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, r.getDisplayMetrics());
        }

        Resources r = context.getResources();
        SharedPreferences widgets = context.getSharedPreferences("widgets", 0);

        int t = widgets.getInt(widgetId + "_theme", 0);
        int w = widgets.getInt(widgetId + "_width", 300);
        int h = widgets.getInt(widgetId + "_height", 60);

        w = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, w, r.getDisplayMetrics());
        h = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, h, r.getDisplayMetrics());

        float scaleX = (float) w / (float) 20;
        float scaleY = (float) h / (float) 5;
        float scale = Math.min(scaleY, scaleX);

        w = (int) (20 * scale);
        h = (int) (5 * scale);

        if ((w <= 0) || (h <= 0)) {
            if (!widgets.contains(widgetId + "_width") && !widgets.contains(widgetId + "_height")) {
                return;
            }
            SharedPreferences.Editor edit = widgets.edit();
            edit.remove(widgetId + "_width");
            edit.remove(widgetId + "_height");
            edit.apply();
            updateAppWidget(context, appWidgetManager, widgetId);
            return;
        }

        Theme theme;
        switch (t) {
            case 0:
                theme = Theme.Light;
                break;
            case 1:
                theme = Theme.Dark;
                break;
            case 2:
                theme = Theme.LightTrans;
                break;
            case 3:
                theme = Theme.Trans;
                break;
            default:
                theme = Theme.Light;
                break;
        }

        Times times = null;
        long id = 0;
        try {
            id = widgets.getLong(widgetId + "", 0L);
        } catch (ClassCastException e) {
            widgets.edit().remove(widgetId + "").apply();
        }
        if (id != 0) {
            times = Times.getTimes(id);
        }
        if (times == null) {

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_city_removed);
            Intent i = new Intent(context, WidgetConfigure.class);
            i.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
            remoteViews.setOnClickPendingIntent(R.id.image, PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT));
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
            return;
        }


        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.vakit_widget);

        LocalDate date = LocalDate.now();
        String[] daytimes = {times.getTime(date, 0), times.getTime(date, 1), times.getTime(date, 2), times.getTime(date, 3), times.getTime(date, 4), times.getTime(date, 5)};

        int next = times.getNext();
        String left = times.getLeft(next, false);

        remoteViews.setOnClickPendingIntent(R.id.widget, Main.getPendingIntent(times));

        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(bmp);
        canvas.scale(0.99f, 0.99f, w / 2, h / 2);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setFilterBitmap(true);

        paint.setStyle(Style.FILL);
        paint.setColor(theme.bgcolor);
        canvas.drawRect(0, 0, w, h, paint);

        paint.setStyle(Style.FILL_AND_STROKE);
        paint.setAntiAlias(true);
        paint.setSubpixelText(true);

        paint.setColor(theme.hovercolor);
        if (next != 0) {
            canvas.drawRect((w * (next - 1)) / 6, h * 3 / 9, w * next / 6, h, paint);
        }
        float s = paint.getStrokeWidth();
        float dip = (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) ? 2f : 3f;
        paint.setStrokeWidth(dip * mDP);
        canvas.drawLine(0, (h * 3) / 9, w, h * 3 / 9, paint);
        // canvas.drawRect(0, 0, w, h * 3 / 9, paint);
        paint.setStrokeWidth(s);

        paint.setColor(theme.textcolor);

        paint.setTextAlign(Align.LEFT);
        paint.setTextSize(h / 4);
        canvas.drawText(" " + times.getName(), 0, h / 4, paint);

        paint.setTextAlign(Align.RIGHT);
        canvas.drawText(left + " ", w, h / 4, paint);

        paint.setTextSize(h / 5);
        paint.setTextAlign(Align.CENTER);
        int y = (h * 6) / 7;
        if (Prefs.use12H()) {
            y += h / 14;
        }

        boolean fits = true;
        String[] vakits = {Vakit.getByIndex(0).getString(), Vakit.GUNES.getString(), Vakit.OGLE.getString(), Vakit.IKINDI.getString(), Vakit.AKSAM.getString(), Vakit.YATSI.getString()};
        do {
            if (!fits) {
                paint.setTextSize((float) (paint.getTextSize() * 0.95));
            }
            fits = true;
            for (String v : vakits) {
                if ((paint.measureText(v) > (w / 6)) && (w > 5)) {
                    fits = false;
                }
            }
        } while (!fits);

        for (int i = 0; i < vakits.length; i++) {
            canvas.drawText(vakits[i], (w * (1 + (2 * i))) / 12, y, paint);
        }


        paint.setTextSize((h * 2) / 9);
        if (Prefs.use12H()) {
            for (int i = 0; i < daytimes.length; i++) {
                String time = Utils.fixTime(daytimes[i]);
                String suffix = time.substring(time.indexOf(" ") + 1);
                time = time.substring(0, time.indexOf(" "));
                paint.setTextSize((h * 2) / 9);
                canvas.drawText(time, (w * (1 + (2 * i))) / 12, h * 6 / 10, paint);
                paint.setTextSize(h / 9);
                canvas.drawText(suffix, (w * (1 + (2 * i))) / 12, h * 7 / 10, paint);
            }
        } else {
            for (int i = 0; i < daytimes.length; i++) {
                canvas.drawText(daytimes[i], (w * (1 + (2 * i))) / 12, h * 3 / 5, paint);
            }
        }


        paint.setStyle(Style.STROKE);
        float stroke = mDP;
        paint.setStrokeWidth(stroke);
        paint.setColor(theme.strokecolor);
        canvas.drawRect(0, 0, w, h, paint);

        remoteViews.setImageViewBitmap(R.id.widget, bmp);

        try {
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        } catch (RuntimeException e) {
            if (!e.getMessage().contains("exceeds maximum bitmap memory usage")) {
                Crashlytics.logException(e);
            }
        }
    }

    @Override
    public void onEnabled(Context context) {
        ComponentName thisWidget = new ComponentName(context, WidgetProviderLong.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        onUpdate(context, manager, manager.getAppWidgetIds(thisWidget));

    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        for (int widgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, widgetId);
        }

    }

    @Override
    public void onDisabled(Context context) {

    }

    @SuppressLint("InlinedApi")
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        int w = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
        int h = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);

        if ((w * h) != 0) {
            SharedPreferences widgets = context.getSharedPreferences("widgets", 0);
            SharedPreferences.Editor edit = widgets.edit();
            edit.putInt(appWidgetId + "_width", w);
            edit.putInt(appWidgetId + "_height", h);
            edit.apply();
        }

        updateAppWidget(context, appWidgetManager, appWidgetId);
    }
}
