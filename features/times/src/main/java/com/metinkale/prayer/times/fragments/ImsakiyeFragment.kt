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

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.metinkale.prayer.times.R
import com.metinkale.prayer.times.times.*
import com.metinkale.prayer.utils.LocaleUtils
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Created by Metin on 21.07.2015.
 */
class ImsakiyeFragment : Fragment() {
    private var adapter: ImsakiyeAdapter? = null
    private var times: Times? = null
    private val today = LocalDate.now()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        bdl: Bundle?
    ): View {
        val layout = LinearLayout(requireContext());
        adapter = ImsakiyeAdapter(requireContext())
        layout.addView(adapter!!.getView(-1, null, layout))
        val lv = ListView(requireContext())
        layout.addView(
            lv,
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        lv.adapter = adapter
        times?.let { setTimes(it) }
        layout.setBackgroundResource(R.color.background)
        layout.orientation = LinearLayout.VERTICAL
        return layout
    }

    fun setTimes(t: Times) {
        times = t
        adapter?.let { adapter ->
            adapter.times = times
            t.dayTimes.let {
                when (it) {
                    is DayTimesWebProvider -> {
                        adapter.minDate =
                            listOfNotNull(today.withDayOfMonth(1), it.firstSyncedDay).max()
                        adapter.maxDate = it.lastSyncedDay ?: adapter.minDate
                    }

                    is DayTimesCalcProvider -> {
                        adapter.minDate = today.withDayOfMonth(1)
                        adapter.maxDate = adapter.minDate.plusYears(1).minusDays(1)
                    }
                }
            }

            adapter.notifyDataSetChanged()

        }
    }

    inner class ImsakiyeAdapter(context: Context) : BaseAdapter() {
        var times: Times? = null
        var minDate: LocalDate = today.withDayOfMonth(1)
        var maxDate: LocalDate = minDate.plusDays(30)
        private val inflater: LayoutInflater = LayoutInflater.from(context)


        override fun getItemId(position: Int): Long {
            return (position + (times?.id ?: 0)).toLong()
        }

        override fun getItem(position: Int): Any {
            return minDate.plusDays(position.toLong())
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val v: ViewGroup =
                (convertView ?: inflater.inflate(
                    R.layout.vakit_imsakiye,
                    parent,
                    false
                )) as ViewGroup
            val a: List<CharSequence>
            if (position == -1) {
                a = listOf(
                    getString(R.string.date),
                    getString(R.string.fajr),
                    getString(R.string.sun),
                    getString(R.string.zuhr),
                    getString(R.string.asr),
                    getString(R.string.maghrib),
                    getString(R.string.ishaa)
                )
            } else if (times == null) {
                a = listOf("00:00", "00:00", "00:00", "00:00", "00:00", "00:00", "00:00")
            } else {
                val date = getItem(position) as LocalDate
                val daytimes = listOf(
                    times!!.getTime(date, Vakit.FAJR.ordinal),
                    times!!.getTime(date, Vakit.SUN.ordinal),
                    times!!.getTime(date, Vakit.DHUHR.ordinal),
                    times!!.getTime(date, Vakit.ASR.ordinal),
                    times!!.getTime(date, Vakit.MAGHRIB.ordinal),
                    times!!.getTime(date, Vakit.ISHAA.ordinal)
                )
                a = listOf(
                    date.format(DateTimeFormatter.ofPattern("dd.MM")),
                    daytimes[0].toLocalTime().let { LocaleUtils.formatTimeForHTML(it) },
                    daytimes[1].toLocalTime().let { LocaleUtils.formatTimeForHTML(it) },
                    daytimes[2].toLocalTime().let { LocaleUtils.formatTimeForHTML(it) },
                    daytimes[3].toLocalTime().let { LocaleUtils.formatTimeForHTML(it) },
                    daytimes[4].toLocalTime().let { LocaleUtils.formatTimeForHTML(it) },
                    daytimes[5].toLocalTime().let { LocaleUtils.formatTimeForHTML(it) }
                )
            }
            for (i in 0..6) {
                val tv = v.getChildAt(i) as TextView
                tv.text = a[i]
            }
            if (getItem(position) as? LocalDate == today) {
                v.setBackgroundResource(R.color.colorPrimary)
            } else if (position == -1) {
                v.setBackgroundResource(R.color.accent)
            } else if (position % 2 == 0) {
                v.setBackgroundResource(R.color.background)
            } else {
                v.setBackgroundResource(R.color.colorPrimaryLight)
            }
            return v
        }

        override fun getCount(): Int {
            return ChronoUnit.DAYS.between(minDate, maxDate).toInt()
        }
    }
}