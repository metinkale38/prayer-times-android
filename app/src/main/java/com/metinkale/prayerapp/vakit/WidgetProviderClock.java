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
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.util.TypedValue;
import android.widget.RemoteViews;
import com.crashlytics.android.Crashlytics;
import com.metinkale.prayer.R;
import com.metinkale.prayerapp.HicriDate;
import com.metinkale.prayerapp.Utils;
import com.metinkale.prayerapp.settings.Prefs;
import com.metinkale.prayerapp.vakit.times.Times;
import com.metinkale.prayerapp.vakit.times.Vakit;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

public class WidgetProviderClock extends AppWidgetProvider {
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

        float scaleX = (float) w / (float) 5;
        float scaleY = (float) h / (float) 2;
        float scale = Math.min(scaleY, scaleX);


        w = (int) (5 * scale);
        h = (int) (2 * scale);

        if (w <= 0 || h <= 0) {
            SharedPreferences.Editor edit = widgets.edit();
            edit.remove(widgetId + "_width");
            edit.remove(widgetId + "_height");
            edit.apply();
            updateAppWidget(context, appWidgetManager, widgetId);
            return;
        }

        Times times = null;
        long id = widgets.getLong(widgetId + "", 0L);
        if (id != 0)
            times = Times.getTimes(widgets.getLong(widgetId + "", 0L));
        if (times == null) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_city_removed);
            Intent i = new Intent(context, WidgetConfigureClock.class);
            i.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
            remoteViews.setOnClickPendingIntent(R.id.image, PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT));
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
            return;
        }


        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.vakit_widget_clock);

        remoteViews.setOnClickPendingIntent(R.id.abovePart, PendingIntent.getActivity(context, (int) System.currentTimeMillis(), new Intent(AlarmClock.ACTION_SHOW_ALARMS), PendingIntent.FLAG_UPDATE_CURRENT));
        remoteViews.setOnClickPendingIntent(R.id.belowPart, Main.getPendingIntent(times));


        int next = times.getNext();
        int last = next - 1;

        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(bmp);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setFilterBitmap(true);


        paint.setStyle(Style.FILL_AND_STROKE);
        paint.setAntiAlias(true);
        paint.setSubpixelText(true);
        paint.setShadowLayer(2, 2, 2, 0xFF555555);
        paint.setTextAlign(Align.CENTER);
        paint.setColor(Color.WHITE);

        LocalTime ltime = LocalTime.now();

        paint.setTextSize(h * 0.55f);
        String time = ltime.toString("HH:mm");
        if (Prefs.use12H()) {
            time = Utils.fixTime(time);
            String suffix = time.substring(time.indexOf(" ") + 1);
            time = time.substring(0, time.indexOf(" "));
            canvas.drawText(time, w / 2 - paint.measureText(suffix) / 4, h * 0.4f, paint);
            paint.setTextSize(h * 0.275f);
            canvas.drawText(suffix, w / 2 + paint.measureText(time), h * 0.2f, paint);
        } else {
            canvas.drawText(time, w / 2, h * 0.4f, paint);
        }


        LocalDate date = LocalDate.now();
        String greg = Utils.format(date);
        String hicri = Utils.format(new HicriDate(date));

        paint.setTextSize(h * 0.12f);
        float m = paint.measureText(greg + "  " + hicri);
        if (m > w * 0.8f) {
            paint.setTextSize(h * 0.12f * w * 0.8f / m);
        }


        paint.setTextAlign(Align.LEFT);
        canvas.drawText(greg, w * .1f, h * 0.55f, paint);
        paint.setTextAlign(Align.RIGHT);
        canvas.drawText(hicri, w * .9f, h * 0.55f, paint);
        remoteViews.setImageViewBitmap(R.id.widget, bmp);

        canvas.drawRect(w * 0.1f, h * 0.6f, w * 0.9f, h * 0.63f, paint);

        if (times.isKerahat()) {
            paint.setColor(0xffbf3f5b);
        } else {
            paint.setColor(Theme.Light.strokecolor);
        }
        long mills1 = times.getMills(last);
        long mills2 = times.getMills(next);
        long passed = System.currentTimeMillis() - mills1;
        float percent = passed / (float) (mills2 - mills1);
        canvas.drawRect(w * 0.1f, h * 0.6f, w * 0.1f + w * 0.8f * percent, h * 0.63f, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(h * 0.2f);
        paint.setTextAlign(Align.LEFT);
        if (Prefs.use12H()) {
            String l = Utils.fixTime(times.getTime(last));
            String s = l.substring(l.indexOf(" ") + 1);
            l = l.substring(0, l.indexOf(" "));
            canvas.drawText(times.getTime(last), w * 0.1f, h * 0.82f, paint);
            paint.setTextSize(h * 0.1f);
            canvas.drawText(s, w * 0.1f + 2 * paint.measureText(l), h * 0.72f, paint);

        } else {
            canvas.drawText(times.getTime(last), w * 0.1f, h * 0.82f, paint);

        }
        paint.setTextSize(h * 0.12f);
        canvas.drawText(Vakit.getByIndex(last).getString(), w * 0.1f, h * 0.95f, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(h * 0.2f);
        paint.setTextAlign(Align.RIGHT);
        if (Prefs.use12H()) {
            String l = Utils.fixTime(times.getTime(next));
            String s = l.substring(l.indexOf(" ") + 1);
            l = l.substring(0, l.indexOf(" "));
            canvas.drawText(l, w * 0.9f - paint.measureText(s) / 2, h * 0.82f, paint);
            paint.setTextSize(h * 0.1f);
            canvas.drawText(s, w * 0.9f, h * 0.72f, paint);

        } else {
            canvas.drawText(times.getTime(next), w * 0.9f, h * 0.82f, paint);
        }
        paint.setTextSize(h * 0.12f);
        canvas.drawText(Vakit.getByIndex(next).getString(), w * 0.9f, h * 0.95f, paint);


        paint.setColor(Color.WHITE);
        paint.setTextSize(h * 0.25f);
        paint.setTextAlign(Align.CENTER);
        paint.setFakeBoldText(true);
        canvas.drawText(times.getLeft(next, false), w * 0.5f, h * 0.9f, paint);
        paint.setFakeBoldText(false);
        try {
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        } catch (RuntimeException e) {
            Crashlytics.logException(e);
        }
    }

    @Override
    public void onEnabled(Context context) {
        ComponentName thisWidget = new ComponentName(context, WidgetProviderClock.class);
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

        if (w * h != 0) {
            SharedPreferences widgets = context.getSharedPreferences("widgets", 0);
            SharedPreferences.Editor edit = widgets.edit();
            edit.putInt(appWidgetId + "_width", w);
            edit.putInt(appWidgetId + "_height", h);
            edit.apply();
        }

        updateAppWidget(context, appWidgetManager, appWidgetId);
    }
}
