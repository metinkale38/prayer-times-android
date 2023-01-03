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
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.metinkale.prayer.times.R
import com.metinkale.prayer.times.times.Times
import com.metinkale.prayer.times.times.Vakit
import com.metinkale.prayer.utils.LocaleUtils
import org.joda.time.LocalDate

/**
 * Created by Metin on 21.07.2015.
 */
class ImsakiyeFragment : Fragment() {
    private var adapter: ImsakiyeAdapter? = null
    private var times: Times? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        bdl: Bundle?
    ): View {
        val lv = ListView(activity)
        adapter = ImsakiyeAdapter(activity)
        lv.adapter = adapter
        setTimes(times)
        val addMore = TextView(activity)
        addMore.text = """
            
            ${getString(R.string.showMore)}
            
            """.trimIndent()
        addMore.gravity = Gravity.CENTER
        addMore.setOnClickListener { view: View? ->
            adapter!!.daysInMonth += 7
            adapter!!.notifyDataSetInvalidated()
        }
        lv.addFooterView(addMore)
        lv.setBackgroundResource(R.color.background)
        return lv
    }

    fun setTimes(t: Times?) {
        times = t
        if (adapter != null) {
            adapter!!.times = times
            adapter!!.notifyDataSetChanged()
            adapter!!.daysInMonth = (adapter!!.getItem(1) as LocalDate).dayOfMonth().maximumValue
        }
    }

    inner class ImsakiyeAdapter(context: Context?) : BaseAdapter() {
        var times: Times? = null
        var daysInMonth: Int
        private val date: LocalDate
        private val today: Int
        private val inflater: LayoutInflater

        init {
            val now = LocalDate.now()
            today = now.dayOfMonth
            date = now.withDayOfMonth(1)
            daysInMonth = date.dayOfMonth().maximumValue
            inflater = LayoutInflater.from(context)
        }

        override fun getItemId(position: Int): Long {
            return (position + (times?.ID ?: 0)).toLong()
        }

        override fun getItem(position: Int): Any {
            return date.plusDays(position)
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var convertView = convertView
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.vakit_imsakiye, parent, false)
            }
            val v = convertView as ViewGroup?
            val a: List<CharSequence>
            if (position == 0) {
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
                val cal = getItem(position - 1) as LocalDate
                val daytimes = listOf(
                    times!!.getTime(cal, Vakit.FAJR.ordinal),
                    times!!.getTime(cal, Vakit.SUN.ordinal),
                    times!!.getTime(cal, Vakit.DHUHR.ordinal),
                    times!!.getTime(cal, Vakit.ASR.ordinal),
                    times!!.getTime(cal, Vakit.MAGHRIB.ordinal),
                    times!!.getTime(cal, Vakit.ISHAA.ordinal)
                )
                a = listOf(
                    cal.toString("dd.MM"),
                    daytimes[0]?.toLocalTime().let { LocaleUtils.formatTimeForHTML(it) } ?: "",
                    daytimes[1]?.toLocalTime().let { LocaleUtils.formatTimeForHTML(it) } ?: "",
                    daytimes[2]?.toLocalTime().let { LocaleUtils.formatTimeForHTML(it) } ?: "",
                    daytimes[3]?.toLocalTime().let { LocaleUtils.formatTimeForHTML(it) } ?: "",
                    daytimes[4]?.toLocalTime().let { LocaleUtils.formatTimeForHTML(it) } ?: "",
                    daytimes[5]?.toLocalTime().let { LocaleUtils.formatTimeForHTML(it) } ?: ""
                )
            }
            for (i in 0..6) {
                val tv = v!!.getChildAt(i) as TextView
                tv.text = a[i]
            }
            if (position == today) {
                v!!.setBackgroundResource(R.color.colorPrimary)
            } else if (position == 0) {
                v!!.setBackgroundResource(R.color.accent)
            } else if (position % 2 == 0) {
                v!!.setBackgroundResource(R.color.colorPrimaryLight)
            } else {
                v!!.setBackgroundResource(R.color.background)
            }
            return v
        }

        override fun getCount(): Int {
            return daysInMonth + 1
        }
    }
}