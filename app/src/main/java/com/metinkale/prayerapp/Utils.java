package com.metinkale.prayerapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.text.style.SuperscriptSpan;
import com.crashlytics.android.Crashlytics;
import com.metinkale.prayer.R;
import com.metinkale.prayerapp.settings.Prefs;
import org.joda.time.LocalDate;

import java.util.Locale;

public class Utils {
    private static final String[] ASSETS = {"/dinigunler/hicriyil.html", "/dinigunler/asure.html", "/dinigunler/mevlid.html", "/dinigunler/3aylar.html", "/dinigunler/regaib.html", "/dinigunler/mirac.html", "/dinigunler/berat.html", "/dinigunler/ramazan.html", "/dinigunler/kadir.html", "/dinigunler/arefe.html", "/dinigunler/ramazanbay.html", "/dinigunler/ramazanbay.html", "/dinigunler/ramazanbay.html", "/dinigunler/arefe.html", "/dinigunler/kurban.html", "/dinigunler/kurban.html", "/dinigunler/kurban.html", "/dinigunler/kurban.html"};
    private static String[] sGMonths, sHMonths, sHolydays, sWeekdays, sShortWeekdays;

    public static CharSequence fixTimeForHTML(String time) {
        if (!Prefs.use12H()) return new SpannableString(time);
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
        if (Prefs.use12H()) try {
            String fix = time.substring(0, time.indexOf(":"));
            String suffix = time.substring(time.indexOf(":"));


            int hour = Integer.parseInt(fix);
            if (hour == 0) return "00" + suffix + " AM";
            else if (hour < 12) return az(hour) + suffix + " AM";
            else if (hour == 12) {
                return "12" + suffix + " PM";
            } else return az(hour - 12) + suffix + " PM";
        } catch (Exception e) {
            Crashlytics.logException(e);
            return time;
        }
        return time;

    }


    public static void init() {
        String newLang = Prefs.getLanguage();
        Context c = App.getContext();


        int year = LocalDate.now().getYear();

        if (year != Prefs.getLastCalIntegration()) {
            MainIntentService.startCalendarIntegration(c);
        }

        if (newLang == null) return;
        Locale locale = new Locale(newLang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        ((ContextWrapper) App.getContext()).getBaseContext().getResources().updateConfiguration(config, ((ContextWrapper) App.getContext()).getBaseContext().getResources().getDisplayMetrics());

        if (Prefs.getLanguage() != newLang) {
            sGMonths = null;
            sHMonths = null;
            sHolydays = null;
            sWeekdays = null;
            sShortWeekdays = null;
            Prefs.setLanguage(newLang);
            PackageManager pm = c.getPackageManager();

            pm.setComponentEnabledSetting(new ComponentName(c, "com.metinkale.prayer.aliasTR"), "tr".equals(newLang) ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

            pm.setComponentEnabledSetting(new ComponentName(c, "com.metinkale.prayer.aliasEN"), "en".equals(newLang) ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

            pm.setComponentEnabledSetting(new ComponentName(c, "com.metinkale.prayer.aliasDE"), "de".equals(newLang) ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

            pm.setComponentEnabledSetting(new ComponentName(c, "com.metinkale.prayer.aliasDefault"), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

        }

    }

    public static String getHolyday(int which) {
        if (sHolydays == null) {
            sHolydays = App.getContext().getResources().getStringArray(R.array.holydays);
        }
        return sHolydays[which];
    }

    public static CharSequence getGregMonth(int which) {
        if (sGMonths == null) {
            sGMonths = App.getContext().getResources().getStringArray(R.array.months);
        }
        return sGMonths[which];
    }

    public static CharSequence getHijriMonth(int which) {
        if (sHMonths == null) {
            sHMonths = App.getContext().getResources().getStringArray(R.array.months_hicri);
        }
        return sHMonths[which];
    }

    public static CharSequence getWeekday(int which) {
        if (sWeekdays == null) {
            sWeekdays = App.getContext().getResources().getStringArray(R.array.week_days);
        }
        return sWeekdays[which];
    }

    public static CharSequence getShortWeekday(int which) {
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

    private static String getDateFormat(boolean hicri) {
        return hicri ? Prefs.getHDF() : Prefs.getDF();
    }

    public static String format(HicriDate date) {
        String format = getDateFormat(true);
        format = format.replace("DD", az(date.Day, 2));

        try {
            format = format.replace("MMM", Utils.getHijriMonth(date.Month));

        } catch (ArrayIndexOutOfBoundsException ex) {
            Crashlytics.logException(ex);

            return "";
        }
        format = format.replace("MM", az(date.Month, 2));
        format = format.replace("YYYY", az(date.Year, 4));
        format = format.replace("YY", az(date.Year, 2));
        return format;
    }


    public static String format(LocalDate date) {
        String format = getDateFormat(true);
        format = format.replace("DD", az(date.getDayOfMonth(), 2));

        try {
            format = format.replace("MMM", Utils.getGregMonth(date.getMonthOfYear()));


        } catch (ArrayIndexOutOfBoundsException ex) {
            Crashlytics.logException(ex);

            return "";
        }
        format = format.replace("MM", az(date.getMonthOfYear(), 2));
        format = format.replace("YYYY", az(date.getYear(), 4));
        format = format.replace("YY", az(date.getYear(), 2));
       return format;
    }

    private static CharSequence az(int Int, int num) {
        String ret = Int + "";
        if (ret.length() < num) {
            for (int i = ret.length(); i < num; i++) {
                ret = "0" + ret;
            }
        } else if (ret.length() > num) {
            ret = ret.substring(ret.length() - num, ret.length());
        }

        return ret;
    }

    public static String getAssetForHolyday(int pos) {
        return Prefs.getLanguage() + ASSETS[pos];
    }


}
