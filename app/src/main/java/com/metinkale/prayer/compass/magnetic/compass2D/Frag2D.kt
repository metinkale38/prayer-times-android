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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import com.metinkale.prayer.R
import com.metinkale.prayer.compass.QiblaListener
import com.metinkale.prayer.compass.magnetic.DegreeLowPassFilter
import com.metinkale.prayer.utils.LocaleUtils.formatNumber

class Frag2D : Fragment(), QiblaListener {
    private var compassView: CompassView? = null
    private var angleTV: TextView? = null
    private var distanceTV: TextView? = null
    private var angle = 0
    private var qiblaDistance = 0.0
    private var qiblaAngle = 0.0


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, bdl: Bundle?): View {
        val v = inflater.inflate(R.layout.compass_2d, container, false)
        compassView = v.findViewById<CompassView?>(R.id.compass)


        angleTV = v.findViewById<TextView?>(R.id.angle)
        distanceTV = v.findViewById<TextView?>(R.id.distance)
        val info = angleTV!!.getParent() as View
        ViewCompat.setElevation(info, info.getPaddingTop().toFloat())

        setAngle(angle)
        setQiblaAngle(qiblaAngle)
        setQiblaDistance(qiblaDistance)
        return v
    }


    override fun setUserLocation(lat: Double, lng: Double, alt: Double) {
    }

    override fun setQiblaAngle(angle: Double) {
        qiblaAngle = angle
        if (angleTV != null) {
            angleTV!!.setText(formatNumber(Math.round(angle).toString() + "°"))
            compassView!!.setQiblaAngle(Math.round(angle).toInt())
        }
    }

    override fun setQiblaDistance(distance: Double) {
        qiblaDistance = distance
        if (distanceTV != null) distanceTV!!.setText(
            formatNumber(
                Math.round(distance).toString() + "km"
            )
        )
    }

    private val lowPassFilter = DegreeLowPassFilter()

    fun setAngle(angle: Int) {
        this@Frag2D.angle = angle
        if (compassView == null) return
        compassView!!.angle = lowPassFilter.filter(angle.toFloat())[0]
    }
}
