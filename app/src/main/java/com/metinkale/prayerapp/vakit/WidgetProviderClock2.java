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
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.*;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.RemoteViews;
import com.crashlytics.android.Crashlytics;
import com.metinkale.prayer.R;
import com.metinkale.prayerapp.Utils;
import com.metinkale.prayerapp.vakit.times.Times;
import com.metinkale.prayerapp.vakit.times.Vakit;
import org.joda.time.LocalDateTime;

public class WidgetProviderClock2 extends AppWidgetProvider {
    private static float mDP;

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int widgetId) {

        if (mDP == 0) {
            Resources r = context.getResources();
            mDP = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, r.getDisplayMetrics());
        }

        Resources r = context.getResources();
        SharedPreferences widgets = context.getSharedPreferences("widgets", 0);


        int w = widgets.getInt(widgetId + "_width", 500);
        int h = widgets.getInt(widgetId + "_height", 200);

        w = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, w, r.getDisplayMetrics());
        h = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, h, r.getDisplayMetrics());

        float scaleX = (float) w / (float) 2;
        float scaleY = (float) h / (float) 2;
        float scale = Math.min(scaleY, scaleX);


        w = (int) (2 * scale);
        h = (int) (2 * scale);

        if ((w <= 0) || (h <= 0)) {
            SharedPreferences.Editor edit = widgets.edit();
            edit.remove(widgetId + "_width");
            edit.remove(widgetId + "_height");
            edit.apply();
            updateAppWidget(context, appWidgetManager, widgetId);
            return;
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
            Intent i = new Intent(context, WidgetConfigureClock.class);
            i.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
            remoteViews.setOnClickPendingIntent(R.id.image, PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT));
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
            return;
        }


        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.vakit_widget_clock);


        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(bmp);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setFilterBitmap(true);


        paint.setStyle(Style.STROKE);
        paint.setAntiAlias(true);
        paint.setSubpixelText(true);
        paint.setShadowLayer(2, 2, 2, 0xFF555555);
        paint.setTextAlign(Align.CENTER);
        paint.setColor(Color.WHITE);


        paint.setColor(0xFFFFFFFF);
        paint.setStrokeWidth(w / 100);

        canvas.drawArc(new RectF(w / 100, w / 100, w - w / 100, h - w / 100), 0, 360, false, paint);

        paint.setColor(Theme.Light.strokecolor);

        int next = times.getNext();
        int last = next - 1;

        long mills1 = times.getMills(last);
        long mills2 = times.getMills(next);
        long passed = System.currentTimeMillis() - mills1;
        float percent = passed / (float) (mills2 - mills1);

        canvas.drawArc(new RectF(w / 100, w / 100, w - w / 100, h - w / 100), -90, percent * 360, false, paint);

        paint.setStrokeWidth(1);
        LocalDateTime ltime = LocalDateTime.now();

        String[] time = Utils.fixTime(ltime.toString("HH:mm")).replace(":", " ").split(" ");
        paint.setTextSize(h * 0.50f);
        paint.setStyle(Style.FILL);
        paint.setColor(Color.WHITE);


        paint.setTextAlign(Align.LEFT);
        if (time.length == 3) {
            canvas.drawText(time[0], w * 0.05f, h * 0.65f, paint);
            paint.setColor(Theme.Light.strokecolor);

            paint.setTextSize(h * 0.18f);
            canvas.drawText(time[1], w * 0.60f, h * 0.45f, paint);
            paint.setTextSize(h * 0.1f);
            paint.setTextSize(h * 0.1f);
            canvas.drawText(time[2], w * 0.80f, h * 0.45f, paint);

            paint.setColor(0xFFFFFFFF);
            paint.setTextSize(h * 0.07f);

            canvas.drawText(Utils.toArabicNrs(ltime.toString("d'.' MMM'.'")), w * 0.60f, h * 0.55f, paint);
            canvas.drawText(Vakit.getByIndex(last).getString(), w * 0.60f, h * 0.65f, paint);
        } else {
            canvas.drawText(time[0], w * 0.10f, h * 0.65f, paint);
            paint.setColor(Theme.Light.strokecolor);
            paint.setTextSize(h * 0.22f);
            canvas.drawText(time[1], w * 0.63f, h * 0.45f, paint);
    
            paint.setColor(0xFFFFFFFF);
            paint.setTextSize(h * 0.07f);

            canvas.drawText(Utils.toArabicNrs(ltime.toString("d'.' MMM'.'")), w * 0.63f, h * 0.55f, paint);
            canvas.drawText(Vakit.getByIndex(last).getString(), w * 0.63f, h * 0.65f, paint);
        }


        paint.setTextAlign(Align.CENTER);

        paint.setTextSize(h * 0.15f);
        canvas.drawText(times.getLeft(next, false), w / 2, h * 0.85f, paint);

        paint.setTextSize(h * 0.12f);
        canvas.drawText(ltime.toString("EEEE"), w / 2, h * 0.22f, paint);


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
        ComponentName thisWidget = new ComponentName(context, WidgetProviderClock2.class);
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
