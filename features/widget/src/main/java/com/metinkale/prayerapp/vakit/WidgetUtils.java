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

package com.metinkale.prayerapp.vakit;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.util.TypedValue;
import android.widget.RemoteViews;

import com.crashlytics.android.Crashlytics;
import com.metinkale.prayer.App;
import com.metinkale.prayer.times.times.Times;
import com.metinkale.prayer.widgets.R;

/**
 * Created by metin on 24.03.2017.
 */

public class WidgetUtils {

    static Theme getTheme(int widgetId) {
        SharedPreferences widgets = App.get().getSharedPreferences("widgets", 0);
        int t = widgets.getInt(widgetId + "_theme", 0);
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
        return theme;
    }

    static Times getTimes(int widgetId) {
        SharedPreferences widgets = App.get().getSharedPreferences("widgets", 0);
        long id = widgets.getLong(widgetId + "", 0L);
        Times t = null;
        if (id != 0) {
            t = Times.getTimes(id);
        }
        if (t == null)
            widgets.edit().remove(widgetId + "").apply();
        return t;
    }

    static void showNoCityWidget(Context context, AppWidgetManager appWidgetManager, int widgetId) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_city_removed);
        Intent i = new Intent(context, WidgetConfigure.class);
        i.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        i.putExtra(WidgetConfigure.ONLYCITY, true);
        remoteViews.setOnClickPendingIntent(R.id.image, PendingIntent.getActivity(context, widgetId, i, PendingIntent.FLAG_CANCEL_CURRENT));
        appWidgetManager.updateAppWidget(widgetId, remoteViews);
    }

    static class Size {
        final int width;
        final int height;

        private Size(int w, int h, float aspectRatio) {
            Resources r = App.get().getResources();
            w = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, w, r.getDisplayMetrics());
            h = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, h, r.getDisplayMetrics());

            int w1 = (int) (h * aspectRatio);
            int h1 = (int) (w / aspectRatio);
            width = Math.min(w, w1);
            height = Math.min(h, h1);
        }
    }

    static Size getSize(Context context, AppWidgetManager appWidgetManager, int widgetId, float aspectRatio) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            Bundle options = appWidgetManager.getAppWidgetOptions(widgetId);
            boolean isPort = context.getResources().getBoolean(R.bool.isPort);
            int w = options.getInt(isPort ?
                    AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH : AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
            int h = options.getInt(isPort ?
                    AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT : AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
            return new Size(w, h, aspectRatio);
        } else {
            AppWidgetProviderInfo info = appWidgetManager.getAppWidgetInfo(widgetId);
            return new Size(info.minWidth, info.minHeight, aspectRatio);
        }
    }

    public static void updateWidgets(@NonNull Context c) {
        try {
            AppWidgetManager manager = AppWidgetManager.getInstance(App.get());

            ComponentName thisWidget = new ComponentName(c, WidgetProvider.class);
            for (int i : manager.getAppWidgetIds(thisWidget)) {
                WidgetProvider.updateAppWidget(c, manager, i);
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
}
