package com.metinkale.prayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import com.metinkale.prayer.receiver.AppEventListener
import com.metinkale.prayer.receiver.AppEventManager
import com.metinkale.prayer.receiver.OnStartListener


class AppBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action ?: "") {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_LOCKED_BOOT_COMPLETED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_TIME_CHANGED -> {
                AppEventManager.sendOnStart()
            }

            Intent.ACTION_SCREEN_ON, Intent.ACTION_USER_UNLOCKED, Intent.ACTION_USER_PRESENT -> {
                if (!screenOn) {
                    AppEventManager.sendScreenOn()
                    sendTimeTick()
                }
                screenOn = true
            }

            Intent.ACTION_SCREEN_OFF -> {
                if (screenOn) AppEventManager.sendScreenOff()
                screenOn = false
            }

            else -> {
                if (screenOn)
                    sendTimeTick()
            }
        }
    }

    private fun sendTimeTick() {
        System.currentTimeMillis().let { time ->
            if (time / 60000 != lastTimeTick / 60000) // only refresh if minute has changed
                AppEventManager.sendTimeTick()
            lastTimeTick = time
        }
    }

    companion object : OnStartListener {
        private var lastTimeTick = 0L
        private var screenOn = true

        override fun onStart() {
            val receiver = AppBroadcastReceiver()
            val filter = IntentFilter()
            filter.addAction(Intent.ACTION_TIME_TICK)
            filter.addAction(Intent.ACTION_USER_PRESENT)
            filter.addAction(Intent.ACTION_SCREEN_ON)
            filter.addAction(Intent.ACTION_SCREEN_OFF)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                filter.addAction(Intent.ACTION_USER_UNLOCKED)
            }
            App.get().registerReceiver(receiver, filter)
        }
    }
}