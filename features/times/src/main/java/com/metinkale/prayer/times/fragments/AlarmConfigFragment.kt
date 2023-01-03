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
import android.content.Context
import android.content.DialogInterface
import android.media.AudioManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AlertDialog
import androidx.core.util.Pair
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.metinkale.prayer.App
import com.metinkale.prayer.BaseActivity
import com.metinkale.prayer.times.BuildConfig
import com.metinkale.prayer.times.R
import com.metinkale.prayer.times.alarm.Alarm
import com.metinkale.prayer.times.alarm.AlarmService
import com.metinkale.prayer.times.alarm.sounds.SoundChooser
import com.metinkale.prayer.times.alarm.sounds.SoundChooserAdapter
import com.metinkale.prayer.times.times.Times
import com.metinkale.prayer.times.times.Vakit
import com.metinkale.prayer.times.utils.HorizontalNumberWheel
import com.metinkale.prayer.times.utils.UpdatableStateFlow
import com.metinkale.prayer.times.utils.bindText
import com.metinkale.prayer.times.utils.mapById
import com.metinkale.prayer.utils.LocaleUtils
import com.metinkale.prayer.utils.PermissionUtils
import kotlinx.coroutines.flow.map
import org.joda.time.LocalDateTime
import java.text.DateFormatSymbols
import java.util.*
import kotlin.math.abs

class AlarmConfigFragment : DialogFragment() {
    private val colorOn = App.get().resources.getColor(R.color.colorPrimary)
    private val colorOff = App.get().resources.getColor(R.color.foregroundSecondary)
    private val weekdays = MutableList<TextView?>(7) { null }
    private val timesView = MutableList<ImageView?>(6) { null }
    private val weekdaysText = MutableList<TextView?>(7) { null }
    private val timesText = MutableList<TextView?>(6) { null }
    private lateinit var vibrate: Switch
    private lateinit var autoDelete: Switch
    private lateinit var minute: HorizontalNumberWheel
    private lateinit var minuteText: TextView
    private lateinit var sounds: RecyclerView
    private lateinit var addSound: Button
    private lateinit var volumeBar: SeekBar
    private lateinit var volumeSpinner: Spinner
    private lateinit var volumeTitle: TextView
    private lateinit var adapter: SoundChooserAdapter
    private lateinit var delete: Button
    private lateinit var silenterUp: View
    private lateinit var silenterDown: View
    private lateinit var silenterValue: EditText

    private lateinit var alarms: UpdatableStateFlow<List<Alarm>>
    private lateinit var alarm: UpdatableStateFlow<Alarm?>
    private lateinit var times: UpdatableStateFlow<Times?>

    fun show(frag: Fragment) {
        if (frag.resources.getBoolean(R.bool.large_layout)) {
            val fragmentManager = frag.childFragmentManager
            this.show(fragmentManager, "alarmconfig")
        } else {
            (frag.activity as? BaseActivity)?.moveToFrag(this)
        }
    }

    override fun dismiss() {
        if (resources.getBoolean(R.bool.large_layout)) super.dismiss() else requireActivity().onBackPressed()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.vakit_notprefs_item, container, false)
        silenterDown = view.findViewById(R.id.silenterDecr)
        silenterUp = view.findViewById(R.id.silenterIncr)
        silenterValue = view.findViewById(R.id.silenter)
        volumeBar = view.findViewById(R.id.volume)
        volumeSpinner = view.findViewById(R.id.volumeSpinner)
        volumeTitle = view.findViewById(R.id.volumeText)
        weekdays[0] = view.findViewById(R.id.sunday)
        weekdays[1] = view.findViewById(R.id.monday)
        weekdays[2] = view.findViewById(R.id.tuesday)
        weekdays[3] = view.findViewById(R.id.wednesday)
        weekdays[4] = view.findViewById(R.id.thursday)
        weekdays[5] = view.findViewById(R.id.friday)
        weekdays[6] = view.findViewById(R.id.saturday)
        weekdaysText[0] = view.findViewById(R.id.sundayText)
        weekdaysText[1] = view.findViewById(R.id.mondayText)
        weekdaysText[2] = view.findViewById(R.id.tuesdayText)
        weekdaysText[3] = view.findViewById(R.id.wednesdayText)
        weekdaysText[4] = view.findViewById(R.id.thursdayText)
        weekdaysText[5] = view.findViewById(R.id.fridayText)
        weekdaysText[6] = view.findViewById(R.id.saturdayText)
        timesView[0] = view.findViewById(R.id.fajr)
        timesView[1] = view.findViewById(R.id.sunrise)
        timesView[2] = view.findViewById(R.id.zuhr)
        timesView[3] = view.findViewById(R.id.asr)
        timesView[4] = view.findViewById(R.id.magrib)
        timesView[5] = view.findViewById(R.id.isha)
        timesText[0] = view.findViewById(R.id.fajrText)
        timesText[1] = view.findViewById(R.id.sunText)
        timesText[2] = view.findViewById(R.id.zuhrText)
        timesText[3] = view.findViewById(R.id.asrText)
        timesText[4] = view.findViewById(R.id.magribText)
        timesText[5] = view.findViewById(R.id.ishaaText)
        timesText[0]?.text = Vakit.FAJR.string
        minute = view.findViewById(R.id.timeAdjust)
        minuteText = view.findViewById(R.id.timeText)
        sounds = view.findViewById(R.id.audio)
        addSound = view.findViewById(R.id.addSound)
        delete = view.findViewById(R.id.delete)
        vibrate = view.findViewById(R.id.vibrate)
        autoDelete = view.findViewById(R.id.deleteAfterSound)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bdl = requireArguments()
        times = Times.getTimesById(bdl.getInt("city"))
        if (times.value == null) {
            dismiss()
        }
        alarms =
            times.map({ it?.alarms ?: emptyList() }, { parent, it -> parent?.copy(alarms = it) })
        alarm = alarms.mapById(bdl.getInt("id"), Alarm::id)

        initMinuteAdj()
        initWeekdays()
        initTimes()
        initSounds()
        initVolume()
        initVibrate()
        initDelete()
        initAutodelete()
        initSilenter()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initSilenter() {
        silenterValue.bindText(lifecycle, alarm.map { LocaleUtils.formatNumber(it?.silenter ?: 0) })
        val incr: Runnable = object : Runnable {
            override fun run() {
                alarm.update { it?.copy(silenter = it.silenter + 1) }
                silenterValue.postDelayed(this, 500)
            }
        }
        val decr: Runnable = object : Runnable {
            override fun run() {
                alarm.update { it?.copy(silenter = it.silenter - 1) }
                silenterValue.postDelayed(this, 500)
            }
        }
        silenterUp.setOnTouchListener { v: View, event: MotionEvent ->
            if (!PermissionUtils.get(requireActivity()).pNotPolicy) return@setOnTouchListener false
            when (event.action) {
                MotionEvent.ACTION_DOWN -> v.post(incr)
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> v.removeCallbacks(incr)
            }
            true
        }
        silenterDown.setOnTouchListener { v: View, event: MotionEvent ->
            if (!PermissionUtils.get(requireActivity()).pNotPolicy) return@setOnTouchListener false
            when (event.action) {
                MotionEvent.ACTION_DOWN -> v.post(decr)
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> v.removeCallbacks(decr)
            }
            true
        }
        val listener = View.OnClickListener { v: View? ->
            PermissionUtils.get(requireActivity()).needNotificationPolicy(requireActivity())
        }
        silenterUp.setOnClickListener(listener)
        silenterDown.setOnClickListener(listener)
        silenterValue.setOnClickListener(listener)
        silenterValue.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                alarm.update { it?.copy(silenter = s.toString().toIntOrNull() ?: 0) }
            }
        })
    }

    private fun initMinuteAdj() {
        minute.setListener { newValue: Int ->
            alarm.update { it?.copy(mins = newValue) }
            if (newValue == 0) {
                minuteText.setText(R.string.onTime)
            } else if (newValue < 0) {
                minuteText.text = getString(R.string.beforeTime, -newValue)
            } else {
                minuteText.text = getString(R.string.afterTime, newValue)
            }
        }
        val value = alarm.value?.mins ?: 0
        var max = 180
        if (abs(value) > max) {
            max = abs(value)
        }
        minute.setMax(max)
        minute.setMin(-max)
        minute.setValue(value)
    }

    private fun initAutodelete() {
        autoDelete.isChecked = alarm.value?.removeNotification ?: false
        autoDelete.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            alarm.update { it?.copy(removeNotification = isChecked) }
        }
    }

    private fun initVibrate() {
        vibrate.isChecked = alarm.value?.vibrate ?: false
        vibrate.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            alarm.update { it?.copy(vibrate = isChecked) }
        }
    }

    private fun initDelete() {
        delete.setOnClickListener { v: View? ->
            AlertDialog.Builder(requireActivity())
                .setTitle(times.value?.name ?: "")
                .setMessage(getString(R.string.delAlarmConfirm, alarm.value?.title ?: ""))
                .setNegativeButton(R.string.no) { dialog: DialogInterface, which: Int -> dialog.dismiss() }
                .setPositiveButton(R.string.yes) { dialog: DialogInterface?, which: Int ->
                    alarms.update { it.filter { it.id != alarm.value?.id } }
                    dismiss()
                }.show()
        }
        if (BuildConfig.DEBUG) delete.setOnLongClickListener { v: View? ->
            requireActivity().window.decorView.performHapticFeedback(
                HapticFeedbackConstants.VIRTUAL_KEY,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
            )
            AlarmService.setAlarm(
                requireActivity(),
                Pair(alarm.value, LocalDateTime.now().plusSeconds(5))
            )
            true
        }
    }

    private fun initTimes() {
        for (i in 0..5) {
            val time = Vakit.getByIndex(i)
            timesText[i]?.text = time.string
            setTime(time, isTime(time))
            timesView[i]?.setOnClickListener { v: View? ->
                requireActivity().window.decorView.performHapticFeedback(
                    HapticFeedbackConstants.VIRTUAL_KEY,
                    HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
                )
                setTime(time, !isTime(time))
            }
        }
    }

    private fun initWeekdays() {
        val weekdays = DateFormatSymbols().weekdays

        //rotate arrays to match first weekday
        val firstWeekday = Calendar.getInstance().firstDayOfWeek
        if (firstWeekday != Calendar.SUNDAY) { //java default is sunday, nothing to do
            val wds = listOf(*this.weekdays.toTypedArray())
            val wdTs = listOf(*weekdaysText.toTypedArray())
            for (i in 0..6) {
                this.weekdays[i] = wds[(8 - firstWeekday + i) % 7]
                weekdaysText[i] = wdTs[(8 - firstWeekday + i) % 7]
            }
        }
        for (i in 0..6) {
            this.weekdays[i]?.text = weekdays[i + 1].replace("ال", "").substring(0, 1)
            weekdaysText[i]?.text = weekdays[i + 1]
            val weekday = i + 1
            setWeekday(i + 1, isWeekday(weekday))
            this.weekdays[i]?.setOnClickListener { v: View? ->
                requireActivity().window.decorView.performHapticFeedback(
                    HapticFeedbackConstants.VIRTUAL_KEY,
                    HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
                )
                setWeekday(weekday, !isWeekday(weekday))
            }
        }
    }

    private fun initVolume() {
        val am = requireActivity().getSystemService(Context.AUDIO_SERVICE) as AudioManager
        volumeBar.max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val dataAdapter = ArrayAdapter(
            requireActivity(),
            android.R.layout.simple_spinner_item,
            listOf(
                getString(R.string.customVolume),
                getString(R.string.volumeRingtone),
                getString(R.string.volumeNotification),
                getString(R.string.volumeAlarm),
                getString(R.string.volumeMedia)
            )
        )
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        volumeSpinner.adapter = dataAdapter
        volumeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                volumeBar.visibility = if (position == 0) View.VISIBLE else View.GONE
                alarm.update {
                    it?.copy(
                        volume = when (position) {
                            0 -> volumeBar.progress
                            1 -> Alarm.VOLUME_MODE_RINGTONE
                            2 -> Alarm.VOLUME_MODE_NOTIFICATION
                            3 -> Alarm.VOLUME_MODE_ALARM
                            4 -> Alarm.VOLUME_MODE_MEDIA
                            else -> it.volume
                        }
                    ) ?: it
                }
                adapter.setVolume(alarm.value?.volume ?: 0)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        if ((alarm.value?.volume ?: 0) >= 0) {
            volumeBar.progress = alarm.value?.volume ?: 0
            volumeBar.visibility = View.VISIBLE
            volumeSpinner.setSelection(0)
        } else {
            volumeBar.progress = volumeBar.max / 2
            volumeBar.visibility = View.GONE
            volumeSpinner.setSelection(-(alarm.value?.volume ?: 0))
        }
        volumeBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                alarm.update { it?.copy(volume = progress) }
                adapter.setVolume(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    private fun initSounds() {
        val sounds = alarm.value?.sounds ?: emptyList()
        adapter = SoundChooserAdapter(this.sounds, sounds, alarm.value, false)
        adapter.setVolume(alarm.value?.volume ?: 0)
        adapter.setShowRadioButton(false)
        this.sounds.adapter = adapter
        volumeBar.visibility = if (adapter.itemCount == 0) View.GONE else View.VISIBLE
        volumeTitle.visibility = if (adapter.itemCount == 0) View.GONE else View.VISIBLE
        volumeSpinner.visibility = if (adapter.itemCount == 0) View.GONE else View.VISIBLE
        autoDelete.visibility = if (sounds.isEmpty()) View.GONE else View.VISIBLE
        addSound.setOnClickListener { v: View? ->
            alarm.value?.let {
                SoundChooser.create(it).show(childFragmentManager, "soundchooser")
            }
        }
        adapter.setOnItemClickListener { vh, sound ->
            val v = vh.view
            if (sounds.contains(sound)) {
                v.setOnLongClickListener { v1: View? ->
                    AlertDialog.Builder(requireActivity())
                        .setTitle(sound.name)
                        .setMessage(getString(R.string.delAlarmConfirm, sound.name))
                        .setNegativeButton(R.string.no) { dialog: DialogInterface, which: Int -> dialog.dismiss() }
                        .setPositiveButton(R.string.yes) { dialog: DialogInterface?, which: Int ->
                            alarm.update { it?.copy(sounds = it.sounds.filter { it.id != sound.id }) }
                            initSounds()
                            initVolume()
                        }.show()
                    true
                }
            }
        }
    }

    private fun isWeekday(weekday: Int): Boolean {
        return alarm.value?.weekdays?.contains(weekday) == true
    }

    private fun isTime(time: Vakit): Boolean {
        return alarm.value?.times?.contains(time) == true
    }

    private fun setWeekday(weekday: Int, enable: Boolean) {
        if (enable) {
            alarm.update { it?.copy(weekdays = it.weekdays + weekday) }
        } else if ((alarm.value?.weekdays?.size ?: 0) > 1) {
            alarm.update { it?.copy(weekdays = it.weekdays.filter { it != weekday }) }
        } else {
            return
        }
        weekdays[weekday - 1]?.setTextColor(if (enable) colorOn else colorOff)
    }

    private fun setTime(time: Vakit, enable: Boolean) {
        if (enable) {
            alarm.update { it?.copy(times = it.times + time) }
        } else if ((alarm.value?.times?.size ?: 0) > 1) {
            alarm.update { it?.copy(times = it.times - time) }
        } else {
            return
        }
        timesView[time.ordinal]?.setColorFilter(if (enable) colorOn else colorOff)
    }

    fun onSoundsChanged() {
        initSounds()
    }

    override fun onPause() {
        adapter.resetAudios()
        super.onPause()
    }

    companion object {
        @JvmStatic
        fun create(alarm: Alarm): AlarmConfigFragment {
            val bdl = Bundle()
            bdl.putInt("city", alarm.city.ID)
            bdl.putInt("id", alarm.id)
            val frag = AlarmConfigFragment()
            frag.arguments = bdl
            return frag
        }
    }
}