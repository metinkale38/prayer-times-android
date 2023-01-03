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
package com.metinkale.prayer.times

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.NumberPicker
import androidx.appcompat.app.AppCompatActivity
import com.metinkale.prayer.times.alarm.SilenterReceiver
import com.metinkale.prayer.utils.PermissionUtils

class SilenterPrompt : AppCompatActivity() {
    private lateinit var widgets: SharedPreferences
    public override fun onCreate(bdl: Bundle?) {
        super.onCreate(bdl)
        widgets = getSharedPreferences("widgets", 0)
        setContentView(R.layout.vakit_silenterprompt)
        val np = findViewById<NumberPicker>(R.id.numberPicker)
        np.minValue = 1
        np.maxValue = 300
        np.value = widgets.getInt("silenterWidget", 15)
        findViewById<View>(R.id.cancel).setOnClickListener { view: View? -> finish() }
        findViewById<View>(R.id.ok).setOnClickListener { v: View ->
            widgets.edit().putInt("silenterWidget", np.value).apply()
            if (PermissionUtils.get(this).pNotPolicy) SilenterReceiver.silent(
                v.context,
                np.value
            ) else PermissionUtils.get(this).needNotificationPolicy(this)
            finish()
        }
    }
}