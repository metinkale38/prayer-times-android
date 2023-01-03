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

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.metinkale.prayer.CrashReporter.recordException
import com.metinkale.prayer.receiver.InternalBroadcastReceiver
import com.metinkale.prayer.times.R
import com.metinkale.prayer.times.alarm.Alarm
import com.metinkale.prayer.times.fragments.AlarmConfigFragment.Companion.create
import com.metinkale.prayer.times.fragments.AlarmsAdapter.SwitchItem
import com.metinkale.prayer.times.times.Times
import com.metinkale.prayer.times.times.setAlarms
import com.metinkale.prayer.times.utils.UpdatableStateFlow
import com.metinkale.prayer.times.utils.checkBatteryOptimizations
import com.metinkale.prayer.times.utils.isIgnoringBatteryOptimizations
import com.metinkale.prayer.times.utils.mapById

class AlarmsFragment : Fragment(), Observer<Times?> {
    private lateinit var times: UpdatableStateFlow<Times?>
    private lateinit var adapter: AlarmsAdapter
    private val alarms by lazy {
        times.map({ it?.alarms ?: emptyList() },
            { parent, it -> parent?.copy(alarms = it) })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.vakit_notprefs, container, false)
        val recyclerView = v.findViewById<RecyclerView>(R.id.recycler)
        times = Times.getTimesById(requireArguments().getInt("city"))


        val layoutManager = LinearLayoutManager(activity)
        recyclerView.layoutManager = layoutManager
        adapter = AlarmsAdapter()
        recyclerView.adapter = adapter
        times.asLiveData().observe({ lifecycle }, this)
        onChanged(times.value)
        setHasOptionsMenu(true)
        return v
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val i = item.itemId
        if (i == R.id.add) {
            val alarm = Alarm()
            times.update { it?.copy(alarms = it.alarms + alarm) }
            create(alarm).show(this)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPause() {
        super.onPause()
        requireActivity().title = getString(R.string.appName)
        Times.setAlarms()
    }

    override fun onResume() {
        super.onResume()
        times.value?.name?.let { requireActivity().title = it }
    }

    override fun onChanged(times: Times?) {
        adapter.clearAllItems()
        adapter.add(object : SwitchItem() {
            override val name: String
                get() = getString(R.string.ongoingNotification)

            override val checked: Boolean
                get() = this@AlarmsFragment.times.value?.ongoing ?: false

            override fun onChange(checked: Boolean) {
                this@AlarmsFragment.times.update { it?.copy(ongoing = checked) }
                InternalBroadcastReceiver.sender(activity).sendTimeTick()
            }
        })
        adapter.add(getString(R.string.notification))
        times?.alarms?.map { it.id }?.forEach { id ->
            val alarm = alarms.mapById(id, Alarm::id)

            adapter.add(object : SwitchItem() {
                override val name: String
                    get() = alarm.value?.title ?: ""

                override fun onChange(checked: Boolean) {
                    if (checked && !isIgnoringBatteryOptimizations(activity!!)) {
                        val dialog = AlertDialog.Builder(
                            activity!!
                        ).create()
                        dialog.setTitle(R.string.batteryOptimizations)
                        dialog.setMessage(getString(R.string.batteryOptimizationsText))
                        dialog.setCancelable(false)
                        dialog.setButton(
                            DialogInterface.BUTTON_POSITIVE,
                            getString(R.string.yes)
                        ) { dialogInterface: DialogInterface?, i: Int ->
                            checkBatteryOptimizations(
                                activity!!
                            )
                        }
                        dialog.setButton(
                            DialogInterface.BUTTON_NEGATIVE,
                            getString(R.string.no)
                        ) { dialogInterface: DialogInterface?, i: Int -> }
                        dialog.show()
                    }
                    alarm.update { it?.copy(isEnabled = true) }
                }

                override val checked: Boolean get() = alarm.value?.isEnabled ?: false

                override fun onClick() {
                    if (!checked) {
                        Toast.makeText(activity, R.string.activateForMorePrefs, Toast.LENGTH_LONG)
                            .show()
                    } else {
                        alarm.value?.let {
                            try {
                                create(it).show(this@AlarmsFragment)
                            } catch (e: Exception) {
                                recordException(e)
                            }
                        }

                    }
                }

                override fun onLongClick(): Boolean {
                    AlertDialog.Builder(activity!!)
                        .setTitle(this@AlarmsFragment.times.value?.name ?: "")
                        .setMessage(getString(R.string.delAlarmConfirm, alarm.value?.title))
                        .setNegativeButton(R.string.no) { dialog: DialogInterface, which: Int -> dialog.dismiss() }
                        .setPositiveButton(R.string.yes) { dialog: DialogInterface?, which: Int ->
                            alarm.update { null }
                            onChanged(times)
                        }.show()
                    return true
                }
            })
        }
        adapter.add("")
        adapter.add(object : AlarmsAdapter.Button() {
            override val name: String
                get() = getString(R.string.addAlarm)

            override fun onClick() {
                times?.ID?.also {
                    val alarm = Alarm(cityId = it)
                    alarms.update { it + alarm }
                    create(alarm).show(this@AlarmsFragment)
                }
            }

            override fun onLongClick(): Boolean {
                return false
            }
        })
    }

    companion object {
        @JvmStatic
        fun create(cityId:Int): AlarmsFragment {
            val bdl = Bundle()
            bdl.putInt("city", cityId)
            val frag = AlarmsFragment()
            frag.arguments = bdl
            return frag
        }
    }
}