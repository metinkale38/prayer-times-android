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
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.metinkale.prayer.BaseActivity.MainFragment
import com.metinkale.prayer.CrashReporter.recordException
import com.metinkale.prayer.receiver.AppEventManager
import com.metinkale.prayer.times.R
import com.metinkale.prayer.times.alarm.Alarm
import com.metinkale.prayer.times.fragments.AlarmsAdapter.SwitchItem
import com.metinkale.prayer.times.times.Times
import com.metinkale.prayer.times.times.setAlarms
import com.metinkale.prayer.times.utils.Store
import com.metinkale.prayer.times.utils.checkBatteryOptimizations
import com.metinkale.prayer.times.utils.isIgnoringBatteryOptimizations
import com.metinkale.prayer.times.utils.mapById

class AlarmsFragment : MainFragment(), Observer<Times?> {
    private lateinit var times: Store<Times?>
    private lateinit var adapter: AlarmsAdapter
    private lateinit var alarms: Store<List<Alarm>>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.vakit_notprefs, container, false)
        val recyclerView = v.findViewById<RecyclerView>(R.id.recycler)
        times = Times.getTimesById(requireArguments().getInt("city"))
        alarms =
            times.map({ it?.alarms ?: emptyList() }, { parent, it ->
                parent?.copy(alarms = it)
            })

        val layoutManager = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = layoutManager
        adapter = AlarmsAdapter()
        recyclerView.adapter = adapter
        times.data.asLiveData().observe(viewLifecycleOwner, this)
        onChanged(times.current)
        setHasOptionsMenu(true)
        return v
    }


    fun checkBatteryOptimization() {
        if (!isIgnoringBatteryOptimizations(requireContext())) {
            val dialog = AlertDialog.Builder(requireContext()).create()
            dialog.setTitle(R.string.batteryOptimizations)
            dialog.setMessage(getString(R.string.batteryOptimizationsText))
            dialog.setCancelable(false)
            dialog.setButton(
                DialogInterface.BUTTON_POSITIVE, getString(R.string.yes)
            ) { _: DialogInterface?, _: Int ->
                checkBatteryOptimizations(requireContext())
            }
            dialog.setButton(
                DialogInterface.BUTTON_NEGATIVE, getString(R.string.no)
            ) { _: DialogInterface?, _: Int -> }
            dialog.show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val i = item.itemId
        if (i == R.id.add) {
            val alarm = Alarm()
            times.update { it?.copy(alarms = it.alarms + alarm) }
            moveToFrag(AlarmFragment.create(alarm))
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
        times.current?.name?.let { requireActivity().title = it }
    }

    override fun onChanged(times: Times?) {
        adapter.clearAllItems()
        adapter.add(object : SwitchItem() {
            override val name: String
                get() = getString(R.string.ongoingNotification)

            override val checked: Boolean
                get() = this@AlarmsFragment.times.current?.ongoing ?: false

            override fun onChange(checked: Boolean) {
                if (checked) checkBatteryOptimization()
                this@AlarmsFragment.times.update {
                    it?.copy(ongoing = checked)
                }
            }
        })
        adapter.add(getString(R.string.notification))
        times?.alarms?.map { it.id }?.forEach { id ->
            val alarm = alarms.mapById(id, Alarm::id)

            adapter.add(object : SwitchItem() {
                override val name: String
                    get() = alarm.current?.title ?: ""

                override fun onChange(checked: Boolean) {
                    if (checked) {
                        checkBatteryOptimization()
                    }
                    alarm.update {
                        it?.copy(enabled = checked)
                    }
                }

                override val checked: Boolean get() = alarm.current?.enabled ?: false

                override fun onClick() {
                    if (!checked) {
                        Toast.makeText(activity, R.string.activateForMorePrefs, Toast.LENGTH_LONG)
                            .show()
                    } else {
                        checkBatteryOptimization()
                        alarm.current?.let {
                            try {
                                moveToFrag(AlarmFragment.create(it))
                            } catch (e: Exception) {
                                recordException(e)
                            }
                        }

                    }
                }

                override fun onLongClick(): Boolean {
                    AlertDialog.Builder(requireContext())
                        .setTitle(this@AlarmsFragment.times.current?.name ?: "")
                        .setMessage(getString(R.string.delAlarmConfirm, alarm.current?.title))
                        .setNegativeButton(R.string.no) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
                        .setPositiveButton(R.string.yes) { _: DialogInterface?, _: Int ->
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
                times?.id?.also {
                    val alarm = Alarm(cityId = it)
                    alarms.update { it + alarm }
                    moveToFrag(AlarmFragment.create(alarm))
                }
            }

            override fun onLongClick(): Boolean {
                return false
            }
        })
    }

    companion object {
        @JvmStatic
        fun create(cityId: Int): AlarmsFragment {
            val bdl = Bundle()
            bdl.putInt("city", cityId)
            val frag = AlarmsFragment()
            frag.arguments = bdl
            return frag
        }
    }
}