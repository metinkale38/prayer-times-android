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

package com.metinkale.prayerapp.compass._3D;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

import com.metinkale.prayer.R;
import com.metinkale.prayerapp.Utils;

public class CompassView extends View {
    private float mX;
    private float mY;
    private float mZ;
    private double mqAngle;
    private float mqDist;
    @NonNull
    private Paint mPaint = new Paint();
    private Drawable mKaabe;
    @NonNull
    private RectF mRectF = new RectF();
    @NonNull
    private Path mPath = new Path();

    public CompassView(@NonNull Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mKaabe = context.getResources().getDrawable(R.drawable.kaabe);

    }

    public CompassView(@NonNull Context context, AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public CompassView(@NonNull Context context) {
        this(context, null);

    }

    @Override
    public void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
        int w = getMeasuredWidth();

        int center = w / 2;

        mPath.reset();
        mPath.setFillType(Path.FillType.EVEN_ODD);
        mPath.moveTo(center, (center / 8) + (center / 2));

        mPath.lineTo((center * 15) / 20, center / 3 + center / 2);

        mPath.lineTo(center, (center / 4) + (center / 2));

        mPath.lineTo((center * 25) / 20, center / 3 + center / 2);
        mPath.close();

    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        int w = getWidth();
        int h = getHeight();

        float cx = w / 2;
        float cy = h + (w / 2.5f);
        float cp = (w * 2) / 3;

        mPaint.setStyle(Style.FILL);
        mPaint.setColor(0xAAFFFFFF);
        canvas.drawCircle(cx, cy, cp, mPaint);

        mPaint.setStyle(Style.STROKE);
        mPaint.setStrokeWidth(w * 0.03f);
        mPaint.setColor(0xFF000000);
        mRectF.set(cx - cp, cy - cp, cx + cp, cy + cp);
        canvas.drawArc(mRectF, -mZ - 95, 10, false, mPaint);

        mPaint.setStyle(Style.FILL);
        mPaint.setColor(0xFF000000);
        mPaint.setTextSize(w / 10);
        mPaint.setTextAlign(Align.CENTER);
        canvas.drawText(Utils.toArabicNrs(Math.round(mX)) + "°", w / 2, h * 0.9f, mPaint);

        mPaint.setTextSize(w / 12);
        mPaint.setTextAlign(Align.RIGHT);
        canvas.drawText(Utils.toArabicNrs(Math.round(mqDist)) + "km", w * 0.45f, h * 0.98f, mPaint);

        mPaint.setTextAlign(Align.LEFT);
        canvas.drawText(Utils.toArabicNrs((int) Math.round(mqAngle)) + "°", w * 0.55f, h * 0.98f, mPaint);

        if (mX > 180) {
            mX -= 360;
        }

        if (mY > 180) {
            mY -= 360;
        }

        if (mqAngle != 0) {
            canvas.translate(0, -0.05f * h);

            int dist = (int) Math.sqrt(Math.pow(Math.abs(getAngle() - mqAngle), 2) + (mY * mY));

            if (dist > 30) {
                float a = getAngle();
                while (Math.abs(mqAngle - (a + 360)) < Math.abs(mqAngle - a)) {
                    a += 360;
                }

                while (Math.abs(mqAngle - (a - 360)) < Math.abs(mqAngle - a)) {
                    a -= 360;
                }

                double r = -Math.toDegrees(Math.atan2(Math.toRadians(a - mqAngle), -Math.toRadians(mY)));
                canvas.rotate((float) r, w / 2, h / 2);
                mPaint.setColor(Color.BLACK);

                mPaint.setStyle(Paint.Style.FILL);
                canvas.drawPath(mPath, mPaint);
                mPaint.setStyle(Paint.Style.STROKE);

                canvas.rotate((float) -r, w / 2, h / 2);

            }

            mPaint.setStyle(Style.FILL);

            if (dist <= 25) {
                mPaint.setColor(0xFFFFFFFF);
                mPaint.setAlpha((int) (170 - (dist * 6.8)));

                canvas.drawCircle(w / 2, h / 2, w * 0.45f, mPaint);

                mPaint.setAlpha(255);
            }

            canvas.translate((-mX * w) / 45, mY * h / 45);

            mPaint.setTextSize(w / 5);

            mPaint.setColor(0xAAFFFFFF);
            canvas.drawCircle(w / 2, (h / 2) - (w / 15), w / 5, mPaint);

            mPaint.setTextAlign(Align.CENTER);
            mPaint.setColor(0xFF000000);
            canvas.drawText("N", w / 2, h / 2, mPaint);

            canvas.translate((int) ((mqAngle * w) / 45), 0);
            int kw = (w / 4) - ((dist * w) / 180);
            if (kw > 0) {
                mKaabe.setBounds((w / 2) - kw, h / 2 - kw, w / 2 + kw, h / 2 + kw);
                mKaabe.draw(canvas);
            }

        }

    }

    int getAngle() {
        int angle = (int) mX;
        while (angle < 0) {
            angle += 360;
        }

        while (angle > 360) {
            angle -= 360;
        }
        return angle;

    }

    public void setAngle(float x, float y, float z) {
        mX = x;
        mY = y;
        mZ = z;
        invalidate();
    }

    public void setQibla(double qiblaAngle, float dist) {
        mqDist = dist;
        mqAngle = qiblaAngle;
        invalidate();
    }
}