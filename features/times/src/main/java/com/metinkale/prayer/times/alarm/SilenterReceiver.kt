package com.metinkale.prayer.times.alarm

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.metinkale.prayer.Preferences
import com.metinkale.prayer.times.R
import com.metinkale.prayer.times.utils.NotificationUtils
import com.metinkale.prayer.utils.PermissionUtils

class SilenterReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (PermissionUtils.get(context).pNotPolicy && intent.hasExtra("mode")) {
            val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            am.ringerMode = intent.getIntExtra("mode", 0)
        }
    }

    companion object {
        fun silent(c: Context, mins: Int) {
            if (!PermissionUtils.get(c).pNotPolicy) {
                var builder: NotificationCompat.Builder =
                    NotificationCompat.Builder(c, NotificationUtils.getPlayingChannel(c))
                builder = builder.setContentTitle(c.getString(R.string.silenterNotificationTitle))
                    .setContentText(c.getString(R.string.silenterNotificationInfo))
                    .setContentIntent(
                        PendingIntent.getActivity(
                            c,
                            0,
                            Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS),
                            PendingIntent.FLAG_IMMUTABLE
                        )
                    )
                    .setSmallIcon(R.drawable.ic_abicon)
                builder.setChannelId(NotificationUtils.getAlarmChannel(c))
                val nm = c.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                nm.notify("silenter", 557457, builder.build())
                return
            }
            val aum = c.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val ringermode = aum.ringerMode
            val modeVibrate = "vibrate" == Preferences.SILENTER_MODE
            val isSilent = ringermode == AudioManager.RINGER_MODE_SILENT
            val isVibrate = ringermode == AudioManager.RINGER_MODE_VIBRATE
            if (modeVibrate && !isVibrate && !isSilent || !modeVibrate && !isSilent) {
                val am = c.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val i = Intent(c, SilenterReceiver::class.java)
                i.putExtra("mode", ringermode)
                val service = PendingIntent.getBroadcast(c, 0, i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                am[AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 1000 * 60 * mins] = service
                aum.ringerMode =
                    if (modeVibrate) AudioManager.RINGER_MODE_VIBRATE else AudioManager.RINGER_MODE_SILENT
            }
        }
    }
}