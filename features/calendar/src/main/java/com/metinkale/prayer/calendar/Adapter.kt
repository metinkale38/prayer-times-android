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
package com.metinkale.prayer.calendar

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.metinkale.prayer.date.HijriDate
import com.metinkale.prayer.date.HijriDate.Companion.getHolydaysForGregYear
import com.metinkale.prayer.date.HijriDate.Companion.getHolydaysForHijriYear
import com.metinkale.prayer.date.HijriDay
import com.metinkale.prayer.utils.LocaleUtils.formatDate
import com.metinkale.prayer.utils.LocaleUtils.getHolyday
import com.metinkale.prayer.utils.LocaleUtils.locale
import java.util.*

class Adapter(private val ctx: Context, year: Int, private val isHijri: Boolean) :
    ArrayAdapter<IntArray?>(ctx, R.layout.calendar_item) {
    private var hijridays: List<Pair<HijriDate, HijriDay>>
    private var hasInfo: Boolean =
        Locale("de").language == locale.language || Locale("tr").language == locale.language

    init {
        hijridays =
            (if (isHijri) getHolydaysForHijriYear(year) else getHolydaysForGregYear(year)).toList()
    }

    override fun getView(pos: Int, convertView: View?, parent: ViewGroup): View {
        lateinit var vh: ViewHolder

        val v = convertView?.also {
            vh = convertView.getTag(R.id.viewholder) as ViewHolder
        } ?: run {
            val inflater =
                ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            inflater.inflate(R.layout.calendar_item, parent, false).also {
                vh = ViewHolder(it, isHijri)
                it.setTag(R.id.viewholder, vh)
            }
        }

        val pair: Pair<HijriDate, HijriDay> = hijridays[pos]
        val hijri = pair.first
        val holyday = pair.second
        if (holyday === HijriDay.MONTH) {
            val greg = hijri.toLocalDate()
            vh.hicri.text = formatDate(hijri)
            vh.date.text = formatDate(greg)
            vh.name.visibility = View.GONE
            vh.next.visibility = View.GONE
            vh.view.setBackgroundResource(R.color.backgroundSecondary)
        } else {
            val greg = hijri.toLocalDate()
            vh.hicri.text = formatDate(hijri)
            vh.date.text = formatDate(greg)
            vh.name.text = getHolyday(holyday)
            vh.next.visibility = if (hasInfo) View.VISIBLE else View.INVISIBLE
            vh.name.visibility = View.VISIBLE
            vh.view.setBackgroundColor(Color.TRANSPARENT)
        }
        v.tag = holyday
        return v
    }

    override fun getCount(): Int {
        return hijridays.size
    }

    data class ViewHolder(
        val view: View,
        val isHijri: Boolean,
        val name: TextView = view.findViewById(R.id.name),
        val date: TextView = view.findViewById(if (isHijri) R.id.hicri else R.id.date),
        val hicri: TextView = view.findViewById(if (isHijri) R.id.date else R.id.hicri),
        val next: ImageView = view.findViewById(R.id.next)
    )
}