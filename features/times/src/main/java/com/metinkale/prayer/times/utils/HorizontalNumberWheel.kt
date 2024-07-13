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
package com.metinkale.prayer.times.utils

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.annotation.Keep
import com.metinkale.prayer.times.R
import com.metinkale.prayer.utils.Utils

class HorizontalNumberWheel @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var min = -180
    private var max = 180
    private var value = 0
    private val lines = 25
    private val stepsPerLine = 5
    private val detector: GestureDetector
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var listener: Listener? = null
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        paint.textSize = h * 0.6f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width
        val h = height
        canvas.drawText(
            value.toString() + "",
            w / 2f,
            h / 2f - (paint.descent() + paint.ascent()) / 2,
            paint
        )
        //   canvas.clipOutRect(w / 2 - paint.measureText(value + ""));
        val textWidth = paint.measureText("00000")
        val lineSpacing = w / lines.toFloat()
        var x = -(value % stepsPerLine) * lineSpacing / stepsPerLine
        while (x < w) {
            if (x < w / 2f - textWidth / 2f || x > w / 2f + textWidth / 2f) {
                canvas.drawLine(x, 0f, x, h.toFloat(), paint)
            }
            x += lineSpacing
        }
    }

    fun setMax(max: Int) {
        this.max = max
        invalidate()
    }

    fun setMin(min: Int) {
        this.min = min
        invalidate()
    }

    @Keep
    fun setValue(value: Int) {
        this.value = value
        if (this.value > max) this.value = max
        if (this.value < min) this.value = min
        if (listener != null) listener!!.onValueChanged(this.value)
        invalidate()
    }

    fun setListener(list: Listener?) {
        listener = list
    }

    fun interface Listener {
        fun onValueChanged(newValue: Int)
    }

    private val gestureListener: GestureDetector.OnGestureListener =
        object : SimpleOnGestureListener() {
            private var startValue = 0f
            override fun onDown(event: MotionEvent): Boolean {
                startValue = value.toFloat()
                val parent = parent
                parent?.requestDisallowInterceptTouchEvent(true)
                return true
            }

            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                return true
            }

            override fun onLongPress(e: MotionEvent) {}
            override fun onDoubleTap(e: MotionEvent): Boolean {
                return true
            }


            override fun onScroll(
                e1: MotionEvent?, e2: MotionEvent,
                distanceX: Float, distanceY: Float
            ): Boolean {
                val stepWidth = width / lines / stepsPerLine
                setValue((startValue + ((e1?.rawX ?: 0f) - e2.rawX) / stepWidth).toInt())
                return true
            }

            override fun onFling(
                event1: MotionEvent?, event2: MotionEvent,
                velocityX: Float, velocityY: Float
            ): Boolean {
                val anim = ObjectAnimator.ofInt(
                    this@HorizontalNumberWheel,
                    "value",
                    value,
                    value - (velocityX / 200).toInt()
                )
                anim.interpolator = DecelerateInterpolator()
                anim.start()
                rootView.setOnTouchListener(null)
                return true
            }
        }

    init {
        paint.color = resources.getColor(R.color.colorPrimary)
        paint.textAlign = Paint.Align.CENTER
        paint.strokeWidth = Utils.convertDpToPixel(getContext(), 1f)
        detector = GestureDetector(context, gestureListener)
        setOnTouchListener { _, event -> detector.onTouchEvent(event) }
    }


}