package com.metinkale.prayer.times.fragments

import com.metinkale.prayer.times.times.Times
import com.metinkale.prayer.times.utils.Store
import kotlinx.coroutines.flow.map

class SettingsFragmentViewModel(var times: Store<Times?>) {


    val name = times.data.map { it?.name ?: "" }
    val tz = times.data.map { it?.timezone?.toString() ?: "" }
    val minuteAdj = times.data.map { it?.minuteAdj }


    fun setName(value: String) = times.update { it?.copy(name = value) }
    fun setTz(value: String) = times.update { it?.copy(timezone = value.toDoubleOrNull() ?: 0.0) }
    fun setMinAdj(index: Int, value: String) = times.update {it?. copy(minuteAdj = it.withMinAdj(index, value)) }
    fun plusTz() = times.update {it?. copy(timezone = it.timezone + 0.5) }
    fun minusTz() = times.update {it?. copy(timezone = it.timezone - 0.5) }
    fun plus(index: Int) = times.update { it?.copy(minuteAdj = it.withMinuteAdj(index, it.minuteAdj[index] + 1)) }
    fun minus(index: Int) = times.update { it?.copy(minuteAdj = it.withMinuteAdj(index, it.minuteAdj[index] - 1)) }


    private fun Times.withMinAdj(index: Int, value: String) =
        withMinuteAdj(index, value.toIntOrNull() ?: 0)

    private fun Times.withMinuteAdj(index: Int, value: Int) =
        minuteAdj.toMutableList().apply { set(index, value) }

}