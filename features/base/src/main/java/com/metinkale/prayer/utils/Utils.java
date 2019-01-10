package com.metinkale.prayer.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.View;

import com.metinkale.prayer.Preferences;

import org.joda.time.DurationFieldType;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.util.Locale;

public class Utils {
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
    
  
    
    public static boolean isPackageInstalled(Context c, String packagename) {
        try {
            c.getPackageManager().getPackageInfo(packagename, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
    
  
    
    
}
