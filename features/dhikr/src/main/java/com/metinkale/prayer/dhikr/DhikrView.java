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

package com.metinkale.prayer.dhikr;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

import com.metinkale.prayer.dhikr.data.Dhikr;

public class DhikrView extends View {
    private final Paint mPaint = new Paint();

    private Dhikr mDhikr;

    @NonNull
    private final RectF mRectF = new RectF();
    private MotionEvent mMotionEvent;

    public DhikrView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

    }

    public DhikrView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DhikrView(Context context) {
        this(context, null);
    }


    @Override
    public void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
        int size = Math.min(getMeasuredWidth(), getMeasuredHeight());
        setMeasuredDimension(size, size);
        mRectF.set(0, 0, size, size);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        if (mDhikr == null) return;
        int width = getWidth();
        int center = width / 2;
        canvas.scale(0.95f, 0.95f, center, center);

        mPaint.setAntiAlias(true);

        mPaint.setStrokeWidth(center / 15f);

        mPaint.setColor(getResources().getColor(R.color.background));
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawCircle(center / 10f, center / 10f, center / 10f, mPaint);

        canvas.drawCircle(center, center, center, mPaint);
        mPaint.setStyle(Paint.Style.STROKE);

        mPaint.setColor(mDhikr.getColor());
        if (mDhikr.getMax() != 0) {
            canvas.drawArc(mRectF, -90, (((mDhikr.getValue() % mDhikr.getMax()) / (float) mDhikr.getMax()) * 360), false, mPaint);
        }
        mPaint.setStrokeWidth(1);

        mPaint.setColor(getResources().getColor(R.color.foreground));
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setTextSize((center * 2) / 5f);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawText((mDhikr.getValue() % mDhikr.getMax()) + "", center, center, mPaint);

        mPaint.setTextSize((center * 2) / 20f);
        canvas.drawText((mDhikr.getValue() / mDhikr.getMax()) + "", center / 10f, center * 0.13f, mPaint);


        mPaint.setTextSize((center * 2) / 10f);
        mPaint.setColor(getResources().getColor(R.color.foregroundSecondary));
        canvas.drawText("/" + mDhikr.getMax(), center, (width * 2) / 3f, mPaint);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.mMotionEvent = event;
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        int radius = getWidth() / 2;
        int[] coords = new int[2];
        getLocationInWindow(coords);

        float x = mMotionEvent.getRawX() - coords[0] - radius;
        float y = mMotionEvent.getRawY() - coords[1] - radius;
        return Math.sqrt(x * x + y * y) < radius * 0.90 && super.performClick();
    }

    public Dhikr getDhikr() {
        return mDhikr;
    }

    public void setDhikr(Dhikr mDhikr) {
        this.mDhikr = mDhikr;
    }
}