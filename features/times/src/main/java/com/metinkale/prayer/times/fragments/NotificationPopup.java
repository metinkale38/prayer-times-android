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

package com.metinkale.prayer.times.fragments;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.TextView;

import com.metinkale.prayer.times.R;
import com.metinkale.prayer.times.alarm.Alarm;
import com.metinkale.prayer.times.alarm.AlarmService;
import com.metinkale.prayer.times.times.Times;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class NotificationPopup extends AppCompatActivity implements SensorEventListener {
    @Nullable
    public static NotificationPopup instance;
    private SensorManager mSensorManager;
    private Sensor mProximity;
    private boolean mReceiverRegistered = false;


    @Override
    public void onResume() {
        super.onResume();
        instance = this;
    }

    @Override
    public void onPause() {
        super.onPause();
        instance = null;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        setContentView(R.layout.vakit_notpopup);

        TextView name = findViewById(R.id.name);
        name.setText(getIntent().getStringExtra("name"));
        TextView vakit = findViewById(R.id.vakit);
        vakit.setText(getIntent().getStringExtra("vakit"));
        vakit.setKeepScreenOn(true);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        if (mProximity == null) {
            IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
            registerReceiver(mReceiver, filter);
            mReceiverRegistered = true;
        } else {
            mSensorManager.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onDestroy() {
        if (mProximity != null)
            mSensorManager.unregisterListener(this);
        if (mReceiverRegistered) {
            unregisterReceiver(mReceiver);
            mReceiverRegistered = false;
        }
        super.onDestroy();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.values[0] > 0) {
            IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
            registerReceiver(mReceiver, filter);
            mReceiverRegistered = true;
            mSensorManager.unregisterListener(this);
            mProximity = null;
            mSensorManager = null;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(@NonNull Context context, Intent intent) {
            context.sendBroadcast(new Intent(context, AlarmService.StopAlarmPlayerReceiver.class));
            finish();
        }
    };

    void onDismiss() {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(getIntent().getIntExtra("city", 0));
        sendBroadcast(new Intent(this, AlarmService.StopAlarmPlayerReceiver.class));
        finish();
    }

    public static void start(Context c, Alarm alarm) {
        Times t = alarm.getCity();
        PowerManager pm = (PowerManager) c.getSystemService(Context.POWER_SERVICE);
        if (Build.VERSION.SDK_INT >= 20 && !pm.isInteractive() || Build.VERSION.SDK_INT < 20 && !pm.isScreenOn()) {
            Intent i = new Intent(c, NotificationPopup.class);
            i.putExtra("city", t.getIntID());
            i.putExtra("name", t.getName() + " (" + t.getSource() + ")");
            i.putExtra("vakit", alarm.getCurrentTitle());
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            c.startActivity(i);
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    public static class MyView extends View implements OnTouchListener {
        private final Paint paint = new Paint();
        private final Drawable icon;
        private Bitmap silent;
        private Bitmap close;
        private MotionEvent touch;
        private boolean acceptTouch;

        public MyView(@NonNull Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            icon = context.getResources().getDrawable(R.drawable.ic_abicon);

            setOnTouchListener(this);
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            silent = drawableToBitmap(MaterialDrawableBuilder.with(getContext()).setIcon(MaterialDrawableBuilder.IconValue.VOLUME_OFF).setColor(Color.WHITE).setSizePx(w / 5).build(), w / 5);
            close = drawableToBitmap(
                    MaterialDrawableBuilder.with(getContext()).setIcon(MaterialDrawableBuilder.IconValue.CLOSE).setColor(Color.WHITE)
                            .setSizePx(w / 5).build(), w / 5);

        }

        public MyView(@NonNull Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public MyView(@NonNull Context context) {
            this(context, null);
        }

        @Override
        public void onMeasure(int widthSpec, int heightSpec) {
            super.onMeasure(widthSpec, heightSpec);
            int size = Math.min(getMeasuredWidth(), getMeasuredHeight());

            setMeasuredDimension(size, size);
        }

        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            int w = getWidth();
            int r = w / 10;

            canvas.translate(w / 2f, w / 2f);
            if (touch == null) {
                icon.setBounds(-r, -r, r, r);
                icon.draw(canvas);
                return;
            }

            int x = (int) touch.getX();
            int y = (int) touch.getY();
            x -= getLeft();
            y -= getTop();
            x -= w / 2;
            y -= w / 2;

            float tr = (float) Math.sqrt((x * x) + (y * y));
            double angle = Math.atan(y / (double) x);
            if (x < 0) {
                angle += Math.PI;
            }
            if (tr >= ((w / 2) - r)) {
                tr = w / 2f - r;
            }

            x = (int) (Math.cos(angle) * tr);
            y = (int) (Math.sin(angle) * tr);

            if ((touch.getAction() == MotionEvent.ACTION_DOWN) && (Math.abs(x) < r) && (Math.abs(y) < r)) {
                acceptTouch = true;
            }

            if (acceptTouch && (touch.getAction() != MotionEvent.ACTION_UP)) {

                paint.setColor(0xFFFFFFFF);
                if (silent != null) {
                    canvas.drawBitmap(silent, -5 * r, -r, paint);
                }
                if (close != null) {
                    canvas.drawBitmap(close, 3 * r, -r, paint);
                }

                icon.setBounds(x - r, y - r, x + r, y + r);
                icon.draw(canvas);

                paint.setStyle(Style.STROKE);
                paint.setColor(0x88FFFFFF);
                canvas.drawCircle(0f, 0f, tr, paint);

            } else {
                icon.setBounds(-r, -r, r, r);
                icon.draw(canvas);

                if (tr > (3 * r)) {
                    if ((Math.abs(angle) < (Math.PI / 10)) && (instance != null)) {
                        instance.finish();
                    }

                    if ((Math.abs(angle - Math.PI) < (Math.PI / 10)) && (instance != null)) {
                        instance.onDismiss();
                    }
                }
            }

        }

        @Override
        public boolean onTouch(View arg0, @NonNull MotionEvent me) {
            touch = me;
            if (me.getAction() == MotionEvent.ACTION_UP) {
                acceptTouch = false;
            }

            invalidate();

            return true;

        }

    }

    public static Bitmap drawableToBitmap(Drawable drawable, int size) {
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ALPHA_8);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}
