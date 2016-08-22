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
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;
import com.crashlytics.android.Crashlytics;
import com.metinkale.prayer.R;
import com.metinkale.prayerapp.App;
import com.metinkale.prayerapp.Utils;
import com.metinkale.prayerapp.vakit.times.Times;
import com.metinkale.prayerapp.vakit.times.other.Vakit;
import org.joda.time.LocalDate;

public class WidgetProvider extends AppWidgetProvider {
    private static float mDP;

    private static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int widgetId) {

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

        int ow = w;
        int oh = h;


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

        LocalDate date = LocalDate.now();
        String[] daytimes = {times.getTime(date, 0), times.getTime(date, 1), times.getTime(date, 2), times.getTime(date, 3), times.getTime(date, 4), times.getTime(date, 5)};

        int next = times.getNext();
        String left = times.getLeft(next, false);


        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.vakit_widget22);
        remoteViews.setInt(R.id.top, "setBackgroundColor", theme.strokecolor);
        remoteViews.setInt(R.id.left, "setBackgroundColor", theme.strokecolor);
        remoteViews.setInt(R.id.right, "setBackgroundColor", theme.strokecolor);
        remoteViews.setInt(R.id.bottom, "setBackgroundColor", theme.strokecolor);
        remoteViews.setInt(R.id.main, "setBackgroundColor", theme.bgcolor);

        h *= 0.99;
        w *= 0.99;
        int vp = oh / 2 - h / 2;
        int hp = ow / 2 - w / 2;
        remoteViews.setViewPadding(R.id.frame, hp, vp, hp, vp);
        int[] nameIds = {R.id.imsak, R.id.gunes, R.id.ogle, R.id.ikindi, R.id.aksam, R.id.yatsi};
        int[] timeIds = {R.id.imsaktime, R.id.gunestime, R.id.ogletime, R.id.ikinditime, R.id.aksamtime, R.id.yatsitime};
        int[] hoverIds = {R.id.hover0, R.id.hover1, R.id.hover2, R.id.hover3, R.id.hover4, R.id.hover5, R.id.hover6};

        remoteViews.setTextViewText(R.id.city, times.getName());
        remoteViews.setTextViewText(R.id.countdown, left);
        remoteViews.setInt(R.id.city, "setTextColor", theme.textcolor);
        remoteViews.setInt(R.id.countdown, "setTextColor", theme.textcolor);
        remoteViews.setTextViewText(R.id.city, times.getName());
        remoteViews.setViewPadding(R.id.city, 0, h / 20, 0, h / 40);

        remoteViews.setTextViewTextSize(R.id.city, TypedValue.COMPLEX_UNIT_PX, h / 9);
        remoteViews.setTextViewTextSize(R.id.countdown, TypedValue.COMPLEX_UNIT_PX, h / 9);
        for (int i = 0; i < nameIds.length; i++) {
            remoteViews.setInt(nameIds[i], "setTextColor", theme.textcolor);
            remoteViews.setInt(timeIds[i], "setTextColor", theme.textcolor);
            remoteViews.setTextViewText(nameIds[i], Vakit.getByIndex(i).getString());
            remoteViews.setTextViewText(timeIds[i], Utils.fixTimeForHTML(daytimes[i]));
            remoteViews.setTextViewTextSize(nameIds[i], TypedValue.COMPLEX_UNIT_PX, h / 12);
            remoteViews.setTextViewTextSize(timeIds[i], TypedValue.COMPLEX_UNIT_PX, h / 12);

            remoteViews.setViewPadding(nameIds[i], w / 6, 0, 0, 0);
            remoteViews.setViewPadding(timeIds[i], 0, 0, w / 6, 0);

            if (i + 1 == next) {
                remoteViews.setInt(hoverIds[i], "setBackgroundColor", theme.hovercolor);
                remoteViews.setViewVisibility(hoverIds[i], View.VISIBLE);
            } else {
                remoteViews.setViewVisibility(hoverIds[i], View.GONE);
            }
        }


        appWidgetManager.updateAppWidget(widgetId, remoteViews);

    }


    public static void updateWidgets(Context c) {
        try {
            AppWidgetManager manager = AppWidgetManager.getInstance(App.getContext());


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
                WidgetProviderClock2
                        .updateAppWidget(c, manager, i);
            }


        } catch (Exception e) {
            Crashlytics.logException(e);
        }
    }

    @Override
    public void onEnabled(Context context) {
        ComponentName thisWidget = new ComponentName(context, WidgetProvider.class);
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
