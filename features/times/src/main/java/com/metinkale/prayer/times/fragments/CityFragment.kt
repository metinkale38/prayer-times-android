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
import android.content.Intent
import android.graphics.ColorMatrixColorFilter
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.asLiveData
import com.metinkale.prayer.CrashReporter.recordException
import com.metinkale.prayer.Preferences
import com.metinkale.prayer.times.R
import com.metinkale.prayer.times.times.DayTimesWebProvider
import com.metinkale.prayer.times.times.Times
import com.metinkale.prayer.times.times.Vakit
import com.metinkale.prayer.times.times.getTime
import com.metinkale.prayer.times.utils.*
import com.metinkale.prayer.utils.LocaleUtils
import com.metinkale.prayer.utils.PermissionUtils
import com.metinkale.prayer.utils.Utils
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import java.util.*

@SuppressLint("ClickableViewAccessibility")
class CityFragment : Fragment() {
    private lateinit var times: Store<Times?>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        bdl: Bundle?
    ): View? {
        times = Times.getTimesByIndex(requireArguments().getInt("index"))

        val v = inflater.inflate(R.layout.vakit_fragment, container, false)

        CityFragmentViewModel.from(times.data.filterNotNull()).asLiveData()
            .observe(viewLifecycleOwner) {
                val arabic = Preferences.USE_ARABIC

                v.findViewById<TextView>(R.id.date).text = it.date
                v.findViewById<TextView>(R.id.city).text = it.city
                v.findViewById<TextView>(R.id.hicri).text = it.hijri
                v.findViewById<ImageView>(R.id.source1).setImageResource(it.icon)
                v.findViewById<ImageView>(R.id.source2).setImageResource(it.icon)
                v.findViewById<ImageView>(R.id.gps).visibility =
                    if (it.autolocation) View.VISIBLE else View.GONE

                v.findViewById<TextView>(R.id.fajrTime).apply {
                    text = it.fajrTime
                    setBackgroundResource(if (it.hoverLine == 0) R.color.accent else R.color.transparent)
                }
                v.findViewById<TextView>(R.id.sunTime).apply {
                    text = it.sunTime
                    setBackgroundResource(if (it.hoverLine == 1) R.color.accent else R.color.transparent)
                }
                v.findViewById<TextView>(R.id.zuhrTime).apply {
                    text = it.dhuhrTime
                    setBackgroundResource(if (it.hoverLine == 2) R.color.accent else R.color.transparent)
                }
                v.findViewById<TextView>(R.id.asrTime).apply {
                    text = it.asrTime
                    setBackgroundResource(if (it.hoverLine == 3) R.color.accent else R.color.transparent)
                }
                v.findViewById<TextView>(R.id.maghribTime).apply {
                    text = it.maghribTime
                    setBackgroundResource(if (it.hoverLine == 4) R.color.accent else R.color.transparent)
                }
                v.findViewById<TextView>(R.id.ishaaTime).apply {
                    text = it.ishaTime
                    setBackgroundResource(if (it.hoverLine == 5) R.color.accent else R.color.transparent)
                }

                v.findViewById<TextView>(R.id.fajr).apply {
                    text = it.fajrTitle
                    setBackgroundResource(if (it.hoverLine == 0) R.color.accent else R.color.transparent)
                    gravity = if (arabic) Gravity.START else Gravity.CENTER
                }
                v.findViewById<TextView>(R.id.sun).apply {
                    text = it.sunTitle
                    setBackgroundResource(if (it.hoverLine == 1) R.color.accent else R.color.transparent)
                    gravity = if (arabic) Gravity.START else Gravity.CENTER
                }
                v.findViewById<TextView>(R.id.zuhr).apply {
                    text = it.dhuhrTitle
                    setBackgroundResource(if (it.hoverLine == 2) R.color.accent else R.color.transparent)
                    gravity = if (arabic) Gravity.START else Gravity.CENTER
                }
                v.findViewById<TextView>(R.id.asr).apply {
                    text = it.asrTitle
                    setBackgroundResource(if (it.hoverLine == 3) R.color.accent else R.color.transparent)
                    gravity = if (arabic) Gravity.START else Gravity.CENTER
                }
                v.findViewById<TextView>(R.id.maghrib).apply {
                    text = it.maghribTitle
                    setBackgroundResource(if (it.hoverLine == 4) R.color.accent else R.color.transparent)
                    gravity = if (arabic) Gravity.START else Gravity.CENTER
                }
                v.findViewById<TextView>(R.id.ishaa).apply {
                    text = it.ishaTitle
                    setBackgroundResource(if (it.hoverLine == 5) R.color.accent else R.color.transparent)
                    gravity = if (arabic) Gravity.START else Gravity.CENTER
                }

                v.findViewById<TextView>(R.id.countdown).text = it.countdown

                v.findViewById<View>(R.id.kerahat).visibility =
                    if (it.isKerahat) View.VISIBLE else View.GONE
            }

        listOf(R.id.source1, R.id.source2).forEach {
            if (Utils.isNightMode(activity)) {
                val matrix = floatArrayOf(
                    1 / 3f, 1 / 3f, 1 / 3f, 0f, 255f,  //red
                    1 / 3f, 1 / 3f, 1 / 3f, 0f, 255f,  //green
                    1 / 3f, 1 / 3f, 1 / 3f, 0f, 255f, 0f, 0f, 0f, 1.0f, 0f
                )
                val filter = ColorMatrixColorFilter(matrix)
                v.findViewById<ImageView>(it).colorFilter = filter
            }
        }

        setHasOptionsMenu(true)

        return v
    }


    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        try {
            inflater.inflate(R.menu.vakit, menu)
        } catch (e: Exception) {
            e.printStackTrace()
            recordException(e)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val times = times.current ?: return super.onOptionsItemSelected(item)
        val i1 = item.itemId
        if (i1 == R.id.notification) {
            if (!PermissionUtils.get(requireContext()).pNotification) {
                PermissionUtils.get(requireContext()).needPostNotification(requireActivity())
            } else {
                val frag = requireActivity().supportFragmentManager.findFragmentByTag("notPrefs")
                if (frag == null) {
                    (parentFragment as? TimesFragment)?.setFooterText("", false)
                    (parentFragment as? TimesFragment)?.moveToFrag(AlarmsFragment.create(times.id))
                } else {
                    (parentFragment as? TimesFragment)?.setFooterText(
                        getString(R.string.monthly),
                        true
                    )
                    (parentFragment as? TimesFragment)?.back()
                }
            }
            //AppRatingDialog.addToOpenedMenus("notPrefs");
        } else if (i1 == R.id.export) {
            (times.dayTimes as? DayTimesWebProvider)?.syncAsync()
            ExportController.export(requireContext(), times)
        } else if (i1 == R.id.refresh) {
            (times.dayTimes as? DayTimesWebProvider)?.syncAsync()
        } else if (i1 == R.id.share) {
            val txt = StringBuilder(getString(R.string.shareTimes, times.name) + ":")
            val date = LocalDate.now()
            for (v in Vakit.values()) {
                txt.append("\n   ").append(v.string).append(": ").append(
                    LocaleUtils.formatTime(times.getTime(date, v.ordinal).toLocalTime())
                )
            }
            val sharingIntent = Intent(Intent.ACTION_SEND)
            sharingIntent.type = "text/plain"
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.appName))
            sharingIntent.putExtra(Intent.EXTRA_TEXT, txt.toString())
            startActivity(
                Intent.createChooser(
                    sharingIntent,
                    resources.getString(R.string.share)
                )
            )
        }
        return super.onOptionsItemSelected(item)
    }
}