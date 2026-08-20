/*
 * Copyright (c) 2013-2026 Metin Kale
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
package com.metinkale.prayer.compass.magnetic.compass2D

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.metinkale.prayer.R
import com.metinkale.prayer.utils.LocaleUtils.formatNumber
import kotlin.math.min

class CompassView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {
    private val path = Path()
    private val paint = Paint()
    private val kabeIcon: Drawable? = ContextCompat.getDrawable(context, R.drawable.compass_kaabe)
    private var currentAngle
    = -80f
    private var qiblaAngle = 0f
    private val bgColor: Int = Color.WHITE
    private val textColor: Int = Color.BLACK
    private val scndTextColor: Int = Color.GRAY
    private val strokeColor: Int = context.getColor(R.color.colorPrimary)

    public override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        val size = min(MeasureSpec.getSize(widthSpec), MeasureSpec.getSize(heightSpec))

        val center = size / 2

        path.reset()
        path.setFillType(Path.FillType.EVEN_ODD)
        path.moveTo(center.toFloat(), center / 8f)

        path.lineTo((center * 15) / 20f, center / 3f)

        path.lineTo(center.toFloat(), center / 4f)

        path.lineTo((center * 25) / 20f, center / 3f)
        path.close()

        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas) {
        val width = getWidth()
        val center = width / 2

        paint.setTextAlign(Paint.Align.CENTER)
        paint.setAntiAlias(true)

        paint.setStrokeWidth(center / 15f)

        paint.setColor(bgColor)
        paint.setStyle(Paint.Style.FILL_AND_STROKE)
        canvas.drawCircle(center.toFloat(), center.toFloat(), (center * 19) / 20f, paint)
        paint.setStyle(Paint.Style.STROKE)

        paint.setColor(strokeColor)
        canvas.drawCircle(center.toFloat(), center.toFloat(), (center * 19) / 20f, paint)
        paint.setStrokeWidth(1f)

        paint.setColor(textColor)

        paint.setTextSize((center * 2) / 5f)

        paint.setStyle(Paint.Style.FILL_AND_STROKE)
        canvas.drawText(
            formatNumber(
                Math.round(
                    this.angle
                )
            ) + "°", center.toFloat(), center + (center / 5f), paint
        )
        paint.setStyle(Paint.Style.STROKE)

        canvas.rotate(-currentAngle, center.toFloat(), center.toFloat())

        paint.setColor(scndTextColor)

        paint.setStyle(Paint.Style.FILL_AND_STROKE)
        canvas.drawPath(path, paint)
        paint.setStyle(Paint.Style.STROKE)

        paint.setTextSize(center / 5f)

        paint.setStyle(Paint.Style.FILL_AND_STROKE)
        canvas.drawText("N", center.toFloat(), (center * 9) / 20f, paint)
        paint.setStyle(Paint.Style.STROKE)

        canvas.rotate(qiblaAngle, center.toFloat(), center.toFloat())

        if (qiblaAngle != 0f) {
            val y = (center * 9) / 20
            val size = center / 8

            kabeIcon!!.setBounds(center - size, y - size, center + size, y + size)
            kabeIcon.draw(canvas)

            paint.setColor(textColor)

            paint.setStyle(Paint.Style.FILL_AND_STROKE)
            canvas.drawPath(path, paint)
            paint.setStyle(Paint.Style.STROKE)
        }
    }

    var angle: Float
        get() {
            var angle = currentAngle
            if (angle < 0) {
                angle += 360f
            }
            return angle
        }
        set(rot) {
            currentAngle = rot
            invalidate()
        }

    fun setQiblaAngle(qiblaAngle: Int) {
        this@CompassView.qiblaAngle = qiblaAngle.toFloat()
        invalidate()
    }
}