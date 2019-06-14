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

package com.metinkale.prayer.compass.magnetic.compass2D;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.annotation.NonNull;

import android.util.AttributeSet;
import android.view.View;

import com.metinkale.prayer.compass.R;
import com.metinkale.prayer.utils.LocaleUtils;
import com.metinkale.prayer.utils.Utils;

public class CompassView extends View {
    private final Path mPath = new Path();
    private final Paint mPaint = new Paint();
    private final Drawable mKaabe;
    private float mAngle = -80;
    private float mqAngle;

    private int mBGColor;
    private int mTextColor;
    private int m2ndTextColor;
    private int mStrokeColor;

    public CompassView(@NonNull Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mKaabe = getResources().getDrawable(R.drawable.kaabe, null);
        } else {
            mKaabe = getResources().getDrawable(R.drawable.kaabe);
        }

        mBGColor = Color.WHITE;
        mTextColor = Color.BLACK;
        m2ndTextColor = Color.GRAY;
        mStrokeColor = getResources().getColor(R.color.colorPrimary);

    }

    public CompassView(@NonNull Context context, AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public CompassView(@NonNull Context context) {
        this(context, null);

    }

    @Override
    public void onMeasure(int widthSpec, int heightSpec) {
        int size = Math.min(MeasureSpec.getSize(widthSpec), MeasureSpec.getSize(heightSpec));

        int center = size / 2;

        mPath.reset();
        mPath.setFillType(Path.FillType.EVEN_ODD);
        mPath.moveTo(center, center / 8f);

        mPath.lineTo((center * 15) / 20f, center / 3f);

        mPath.lineTo(center, center / 4f);

        mPath.lineTo((center * 25) / 20f, center / 3f);
        mPath.close();

        setMeasuredDimension(size, size);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        int width = getWidth();
        int center = width / 2;

        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setAntiAlias(true);

        mPaint.setStrokeWidth(center / 15f);

        mPaint.setColor(mBGColor);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawCircle(center, center, (center * 19) / 20f, mPaint);
        mPaint.setStyle(Paint.Style.STROKE);

        mPaint.setColor(mStrokeColor);
        canvas.drawCircle(center, center, (center * 19) / 20f, mPaint);
        mPaint.setStrokeWidth(1);

        mPaint.setColor(mTextColor);

        mPaint.setTextSize((center * 2) / 5f);

        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawText(LocaleUtils.formatNumber(Math.round(getAngle())) + "°", center, center + (center / 5f), mPaint);
        mPaint.setStyle(Paint.Style.STROKE);

        canvas.rotate(-mAngle, center, center);

        mPaint.setColor(m2ndTextColor);

        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawPath(mPath, mPaint);
        mPaint.setStyle(Paint.Style.STROKE);

        mPaint.setTextSize(center / 5f);

        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawText("N", center, (center * 9) / 20f, mPaint);
        mPaint.setStyle(Paint.Style.STROKE);

        canvas.rotate(mqAngle, center, center);

        if (mqAngle != 0) {
            int y = (center * 9) / 20;
            int size = center / 8;

            mKaabe.setBounds(center - size, y - size, center + size, y + size);
            mKaabe.draw(canvas);

            mPaint.setColor(mTextColor);

            mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            canvas.drawPath(mPath, mPaint);
            mPaint.setStyle(Paint.Style.STROKE);

        }

    }

    float getAngle() {
        float angle = mAngle;
        if (angle < 0) {
            angle += 360;
        }
        return angle;

    }

    public void setAngle(float rot) {
        mAngle = rot;
        invalidate();
    }

    public float getQiblaAngle() {
        float angle = mqAngle;
        if (angle < 0) {
            angle += 360;
        }
        return angle;
    }

    public void setQiblaAngle(int qiblaAngle) {
        mqAngle = qiblaAngle;
        invalidate();
    }
}