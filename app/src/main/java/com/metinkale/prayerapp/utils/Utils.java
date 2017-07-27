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

package com.metinkale.prayerapp.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Size;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.text.style.SuperscriptSpan;
import android.util.DisplayMetrics;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.metinkale.prayer.R;
import com.metinkale.prayerapp.App;
import com.metinkale.prayerapp.HicriDate;
import com.metinkale.prayerapp.MainIntentService;
import com.metinkale.prayerapp.settings.Prefs;

import org.joda.time.LocalDate;

import java.util.Arrays;
import java.util.Locale;

public class Utils {
    private static final String[] ASSETS = {"/dinigunler/hicriyil.html", "/dinigunler/asure.html", "/dinigunler/mevlid.html", "/dinigunler/3aylar.html", "/dinigunler/regaib.html", "/dinigunler/mirac.html", "/dinigunler/berat.html", "/dinigunler/ramazan.html", "/dinigunler/kadir.html", "/dinigunler/arefe.html", "/dinigunler/ramazanbay.html", "/dinigunler/ramazanbay.html", "/dinigunler/ramazanbay.html", "/dinigunler/arefe.html", "/dinigunler/kurban.html", "/dinigunler/kurban.html", "/dinigunler/kurban.html", "/dinigunler/kurban.html"};
    @Nullable
    private static String[] sGMonths;
    @Nullable
    private static String[] sHMonths;
    @Nullable
    private static String[] sHolydays;

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


    @NonNull
    public static String fixTime(@NonNull String time) {
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


    public static void init(@NonNull Context c) {
        Locale locale = getLocale();
        Crashlytics.setString("lang", locale.getLanguage());
        Crashlytics.setString("digits", Prefs.getDigits());

        int year = LocalDate.now().getYear();

        if (year != Prefs.getLastCalSync()) {
            MainIntentService.startCalendarIntegration(c);
            Prefs.setLastCalSync(year);
        }


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


    @NonNull
    public static Locale getLocale() {
        String language = Prefs.getLanguage();
        if ("system".equals(language)) return App.get().getSystemLocale();
        else return new Locale(language);
    }

    public static void changeLanguage(String language) {
        Context c = App.get();

        if (language == null || language.isEmpty()) language = "system";
        Prefs.setLanguage(language);

        sGMonths = null;
        sHMonths = null;
        sHolydays = null;
        PackageManager pm = c.getPackageManager();
        String[] languages = App.get().getResources().getStringArray(R.array.language_val);
        boolean hasEnabledActivity = false;
        for (String lang : languages) {
            if (lang.equals("system")) continue;

            if (lang.equals(language)) {
                pm.setComponentEnabledSetting(
                        new ComponentName(c, "com.metinkale.prayer.alias" + lang.toUpperCase(Locale.ENGLISH)),
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                hasEnabledActivity = true;
            } else {
                pm.setComponentEnabledSetting(
                        new ComponentName(c, "com.metinkale.prayer.alias" + lang.toUpperCase(Locale.ENGLISH)),
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            }
        }

        pm.setComponentEnabledSetting(new ComponentName(c, "com.metinkale.prayer.aliasDefault"),
                hasEnabledActivity ?
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED :
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

    }


    @NonNull
    public static String getLanguage(@Size(min = 1) String... allow) {
        Locale lang = Utils.getLocale();
        Locale[] locales = new Locale[allow.length];
        for (int i = 0; i < allow.length; i++) {
            locales[i] = new Locale(allow[i]);
        }

        for (int i = 0; i < locales.length; i++) {
            if (lang.equals(locales[i])) return allow[i];
        }

        return allow[0];
    }

    @Nullable
    public static String getHolyday(int which) {
        if (sHolydays == null) {
            sHolydays = App.get().getResources().getStringArray(R.array.holydays);
        }
        return sHolydays[which];
    }

    @Nullable
    public static String getGregMonth(int which) {
        if (sGMonths == null) {
            sGMonths = App.get().getResources().getStringArray(R.array.months);
        }
        return sGMonths[which];
    }

    @Nullable
    public static String getHijriMonth(int which) {
        if (sHMonths == null) {
            sHMonths = App.get().getResources().getStringArray(R.array.months_hicri);
        }
        return sHMonths[which];
    }


    @NonNull
    public static String az(int i) {
        if (i < 10) {
            return "0" + i;
        }
        return i + "";
    }

    @Nullable
    private static String getDateFormat(boolean hicri) {
        return hicri ? Prefs.getHDF() : Prefs.getDF();
    }

    @Nullable
    public static String format(@NonNull HicriDate date) {
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


    @Nullable
    public static String format(@NonNull LocalDate date) {
        String format = getDateFormat(false);
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

    @NonNull
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

    @Nullable
    public static String getAssetForHolyday(int pos) {
        return Utils.getLanguage("en", "de", "tr") + ASSETS[pos - 1];
    }


    @NonNull
    public static String toArabicNrs(@NonNull String str) {
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

    @Nullable
    public static String toArabicNrs(int nr) {
        return toArabicNrs(nr + "");
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp      A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px      A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent dp equivalent to px value
     */
    public static float convertPixelsToDp(float px, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return dp;
    }
}
