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
package com.metinkale.prayer.times.alarm.sounds

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.media.RingtoneManager
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.RecyclerView
import com.metinkale.prayer.App
import com.metinkale.prayer.BaseActivity.MainFragment
import com.metinkale.prayer.times.R
import com.metinkale.prayer.times.alarm.sounds.Sounds.downloadData
import com.metinkale.prayer.times.alarm.sounds.Sounds.getSound
import com.metinkale.prayer.times.alarm.sounds.Sounds.rootSounds
import com.metinkale.prayer.utils.PermissionUtils
import kotlinx.serialization.json.Json

class SoundChooser : MainFragment() {
    private lateinit var adapter: SoundChooserAdapter
    private lateinit var recyclerView: RecyclerView


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.sound_chooser, container, false)
        recyclerView = view.findViewById(R.id.list)

        view.findViewById<Button>(R.id.ok).setOnClickListener {
            val selected = adapter.selected
            selected?.let {
                setFragmentResult("addSound", Bundle().apply {
                    putString("json", Json.encodeToString(SoundSerializer, it))
                })
            }
            back()
        }

        view.findViewById<Button>(R.id.cancel).setOnClickListener {
            back()
        }

        view.findViewById<Button>(R.id.other).setOnClickListener {

            val builder = AlertDialog.Builder(requireActivity())
            builder.setTitle(R.string.other)
                .setItems(R.array.soundPicker) { _: DialogInterface?, which1: Int ->
                    if (which1 == 0) {
                        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                            addCategory(Intent.CATEGORY_OPENABLE)
                            type = "audio/*"
                        }
                        filePicker.launch(intent)
                    } else {
                        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL)
                            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, false)
                            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                        }
                        audioPicker.launch(intent)
                    }
                }
            builder.show()


        }

        if (Sounds.sounds.isEmpty()) {
            downloadData(activity) { init() }
        }

        init()
        return view
    }


    private fun init() {
        val sounds = rootSounds
        for (sound in sounds) {
            if (sound is UserSound) {
                try {
                    sound.name
                } catch (e: SecurityException) {
                    if (!PermissionUtils.get(requireActivity()).pStorage) PermissionUtils.get(
                        requireActivity()
                    ).needStorage(requireActivity())
                    return
                }
            }
        }
        adapter = SoundChooserAdapter(recyclerView, sounds)
        recyclerView.adapter = adapter
    }

    override fun onPause() {
        super.onPause()
        adapter.resetAudios()
    }


    private val filePicker: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result?.data?.data?.also { uri ->
                    val contentResolver = App.get().contentResolver

                    val takeFlags: Int =
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    contentResolver.takePersistableUriPermission(uri, takeFlags)

                    setFragmentResult("addSound", Bundle().apply {
                        putString(
                            "json",
                            Json.encodeToString(SoundSerializer, getSound(uri.toString()))
                        )
                    })
                    back()

                }
            }
        }

    private val audioPicker: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result?.data?.also { result ->
                    val selected = getSound(
                        result.getParcelableExtra<Parcelable>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
                            .toString()
                    )

                    setFragmentResult("addSound", Bundle().apply {
                        putString("json", Json.encodeToString(SoundSerializer, selected))
                    })
                    back()
                }
            }
        }

}