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

package com.metinkale.prayerapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
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
    private static String[] sGMonths;
    private static String[] sHMonths;
    private static String[] sHolydays;
    private static String[] sWeekdays;
    private static String[] sShortWeekdays;

    public static CharSequence fixTimeForHTML(String time) {
        time = fixTime(time);
        if (!Prefs.use12H()) {
            return time;
        }
        int d = time.indexOf(" ");
        if (d < 0) return time;
        time = time.replace(" ", "");

        int s = time.length();
        Spannable span = new SpannableString(time);
        span.setSpan(new SuperscriptSpan(), d, s, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        span.setSpan(new RelativeSizeSpan(0.5f), d, s, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return span;
    }


    public static String fixTime(String time) {
        if (Prefs.use12H() && time.contains(":")) {
            try {
                String fix = time.substring(0, time.indexOf(":"));
                String suffix = time.substring(time.indexOf(":"));


                int hour = Integer.parseInt(fix);
                if (hour == 0) {
                    time = "00" + suffix + " AM";
                } else if (hour < 12) {
                    time = az(hour) + suffix + " AM";
                } else if (hour == 12) {
                    time = "12" + suffix + " PM";
                } else {
                    time = az(hour - 12) + suffix + " PM";
                }
            } catch (Exception e) {
                Crashlytics.logException(e);
                return time;
            }
        }
        return toArabicNrs(time);

    }


    public static void init(Context c) {
        String newLang = Prefs.getLanguage();


        Crashlytics.setString("lang", newLang);
        Crashlytics.setString("digits", Prefs.getDigits());

        int year = LocalDate.now().getYear();

        if (year != Prefs.getLastCalSync()) {
            MainIntentService.startCalendarIntegration(c);
        }
        Prefs.setLastCalSync(year);

        if (newLang == null) {
            return;
        }
        Locale locale = new Locale(newLang);
        Configuration config = new Configuration();
        Locale.setDefault(locale);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(locale);
        } else {
            config.locale = locale;
        }

        c.getResources()
                .updateConfiguration(config,
                        c.getResources().getDisplayMetrics());


    }

    public static void changeLanguage(String language) {
        Context c = App.getContext();

        Prefs.setLanguage(language);

        sGMonths = null;
        sHMonths = null;
        sHolydays = null;
        sWeekdays = null;
        sShortWeekdays = null;
        PackageManager pm = c.getPackageManager();

        pm.setComponentEnabledSetting(new ComponentName(c, "com.metinkale.prayer.aliasTR"), "tr".equals(language) ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

        pm.setComponentEnabledSetting(new ComponentName(c, "com.metinkale.prayer.aliasAR"), "ar".equals(language) ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

        pm.setComponentEnabledSetting(new ComponentName(c, "com.metinkale.prayer.aliasEN"), "en".equals(language) ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

        pm.setComponentEnabledSetting(new ComponentName(c, "com.metinkale.prayer.aliasDE"), "de".equals(language) ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

        pm.setComponentEnabledSetting(new ComponentName(c, "com.metinkale.prayer.aliasDefault"), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

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
                changeLanguage(act.getResources().getStringArray(R.array.language_val)[which]);
                init(act);
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
            format = format.replace("MMM", getHijriMonth(date.Month - 1));

        } catch (ArrayIndexOutOfBoundsException ex) {
            Crashlytics.logException(ex);

            return "";
        }
        format = format.replace("MM", az(date.Month, 2));
        format = format.replace("YYYY", az(date.Year, 4));
        format = format.replace("YY", az(date.Year, 2));
        return toArabicNrs(format);
    }


    public static String format(LocalDate date) {
        String format = getDateFormat(true);
        format = format.replace("DD", az(date.getDayOfMonth(), 2));

        try {
            format = format.replace("MMM", getGregMonth(date.getMonthOfYear() - 1));


        } catch (ArrayIndexOutOfBoundsException ex) {
            Crashlytics.logException(ex);

            return "";
        }
        format = format.replace("MM", az(date.getMonthOfYear(), 2));
        format = format.replace("YYYY", az(date.getYear(), 4));
        format = format.replace("YY", az(date.getYear(), 2));
        return toArabicNrs(format);
    }

    private static String az(int Int, int num) {
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
        return Prefs.getLanguage() + ASSETS[pos - 1];
    }


    public static String toArabicNrs(String str) {
        if (str == null) return null;
        if (Prefs.getDigits().equals("normal")) return str;
        char[] arabicChars = {'٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩'};
        if (Prefs.getDigits().equals("farsi")) {
            arabicChars[4] = '۴';
            arabicChars[5] = '۵';
            arabicChars[6] = '۶';
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            if (Character.isDigit(str.charAt(i))) {
                builder.append(arabicChars[(int) (str.charAt(i)) - 48]);
            } else {
                builder.append(str.charAt(i));
            }
        }
        return builder.toString();
    }

    public static String toArabicNrs(int nr) {
        return toArabicNrs(nr + "");
    }
}
