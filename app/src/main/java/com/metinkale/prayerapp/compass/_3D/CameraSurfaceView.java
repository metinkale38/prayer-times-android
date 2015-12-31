package com.metinkale.prayerapp.compass._3D;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

@SuppressWarnings("deprecation")
public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private DisplayMetrics mMetrics;

    public CameraSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mMetrics = getResources().getDisplayMetrics();
        mHolder = getHolder();
        mHolder.addCallback(this);

    }

    public CameraSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public CameraSurfaceView(Context context) {
        this(context, null);

    }

    @Override
    public void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);

        setMeasuredDimension(mMetrics.widthPixels, mMetrics.heightPixels);

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            if (mCamera == null) {
                // Open the Camera in preview mode
                mCamera = Camera.open();
            }
            mCamera.setDisplayOrientation(90);
            mCamera.setPreviewDisplay(mHolder);
        } catch (Exception e) {
            setVisibility(View.GONE);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        if (mCamera == null) {
            return;
        }
        // Now that the size is known, set up the camera parameters and begin
        // the preview.

        Camera.Parameters parameters = mCamera.getParameters();
        Size size = getBestPreviewSize(width, height, parameters);
        if (size != null) {
            parameters.setPreviewSize(size.width, size.height);
        }
        mCamera.setParameters(parameters);
        mCamera.startPreview();
    }

    private Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters parameters) {
        Camera.Size result = null;

        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result = size;
                } else {
                    int resultArea = result.width * result.height;
                    int newArea = size.width * size.height;

                    if (newArea > resultArea) {
                        result = size;
                    }
                }
            }
        }

        return result;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera == null) {
            return;
        }

        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    public Camera getCamera() {
        return mCamera;
    }
}