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
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.asLiveData
import com.metinkale.prayer.times.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * Created by Metin on 21.07.2015.
 */
class SettingsFragment : Fragment() {
    private val timesIdFlow = MutableStateFlow(-1)
    var timesId: Int
        get() = timesIdFlow.value
        set(value) = timesIdFlow.update { value }

    private val viewModel = SettingsFragmentViewModel(timesIdFlow)


    private fun EditText.bind(
        get: SettingsFragmentViewModel.() -> Flow<String>,
        set: SettingsFragmentViewModel.(String) -> Unit
    ) {
        get(viewModel).asLiveData().observe({ lifecycle }) { setText(it) }
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) =
                set.invoke(viewModel, editable.toString())
        })
    }


    @SuppressLint("WrongViewCast")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        bdl: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.vakit_settings, container, false)
        v.findViewById<EditText>(R.id.name).bind({ name }, { setName(it) })
        v.findViewById<EditText>(R.id.timezonefix).bind({ tz }, { setTz(it) })

        v.findViewById<ViewGroup>(R.id.tz).also {
            it.findViewById<View>(R.id.plus).setOnClickListener { viewModel.plusTz() }
            it.findViewById<View>(R.id.minus).setOnClickListener { viewModel.minusTz() }
        }

        val vg = v.findViewById<ViewGroup>(R.id.minAdj)
        for (i in 0..5) {
            vg.getChildAt(i + 1).also {
                it.findViewById<EditText>(R.id.nr)
                    .bind({ minuteAdj.map { it?.get(i)?.toString() ?: "0" } }, { setMinAdj(i, it) })
                it.findViewById<View>(R.id.plus).setOnClickListener { viewModel.plus(i) }
                it.findViewById<View>(R.id.minus).setOnClickListener { viewModel.minus(i) }
            }
        }
        return v
    }
}
