package com.metinkale.prayerapp.vakit;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.TextView;
import com.metinkale.prayer.R;
import com.metinkale.prayerapp.App.NotIds;

public class NotificationPopup extends Activity {
    static NotificationPopup instance;
    private TextView name;
    private TextView vakit;


    public void onResume() {
        super.onResume();
        instance = this;
    }

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

        this.setContentView(R.layout.vakit_notpopup);

        name = ((TextView) findViewById(R.id.name));
        name.setText(this.getIntent().getStringExtra("name"));
        vakit = ((TextView) findViewById(R.id.vakit));
        vakit.setText(this.getIntent().getStringExtra("vakit"));
        vakit.setKeepScreenOn(true);

        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        BroadcastReceiver mReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                context.sendBroadcast(new Intent(context, AlarmReceiver.Audio.class));
                finish();
            }
        };
        registerReceiver(mReceiver, filter);


    }

    void onDismiss() {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(getIntent().getIntExtra("city", 0) + "", NotIds.ALARM);
        this.sendBroadcast(new Intent(this, AlarmReceiver.Audio.class));
        finish();
    }


    @SuppressLint("ClickableViewAccessibility")
    public static class MyView extends View implements OnTouchListener {
        private final Paint paint = new Paint();
        private final Drawable icon, silent, close;
        private MotionEvent touch;
        private boolean acceptTouch = false;

        public MyView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            icon = context.getResources().getDrawable(R.drawable.ic_abicon);
            silent = context.getResources().getDrawable(R.drawable.ic_silent);
            close = context.getResources().getDrawable(R.drawable.ic_exit);
            this.setOnTouchListener(this);
        }

        public MyView(Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public MyView(Context context) {
            this(context, null);
        }

        @Override
        public void onMeasure(int widthSpec, int heightSpec) {
            super.onMeasure(widthSpec, heightSpec);
            int size = Math.min(getMeasuredWidth(), getMeasuredHeight());

            setMeasuredDimension(size, size);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            int w = this.getWidth();
            int r = w / 10;

            canvas.translate(w / 2, w / 2);
            if (touch == null) {
                icon.setBounds(-r, -r, r, r);
                icon.draw(canvas);
                return;
            }

            int x = (int) touch.getX();
            int y = (int) touch.getY();
            x -= this.getLeft();
            y -= this.getTop();
            x -= w / 2;
            y -= w / 2;

            float tr = (float) Math.sqrt(x * x + y * y);
            double angle = Math.atan(y / (double) x);
            if (x < 0)
                angle += Math.PI;
            if (tr >= w / 2 - r)
                tr = w / 2 - r;

            x = (int) (Math.cos(angle) * tr);
            y = (int) (Math.sin(angle) * tr);

            if (touch.getAction() == MotionEvent.ACTION_DOWN && Math.abs(x) < r && Math.abs(y) < r)
                acceptTouch = true;

            if (acceptTouch && touch.getAction() != MotionEvent.ACTION_UP) {
                silent.setBounds(-5 * r, -r, -3 * r, r);

                silent.draw(canvas);

                close.setBounds(3 * r, -r, 5 * r, r);
                close.draw(canvas);

                icon.setBounds(x - r, y - r, x + r, y + r);

                paint.setStyle(Style.STROKE);
                paint.setColor(0x88FFFFFF);
                canvas.drawCircle(0f, 0f, tr, paint);

            } else {
                icon.setBounds(-r, -r, r, r);

                if (tr > 3 * r) {
                    if (Math.abs(angle) < Math.PI / 10 && instance != null)
                        instance.finish();

                    if (Math.abs(angle - Math.PI) < Math.PI / 10 && instance != null)
                        instance.onDismiss();
                }
            }

            icon.draw(canvas);

        }

        @Override
        public boolean onTouch(View arg0, MotionEvent me) {
            touch = me;
            if (me.getAction() == MotionEvent.ACTION_UP)
                acceptTouch = false;

            this.invalidate();

            return true;

        }

    }

}
