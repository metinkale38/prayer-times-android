package com.metinkale.prayer.utils;

import android.app.Notification;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;


public class MyNotificationBuilder {

    private static final String COLOR_SEARCH_1ST = "COLOR_SEARCH_1ST";
    private static final String COLOR_SEARCH_2ND = "COLOR_SEARCH_2ND";
    private static Integer COLOR_1ST = null;
    private static Integer COLOR_2ND = null;



    private static Bitmap getIconFromMinutes(Context ctx, long left) {
        String text = left + "";
        Resources r = ctx.getResources();
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, r.getDisplayMetrics());
        Bitmap b = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_4444);
        Canvas c = new Canvas(b);
        Paint paint = new Paint();
        final float testTextSize = 48f;
        paint.setTextSize(testTextSize);
        Rect bounds = new Rect();
        paint.getTextBounds(text.length() == 1 ? "0" + text : text, 0, text.length() == 1 ? 2 : text.length(), bounds);
        float desiredTextSize = testTextSize * (px * 0.9f) / bounds.width();
        paint.setTextSize(desiredTextSize);
        paint.setColor(0xFFFFFFFF);
        paint.setTextAlign(Paint.Align.CENTER);
        int yPos = (int) ((c.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2));
        c.drawText(text, px / 2, yPos, paint);
        c.drawText(text, px / 2, yPos, paint);
        return b;
    }


    private static boolean recurseGroup(@NonNull ViewGroup gp) {
        int count = gp.getChildCount();
        for (int i = 0; i < count; ++i) {
            View v = gp.getChildAt(i);
            if (v instanceof TextView) {
                TextView text = (TextView) v;
                String szText = text.getText().toString();
                if (COLOR_SEARCH_1ST.equals(szText)) {
                    COLOR_1ST = text.getCurrentTextColor();
                }
                if (COLOR_SEARCH_2ND.equals(szText)) {
                    COLOR_2ND = text.getCurrentTextColor();
                }

                if ((COLOR_1ST != null) && (COLOR_2ND != null)) {
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

    private static void extractColors(Context ctx) {
        if (COLOR_1ST != null && COLOR_2ND != null) {
            return;
        }


        try {
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(ctx);
            mBuilder.setContentTitle(COLOR_SEARCH_1ST)
                    .setContentText(COLOR_SEARCH_2ND);
            Notification ntf = mBuilder.build();
            LinearLayout group = new LinearLayout(ctx);
            ViewGroup event = (ViewGroup) ntf.contentView.apply(ctx, group);
            recurseGroup(event);
            group.removeAllViews();
        } catch (Exception e) {
            //  e.printStackTrace();
        }
        if (COLOR_1ST == null) {
            COLOR_1ST = Color.BLACK;
        }
        if (COLOR_2ND == null) {
            COLOR_2ND = Color.DKGRAY;
        }
    }


}
