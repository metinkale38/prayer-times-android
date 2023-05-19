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
import androidx.core.content.ContextCompat
import androidx.core.util.Pair
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.RecyclerView
import com.metinkale.prayer.App
import com.metinkale.prayer.BaseActivity.MainFragment
import com.metinkale.prayer.times.BuildConfig
import com.metinkale.prayer.times.R
import com.metinkale.prayer.times.alarm.Alarm
import com.metinkale.prayer.times.alarm.AlarmService
import com.metinkale.prayer.times.alarm.sounds.SoundChooser
import com.metinkale.prayer.times.alarm.sounds.SoundSerializer
import com.metinkale.prayer.times.alarm.sounds.SoundsAdapter
import com.metinkale.prayer.times.times.Times
import com.metinkale.prayer.times.times.Vakit
import com.metinkale.prayer.times.utils.HorizontalNumberWheel
import com.metinkale.prayer.times.utils.Store
import com.metinkale.prayer.times.utils.mapById
import com.metinkale.prayer.utils.LocaleUtils
import com.metinkale.prayer.utils.PermissionUtils
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import java.text.DateFormatSymbols
import java.time.LocalDateTime
import java.util.*
import kotlin.math.abs

class AlarmFragment : MainFragment() {
    private val colorOn = ContextCompat.getColor(App.get(), R.color.colorPrimary)
    private val colorOff = ContextCompat.getColor(App.get(), R.color.foregroundSecondary)
    private val timesView = MutableList<ImageView?>(6) { null }


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
    private lateinit var adapter: SoundsAdapter
    private lateinit var delete: Button
    private lateinit var silenterUp: View
    private lateinit var silenterDown: View
    private lateinit var silenterValue: EditText

    private lateinit var alarms: Store<List<Alarm>>
    private lateinit var alarm: Store<Alarm?>
    private lateinit var times: Store<Times?>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bdl = requireArguments()
        times = Times.getTimesById(bdl.getInt("city"))
        if (times.current == null) {
            back()
        }
        alarms =
            times.map({ it?.alarms ?: emptyList() }, { parent, it -> parent?.copy(alarms = it) })
        alarm = alarms.mapById(bdl.getInt("id"), Alarm::id)

        setFragmentResultListener("addSound") { _, bundle ->
            bundle.getString("json")?.let { Json.decodeFromString(SoundSerializer, it) }
                ?.let { sound ->
                    alarm.update {
                        it?.let {
                            it.copy(sounds = it.sounds + sound)
                        }
                    }
                }
        }


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
        initMinuteAdj()
        initWeekdays(view)
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
        alarm.data.map { LocaleUtils.formatNumber(it?.silenter ?: 0) }.asLiveData()
            .observe(viewLifecycleOwner) { silenterValue.setText(it) }

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
        val listener = View.OnClickListener {
            PermissionUtils.get(requireActivity()).needNotificationPolicy(requireActivity())
        }
        silenterUp.setOnClickListener(listener)
        silenterDown.setOnClickListener(listener)
        silenterValue.setOnClickListener(listener)
        silenterValue.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val value = s.toString().toIntOrNull() ?: 0
                if (alarm.current?.silenter != value)
                    alarm.update { it?.copy(silenter = value) }
            }
        })
    }

    private fun initMinuteAdj() {
        minute.setListener { newValue: Int ->
            if (alarm.current?.mins != newValue)
                alarm.update { it?.copy(mins = newValue) }
            if (newValue == 0) {
                minuteText.setText(R.string.onTime)
            } else if (newValue < 0) {
                minuteText.text = getString(R.string.beforeTime, -newValue)
            } else {
                minuteText.text = getString(R.string.afterTime, newValue)
            }
        }
        val value = alarm.current?.mins ?: 0
        var max = 180
        if (abs(value) > max) {
            max = abs(value)
        }
        minute.setMax(max)
        minute.setMin(-max)
        minute.setValue(value)
    }

    private fun initAutodelete() {
        autoDelete.isChecked = alarm.current?.removeNotification ?: false
        autoDelete.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            alarm.update { it?.copy(removeNotification = isChecked) }
        }
    }

    private fun initVibrate() {
        vibrate.isChecked = alarm.current?.vibrate ?: false
        vibrate.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            alarm.update { it?.copy(vibrate = isChecked) }
        }
    }

    private fun initDelete() {
        delete.setOnClickListener { _: View? ->
            AlertDialog.Builder(requireActivity())
                .setTitle(times.current?.name ?: "")
                .setMessage(getString(R.string.delAlarmConfirm, alarm.current?.title ?: ""))
                .setNegativeButton(R.string.no) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
                .setPositiveButton(R.string.yes) { _: DialogInterface?, _: Int ->
                    alarms.update { it.filter { it.id != alarm.current?.id } }
                    back()
                }.show()
        }
        if (BuildConfig.DEBUG) delete.setOnLongClickListener {
            requireActivity().window.decorView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            AlarmService.setAlarm(
                requireActivity(),
                Pair(alarm.current, LocalDateTime.now().plusSeconds(5))
            )
            true
        }
    }

    private fun initTimes() {
        for (i in 0..5) {
            val time = Vakit.getByIndex(i)
            timesText[i]?.text = time.string
            timesView[time.ordinal]?.setColorFilter(if (alarm.current?.times?.contains(time) == true) colorOn else colorOff)


            timesView[i]?.setOnClickListener { _: View? ->
                requireActivity().window.decorView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)

                if (alarm.current?.times?.contains(time) == false) {
                    alarm.update { it?.copy(times = it.times + time) }
                    timesView[time.ordinal]?.setColorFilter(colorOn)
                } else if ((alarm.current?.times?.size ?: 0) > 1) {
                    alarm.update { it?.copy(times = it.times - time) }
                    timesView[time.ordinal]?.setColorFilter(colorOff)
                }


            }

        }
    }

    private fun initWeekdays(v: View) {
        val weekdayNames = DateFormatSymbols().weekdays

        var weekdays = listOf(
            R.id.sunday,
            R.id.monday,
            R.id.tuesday,
            R.id.wednesday,
            R.id.thursday,
            R.id.friday,
            R.id.saturday
        ).map { v.findViewById<TextView>(it) }
        var weekdaysText = listOf(
            R.id.sundayText,
            R.id.mondayText,
            R.id.tuesdayText,
            R.id.wednesdayText,
            R.id.thursdayText,
            R.id.fridayText,
            R.id.saturdayText
        ).map { v.findViewById<TextView>(it) }


        //rotate arrays to match first weekday
        val rotate = Calendar.getInstance().firstDayOfWeek - 1
        weekdays = weekdays.takeLast(rotate) + weekdays.dropLast(rotate)
        weekdaysText = weekdaysText.takeLast(rotate) + weekdaysText.dropLast(rotate)
        for (i in 0..6) {
            val weekday = i + 1
            weekdays[i]?.text = weekdayNames[weekday].replace("ال", "").substring(0, 1)
            weekdaysText[i]?.text = weekdayNames[weekday]
            weekdays[i]?.setTextColor(if (alarm.current?.weekdays?.contains(weekday) == true) colorOn else colorOff)
            weekdays[i]?.setOnClickListener { _: View? ->
                requireActivity().window.decorView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)

                if (alarm.current?.weekdays?.contains(weekday) == false) {
                    alarm.update { it?.copy(weekdays = it.weekdays + weekday) }
                    weekdays[weekday - 1]?.setTextColor(colorOn)
                } else if ((alarm.current?.weekdays?.size ?: 0) > 1) {
                    alarm.update { it?.copy(weekdays = it.weekdays - weekday) }
                    weekdays[weekday - 1]?.setTextColor(colorOff)
                }


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
                view: View?,
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
                adapter.setVolume(alarm.current?.volume ?: 0)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        if ((alarm.current?.volume ?: 0) >= 0) {
            volumeBar.progress = alarm.current?.volume ?: 0
            volumeBar.visibility = View.VISIBLE
            volumeSpinner.setSelection(0)
        } else {
            volumeBar.progress = volumeBar.max / 2
            volumeBar.visibility = View.GONE
            volumeSpinner.setSelection(-(alarm.current?.volume ?: 0))
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
        val soundsStore = alarm.map({ it?.sounds ?: emptyList() },
            { alarm, sounds -> alarm?.copy(sounds = sounds) })
        adapter = SoundsAdapter(this.sounds, soundsStore)
        adapter.setVolume(alarm.current?.volume ?: 0)
        this.sounds.adapter = adapter
        volumeBar.visibility = if (adapter.itemCount == 0) View.GONE else View.VISIBLE
        volumeTitle.visibility = if (adapter.itemCount == 0) View.GONE else View.VISIBLE
        volumeSpinner.visibility = if (adapter.itemCount == 0) View.GONE else View.VISIBLE


        soundsStore.data.asLiveData().observe(viewLifecycleOwner) {
            autoDelete.visibility = if (it.isEmpty()) View.GONE else View.VISIBLE
        }
        addSound.setOnClickListener { moveToFrag(SoundChooser()) }

        adapter.onDelete = { sound ->
            AlertDialog.Builder(requireActivity())
                .setTitle(sound.name)
                .setMessage(getString(R.string.delAlarmConfirm, sound.name))
                .setNegativeButton(R.string.no) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
                .setPositiveButton(R.string.yes) { _: DialogInterface?, _: Int ->
                    alarm.update { it?.copy(sounds = it.sounds.filter { it.id != sound.id }) }
                    initSounds()
                    initVolume()
                }.show()
        }
    }

    override fun onPause() {
        adapter.resetAudios()
        super.onPause()
    }

    companion object {
        @JvmStatic
        fun create(alarm: Alarm): AlarmFragment {
            val bdl = Bundle()
            bdl.putInt("city", alarm.city.id)
            bdl.putInt("id", alarm.id)
            val frag = AlarmFragment()
            frag.arguments = bdl
            return frag
        }
    }
}