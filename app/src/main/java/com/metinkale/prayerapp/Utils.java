package com.metinkale.prayerapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.text.style.SuperscriptSpan;
import com.metinkale.prayer.R;
import com.metinkale.prayerapp.settings.Prefs;

import java.util.Calendar;
import java.util.Locale;

public class Utils {
    private static String[] sGMonths, sHMonths, sHolydays, sWeekdays, sShortWeekdays;

    public static Spanned fixTimeForHTML(String time) {
        if (!Prefs.use12H())
            return new SpannableString(time);
        time = fixTime(time);
        int d = time.indexOf(" ");
        time = time.replace(" ", "");

        int s = time.length();
        Spannable span = new SpannableString(time);
        span.setSpan(new SuperscriptSpan(), d, s, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        span.setSpan(new RelativeSizeSpan(0.5f), d, s, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return span;
    }


    public static String fixTime(String time) {
        if (Prefs.use12H())
            try {
                String fix = time.substring(0, time.indexOf(":"));
                String suffix = time.substring(time.indexOf(":"));


                int hour = Integer.parseInt(fix);
                if (hour == 0)
                    return "00" + suffix + " AM";
                else if (hour < 12)
                    return az(hour) + suffix + " AM";
                else if (hour == 12) {
                    return "12" + suffix + " PM";
                } else
                    return az(hour - 12) + suffix + " PM";
            } catch (Exception e) {
                App.e(e);
                return time;
            }
        return time;

    }


    public static void init() {
        DiyanetTakvimi.init();
        String newLang = Prefs.getLanguage();
        Context c = App.getContext();


        int year = Calendar.getInstance().get(Calendar.YEAR);

        if (year != Prefs.getLastCalIntegration()) {
            MainIntentService.startCalendarIntegration(c);
        }

        if (newLang == null)
            return;
        Locale locale = new Locale(newLang);
        Locale.setDefault(locale);
        android.content.res.Configuration config = new android.content.res.Configuration();
        config.locale = locale;
        ((Application) App.getContext()).getBaseContext().getResources().updateConfiguration(config, ((Application) App.getContext()).getBaseContext().getResources().getDisplayMetrics());

        if (Prefs.getLanguage() != newLang) {
            sGMonths = null;
            sHMonths = null;
            sHolydays = null;
            sWeekdays = null;
            sShortWeekdays = null;
            Prefs.setLanguage(newLang);
            PackageManager pm = c.getPackageManager();

            pm.setComponentEnabledSetting(new ComponentName(c, "com.metinkale.prayer.aliasTR"), newLang.equals("tr") ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

            pm.setComponentEnabledSetting(new ComponentName(c, "com.metinkale.prayer.aliasEN"), newLang.equals("en") ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

            pm.setComponentEnabledSetting(new ComponentName(c, "com.metinkale.prayer.aliasDE"), newLang.equals("de") ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

            pm.setComponentEnabledSetting(new ComponentName(c, "com.metinkale.prayer.aliasDefault"), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

        }

    }

    public static String getHolyday(int which) {
        if (sHolydays == null) {
            sHolydays = App.getContext().getResources().getStringArray(R.array.holydays);
        }
        return sHolydays[which];
    }

    public static String getGregMonth(int which) {
        if (sGMonths == null) {
            sGMonths = App.getContext().getResources().getStringArray(R.array.months);
        }
        return sGMonths[which];
    }

    public static String getHijriMonth(int which) {
        if (sHMonths == null) {
            sHMonths = App.getContext().getResources().getStringArray(R.array.months_hicri);
        }
        return sHMonths[which];
    }

    public static String getWeekday(int which) {
        if (sWeekdays == null) {
            sWeekdays = App.getContext().getResources().getStringArray(R.array.week_days);
        }
        return sWeekdays[which];
    }

    public static String getShortWeekday(int which) {
        if (sShortWeekdays == null) {
            sShortWeekdays = App.getContext().getResources().getStringArray(R.array.week_days_short);
        }
        return sShortWeekdays[which];
    }

    public static String[] getAllHolydays() {
        return sHolydays;
    }

    public static boolean askLang(final Activity act) {
        if (Prefs.getLanguage() != null) {
            return false;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(act);
        builder.setTitle(R.string.language).setItems(R.array.language, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Prefs.setLanguage(act.getResources().getStringArray(R.array.language_val)[which]);
                init();
                act.finish();
                act.startActivity(new Intent(act, act.getClass()));
            }
        }).setCancelable(false);

        builder.show();
        return true;
    }

    public static String az(int i) {
        if (i < 10) {
            return "0" + i;
        }
        return i + "";
    }

    public static String getDateFormat(boolean hicri) {
        return hicri ? Prefs.getHDF() : Prefs.getDF();
    }


}
