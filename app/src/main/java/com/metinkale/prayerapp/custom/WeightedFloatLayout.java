package com.metinkale.prayerapp.custom;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class WeightedFloatLayout extends ViewGroup
{

    private final Rect rect = new Rect();

    public WeightedFloatLayout(Context context)
    {
        super(context);
    }

    public WeightedFloatLayout(Context context, AttributeSet attributeSet)
    {
        super(context, attributeSet);
    }

    public WeightedFloatLayout(Context context, AttributeSet attributeSet, int defStyle)
    {
        super(context, attributeSet, defStyle);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {

        int w = r - l;
        int h = b - t;

        rect.set(0, 0, 0, 0);
        final int count = getChildCount();
        for(int i = 0; i < count; i++)
        {
            View child = getChildAt(i);
            LayoutParams lp = child.getLayoutParams();
            int ch = h * lp.height / 1000;
            int cw = w * lp.width / 1000;

            if(rect.right == w)
            {
                rect.top = rect.bottom;
                rect.bottom += ch;
                rect.left = 0;
                rect.right = cw;
            } else
            {
                rect.bottom = rect.top + ch;
                rect.left = rect.right;
                rect.right += cw;
            }
            child.layout(rect.left, rect.top, rect.right, rect.bottom);

            if(child instanceof ViewGroup && ((ViewGroup) child).getChildAt(0) instanceof TextView)
            {

                ((TextView) ((ViewGroup) child).getChildAt(0)).setTextSize(TypedValue.COMPLEX_UNIT_PX, ch * 7 / 10);
                child.measure(MeasureSpec.makeMeasureSpec(rect.width(), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(rect.height(), MeasureSpec.EXACTLY));
            }
            if(child instanceof TextView)
            {
                ((TextView) child).setTextSize(TypedValue.COMPLEX_UNIT_PX, ch * 7 / 10);
            }
        }
    }
}
