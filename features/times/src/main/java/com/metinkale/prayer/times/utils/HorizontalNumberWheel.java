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

package com.metinkale.prayer.times.utils;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.Nullable;

import com.metinkale.prayer.times.R;
import com.metinkale.prayer.utils.Utils;

public class HorizontalNumberWheel extends View {


    private int min = -180;
    private int max = 180;
    private int value = 0;
    private int lines = 25;
    private int stepsPerLine = 5;
    private GestureDetector detector;
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Listener listener;

    public HorizontalNumberWheel(Context context) {
        this(context, null);
    }

    public HorizontalNumberWheel(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HorizontalNumberWheel(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        paint.setColor(getResources().getColor(R.color.colorPrimary));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setStrokeWidth(Utils.convertDpToPixel(getContext(), 1));

        detector = new GestureDetector(context, gestureListener);
        setOnTouchListener(touchListener);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        paint.setTextSize(h * 0.6f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();
        canvas.drawText(value + "", w / 2f, h / 2f - (paint.descent() + paint.ascent()) / 2, paint);
        //   canvas.clipOutRect(w / 2 - paint.measureText(value + ""));

        float textWidth = paint.measureText("00000");

        float lineSpacing = w / (float) lines;

        for (float x = -(value % stepsPerLine) * lineSpacing / stepsPerLine; x < w; x += lineSpacing) {
            if (x < w / 2f - textWidth / 2f || x > w / 2f + textWidth / 2f) {
                canvas.drawLine(x, 0, x, h, paint);
            }
        }


    }

    public void setMax(int max) {
        this.max = max;
        invalidate();
    }


    public void setMin(int min) {
        this.min = min;
        invalidate();
    }


    public void setValue(int value) {
        this.value = value;
        if (this.value > max)
            this.value = max;
        if (this.value < min)
            this.value = min;
        if (listener != null) listener.onValueChanged(this.value);
        invalidate();
    }

    public void setListener(Listener list) {
        this.listener = list;
    }

    public interface Listener {
        void onValueChanged(int newValue);
    }


    private final View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return detector.onTouchEvent(event);

        }
    };

    private final GestureDetector.OnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {


        private float startValue;

        @Override
        public boolean onDown(MotionEvent event) {
            startValue = value;
            ViewParent parent = getParent();
            if (parent != null) {
                parent.requestDisallowInterceptTouchEvent(true);
            }
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {


            int stepWidth = getWidth() / lines / stepsPerLine;
            setValue((int) (startValue + (e1.getRawX() - e2.getRawX()) / stepWidth));

            return true;
        }

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2,
                               float velocityX, float velocityY) {
            ObjectAnimator anim = ObjectAnimator.ofInt(HorizontalNumberWheel.this, "value", value, value - (int) (velocityX / 200));
            anim.setInterpolator(new DecelerateInterpolator());
            anim.start();
            getRootView().setOnTouchListener(null);
            return true;
        }
    };

}
