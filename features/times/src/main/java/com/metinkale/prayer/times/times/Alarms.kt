package com.metinkale.prayer.times.times

import com.metinkale.prayer.App
import com.metinkale.prayer.times.alarm.Alarm
import com.metinkale.prayer.times.alarm.AlarmService
import org.joda.time.LocalDateTime


private fun Times.getNextAlarm(): Pair<Alarm, LocalDateTime>? {
    var alarm: Alarm? = null
    var time: LocalDateTime? = null
    for (a in alarms) {
        if (!a.isEnabled) continue
        val nextAlarm: LocalDateTime = a.nextAlarm ?: continue
        if (time == null || time.isAfter(nextAlarm)) {
            alarm = a
            time = nextAlarm
        }
    }
    if (alarm == null || time == null)
        return null
    return alarm to time
}


fun TimesCompanion.getNextAlarm(): Pair<Alarm, LocalDateTime>? {
    var pair: Pair<Alarm, LocalDateTime>? = null
    value.forEach { t ->
        t.getNextAlarm()?.let { nextAlarm ->
            if (pair == null || pair!!.second.isAfter(nextAlarm.second)
            ) {
                pair = nextAlarm
            }
        }
    }
    return pair
}


fun TimesCompanion.setAlarms() {
    val nextAlarm = getNextAlarm()
    if (nextAlarm != null)
        AlarmService.setAlarm(App(), androidx.core.util.Pair(nextAlarm.first, nextAlarm.second))
}
