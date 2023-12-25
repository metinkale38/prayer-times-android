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
package com.metinkale.prayer.settings

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckedTextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import com.metinkale.prayer.BaseActivity
import com.metinkale.prayer.Preferences
import com.metinkale.prayer.calendar.runCalendarIntegration
import com.metinkale.prayer.utils.LocaleUtils.getSupportedLanguages
import com.metinkale.prayer.utils.LocaleUtils.locale
import com.metinkale.prayer.utils.Utils
import java.util.*

class SettingsFragment : PreferenceFragmentCompat(), Preference.OnPreferenceClickListener,
    Preference.OnPreferenceChangeListener {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.settings)
        if (arguments != null && requireArguments().getBoolean("showKerahatDuration"))
            preferenceScreen =
                findPreference("kerahatDuration") as PreferenceScreen else {
            findPreference("numbers").onPreferenceChangeListener = this
            findPreference("backupRestore").onPreferenceClickListener = this
            findPreference("calendarIntegration").onPreferenceClickListener = this
            findPreference("ongoingIcon").onPreferenceClickListener = this
            findPreference("ongoingNumber").onPreferenceClickListener = this
            findPreference("kerahatDuration").onPreferenceClickListener = this
            if (Build.VERSION.SDK_INT < 24) findPreference("showLegacyWidgets").isEnabled = false
            findPreference("arabicNames").isEnabled = Locale("ar").language != locale.language
            val lang = findPreference("language") as ListPreference
            lang.onPreferenceChangeListener = this
            val languages = getSupportedLanguages(
                requireActivity()
            )
            val entries = arrayOfNulls<CharSequence>(languages.size)
            val values = arrayOfNulls<CharSequence>(languages.size)
            for (i in languages.indices) {
                val trans = languages[i]
                entries[i] = trans.displayText
                values[i] = trans.language
            }
            lang.entries = entries
            lang.entryValues = values
            val colors = arrayOf(
                intArrayOf(0, 0),
                intArrayOf(-0x1, -0xcccccd),
                intArrayOf(-0x222223, -0xddddde),
                intArrayOf(-0x444445, -0xeeeeef),
                intArrayOf(-0x666667, -0x1000000),
                intArrayOf(-0x99999a, -0x1),
                intArrayOf(-0xbbbbbc, -0x111112),
                intArrayOf(-0xddddde, -0x222223),
                intArrayOf(-0x1000000, -0x333334)
            )
            val ongoingColor = findPreference("ongoingColor")
            ongoingColor.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    val dlg = AlertDialog.Builder(
                        requireActivity()
                    )
                    dlg.setAdapter(object : ArrayAdapter<Any?>(
                        requireActivity(), android.R.layout.select_dialog_singlechoice
                    ) {
                        override fun getCount(): Int = colors.size

                        override fun getView(
                            position: Int, convertView: View?, parent: ViewGroup
                        ): View {
                            val v = LayoutInflater.from(context).inflate(
                                android.R.layout.select_dialog_singlechoice, parent, false
                            ) as CheckedTextView
                            val p = Utils.convertDpToPixel(context, 4f).toInt()
                            v.setPadding(4 * p, p, 4 * p, p)
                            v.text = if (position == 0) "System" else "Abc"
                            v.setBackgroundColor(if (position == 0) Color.WHITE else colors[position][0])
                            v.setTextColor(if (position == 0) Color.BLACK else colors[position][1])
                            v.isChecked =
                                colors[position][0] == Preferences.ONGOING_BG_COLOR && colors[position][1] == Preferences.ONGOING_TEXT_COLOR
                            return v
                        }
                    }) { _: DialogInterface?, which: Int ->
                        Preferences.ONGOING_BG_COLOR = colors[which][0]
                        Preferences.ONGOING_TEXT_COLOR = colors[which][1]
                    }
                    dlg.show()
                    true
                }
        }
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        when (preference.key) {
            "backupRestore" -> {
                startActivity(Intent(activity, BackupRestoreActivity::class.java))
                return true
            }
            "kerahatDuration" -> {
                val frag = SettingsFragment()
                val bdl = Bundle()
                bdl.putBoolean("showKerahatDuration", true)
                frag.arguments = bdl
                (activity as BaseActivity?)!!.moveToFrag(frag)
                return true
            }
            "calendarIntegration" -> {
                runCalendarIntegration(requireActivity())
                return true
            }
        }
        return false
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        if (preference is NumberPickerPreference) {
            var handled = false
            if (parentFragment is OnPreferenceDisplayDialogCallback) {
                handled =
                    (parentFragment as OnPreferenceDisplayDialogCallback).onPreferenceDisplayDialog(
                        this, preference
                    )
            }
            if (!handled && activity is OnPreferenceDisplayDialogCallback) {
                handled =
                    (activity as OnPreferenceDisplayDialogCallback?)!!.onPreferenceDisplayDialog(
                        this, preference
                    )
            }
            if (handled) {
                return
            }
            if (requireFragmentManager().findFragmentByTag("numberpicker") != null) {
                return
            }
            val f: DialogFragment =
                NumberPickerPreference.NumberPickerPreferenceDialogFragmentCompat.newInstance(
                    preference.getKey()
                )
            f.setTargetFragment(this, 0)
            f.show(requireFragmentManager(), "numberpicker")
        } else super.onDisplayPreferenceDialog(preference)
    }

    override fun onPreferenceChange(pref: Preference, newValue: Any): Boolean {
        if ("language" == pref.key || "digits" == pref.key) {
            if ("language" == pref.key) Preferences.LANGUAGE = newValue as String
            val act: Activity? = activity
            act!!.finish()
            val i = Intent(act, act.javaClass)
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            act.startActivity(i)
        }
        return true
    }
}