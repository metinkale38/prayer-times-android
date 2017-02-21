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
 *
 */

package com.metinkale.prayerapp.vakit;

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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.TypedValue;
import android.widget.RemoteViews;

import com.crashlytics.android.Crashlytics;
import com.metinkale.prayer.R;
import com.metinkale.prayerapp.App;
import com.metinkale.prayerapp.Utils;
import com.metinkale.prayerapp.settings.Prefs;
import com.metinkale.prayerapp.vakit.times.Times;
import com.metinkale.prayerapp.vakit.times.other.Vakit;

import org.joda.time.LocalDate;

public class WidgetProvider extends AppWidgetProvider {
    private static float mDP;

    private static void updateAppWidget(@NonNull Context context, @NonNull AppWidgetManager appWidgetManager, int widgetId) {

        if (mDP == 0) {
            Resources r = context.getResources();
            mDP = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, r.getDisplayMetrics());
        }

        Resources r = context.getResources();
        SharedPreferences widgets = context.getSharedPreferences("widgets", 0);

        int t = widgets.getInt(widgetId + "_theme", 0);
        int w = widgets.getInt(widgetId + "_width", 130);
        int h = widgets.getInt(widgetId + "_height", 160);


        w = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, w, r.getDisplayMetrics());
        h = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, h, r.getDisplayMetrics());


        float scaleX = (float) w / (float) 13;
        float scaleY = (float) h / (float) 16;
        float scale = Math.min(scaleY, scaleX);

        //Workaround for exception "RemoteViews for widget update exceeds maximum bitmap memory usage"
        //scale = WidgetProvider.correctScaleFactorIfNeeded(context, scale, 13, 16);

        w = (int) (13 * scale);
        h = (int) (16 * scale);


        if ((w <= 0) || (h <= 0)) {
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
        if (Prefs.getVakitIndicator().equals("next")) next++;


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

        paint.setColor(theme.textcolor);
        paint.setStyle(Style.FILL_AND_STROKE);
        paint.setAntiAlias(true);
        paint.setSubpixelText(true);

        double l = h / 10;
        paint.setTextSize((int) l);
        paint.setTextAlign(Align.CENTER);
        canvas.drawText(times.getName(), w / 2, (int) (l * 1.8), paint);

        paint.setTextSize((int) ((l * 8) / 10));

        if (next != 0) {
            paint.setColor(theme.hovercolor);
            canvas.drawRect(0, (int) (l * (next + 1.42)), w, (int) (l * (next + 2.42)), paint);
        }
        paint.setColor(theme.textcolor);

        paint.setTextAlign(Align.LEFT);
        canvas.drawText(Vakit.getByIndex(0).getString(), w / 6, (int) (l * 3.2), paint);
        canvas.drawText(Vakit.GUNES.getString(), w / 6, (int) (l * 4.2), paint);
        canvas.drawText(Vakit.OGLE.getString(), w / 6, (int) (l * 5.2), paint);
        canvas.drawText(Vakit.IKINDI.getString(), w / 6, (int) (l * 6.2), paint);
        canvas.drawText(Vakit.AKSAM.getString(), w / 6, (int) (l * 7.2), paint);
        canvas.drawText(Vakit.YATSI.getString(), w / 6, (int) (l * 8.2), paint);

        paint.setTextAlign(Align.RIGHT);
        if (Prefs.use12H()) {
            for (int i = 0; i < daytimes.length; i++) {
                String time = Utils.fixTime(daytimes[i]);
                String suffix = time.substring(time.indexOf(" ") + 1);
                time = time.substring(0, time.indexOf(" "));
                paint.setTextSize((int) ((l * 8) / 10));
                canvas.drawText(time, ((w * 5) / 6) - paint.measureText("A"), (int) (l * 3.2 + i * l), paint);
                paint.setTextSize((int) ((l * 4) / 10));
                canvas.drawText(suffix, ((w * 5) / 6) + (paint.measureText(time) / 4), (int) (l * 3 + i * l), paint);
            }
        } else {
            for (int i = 0; i < daytimes.length; i++) {
                canvas.drawText(Utils.toArabicNrs(daytimes[i]), (w * 5) / 6, (int) (l * 3.2 + i * l), paint);
            }
        }
        paint.setTextSize((int) l);
        paint.setTextAlign(Align.CENTER);
        canvas.drawText(left, w / 2, (int) (l * 9.5), paint);

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


    public static void updateWidgets(@NonNull Context c) {
        try {
            AppWidgetManager manager = AppWidgetManager.getInstance(App.get());

            ComponentName thisWidget = new ComponentName(c, WidgetProvider.class);
            for (int i : manager.getAppWidgetIds(thisWidget)) {
                updateAppWidget(c, manager, i);
            }

            thisWidget = new ComponentName(c, WidgetProviderSmall.class);

            for (int i : manager.getAppWidgetIds(thisWidget)) {
                WidgetProviderSmall.updateAppWidget(c, manager, i);
            }

            thisWidget = new ComponentName(c, WidgetProviderLong.class);

            for (int i : manager.getAppWidgetIds(thisWidget)) {
                WidgetProviderLong.updateAppWidget(c, manager, i);
            }

            thisWidget = new ComponentName(c, WidgetProviderSilenter.class);

            for (int i : manager.getAppWidgetIds(thisWidget)) {
                WidgetProviderSilenter.updateAppWidget(c, manager, i);
            }

            thisWidget = new ComponentName(c, WidgetProviderClock.class);

            for (int i : manager.getAppWidgetIds(thisWidget)) {
                WidgetProviderClock.updateAppWidget(c, manager, i);
            }

            thisWidget = new ComponentName(c, WidgetProviderClock2.class);

            for (int i : manager.getAppWidgetIds(thisWidget)) {
                WidgetProviderClock2.updateAppWidget(c, manager, i);
            }


        } catch (Exception e) {
            Crashlytics.logException(e);
        }
    }

    @Override
    public void onEnabled(@NonNull Context context) {
        ComponentName thisWidget = new ComponentName(context, WidgetProvider.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        onUpdate(context, manager, manager.getAppWidgetIds(thisWidget));

    }

    @Override
    public void onUpdate(@NonNull Context context, @NonNull AppWidgetManager appWidgetManager, @NonNull int[] appWidgetIds) {

        for (int widgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, widgetId);
        }

    }

    @Override
    public void onDisabled(Context context) {

    }

    @Override
    public void onAppWidgetOptionsChanged(@NonNull Context context, @NonNull AppWidgetManager appWidgetManager, int appWidgetId, @NonNull Bundle newOptions) {
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
