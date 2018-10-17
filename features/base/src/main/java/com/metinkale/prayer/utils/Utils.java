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

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Size;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.text.style.SuperscriptSpan;
import android.util.DisplayMetrics;
import android.view.View;

import com.crashlytics.android.Crashlytics;
import com.metinkale.prayer.App;
import com.metinkale.prayer.HijriDate;
import com.metinkale.prayer.MainIntentService;
import com.metinkale.prayer.Prefs;
import com.metinkale.prayer.base.R;

import org.joda.time.LocalDate;

import java.text.DateFormatSymbols;
import java.util.Locale;

public class Utils {
    
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
    
    public static void init(@NonNull Context c) {
        Locale locale = Utils.getLocale();
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
        
        c.getResources().updateConfiguration(config, c.getResources().getDisplayMetrics());
    }
    
    
    public static CharSequence fixTimeForHTML(String time) {
        time = fixTime(time);
        if (!Prefs.use12H()) {
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
    
    
    @NonNull
    public static Locale getLocale() {
        String language = Prefs.getLanguage();
        if ("system".equals(language))
            return App.get().getSystemLocale();
        else
            return new Locale(language);
    }
    
    
    @NonNull
    public static String getLanguage(@Size(min = 1) String... allow) {
        Locale lang = Utils.getLocale();
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
    public static String getHolyday(@IntRange(from = 0, to = 17) int which) {
        return App.get().getResources().getString(HOLYDAYS[which]);
    }
    
    @NonNull
    public static String getGregMonth(@IntRange(from = 0, to = 11) int which) {
        if (Prefs.getLanguage().equals("system"))
            return new DateFormatSymbols(getLocale()).getMonths()[which];
        else
            return App.get().getResources().getString(GMONTHS[which]);
    }
    
    @NonNull
    public static String getHijriMonth(@IntRange(from = 0, to = 11) int which) {
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
        return hicri ? Prefs.getHijriDateFormat() : Prefs.getDateFormat();
    }
    
    @NonNull
    public static String format(@NonNull HijriDate date) {
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
    
    
    @NonNull
    public static String toArabicNrs(@NonNull String str) {
        if (Prefs.getDigits().equals("normal"))
            return str;
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
     * @param context Context to get resources and device specific display metrics
     * @param dp      A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float convertDpToPixel(Context context, float dp) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }
    
    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param context Context to get resources and device specific display metrics
     * @param px      A value in px (pixels) unit. Which we need to convert into db
     * @return A float value to represent dp equivalent to px value
     */
    public static float convertPixelsToDp(Context context, float px) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return dp;
    }
    
    public static boolean isRTL(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Configuration config = ctx.getResources().getConfiguration();
            return config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
        }
        return false;
    }
    
    public static String readableSize(int bytes) {
        int unit = 1024;
        if (bytes < unit)
            return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        char pre = "kMGTPE".charAt(exp - 1);
        return String.format(Locale.getDefault(), "%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
    
    public static boolean isPackageInstalled(Context c, String packagename) {
        try {
            c.getPackageManager().getPackageInfo(packagename, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
    
}
