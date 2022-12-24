/*
 * Copyright (c) 2013-2019 Metin Kale
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

package com.metinkale.prayer.compass.time;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.metinkale.prayer.compass.R;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.metinkale.praytimes.PrayTimes;
import org.metinkale.praytimes.QiblaTime;
import org.metinkale.praytimes.Times;

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
    private final Path mTopPath = new Path();
    private final Path mRightPath = new Path();
    private final Path mLeftPath = new Path();
    private final Path mBottomPath = new Path();
    private final Path mClipPath = new Path();
    private double mQiblaAngle;
    private double mLat;
    private double mLng;
    private double mAlt;

    public QiblaTimeView(@NonNull Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mKaabe = ContextCompat.getDrawable(context, R.drawable.kaabe);

        int blue = getResources().getColor(R.color.colorPrimary);

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

        mOuterStrokePaint.setStrokeWidth(w / 30f);
        mTextPaint.setTextSize(w / 20f);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mYellowPaint.setStrokeWidth(w / 50f);


        mTopPath.reset();
        mTopPath.moveTo(w / 2f, w / 6f);
        mTopPath.lineTo(w / 2f + w / 10f, w / 30f);
        mTopPath.lineTo(w / 2f - w / 10f, w / 30f);
        mTopPath.lineTo(w / 2f, w / 6f);

        mRightPath.reset();
        mRightPath.moveTo(w - w / 6f, w / 2f);
        mRightPath.lineTo(w - w / 30f, w / 2f + w / 10f);
        mRightPath.lineTo(w - w / 30f, w / 2f - w / 10f);
        mRightPath.lineTo(w - w / 6f, w / 2f);

        mLeftPath.reset();
        mLeftPath.moveTo(w / 6f, w / 2f);
        mLeftPath.lineTo(w / 30f, w / 2f + w / 10f);
        mLeftPath.lineTo(w / 30f, w / 2f - w / 10f);
        mLeftPath.lineTo(w / 6f, w / 2f);

        mBottomPath.reset();
        mBottomPath.moveTo(w / 2f, w - w / 6f);
        mBottomPath.lineTo(w / 2f + w / 10f, w - w / 30f);
        mBottomPath.lineTo(w / 2f - w / 10f, w - w / 30f);
        mBottomPath.lineTo(w / 2f, w - w / 6f);

        mClipPath.reset();
        mClipPath.addCircle(w / 2f, w / 2f, w * 0.45f, Path.Direction.CCW);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        int width = getWidth();
        int center = width / 2;

        canvas.drawCircle(center, center, (center * 19) / 20f, mBackgroundPaint);

        int size = center / 10;
        //sun line
        if (mShowSun) {
            canvas.drawLine(center, center, (float) (center - 0.9 * center * Math.cos(Math.toRadians(mCurrentAngle))),
                    (float) (center - 0.9 * center * Math.sin(Math.toRadians(mCurrentAngle))), mYellowPaint);
            canvas.save();
            canvas.clipPath(mClipPath);
            canvas.drawCircle((float) (center - 0.85 * center * Math.cos(Math.toRadians(mCurrentAngle))),
                    (float) (center - 0.85 * center * Math.sin(Math.toRadians(mCurrentAngle))), size, mSunPaint);
            canvas.restore();
        }

        float nightAngle = (float) (mSunriseAngle - mSunsetAngle);
        canvas.drawArc(new RectF(center - center * 0.9f, center - center * 0.9f, center + center * 0.9f, center + center * 0.9f),
                (float) -mSunriseAngle + 90, nightAngle, true, mNightPaint);

        float sw = mYellowPaint.getStrokeWidth() / 2;
        canvas.drawArc(new RectF(center - center * 0.9f + sw, center - center * 0.9f + sw, center + center * 0.9f - sw, center + center * 0.9f - sw),
                (float) -mSunriseAngle + 90, -(360 - nightAngle), false, mYellowPaint);


        canvas.drawCircle(center, center, center / 10f, mCenterPaint);

        int y = center - center / 2;
        mKaabe.setBounds(center - size, y - size, center + size, y + size);
        mKaabe.draw(canvas);

        canvas.drawCircle(center, center, (center * 19) / 20f, mOuterStrokePaint);

        float textShift = center / 30f;
        if (mQiblaTime != null) {
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


    }

    public void setAngle(double angle) {
        mQiblaAngle = angle;
        post(mUpdate);
    }

    public void setLocation(double lat, double lng, double alt) {
        mLat = lat;
        mLng = lng;
        mAlt = alt;
        post(mUpdate);
    }

    private final Runnable mUpdate = new Runnable() {
        @Override
        public void run() {
            removeCallbacks(this);
            mPrayTimes.setCoordinates(mLat, mLng, mAlt);
            mQiblaTime = mPrayTimes.getQiblaTime();
            LocalTime sunrise = LocalTime.parse(mPrayTimes.getTime(Times.Sunrise));
            LocalTime sunset = LocalTime.parse(mPrayTimes.getTime(Times.Sunset));
            LocalTime current = LocalTime.now();

            mShowSun = !(sunset.isBefore(current) || sunrise.isAfter(current));

            mSunriseAngle = Math.toDegrees(getAzimuth(sunrise.toDateTimeToday().getMillis(), mLat, mLng)) - mQiblaAngle - 90;
            mSunsetAngle = Math.toDegrees(getAzimuth(sunset.toDateTimeToday().getMillis(), mLat, mLng)) - mQiblaAngle - 90;
            mCurrentAngle = Math.toDegrees(getAzimuth(current.toDateTimeToday().getMillis(), mLat, mLng)) - mQiblaAngle - 90;

            while (mSunriseAngle < 0) mSunriseAngle += 360;
            while (mSunsetAngle < 0) mSunsetAngle += 360;
            while (mSunriseAngle >= 360) mSunriseAngle -= 360;
            while (mSunsetAngle >= 360) mSunsetAngle -= 360;
            if (mSunsetAngle > mSunriseAngle) mSunsetAngle -= 360;


            invalidate();
        }
    };


    public static double getAzimuth(long mills, double lat, double lng) {
        //yes it's my library, but i did not want to make this class public for everyone :)
        //alternatively SunCalc library could be used, if prayer times are not needed
        try {
            Method m = Class.forName("org.metinkale.praytimes.QiblaTimeCalculator")
                    .getDeclaredMethod("getAzimuth", long.class, double.class, double.class);
            m.setAccessible(true);
            return (double) m.invoke(null, mills, lat, lng);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }
}