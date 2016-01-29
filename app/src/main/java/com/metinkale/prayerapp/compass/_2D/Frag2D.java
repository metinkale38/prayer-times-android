package com.metinkale.prayerapp.compass._2D;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;
import com.metinkale.prayer.R;
import com.metinkale.prayerapp.compass.LowPassFilter;
import com.metinkale.prayerapp.compass.Main;
import com.metinkale.prayerapp.compass.Main.MyCompassListener;

public class Frag2D extends Fragment implements MyCompassListener
{
    private final static OvershootInterpolator overshootInterpolator = new OvershootInterpolator();
    private final static AccelerateInterpolator accelerateInterpolator = new AccelerateInterpolator();
    private CompassView mCompassView;
    private TextView mAngle, mDist;
    private View mInfo;
    private float[] mGravity = new float[3];
    private float[] mGeo = new float[3];

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bdl)
    {
        View v = inflater.inflate(R.layout.compass_2d, container, false);
        mCompassView = (CompassView) v.findViewById(R.id.compass);

        mAngle = (TextView) v.findViewById(R.id.angle);
        mDist = (TextView) v.findViewById(R.id.distance);
        mInfo = v.findViewById(R.id.infobox);

        View info = (View) mAngle.getParent();
        ViewCompat.setElevation(info, info.getPaddingTop());
        onUpdateDirection();
        return v;
    }

    @Override
    public void onUpdateDirection()
    {
        if(mCompassView != null)
        {
            mCompassView.setQiblaAngle((int) Main.getQiblaAngle());
            mAngle.setText(Math.round(mCompassView.getQiblaAngle()) + "°");
            mDist.setText(Math.round(Main.getDistance()) + "km");
        }

    }

    @Override
    public void onUpdateSensors(float[] rot)
    {
        if(mCompassView != null)
        {
            // mCompassView.setAngle(rot[0]);
            mGravity = LowPassFilter.filter(((Main) getActivity()).mMagAccel.mAccelVals, mGravity);
            mGeo = LowPassFilter.filter(((Main) getActivity()).mMagAccel.mMagVals, mGeo);

            if(mGravity != null && mGeo != null)
            {
                float R[] = new float[9];
                float I[] = new float[9];
                boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeo);
                if(success)
                {
                    float orientation[] = new float[3];
                    SensorManager.getOrientation(R, orientation);

                    mCompassView.setAngle((int) Math.toDegrees(orientation[0]));

                }
            }
        }

    }

    public boolean mHidden = false;

    public void show()
    {
        mHidden = false;
        mCompassView.post(new Runnable()
        {
            public void run()
            {
                ObjectAnimator scaleX = ObjectAnimator.ofFloat(mCompassView, "scaleX", 0, 1);
                ObjectAnimator scaleY = ObjectAnimator.ofFloat(mCompassView, "scaleY", 0, 1);

                ObjectAnimator scaleX2 = ObjectAnimator.ofFloat(mInfo, "scaleX", 0, 1);
                ObjectAnimator scaleY2 = ObjectAnimator.ofFloat(mInfo, "scaleY", 0, 1);
                AnimatorSet animSetXY = new AnimatorSet();
                animSetXY.playTogether(scaleX, scaleY, scaleX2, scaleY2);
                animSetXY.setInterpolator(overshootInterpolator);
                animSetXY.setDuration(300);
                animSetXY.start();
            }
        });

    }

    public void hide()
    {
        mHidden = false;
        mCompassView.post(new Runnable()
        {
            public void run()
            {
                ObjectAnimator scaleX = ObjectAnimator.ofFloat(mCompassView, "scaleX", 1, 0);
                ObjectAnimator scaleY = ObjectAnimator.ofFloat(mCompassView, "scaleY", 1, 0);

                ObjectAnimator scaleX2 = ObjectAnimator.ofFloat(mInfo, "scaleX", 1, 0);
                ObjectAnimator scaleY2 = ObjectAnimator.ofFloat(mInfo, "scaleY", 1, 0);

                AnimatorSet animSetXY = new AnimatorSet();
                animSetXY.playTogether(scaleX, scaleY, scaleX2, scaleY2);
                animSetXY.setInterpolator(accelerateInterpolator);
                animSetXY.setDuration(300);
                animSetXY.start();
            }
        });

    }

}
