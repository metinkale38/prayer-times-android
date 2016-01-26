package com.metinkale.prayerapp.vakit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

@SuppressLint("ClickableViewAccessibility")
public class MyPaneView extends RelativeLayout {
    private View mMain, mBottom, mTop;
    private int mWidth, mHeight;
    private boolean mEnabled;
    private Mode mMode = Mode.Click;
    private float mLastY;
    private boolean mSwipingUp;
    private Runnable mTopOpen, mBottomOpen;

    enum Mode {
        SlideTop,
        SlideBottom,
        Click
    }

    public void setOnTopOpen(Runnable run) {
        mTopOpen = run;
    }

    public void setOnBottomOpen(Runnable run) {
        mBottomOpen = run;
    }

    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
    }

    public MyPaneView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public MyPaneView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyPaneView(Context context) {
        this(context, null);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        if (mTop == null) mTop = child;
        else if (mMain == null) {
            mMain = child;
        } else if (mBottom == null) {
            mBottom = child;
        } else {
            throw new RuntimeException("Only 3 Childs allowed (top, main, bottom)");
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!mEnabled) return false;
        if (mMain.getTranslationY() == 0) {
            if (ev.getY() < mHeight / 10) {
                return true;
            } else if (ev.getY() > mHeight * 9 / 10 + mMain.getTranslationY()) {
                return true;
            }
        } else if (ev.getY() > mMain.getTranslationY() && ev.getY() < mMain.getTranslationY() + mMain.getHeight())
            return true;
        return false;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (mTop != null)
            mTop.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(getMeasuredHeight() / 2, MeasureSpec.AT_MOST));


        if (mBottom != null) {
            mBottom.getLayoutParams().height = getMeasuredHeight() * 2 / 3;
        }

        if (mMain != null) mMain.bringToFront();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        float y = ev.getY();
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (y < mHeight / 10 + mMain.getTranslationY()) {
                mMode = Mode.SlideTop;
                mTop.setVisibility(View.VISIBLE);
                mBottom.setVisibility(View.GONE);
                if (mTopOpen != null)
                    post(mTopOpen);
            } else if (y > mHeight * 9 / 10 + mMain.getTranslationY()) {
                mMode = Mode.SlideBottom;
                mTop.setVisibility(View.GONE);
                mBottom.setVisibility(View.VISIBLE);
                if (mBottomOpen != null)
                    post(mBottomOpen);
            } else mMode = Mode.Click;


        } else if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            switch (mMode) {
                case SlideTop: {
                    float ty = mMain.getTranslationY() + (y - mLastY);
                    ty = Math.max(ty, 0);
                    ty = Math.min(ty, mTop.getHeight());
                    mMain.setTranslationY(ty);
                    break;
                }
                case SlideBottom: {
                    float ty = mMain.getTranslationY() + (y - mLastY);
                    ty = Math.min(ty, 0);
                    ty = Math.max(ty, -mBottom.getHeight());
                    mMain.setTranslationY(ty);
                    break;
                }
                case Click:
                    break;
            }
        } else if (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_CANCEL) {
            switch (mMode) {
                case SlideTop: {
                    if (!mSwipingUp)
                        mMain.animate().translationY(mTop.getHeight());
                    else mMain.animate().translationY(0);
                    break;
                }
                case SlideBottom: {
                    if (mSwipingUp)
                        mMain.animate().translationY(-mBottom.getHeight());
                    else mMain.animate().translationY(0);
                    break;
                }
                case Click:
                    mMain.animate().translationY(0);

                    break;
            }
        }
        if (y > mLastY) mSwipingUp = false;
        else if (y < mLastY) mSwipingUp = true;
        mLastY = y;
        return true;
    }


}

