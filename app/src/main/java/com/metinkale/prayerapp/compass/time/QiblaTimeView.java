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
 *
 */

package com.metinkale.prayerapp.compass.time;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

import com.metinkale.prayer.R;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.metinkale.praytimes.Constants;
import org.metinkale.praytimes.PrayTimes;
import org.metinkale.praytimes.QiblaTime;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class QiblaTimeView extends View {
    private final Paint mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mOuterStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mCenterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mNightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mYellowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mSunPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mTrianglePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Drawable mKaabe;
    private final PrayTimes mPrayTimes = new PrayTimes();
    private QiblaTime mQiblaTime;
    private double mSunriseAngle;
    private double mSunsetAngle;
    private double mCurrentAngle;
    private boolean mShowSun;
    private Path mTopPath = new Path();
    private Path mRightPath = new Path();
    private Path mLeftPath = new Path();
    private Path mBottomPath = new Path();
    private Path mClipPath = new Path();
    private double mQiblaAngle;

    public QiblaTimeView(@NonNull Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mKaabe = context.getResources().getDrawable(R.drawable.kaabe, null);
        } else {
            mKaabe = context.getResources().getDrawable(R.drawable.kaabe);
        }

        int blue = 0xFF33B5E5;

        mTrianglePaint.setColor(blue);
        mTrianglePaint.setStyle(Paint.Style.FILL);
        mBackgroundPaint.setColor(Color.WHITE);
        mBackgroundPaint.setStyle(Paint.Style.FILL);
        mOuterStrokePaint.setColor(blue);
        mOuterStrokePaint.setStyle(Paint.Style.STROKE);
        mCenterPaint.setColor(blue);
        mCenterPaint.setStyle(Paint.Style.FILL);
        mNightPaint.setColor(Color.BLACK);
        mNightPaint.setAlpha(150);
        mNightPaint.setStyle(Paint.Style.FILL);
        mYellowPaint.setColor(Color.YELLOW);
        mYellowPaint.setStyle(Paint.Style.STROKE);
        mTextPaint.setColor(Color.WHITE);
        mSunPaint.setColor(Color.YELLOW);
        mSunPaint.setStyle(Paint.Style.FILL);


        LocalDate date = LocalDate.now();
        mPrayTimes.setDate(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth());
    }

    public QiblaTimeView(@NonNull Context context, AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public QiblaTimeView(@NonNull Context context) {
        this(context, null);

    }

    @Override
    public void onMeasure(int widthSpec, int heightSpec) {
        int size = Math.min(MeasureSpec.getSize(widthSpec), MeasureSpec.getSize(heightSpec));

        setMeasuredDimension(size, size);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mOuterStrokePaint.setStrokeWidth(w / 30);
        mTextPaint.setTextSize(w / 20);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mYellowPaint.setStrokeWidth(w / 50);


        mTopPath.reset();
        mTopPath.moveTo(w / 2, w / 6);
        mTopPath.lineTo(w / 2 + w / 10, w / 30);
        mTopPath.lineTo(w / 2 - w / 10, w / 30);
        mTopPath.lineTo(w / 2, w / 6);

        mRightPath.reset();
        mRightPath.moveTo(w - w / 6, w / 2);
        mRightPath.lineTo(w - w / 30, w / 2 + w / 10);
        mRightPath.lineTo(w - w / 30, w / 2 - w / 10);
        mRightPath.lineTo(w - w / 6, w / 2);

        mLeftPath.reset();
        mLeftPath.moveTo(w / 6, w / 2);
        mLeftPath.lineTo(w / 30, w / 2 + w / 10);
        mLeftPath.lineTo(w / 30, w / 2 - w / 10);
        mLeftPath.lineTo(w / 6, w / 2);

        mBottomPath.reset();
        mBottomPath.moveTo(w / 2, w - w / 6);
        mBottomPath.lineTo(w / 2 + w / 10, w - w / 30);
        mBottomPath.lineTo(w / 2 - w / 10, w - w / 30);
        mBottomPath.lineTo(w / 2, w - w / 6);

        mClipPath.reset();
        mClipPath.addCircle(w / 2, w / 2, w * 0.45f, Path.Direction.CCW);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        int width = getWidth();
        int center = width / 2;

        canvas.drawCircle(center, center, (center * 19) / 20, mBackgroundPaint);

        int size = center / 10;
        //sun line
        if (mShowSun) {
            canvas.drawLine(center, center,
                    (float) (center - 0.9 * center * Math.cos(Math.toRadians(mCurrentAngle))),
                    (float) (center - 0.9 * center * Math.sin(Math.toRadians(mCurrentAngle))),
                    mYellowPaint);
            canvas.save();
            canvas.clipPath(mClipPath);
            canvas.drawCircle((float) (center - 0.85 * center * Math.cos(Math.toRadians(mCurrentAngle))),
                    (float) (center - 0.85 * center * Math.sin(Math.toRadians(mCurrentAngle))),
                    size, mSunPaint);
            canvas.restore();
        }

        float nightAngle = (float) -distance(mSunsetAngle, mSunriseAngle);
        canvas.drawArc(center - center * 0.9f, center - center * 0.9f, center + center * 0.9f,
                center + center * 0.9f, (float) -mSunsetAngle, nightAngle, true, mNightPaint);

        float sw = mYellowPaint.getStrokeWidth() / 2;
        canvas.drawArc(center - center * 0.9f + sw, center - center * 0.9f + sw, center + center * 0.9f - sw,
                center + center * 0.9f - sw,
                (float) -mSunsetAngle, nightAngle > 0 ? -(360 - nightAngle) : 360 + nightAngle, false, mYellowPaint);


        canvas.drawCircle(center, center, center / 10, mCenterPaint);

        int y = center - center / 2;
        mKaabe.setBounds(center - size, y - size, center + size, y + size);
        mKaabe.draw(canvas);

        canvas.drawCircle(center, center, (center * 19) / 20, mOuterStrokePaint);

        float textShift = center / 30;
        if (mQiblaTime.getFront() != null) {
            canvas.drawPath(mTopPath, mTrianglePaint);
            canvas.drawText(mQiblaTime.getFront(), center, center - center * 0.9f + textShift, mTextPaint);
        }
        if (mQiblaTime.getLeft() != null) {
            canvas.drawPath(mLeftPath, mTrianglePaint);
            canvas.drawText(mQiblaTime.getLeft(), center - center * 0.84f, center + textShift, mTextPaint);
        }
        if (mQiblaTime.getRight() != null) {
            canvas.drawPath(mRightPath, mTrianglePaint);
            canvas.drawText(mQiblaTime.getRight(), center + center * 0.84f, center + textShift, mTextPaint);
        }
        if (mQiblaTime.getBack() != null) {
            canvas.drawPath(mBottomPath, mTrianglePaint);
            canvas.drawText(mQiblaTime.getBack(), center, center + center * 0.9f + textShift, mTextPaint);
        }


    }

    public void setLocation(Location location, double qiblaAngle) {
        if (location == null) return;
        mPrayTimes.setCoordinates(location.getLatitude(), location.getLongitude(), 0);
        mQiblaAngle = qiblaAngle;
        mQiblaTime = mPrayTimes.getQiblaTime();
        LocalTime sunrise = LocalTime.parse(mPrayTimes.getTime(Constants.TIMES_SUNRISE));
        LocalTime sunset = LocalTime.parse(mPrayTimes.getTime(Constants.TIMES_SUNSET));
        LocalTime current = LocalTime.now();

        mShowSun = !(sunset.isBefore(current) || sunrise.isAfter(current));

        mSunriseAngle = Math.toDegrees(getAzimuth(sunrise.toDateTimeToday().getMillis(), location.getLatitude(), location.getLongitude())) - qiblaAngle - 90;
        mSunsetAngle = Math.toDegrees(getAzimuth(sunset.toDateTimeToday().getMillis(), location.getLatitude(), location.getLongitude())) - qiblaAngle - 90;
        mCurrentAngle = Math.toDegrees(getAzimuth(current.toDateTimeToday().getMillis(), location.getLatitude(), location.getLongitude())) - qiblaAngle - 90;
    }

    /**
     * Length (angular) of a shortest way between two angles.
     * It will be in range [0, 180].
     * Source: https://stackoverflow.com/questions/7570808/how-do-i-calculate-the-difference-of-two-angle-measures
     */
    private double distance(double a, double b) {
        double d = Math.abs(a - b) % 360;
        double r = d > 180 ? 360 - d : d;

        int sign = (a - b >= 0 && a - b <= 180) || (a - b <= -180 && a - b >= -360) ? 1 : -1;
        r *= sign;
        return r;
    }


    public static double getAzimuth(long mills, double lat, double lng) {
        //yes it's my library, but i did not want to make this class public for everyone :)
        //alternatively SunCalc library could be used
        try {
            Method m = Class.forName("org.metinkale.praytimes.QiblaTimeCalculator")
                    .getDeclaredMethod("getAzimuth", long.class, double.class, double.class);
            m.setAccessible(true);
            return (double) m.invoke(null, mills, lat, lng);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }
}