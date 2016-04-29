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

package com.metinkale.prayerapp.custom;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import com.metinkale.prayer.R;

public class ShadowContainer extends ViewGroup {
    private Drawable mShadow;
    private float mShadowSize = 1f, mMaxShadowSize;
    private boolean mTop, mLeft, mRight, mBottom;

    @SuppressLint("NewApi")
    public ShadowContainer(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs, defStyleAttr, defStyleRes);

    }

    public ShadowContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr, 0);

    }

    public ShadowContainer(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(attrs, 0, 0);

    }

    public ShadowContainer(Context context) {
        super(context);
        init(null, 0, 0);
    }

    @SuppressLint("NewApi")
    private void init(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mShadow = getResources().getDrawable(R.drawable.shadow);
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ShadowContainer, defStyleAttr, defStyleRes);

            mBottom = a.getBoolean(R.styleable.ShadowContainer_shadow, false);
            mTop = mBottom;
            mLeft = mTop;
            mRight = mLeft;

            mBottom = a.getBoolean(R.styleable.ShadowContainer_shadowBottom, false);
            mLeft = a.getBoolean(R.styleable.ShadowContainer_shadowLeft, false);
            mRight = a.getBoolean(R.styleable.ShadowContainer_shadowRight, false);
            mTop = a.getBoolean(R.styleable.ShadowContainer_shadowTop, false);
            mShadowSize = a.getFloat(R.styleable.ShadowContainer_shadowSize, 1);
            mMaxShadowSize = a.getDimensionPixelSize(R.styleable.ShadowContainer_maxShadowSize, 0);

            a.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //int wm = MeasureSpec.getMode(widthMeasureSpec);
        int hm = MeasureSpec.getMode(heightMeasureSpec);

        int w = MeasureSpec.getSize(widthMeasureSpec);
        int h = MeasureSpec.getSize(heightMeasureSpec);

        if (hm == MeasureSpec.UNSPECIFIED) {
            getChildAt(0).measure(widthMeasureSpec, heightMeasureSpec);
            h = getChildAt(0).getMeasuredHeight();
            if (mTop) {
                h += mShadowSize * mMaxShadowSize;
            }
            if (mBottom) {
                h += mShadowSize * mMaxShadowSize;
            }

        }
        setMeasuredDimension(w, h);
        if (mTop) {
            h -= mShadowSize * mMaxShadowSize;
        }

        if (mLeft) {
            w -= mShadowSize * mMaxShadowSize;
        }

        if (mRight) {
            w -= mShadowSize * mMaxShadowSize;
        }

        if (mBottom) {
            h -= mShadowSize * mMaxShadowSize;
        }

        getChildAt(0).measure(MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        r -= l;
        b -= t;
        t = 0;
        l = 0;

        if (mTop) {
            t += mShadowSize * mMaxShadowSize;

        }

        if (mLeft) {
            l += mShadowSize * mMaxShadowSize;
        }

        if (mRight) {
            r -= mShadowSize * mMaxShadowSize;
        }

        if (mBottom) {
            b -= mShadowSize * mMaxShadowSize;
        }
        getChildAt(0).layout(l, t, r, b);

    }

    public void setShadowSize(float shadowSize) {
        mShadowSize = shadowSize;
    }

    public void setMaxShadowSize(float maxShadowSize) {
        mMaxShadowSize = maxShadowSize;
    }

    public void setShadows(boolean left, boolean top, boolean right, boolean bottom) {
        mLeft = left;
        mTop = top;
        mRight = right;
        mBottom = bottom;
    }

    @Override
    public void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        int size = (int) (mShadowSize * mMaxShadowSize);

        if (mTop) {
            mShadow.setBounds(0, 0, getWidth(), size);
            mShadow.draw(canvas);
        }
        if (mLeft) {
            mShadow.setBounds(0, 0, size, getHeight());
            mShadow.draw(canvas);
        }
        if (mRight) {
            mShadow.setBounds(canvas.getWidth() - size, 0, canvas.getWidth(), canvas.getHeight());
            mShadow.draw(canvas);
        }
        if (mBottom) {
            mShadow.setBounds(0, canvas.getHeight() - size, getWidth(), canvas.getHeight());
            mShadow.draw(canvas);
        }

    }
}