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

package com.metinkale.prayerapp.compass._2D;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import com.metinkale.prayer.R;

public class CompassView extends View {
    private final Path mPath = new Path();
    private final Paint mPaint = new Paint();
    private final Drawable mKaabe;
    private float mAngle = -80;
    private float mqAngle;

    public CompassView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mKaabe = context.getResources().getDrawable(R.drawable.kaabe);

    }

    public CompassView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public CompassView(Context context) {
        this(context, null);

    }

    @Override
    public void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
        int size = Math.min(getMeasuredWidth(), getMeasuredHeight());

        int center = size / 2;

        mPath.reset();
        mPath.setFillType(Path.FillType.EVEN_ODD);
        mPath.moveTo(center, center / 8);

        mPath.lineTo(center * 15 / 20, center / 3);

        mPath.lineTo(center, center / 4);

        mPath.lineTo(center * 25 / 20, center / 3);
        mPath.close();

        setMeasuredDimension(size, size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width = getWidth();
        int center = width / 2;

        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setAntiAlias(true);

        mPaint.setStrokeWidth(center / 15);

        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawCircle(center, center, center * 19 / 20, mPaint);
        mPaint.setStyle(Paint.Style.STROKE);

        mPaint.setColor(0xFF33B5E5);
        canvas.drawCircle(center, center, center * 19 / 20, mPaint);
        mPaint.setStrokeWidth(1);

        mPaint.setColor(Color.BLACK);

        mPaint.setTextSize(center * 2 / 5);

        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawText(Math.round(getAngle()) + "°", center, center + center / 5, mPaint);
        mPaint.setStyle(Paint.Style.STROKE);

        canvas.rotate(-mAngle, center, center);

        mPaint.setColor(Color.GRAY);

        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawPath(mPath, mPaint);
        mPaint.setStyle(Paint.Style.STROKE);

        mPaint.setTextSize(center / 5);

        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawText("N", center, center * 9 / 20, mPaint);
        mPaint.setStyle(Paint.Style.STROKE);

        canvas.rotate(mqAngle, center, center);

        if (mqAngle != 0) {
            int y = center * 9 / 20;
            int size = center / 8;
            mKaabe.setBounds(center - size, y - size, center + size, y + size);
            mKaabe.draw(canvas);

            mPaint.setColor(Color.BLACK);

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