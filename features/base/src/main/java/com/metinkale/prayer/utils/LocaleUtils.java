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

package com.metinkale.prayer.utils;

import android.app.UiModeManager;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.LocaleList;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.text.style.SuperscriptSpan;

import com.crashlytics.android.Crashlytics;
import com.metinkale.prayer.App;
import com.metinkale.prayer.HijriDate;
import com.metinkale.prayer.CalendarIntegrationService;
import com.metinkale.prayer.Preferences;
import com.metinkale.prayer.base.R;


import org.joda.time.DurationFieldType;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.ReadableInstant;
import org.joda.time.ReadablePartial;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.annotation.Size;
import androidx.core.os.LocaleListCompat;

import static android.content.Context.UI_MODE_SERVICE;

public class LocaleUtils {

    private static int[] GMONTHS =
            new int[]{R.string.gmonth1, R.string.gmonth2, R.string.gmonth3, R.string.gmonth4, R.string.gmonth5, R.string.gmonth6, R.string.gmonth7,
                    R.string.gmonth8, R.string.gmonth9, R.string.gmonth10, R.string.gmonth11, R.string.gmonth12};

    private static int[] HMONTHS =
            new int[]{R.string.hmonth1, R.string.hmonth2, R.string.hmonth3, R.string.hmonth4, R.string.hmonth5, R.string.hmonth6, R.string.hmonth7,
                    R.string.hmonth8, R.string.hmonth9, R.string.hmonth10, R.string.hmonth11, R.string.hmonth12};

    private static int[] HOLYDAYS =
            new int[]{R.string.holyday1, R.string.holyday2, R.string.holyday3, R.string.holyday4, R.string.holyday5, R.string.holyday6,
                    R.string.holyday7, R.string.holyday8, R.string.holyday9, R.string.holyday10, R.string.holyday11, R.string.holyday12,
                    R.string.holyday13, R.string.holyday14, R.string.holyday15, R.string.holyday16, R.string.holyday17, R.string.holyday18};


    private static final LocaleListCompat DEFAULT_LOCALES = LocaleListCompat.getDefault();

    public static LocaleListCompat getDefaultLocales() {
        return DEFAULT_LOCALES;
    }

    public static void init(@NonNull Context c) {
        initLocale(c);

        UiModeManager systemService = (UiModeManager) c.getSystemService(UI_MODE_SERVICE);
        if (systemService != null)
            systemService.setNightMode(UiModeManager.MODE_NIGHT_NO);


        int year = LocalDate.now().getYear();
        if (year == 2019) {
            year = 201904; // force update in 2019
        }

        if (year != Preferences.LAST_CAL_SYNC.get()) {
            CalendarIntegrationService.startCalendarIntegration(c);
            Preferences.LAST_CAL_SYNC.set(year);
        }

    }

    private static void initLocale(Context c) {
        Crashlytics.setString("lang", Preferences.LANGUAGE.get());
        Crashlytics.setString("digits", Preferences.DIGITS.get());
        Configuration config = new Configuration();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            LocaleList localeList = getLocales();
            LocaleList.setDefault(localeList);
            config.setLocales(localeList);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Locale locale = getLocale();
            config.setLocale(locale);
            Locale.setDefault(locale);
        } else {
            Locale locale = getLocale();
            config.locale = locale;
            Locale.setDefault(locale);
        }

        c.getResources().updateConfiguration(config, c.getResources().getDisplayMetrics());
    }


    public static CharSequence formatTimeForHTML(LocalTime localTime) {
        String time = formatTime(localTime);
        if (!Preferences.CLOCK_12H.get()) {
            return time;
        }
        int d = time.indexOf(" ");
        if (d < 0)
            return time;
        time = time.replace(" ", "");

        int s = time.length();
        Spannable span = new SpannableString(time);
        span.setSpan(new SuperscriptSpan(), d, s, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        span.setSpan(new RelativeSizeSpan(0.5f), d, s, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return span;
    }


    @NonNull
    public static String formatTime(LocalTime localTime) {
        String time = localTime == null ? "00:00" : localTime.toString("HH:mm");
        if (Preferences.CLOCK_12H.get() && time.contains(":")) {
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
        return formatNumber(time);

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @NonNull
    public static LocaleList getLocales() {
        return LocaleList.forLanguageTags(getLocalesCompat().toLanguageTags());
    }


    @NonNull
    public static LocaleListCompat getLocalesCompat() {
        if ("system".equals(Preferences.LANGUAGE.get()))
            return DEFAULT_LOCALES;

        Locale locale = LocaleUtils.getLocale();
        ArrayList<Locale> locales = new ArrayList<>(DEFAULT_LOCALES.size() + 1);
        locales.add(LocaleUtils.getLocale());
        for (int i = 0; i < DEFAULT_LOCALES.size(); i++) {
            Locale l = DEFAULT_LOCALES.get(i);
            if (!locales.contains(locale)) {
                locales.add(l);
            }
        }
        return LocaleListCompat.create(locales.toArray(new Locale[0]));
    }

    @NonNull
    public static Locale getLocale() {
        String language = Preferences.LANGUAGE.get();
        if ("system".equals(language))
            return DEFAULT_LOCALES.get(0);
        else
            return new Locale(language);
    }


    @NonNull
    public static String getLanguage(@Size(min = 1) String... allow) {
        Locale lang = LocaleUtils.getLocale();
        Locale[] locales = new Locale[allow.length];
        for (int i = 0; i < allow.length; i++) {
            locales[i] = new Locale(allow[i]);
        }

        for (int i = 0; i < locales.length; i++) {
            if (lang.getLanguage().equals(locales[i].getLanguage()))
                return allow[i];
        }

        return allow[0];
    }

    @NonNull
    public static String getHolyday(@IntRange(from = 1, to = 18) int which) {
        return App.get().getResources().getString(HOLYDAYS[which - 1]);
    }

    @NonNull
    private static String getGregMonth(@IntRange(from = 0, to = 11) int which) {
        if (Preferences.LANGUAGE.get().equals("system"))
            return new DateFormatSymbols(getLocale()).getMonths()[which];
        else
            return App.get().getResources().getString(GMONTHS[which]);
    }

    @NonNull
    private static String getHijriMonth(@IntRange(from = 0, to = 11) int which) {
        return App.get().getResources().getString(HMONTHS[which]);
    }


    @NonNull
    public static String az(int i) {
        if (i < 10) {
            return "0" + i;
        }
        return i + "";
    }

    @NonNull
    private static String getDateFormat(boolean hicri) {
        return hicri ? Preferences.HIJRI_DATE_FORMAT.get() : Preferences.DATE_FORMAT.get();
    }

    @NonNull
    public static String formatDate(@NonNull HijriDate date) {
        String format = getDateFormat(true);
        format = format.replace("DD", az(date.getDay(), 2));

        if (format.contains("MMM")) {
            try {
                format = format.replace("MMM", getHijriMonth(date.getMonth() - 1));

            } catch (ArrayIndexOutOfBoundsException ex) {
                Crashlytics.logException(ex);

                return "";
            }
        }
        format = format.replace("MM", az(date.getMonth(), 2));
        format = format.replace("YYYY", az(date.getYear(), 4));
        format = format.replace("YY", az(date.getYear(), 2));
        return formatNumber(format);
    }


    @NonNull
    public static String formatDate(@NonNull LocalDate date) {
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
        return formatNumber(format);
    }

    @NonNull
    private static String az(int Int, int num) {
        StringBuilder ret = new StringBuilder(Int + "");
        if (ret.length() < num) {
            for (int i = ret.length(); i < num; i++) {
                ret.insert(0, "0");
            }
        } else if (ret.length() > num) {
            ret = new StringBuilder(ret.substring(ret.length() - num, ret.length()));
        }

        return ret.toString();
    }


    @NonNull
    public static String formatNumber(@NonNull String str) {
        if (Preferences.DIGITS.get().equals("normal"))
            return str;
        char[] arabicChars = {'٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩'};
        if (Preferences.DIGITS.get().equals("farsi")) {
            arabicChars[4] = '۴';
            arabicChars[5] = '۵';
            arabicChars[6] = '۶';
        }

        str = str.replace("AM", "ص").replace("PM", "م");

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

    @NonNull
    public static String formatNumber(int nr) {
        return formatNumber(nr + "");
    }

    public static String formatNumber(double doub) {
        return formatNumber(String.format(LocaleUtils.getLocale(), "%f", doub));
    }

    public static class Translation {
        private final String language;
        private final int progress;

        public Translation(String language, int progress) {
            this.language = language;
            this.progress = progress;
        }

        public String getLanguage() {
            return language;
        }

        public int getProgress() {
            return progress;
        }

        public String getDisplayLanguage() {
            if (language.equals("system"))
                return App.get().getResources().getString(R.string.systemLanguage);
            if (language.equals("ku"))
                return "Kurdî";
            Locale locale = new Locale(language);
            return locale.getDisplayLanguage(locale);
        }

        public CharSequence getDisplayText() {
            if (getProgress() < 0)
                return getDisplayLanguage();
            else
                return Html.fromHtml(getDisplayLanguage() + "&nbsp;<small>(" + getProgress() + "%)</small>");
        }
    }

    public static List<Translation> getSupportedLanguages(Context c) {
        String[] languages = c.getResources().getStringArray(R.array.languages);
        List<Translation> translations = new ArrayList<>();
        for (String lang : languages) {
            int divider = lang.indexOf("|");
            int progress = Integer.parseInt(lang.substring(divider + 1));
            lang = lang.substring(0, divider);
            if (lang.equals("kur"))
                lang = "ku";
            if (progress > 40) {
                translations.add(new Translation(lang, progress));
            }
        }

        Collections.sort(translations, (t1, t2) -> -Integer.compare(t1.getProgress(), t2.getProgress()));

        translations.add(0, new Translation("system", -1));
        return translations;
    }

    private static final PeriodFormatter PERIOD_FORMATTER_HMS =
            new PeriodFormatterBuilder().printZeroIfSupported().minimumPrintedDigits(2).appendHours().appendLiteral(":").minimumPrintedDigits(2)
                    .appendMinutes().appendLiteral(":").appendSeconds().toFormatter();
    private static final PeriodFormatter PERIOD_FORMATTER_HM =
            new PeriodFormatterBuilder().printZeroIfSupported().minimumPrintedDigits(2).appendHours().appendLiteral(":").minimumPrintedDigits(2)
                    .appendMinutes().toFormatter();


    public static String formatPeriod(ReadableInstant from, ReadableInstant to) {
        return formatPeriod(new Period(from, to, PeriodType.dayTime()), false);
    }


    public static String formatPeriod(ReadablePartial from, ReadablePartial to) {
        return formatPeriod(new Period(from, to, PeriodType.dayTime()), false);
    }


    public static String formatPeriod(ReadableInstant from, ReadableInstant to, boolean showSecs) {
        return formatPeriod(new Period(from, to, PeriodType.dayTime()), showSecs);
    }


    public static String formatPeriod(ReadablePartial from, ReadablePartial to, boolean showSecs) {
        return formatPeriod(new Period(from, to, PeriodType.dayTime()), showSecs);
    }

    public static String formatPeriod(Period period, boolean showsecs) {
        if (showsecs) {

            return LocaleUtils.formatNumber(PERIOD_FORMATTER_HMS.print(period));
        } else if (Preferences.COUNTDOWN_TYPE.get().equals(Preferences.COUNTDOWN_TYPE_FLOOR)) {
            return LocaleUtils.formatNumber(PERIOD_FORMATTER_HM.print(period));
        } else {
            period = period.withFieldAdded(DurationFieldType.minutes(), 1);
            return LocaleUtils.formatNumber(PERIOD_FORMATTER_HM.print(period));
        }

    }

    public static String readableSize(int bytes) {
        int unit = 1024;
        if (bytes < unit)
            return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        char pre = "kMGTPE".charAt(exp - 1);
        return String.format(Locale.getDefault(), "%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

}
