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

package com.metinkale.prayer.dhikr;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.View.OnClickListener;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;



public class VibrationModeView extends View implements OnClickListener {
    private static final ColorFilter sCFActive = new PorterDuffColorFilter(0xffffffff, Mode.SRC_ATOP);
    private static final ColorFilter sCFInactive = new PorterDuffColorFilter(0xffa0a0a0, Mode.SRC_ATOP);

    @NonNull
    private final Paint mPaint;
    private Drawable mDrawable;
    private PrefsFunctions mFunc;

    public VibrationModeView(@NonNull Context c) {
        this(c, null, 0);
    }


    public VibrationModeView(@NonNull Context c, AttributeSet attrs) {
        this(c, attrs, 0);
    }


    public VibrationModeView(@NonNull Context c, AttributeSet attrs, int defStyle) {
        super(c, attrs, defStyle);
        mDrawable = c.getResources().getDrawable(R.drawable.ic_vibration_white_24dp);
        setOnClickListener(this);

        ViewCompat.setTranslationY(this, getResources().getDimension(R.dimen.dimen4dp));

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
    }

    public void setPrefFunctions(PrefsFunctions func) {
        mFunc = func;
    }


    @Override
    public void draw(@NonNull Canvas canvas) {
        super.draw(canvas);
        canvas.scale(0.8f, 0.8f, canvas.getWidth() / 2f, canvas.getHeight() / 2f);
        Object o = getValue();
        boolean active = !o.equals(-1);
        mPaint.setColor(active ? 0xff03A9F4 : 0xffe0e0e0);
        int w = getHeight();
        canvas.drawCircle(w / 2f, w / 2f, w / 2f, mPaint);

        int p = w / 7;
        mDrawable.setBounds(p, p, w - p, w - p);
        mDrawable.setColorFilter(active ? sCFActive : sCFInactive);
        mDrawable.draw(canvas);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int size = (width > height) ? height : width;
        setMeasuredDimension(size, size);
    }

    public int getValue() {
        if (mFunc == null) {
            return 0;
        } else {
            return mFunc.getValue();
        }
    }

    public void setValue(int obj) {
        if (mFunc != null) {
            mFunc.setValue(obj);
        }
        invalidate();
    }

    @Override
    public void onClick(View v) {
        int i = getValue();
        i++;
        if ((i < -1) || (i > 1)) {
            i = -1;
        }
        setValue(i);
        performHapticFeedback(HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING | HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);

    }


    public interface PrefsFunctions {
        int getValue();

        void setValue(int obj);
    }
}
