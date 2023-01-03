/*
 * Copyright (c) 2013-2023 Metin Kale
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
package com.metinkale.prayer.times.fragments

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.*
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.PowerManager
import android.util.AttributeSet
import android.view.*
import android.view.View.OnTouchListener
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.metinkale.prayer.times.R
import com.metinkale.prayer.times.alarm.Alarm
import com.metinkale.prayer.times.alarm.AlarmService.StopAlarmPlayerReceiver
import com.metinkale.prayer.times.fragments.NotificationPopup
import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.min
import kotlin.math.sqrt

class NotificationPopup : AppCompatActivity(), SensorEventListener {
    private var mSensorManager: SensorManager? = null
    private var mProximity: Sensor? = null
    private var mReceiverRegistered = false
    public override fun onResume() {
        super.onResume()
        instance = this
    }

    public override fun onPause() {
        super.onPause()
        instance = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        val decorView = window.decorView
        val uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN
        decorView.systemUiVisibility = uiOptions
        setContentView(R.layout.vakit_notpopup)
        val name = findViewById<TextView>(R.id.name)
        name.text = intent.getStringExtra("name")
        val vakit = findViewById<TextView>(R.id.vakit)
        vakit.text = intent.getStringExtra("vakit")
        vakit.keepScreenOn = true
        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        mProximity = mSensorManager!!.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        if (mProximity == null) {
            val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
            registerReceiver(mReceiver, filter)
            mReceiverRegistered = true
        } else {
            mSensorManager!!.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onDestroy() {
        if (mProximity != null) mSensorManager!!.unregisterListener(this)
        if (mReceiverRegistered) {
            unregisterReceiver(mReceiver)
            mReceiverRegistered = false
        }
        super.onDestroy()
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.values[0] > 0) {
            val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
            registerReceiver(mReceiver, filter)
            mReceiverRegistered = true
            mSensorManager!!.unregisterListener(this)
            mProximity = null
            mSensorManager = null
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    var mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            context.sendBroadcast(Intent(context, StopAlarmPlayerReceiver::class.java))
            finish()
        }
    }

    fun onDismiss() {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(intent.getIntExtra("city", 0))
        sendBroadcast(Intent(this, StopAlarmPlayerReceiver::class.java))
        finish()
    }

    @SuppressLint("ClickableViewAccessibility")
    class MyView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
    ) : View(context, attrs, defStyleAttr), OnTouchListener {
        private val paint = Paint()
        private val icon: Drawable? = ContextCompat.getDrawable(getContext(), R.drawable.ic_abicon)
        private var silent: Bitmap? = null
        private var close: Bitmap? = null
        private var distX = 0
        private var distY = 0
        private var touchXStart = 0
        private var touchYStart = 0
        private var distance = 0f

        init {
            setOnTouchListener(this)
        }

        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(w, h, oldw, oldh)
            silent = drawableToBitmap(
                ContextCompat.getDrawable(context, R.drawable.ic_volume_off),
                w / 5
            )
            close = drawableToBitmap(ContextCompat.getDrawable(context, R.drawable.ic_close), w / 5)
        }

        public override fun onMeasure(widthSpec: Int, heightSpec: Int) {
            super.onMeasure(widthSpec, heightSpec)
            val size = min(measuredWidth, measuredHeight)
            setMeasuredDimension(size, size)
        }

        override fun onDraw(canvas: Canvas) {
            val w = width
            val r = w / 10
            canvas.translate(w / 2f, w / 2f)
            icon!!.setBounds(distX - r, distY - r, distX + r, distY + r)
            icon.draw(canvas)
            if (distX == 0 && distY == 0) {
                return
            }
            paint.color = -0x1
            if (silent != null) {
                canvas.drawBitmap(silent!!, (-5 * r).toFloat(), -r.toFloat(), paint)
            }
            if (close != null) {
                canvas.drawBitmap(close!!, (3 * r).toFloat(), -r.toFloat(), paint)
            }
            paint.style = Paint.Style.STROKE
            paint.color = -0x77000001
            canvas.drawCircle(0f, 0f, distance, paint)
        }

        override fun onTouch(arg0: View, me: MotionEvent): Boolean {
            val w = width
            val h = width
            val touchRadius = w / 10


            // get x/y relative to center
            var x = me.rawX.toInt()
            var y = me.rawY.toInt()
            val location = IntArray(2)
            getLocationOnScreen(location)
            x -= location[0]
            y -= location[1]
            x -= w / 2
            y -= h / 2

            // save start position
            if (me.action == MotionEvent.ACTION_DOWN) {
                touchXStart = x
                touchYStart = y
            }

            // only accept touches inside touchRadius
            if (sqrt((touchXStart * touchXStart + touchYStart * touchYStart).toDouble()) < touchRadius) {
                distance = sqrt((distX * distX + distY * distY).toDouble()).toFloat()
                distX = x - touchXStart
                distY = y - touchYStart
                var angle = atan(distY / distX.toDouble())
                if (x < 0) {
                    angle += Math.PI
                }
                if (distance >= w / 2f - touchRadius) {
                    distance = w / 2f - touchRadius
                }
                if (me.action == MotionEvent.ACTION_UP) {
                    if (distance > 3 * touchRadius) {
                        if (abs(angle) < Math.PI / 10 && instance != null) {
                            instance!!.finish()
                        }
                        if (abs(angle - Math.PI) < Math.PI / 10 && instance != null) {
                            instance!!.onDismiss()
                        }
                    }
                }
            }
            if (me.action == MotionEvent.ACTION_UP) {
                distX = 0
                distY = 0
            }
            invalidate()
            return true
        }
    }

    companion object {
        var instance: NotificationPopup? = null
        fun start(c: Context, alarm: Alarm) {
            val (ID, name, source) = alarm.city
            val pm = c.getSystemService(POWER_SERVICE) as PowerManager
            if (!pm.isInteractive) {
                val i = Intent(c, NotificationPopup::class.java)
                i.putExtra("city", ID)
                i.putExtra("name", "$name ($source)")
                i.putExtra("vakit", alarm.buildNotificationTitle())
                i.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                c.startActivity(i)
            }
        }

        fun drawableToBitmap(drawable: Drawable?, size: Int): Bitmap {
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ALPHA_8)
            val canvas = Canvas(bitmap)
            drawable!!.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            return bitmap
        }
    }
}