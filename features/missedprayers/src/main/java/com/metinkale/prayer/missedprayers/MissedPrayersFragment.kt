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
package com.metinkale.prayer.missedprayers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.edit
import com.metinkale.prayer.BaseActivity
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

class MissedPrayersFragment : BaseActivity.MainFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.kaza_main, container, false)
        val vg = view.findViewById<ViewGroup>(R.id.main)

        val ids = intArrayOf(
            R.string.morningPrayer,
            R.string.zuhr,
            R.string.asr,
            R.string.maghrib,
            R.string.ishaa,
            R.string.witr,
            R.string.fasting
        )
        for (i in 0..6) {
            val v = vg.getChildAt(i)
            val name = v.findViewById<TextView>(R.id.text)
            val nr = v.findViewById<EditText>(R.id.nr)
            val plus = v.findViewById<ImageView>(R.id.plus)
            val minus = v.findViewById<ImageView>(R.id.minus)

            name.setText(ids[i])

            plus.setOnClickListener { _: View? ->
                var txt = nr.getText().toString()
                if (txt.isEmpty()) txt = "0"
                var i12 = 0
                try {
                    i12 = txt.toInt()
                } catch (_: Exception) {
                } finally {
                    i12++
                }
                nr.setText("$i12")
            }

            minus.setOnClickListener { _: View? ->
                var txt = nr.getText().toString()
                if (txt.isEmpty()) txt = "0"
                var i1 = 0
                try {
                    i1 = txt.toInt()
                } finally {
                    i1--
                }
                nr.setText("$i1")
            }
        }

        return view
    }

    override fun onResume() {
        super.onResume()

        val vg = requireView().findViewById<ViewGroup>(R.id.main)
        val prefs = requireActivity().getSharedPreferences("kaza", 0)
        for (i in 0..6) {
            val v = vg.getChildAt(i)
            val nr = v.findViewById<EditText>(R.id.nr)
            nr.setText(prefs.getString("count" + i, "0"))
        }



        prefs.getString("updated", null)?.let { lastUpdate ->
            val formatter = DateTimeFormatter
                .ofLocalizedDateTime(FormatStyle.MEDIUM)
                .withLocale(Locale.GERMANY)
            val formatted = LocalDateTime.parse(lastUpdate).format(formatter)
            vg.findViewById<TextView>(R.id.lastUpdate).text = formatted
        }
    }

    override fun onPause() {
        super.onPause()

        val vg = requireActivity().findViewById<ViewGroup>(R.id.main)
        var updated = false
        requireActivity().getSharedPreferences("kaza", 0).let { prefs ->
            prefs.edit {
                for (i in 0..6) {
                    val v = vg.getChildAt(i)
                    val nr = v.findViewById<EditText>(R.id.nr)
                    val key = "count$i"
                    val value = nr.getText().toString()

                    if (prefs.getString(key, "") != value) {
                        updated = true
                    }

                    putString(key, value)
                }

                if (updated) {
                    putString("updated", LocalDateTime.now().toString())
                }
            }
        }


    }
}
