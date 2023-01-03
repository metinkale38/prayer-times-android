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

import android.os.Bundle
import com.metinkale.prayer.BaseActivity
import com.metinkale.prayer.times.LocationReceiver.Companion.triggerUpdate
import com.metinkale.prayer.times.fragments.SearchCityFragment
import com.metinkale.prayer.times.fragments.TimesFragment

open class MainActivity : BaseActivity(R.string.appName, R.mipmap.ic_launcher, TimesFragment()) {
    override fun onStart() {
        super.onStart()
        triggerUpdate(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent.getBooleanExtra("openCitySearch", false)) {
            moveToFrag(SearchCityFragment())
        }
    }
}