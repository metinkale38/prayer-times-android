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
package com.metinkale.prayer.compass

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.appcompat.app.AppCompatActivity
import com.metinkale.prayer.Preferences
import com.metinkale.prayer.utils.Geocoder

class LocationPicker : AppCompatActivity(), TextWatcher, OnItemClickListener {
    private var mAdapter: ArrayAdapter<Geocoder.Result>? = null
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.compass_location)
        val list = findViewById<ListView>(R.id.listView)
        list.onItemClickListener = this
        mAdapter = object : ArrayAdapter<Geocoder.Result>(
            this,
            android.R.layout.simple_list_item_1,
            android.R.id.text1
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val v = super.getView(position, convertView, parent)
                if (v is TextView) {
                    v.setTextColor(Color.BLACK)
                    v.text = getItem(position)!!.name
                }
                return v
            }
        }
        list.adapter = mAdapter
        val city = findViewById<EditText>(R.id.location)
        city.addTextChangedListener(this)
    }

    override fun afterTextChanged(txt: Editable) {
        Geocoder.search(txt.toString()) { result: Geocoder.Result? ->
            if (result == null) return@search
            mAdapter!!.clear()
            mAdapter!!.add(result)
        }
    }

    override fun beforeTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {}
    override fun onTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {}
    override fun onItemClick(arg0: AdapterView<*>?, arg1: View, pos: Int, arg3: Long) {
        val a = mAdapter!!.getItem(pos)
        Preferences.COMPASS_LAT = a!!.lat.toFloat()
        Preferences.COMPASS_LNG = a.lon.toFloat()
        finish()
    }
}