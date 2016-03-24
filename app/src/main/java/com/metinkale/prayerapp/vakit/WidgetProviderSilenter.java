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
import android.util.TypedValue;
import android.widget.RemoteViews;
import com.crashlytics.android.Crashlytics;
import com.metinkale.prayer.R;

public class WidgetProviderSilenter extends AppWidgetProvider {
    private static float mDP;

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int widgetId) {

        if (mDP == 0) {
            Resources r = context.getResources();
            mDP = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, r.getDisplayMetrics());
        }
        Resources r = context.getResources();
        SharedPreferences widgets = context.getSharedPreferences("widgets", 0);
        int t = widgets.getInt(widgetId + "_theme", 0);
        int w = widgets.getInt(widgetId + "_width", 60);
        int h = widgets.getInt(widgetId + "_height", 60);


        w = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, w, r.getDisplayMetrics());
        h = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, h, r.getDisplayMetrics());

        int s = Math.min(w, h);

        if (s <= 0) {
            SharedPreferences.Editor edit = widgets.edit();
            edit.remove(widgetId + "_width");
            edit.remove(widgetId + "_height");
            edit.apply();
            updateAppWidget(context, appWidgetManager, widgetId);
            return;
        }

        Theme theme = null;
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
        }

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.vakit_widget);

        Intent i = new Intent(context, SilenterPrompt.class);
        remoteViews.setOnClickPendingIntent(R.id.widget, PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT));

        Bitmap bmp = Bitmap.createBitmap(s, s, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(bmp);
        canvas.scale(0.99f, 0.99f, s / 2, s / 2);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setFilterBitmap(true);

        paint.setStyle(Style.FILL);
        paint.setColor(theme.bgcolor);
        canvas.drawRect(0, 0, s, s, paint);

        paint.setColor(theme.textcolor);
        paint.setStyle(Style.FILL_AND_STROKE);
        paint.setAntiAlias(true);
        paint.setSubpixelText(true);

        paint.setColor(theme.hovercolor);

        paint.setColor(theme.textcolor);

        paint.setTextSize((s * 25) / 100);
        paint.setTextAlign(Align.CENTER);
        canvas.drawText("Sessize", s / 2, (s * 125) / 300, paint);
        canvas.drawText("al", s / 2, (s * 25) / 30, paint);

        paint.setStyle(Style.STROKE);
        float stroke = mDP;
        paint.setStrokeWidth(stroke);
        paint.setColor(theme.strokecolor);
        canvas.drawRect(0, 0, s, s, paint);

        remoteViews.setImageViewBitmap(R.id.widget, bmp);

        try {
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        } catch (RuntimeException e) {
            Crashlytics.logException(e);
        }

    }

    @Override
    public void onEnabled(Context context) {
        ComponentName thisWidget = new ComponentName(context, WidgetProviderSilenter.class);
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
