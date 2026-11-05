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
package com.metinkale.prayer.settings

import android.widget.Toast
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import com.metinkale.prayer.R
import com.metinkale.prayer.StringPreference
import dev.metinkale.openprayertimes.OpenPrayerTimes

object FlavorSettings {
    private const val SERVER_URL_KEY = "server_url"
    private const val SERVER_URL_RESET_KEY = "server_url_reset"

    private val serverUrlPreference =
        StringPreference(SERVER_URL_KEY, OpenPrayerTimes.DEFAULT_BASE_URL)

    fun configure() {
        try {
            OpenPrayerTimes.configure(serverUrlPreference.getValue())
        } catch (_: IllegalArgumentException) {
            serverUrlPreference.setValue(OpenPrayerTimes.DEFAULT_BASE_URL)
            OpenPrayerTimes.configure(OpenPrayerTimes.DEFAULT_BASE_URL)
            return
        }

        val normalized = OpenPrayerTimes.baseUrl()
        if (serverUrlPreference.getValue() != normalized) {
            serverUrlPreference.setValue(normalized)
        }
    }

    fun onPreferenceChanged(key: String?) {
        if (key == SERVER_URL_KEY) {
            configure()
        }
    }

    fun addPreferences(fragment: PreferenceFragmentCompat) {
        val category = fragment.findPreference<PreferenceCategory>("other") ?: return
        val context = fragment.requireContext()

        val serverUrl = EditTextPreference(context).apply {
            key = SERVER_URL_KEY
            setTitle(R.string.serverUrl)
            setDialogMessage(context.getString(R.string.serverUrlDescription))
            setDefaultValue(OpenPrayerTimes.DEFAULT_BASE_URL)
            summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()
            text = serverUrlPreference.getValue()
            setOnPreferenceChangeListener { _, newValue ->
                try {
                    OpenPrayerTimes.configure(newValue.toString())
                    true
                } catch (_: IllegalArgumentException) {
                    Toast.makeText(context, R.string.serverUrlInvalid, Toast.LENGTH_LONG).show()
                    false
                }
            }
        }

        val reset = Preference(context).apply {
            key = SERVER_URL_RESET_KEY
            setTitle(R.string.serverUrlReset)
            setOnPreferenceClickListener {
                serverUrlPreference.setValue(OpenPrayerTimes.DEFAULT_BASE_URL)
                OpenPrayerTimes.configure(OpenPrayerTimes.DEFAULT_BASE_URL)
                serverUrl.text = OpenPrayerTimes.DEFAULT_BASE_URL
                true
            }
        }

        category.addPreference(serverUrl)
        category.addPreference(reset)
    }
}
