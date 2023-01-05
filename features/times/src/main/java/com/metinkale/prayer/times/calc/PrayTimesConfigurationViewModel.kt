package com.metinkale.prayer.times.calc

import androidx.lifecycle.ViewModel
import com.metinkale.prayer.times.times.Times
import com.metinkale.prayer.times.times.Vakit
import dev.metinkale.prayertimes.calc.HighLatsAdjustment
import dev.metinkale.prayertimes.calc.Method
import dev.metinkale.prayertimes.calc.PrayTimes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update


class PrayTimesConfigurationViewModel(
    prayTimes: PrayTimes,
    private val save: PrayTimesConfigurationViewModel.(PrayTimes) -> Unit
) :
    ViewModel() {
    val prayTimes: MutableStateFlow<PrayTimes> = MutableStateFlow(prayTimes)
    val asrType = MutableStateFlow(Times.AsrType.Shafi)


    fun save() = save(prayTimes.value)

    fun setMethod(method: Method) {
        prayTimes.update { it.copy(method = method) }
    }

    fun setMinuteDrift(vakit: Vakit, num: Int, mins: Int) {
        prayTimes.update {
            it.copy(
                method = when (vakit) {
                    Vakit.FAJR -> it.method.copy(fajrMinute = mins, imsakMinute = mins)
                    Vakit.SUN -> it.method.copy(sunriseMinute = mins)
                    Vakit.DHUHR -> it.method.copy(dhuhrMinute = mins)
                    Vakit.ASR -> if (num == 0) it.method.copy(asrShafiMinute = mins)
                    else it.method.copy(asrHanafiMinute = mins)
                    Vakit.MAGHRIB -> it.method.copy(maghribMinute = mins)
                    Vakit.ISHAA -> it.method.copy(ishaaMinute = mins)
                }
            )
        }
    }


    fun setAngle(vakit: Vakit, angle: Double?) {
        prayTimes.update {
            it.copy(
                method = when (vakit) {
                    Vakit.FAJR -> it.method.copy(fajrAngle = angle, imsakAngle = angle)
                    Vakit.MAGHRIB -> it.method.copy(maghribAngle = angle)
                    Vakit.ISHAA -> it.method.copy(ishaaAngle = angle)
                    else -> it.method
                }
            )
        }
    }

    fun setAngleDrift(vakit: Vakit, angle: Double) {
        prayTimes.update {
            it.copy(
                method = when (vakit) {
                    Vakit.FAJR -> it.method.copy(
                        fajrAngle = angle.takeIf { it != 0.0 },
                        imsakAngle = angle.takeIf { it != 0.0 })
                    Vakit.MAGHRIB -> it.method.copy(maghribAngle = angle.takeIf { it != 0.0 })
                    Vakit.ISHAA -> it.method.copy(ishaaAngle = angle.takeIf { it != 0.0 })
                    else -> it.method
                }
            )
        }
    }

    fun setHighLats(highLatsAdjustment: HighLatsAdjustment) {
        prayTimes.update { it.copy(method = it.method.copy(highLatsAdjustment)) }
    }

    fun getAngle(vakit: Vakit): Double? = when (vakit) {
        Vakit.FAJR -> prayTimes.value.method.fajrAngle ?: 0.0
        Vakit.MAGHRIB -> prayTimes.value.method.maghribAngle ?: 0.0
        Vakit.ISHAA -> prayTimes.value.method.ishaaAngle ?: 0.0
        else -> null
    }

    fun getMinuteDrift(vakit: Vakit, num: Int): Int = when (vakit) {
        Vakit.FAJR -> prayTimes.value.method.fajrMinute
        Vakit.SUN -> prayTimes.value.method.sunriseMinute
        Vakit.DHUHR -> prayTimes.value.method.dhuhrMinute
        Vakit.ASR -> if (num == 0) prayTimes.value.method.asrShafiMinute else prayTimes.value.method.asrHanafiMinute
        Vakit.MAGHRIB -> prayTimes.value.method.maghribMinute
        Vakit.ISHAA -> prayTimes.value.method.ishaaMinute
    }

    fun setAsrType(type: Times.AsrType, checked: Boolean) {
        val types = asrType.value.let {
            if (it == Times.AsrType.Both) listOf(
                Times.AsrType.Shafi,
                Times.AsrType.Hanafi
            ) else listOf(it)
        }.toMutableSet()
        when {
            type == Times.AsrType.Hanafi && checked -> types.add(Times.AsrType.Hanafi)
            type == Times.AsrType.Shafi && checked -> types.add(Times.AsrType.Shafi)
            type == Times.AsrType.Hanafi && !checked -> types.remove(Times.AsrType.Hanafi)
            type == Times.AsrType.Shafi && !checked -> types.remove(Times.AsrType.Shafi)
        }

        asrType.value = when {
            Times.AsrType.Hanafi in types && Times.AsrType.Shafi in types -> Times.AsrType.Both
            Times.AsrType.Hanafi in types -> Times.AsrType.Hanafi
            else -> Times.AsrType.Shafi
        }

    }


}