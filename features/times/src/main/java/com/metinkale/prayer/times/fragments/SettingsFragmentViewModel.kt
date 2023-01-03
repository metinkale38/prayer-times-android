package com.metinkale.prayer.times.fragments

import com.metinkale.prayer.times.times.Times
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class SettingsFragmentViewModel(var timesId: StateFlow<Int>) {

    val times = combine(Times, timesId) { list, id -> list.firstOrNull { it.ID == id } }

    val name = times.map { it?.name ?: "" }
    val tz = times.map { it?.timezone?.toString() ?: "" }
    val minuteAdj = times.map { it?.minuteAdj }

    private fun update(copy: Times.() -> Times) =
        Times.getTimesById(timesId.value).update { it?.copy() }

    fun setName(value: String) = update { copy(name = value) }
    fun setTz(value: String) = update { copy(timezone = value.toDoubleOrNull() ?: 0.0) }
    fun setMinAdj(index: Int, value: String) = update { copy(minuteAdj = withMinAdj(index, value)) }
    fun plusTz() = update { copy(timezone = timezone + 0.5) }
    fun minusTz() = update { copy(timezone = timezone - 0.5) }
    fun plus(index: Int) = update { copy(minuteAdj = withMinuteAdj(index, minuteAdj[index] + 1)) }
    fun minus(index: Int) = update { copy(minuteAdj = withMinuteAdj(index, minuteAdj[index] - 1)) }


    private fun Times.withMinAdj(index: Int, value: String) =
        withMinuteAdj(index, value.toIntOrNull() ?: 0)

    private fun Times.withMinuteAdj(index: Int, value: Int) =
        minuteAdj.toMutableList().apply { set(index, value) }

}