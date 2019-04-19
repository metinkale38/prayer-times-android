package com.metinkale.prayer.times;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.SystemClock;
import android.text.Html;
import android.text.Spannable;
import android.text.style.StyleSpan;
import android.util.Pair;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.metinkale.prayer.InternalBroadcastReceiver;
import com.metinkale.prayer.Preferences;
import com.metinkale.prayer.times.fragments.TimesFragment;
import com.metinkale.prayer.times.times.Times;
import com.metinkale.prayer.times.times.Vakit;
import com.metinkale.prayer.times.utils.NotificationUtils;
import com.metinkale.prayer.utils.ForegroundService;
import com.metinkale.prayer.utils.LocaleUtils;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

public class OngoingNotificationsReceiver extends InternalBroadcastReceiver implements InternalBroadcastReceiver.OnTimeTickListener {
    private static final String COLOR_SEARCH_1ST = "COLOR_SEARCH_1ST";
    private static final String COLOR_SEARCH_2ND = "COLOR_SEARCH_2ND";
    private static final String FOREGROUND_NEEDY_ONGOING = "ongoing";
    private Integer mColor1st = null;
    private Integer mColor2nd = null;

    @Override
    public void onTimeTick() {
        NotificationManager notMan = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        extractColors();


        LocalDate cal = LocalDate.now();

        List<Pair<Integer, Notification>> notifications = new ArrayList<>();
        for (Times t : Times.getTimes()) {
            if (!t.isOngoingNotificationActive()) {
                notMan.cancel(t.getIntID());
                continue;
            }


            boolean icon = Preferences.SHOW_ONGOING_ICON.get();
            boolean number = Preferences.SHOW_ONGOING_NUMBER.get();
            Crashlytics.setBool("showIcon", icon);
            Crashlytics.setBool("showNumber", number);

            Notification noti;
            RemoteViews views = new RemoteViews(getContext().getPackageName(), R.layout.notification_layout);

            int[] timeIds = {R.id.time0, R.id.time1, R.id.time2, R.id.time3, R.id.time4, R.id.time5};
            int[] vakitIds = {R.id.fajr, R.id.sun, R.id.zuhr, R.id.asr, R.id.maghrib, R.id.ishaa};

            int marker = t.getCurrentTime();
            if (Preferences.VAKIT_INDICATOR_TYPE.get().equals("next"))
                marker = marker + 1;
            for (Vakit vakit : Vakit.values()) {
                LocalTime time = t.getTime(cal, vakit.ordinal()).toLocalTime();
                if (marker == vakit.ordinal()) {
                    views.setTextViewText(vakitIds[vakit.ordinal()], Html.fromHtml("<strong><em>" + vakit.getString() + "</em></strong>"));
                    if (Preferences.CLOCK_12H.get()) {
                        Spannable span = (Spannable) LocaleUtils.formatTimeForHTML(time);
                        span.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), 0, span.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        views.setTextViewText(timeIds[vakit.ordinal()], span);
                    } else
                        views.setTextViewText(timeIds[vakit.ordinal()],
                                Html.fromHtml("<strong><em>" + LocaleUtils.formatTimeForHTML(time) + "</em></strong>"));
                } else {
                    views.setTextViewText(vakitIds[vakit.ordinal()], vakit.getString());
                    views.setTextViewText(timeIds[vakit.ordinal()], LocaleUtils.formatTimeForHTML(time));
                }
            }


            DateTime nextTime = t.getTime(cal, t.getNextTime()).toDateTime();
            if (Build.VERSION.SDK_INT >= 24 && Preferences.COUNTDOWN_TYPE.get().equals(Preferences.COUNTDOWN_TYPE_SHOW_SECONDS)) {
                views.setChronometer(R.id.countdown, nextTime.getMillis() - (System.currentTimeMillis() - SystemClock.elapsedRealtime()), null, true);
            } else {
                String txt = LocaleUtils.formatPeriod(DateTime.now(), nextTime, false);
                views.setString(R.id.countdown, "setFormat", txt);
                views.setChronometer(R.id.countdown, 0, txt, false);
            }

            views.setTextViewText(R.id.city, t.getName());


            views.setTextColor(R.id.fajr, mColor1st);
            views.setTextColor(R.id.sun, mColor1st);
            views.setTextColor(R.id.zuhr, mColor1st);
            views.setTextColor(R.id.asr, mColor1st);
            views.setTextColor(R.id.maghrib, mColor1st);
            views.setTextColor(R.id.ishaa, mColor1st);

            views.setTextColor(R.id.time0, mColor1st);
            views.setTextColor(R.id.time1, mColor1st);
            views.setTextColor(R.id.time2, mColor1st);
            views.setTextColor(R.id.time3, mColor1st);
            views.setTextColor(R.id.time4, mColor1st);
            views.setTextColor(R.id.time5, mColor1st);


            views.setTextColor(R.id.time, mColor1st);
            views.setTextColor(R.id.city, mColor1st);


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Notification.Builder notBuilder =
                        new Notification.Builder(getContext()).setContent(views).setContentIntent(TimesFragment.getPendingIntent(t)).setSmallIcon(
                                icon ? (number ? Icon.createWithBitmap(getIconFromMinutes(t)) :
                                        Icon.createWithResource(getContext(), R.drawable.ic_abicon)) :
                                        Icon.createWithResource(getContext(), R.drawable.ic_placeholder)).setOngoing(true);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    notBuilder.setChannelId(NotificationUtils.getOngoingChannel(getContext()).getId());
                }

                noti = notBuilder.build();
            } else {
                noti = new NotificationCompat.Builder(getContext()).setContent(views).setContentIntent(TimesFragment.getPendingIntent(t))
                        .setSmallIcon(icon ? R.drawable.ic_abicon : R.drawable.ic_placeholder).setOngoing(true).build();
            }


            if (Build.VERSION.SDK_INT >= 16) {
                noti.priority = Notification.PRIORITY_LOW;
            }
            noti.when = icon ? System.currentTimeMillis() : 0;

            notifications.add(new Pair<>(t.getIntID(), noti));
        }

        if (!notifications.isEmpty()) {
            for (int i = 0; i < notifications.size(); i++) {
                Pair<Integer, Notification> pair = notifications.get(i);
                if (i == 0) {
                    ForegroundService.addNeedy(getContext(), FOREGROUND_NEEDY_ONGOING, pair.second, pair.first);
                } else {
                    notMan.notify(pair.first, pair.second);
                }
            }
        } else {
            ForegroundService.removeNeedy(getContext(), FOREGROUND_NEEDY_ONGOING);
        }

    }


    private Bitmap getIconFromMinutes(Times t) {
        String left = LocaleUtils.formatPeriod(LocalDateTime.now(), t.getTime(LocalDate.now(), t.getNextTime()), false);
        String hour = left.substring(0, 2);
        String minute = left.substring(3, 5);


        Resources r = getContext().getResources();
        float size = 24;
        float twoLineTextSize = size * 1.2f;
        float singleLineTextSize = size * 1.6f;
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, size, r.getDisplayMetrics());
        Bitmap b = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        //paint.setTypeface(Typeface.MONOSPACE);
        paint.setColor(0xFFFFFFFF);
        paint.setTextAlign(Paint.Align.CENTER);
        if (!hour.equals("00")) {
            paint.setTextSize(twoLineTextSize);
            int yPos = (int) ((c.getHeight() / 4) - ((paint.descent() + paint.ascent()) / 2));

            c.drawText(hour, px / 2f, yPos, paint);
            c.drawText(minute, px / 2f, yPos + c.getHeight() / 2f, paint);
        } else {
            paint.setTextSize(singleLineTextSize);
            int yPos = (int) ((c.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2));
            c.drawText(minute, px / 2f, yPos, paint);
        }
        return b;
    }


    private boolean recurseGroup(@NonNull ViewGroup gp) {
        int count = gp.getChildCount();
        for (int i = 0; i < count; ++i) {
            View v = gp.getChildAt(i);
            if (v instanceof TextView) {
                TextView text = (TextView) v;
                String szText = text.getText().toString();
                if (COLOR_SEARCH_1ST.equals(szText)) {
                    mColor1st = text.getCurrentTextColor();
                }
                if (COLOR_SEARCH_2ND.equals(szText)) {
                    mColor2nd = text.getCurrentTextColor();
                }

                if ((mColor1st != null) && (mColor2nd != null)) {
                    return true;
                }
            } else if (gp.getChildAt(i) instanceof ViewGroup) {
                if (recurseGroup((ViewGroup) v)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void extractColors() {
        if (mColor1st != null && mColor2nd != null) {
            return;
        }


        try {
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getContext());
            mBuilder.setContentTitle(COLOR_SEARCH_1ST).setContentText(COLOR_SEARCH_2ND);
            Notification ntf = mBuilder.build();
            LinearLayout group = new LinearLayout(getContext());
            ViewGroup event = (ViewGroup) ntf.contentView.apply(getContext(), group);
            recurseGroup(event);
            group.removeAllViews();
        } catch (Exception e) {
            //  e.printStackTrace();
        }
        if (mColor1st == null) {
            mColor1st = Color.BLACK;
        }
        if (mColor2nd == null) {
            mColor2nd = Color.DKGRAY;
        }
    }

}
