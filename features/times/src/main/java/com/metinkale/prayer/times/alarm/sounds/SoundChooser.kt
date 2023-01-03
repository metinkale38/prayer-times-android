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

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.inlineactivityresult.startActivityForResult
import com.metinkale.prayer.times.R
import com.metinkale.prayer.times.alarm.Alarm
import com.metinkale.prayer.times.alarm.sounds.Sounds.downloadData
import com.metinkale.prayer.times.alarm.sounds.Sounds.getSound
import com.metinkale.prayer.times.alarm.sounds.Sounds.rootSounds
import com.metinkale.prayer.times.alarm.sounds.Sounds.sounds
import com.metinkale.prayer.times.fragments.AlarmConfigFragment
import com.metinkale.prayer.times.times.Times
import com.metinkale.prayer.times.utils.UpdatableStateFlow
import com.metinkale.prayer.times.utils.mapById
import com.metinkale.prayer.utils.FileChooser
import com.metinkale.prayer.utils.PermissionUtils
import java.io.File

class SoundChooser : DialogFragment() {
    private lateinit var adapter: SoundChooserAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var alarm: UpdatableStateFlow<Alarm?>

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bdl = arguments
        val id = bdl?.getInt("id")
        val times = Times.getTimesById(bdl!!.getInt("city"))
        val alarms = times.map(get = { it?.alarms ?: emptyList() },
            set = { parent, it -> parent?.copy(alarms = it) })
        alarm = alarms.mapById(id, Alarm::id)
        val sounds = alarm.map(get = { it?.sounds ?: emptyList() },
            { parent, it -> parent?.copy(sounds = it) })

        val b = AlertDialog.Builder(
            requireActivity()
        ).setTitle(R.string.addAudio).setPositiveButton(
            R.string.ok
        ) { dialog: DialogInterface, whichButton: Int ->
            val selected = adapter.selected
            if (selected != null) {
                sounds.update { it + selected }
            }
            (parentFragment as AlarmConfigFragment?)!!.onSoundsChanged()
            dialog.dismiss()
        }.setNegativeButton(
            R.string.cancel
        ) { dialog: DialogInterface, _: Int -> dialog.dismiss() }.setNeutralButton(
            R.string.other
        ) { dialog: DialogInterface, _: Int ->
            val builder = AlertDialog.Builder(
                requireActivity()
            )
            builder.setTitle(R.string.other).setItems(R.array.soundPicker,
                DialogInterface.OnClickListener { _: DialogInterface?, which1: Int ->
                    if (which1 == 0) {
                        if (!PermissionUtils.get(requireActivity()).pStorage) {
                            PermissionUtils.get(requireActivity()).needStorage(requireActivity())
                            return@OnClickListener
                        }
                        val chooser = FileChooser(requireActivity())
                        chooser.showDialog()
                        chooser.setFileListener { file: File? ->
                            val selected = getSound(Uri.fromFile(file).toString())
                            sounds.update { it + selected }
                            (parentFragment as AlarmConfigFragment?)!!.onSoundsChanged()
                            dialog.dismiss()
                        }
                    } else {
                        val i = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
                        i.putExtra(
                            RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL
                        )
                        i.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, false)
                        i.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                        requireActivity().startActivityForResult(
                            i, 71
                        ) { success: Boolean?, result: Intent ->
                            val selected = getSound(
                                result.getParcelableExtra<Parcelable>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
                                    .toString()
                            )

                            sounds.update { it + selected }
                            (parentFragment as AlarmConfigFragment?)!!.onSoundsChanged()
                            dialog.dismiss()
                        }
                    }
                })
            builder.show()
        }
        b.setView(createView(requireActivity().layoutInflater, null, null))
        return b.create()
    }

    fun createView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.sound_chooser, container, false)
        recyclerView = view.findViewById(R.id.list)
        if (sounds.isEmpty()) {
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
                    ).needStorage(
                        requireActivity()
                    )
                    return
                }
            }
        }
        adapter = SoundChooserAdapter(
            recyclerView, sounds, alarm.value, true
        )
        recyclerView.adapter = adapter
    }

    override fun onPause() {
        super.onPause()
        adapter.resetAudios()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT
        )
    }

    companion object {
        fun create(alarm: Alarm): SoundChooser {
            val bdl = Bundle()
            bdl.putInt("city", alarm.city.ID)
            bdl.putInt("id", alarm.id)
            val frag = SoundChooser()
            frag.arguments = bdl
            return frag
        }
    }
}