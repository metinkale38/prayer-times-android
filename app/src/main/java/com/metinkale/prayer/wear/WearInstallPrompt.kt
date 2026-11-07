package com.metinkale.prayer.wear

import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.metinkale.prayer.Preferences
import com.metinkale.prayer.R
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.launch

object WearInstallPrompt {
    private val checked = AtomicBoolean(false)

    fun maybeShow(activity: AppCompatActivity) {
        if (!checked.compareAndSet(false, true)) return
        if (Preferences.WEAR_INSTALL_PROMPT_DISMISSED) return

        activity.lifecycleScope.launch {
            val nodeId = WearPhoneSupport.watchWithoutAppNodeId(activity) ?: return@launch
            if (activity.isFinishing || activity.isDestroyed) return@launch

            AlertDialog.Builder(activity)
                .setTitle(R.string.wearInstallTitle)
                .setMessage(R.string.wearInstallMessage)
                .setPositiveButton(R.string.wearInstallNow) { _, _ ->
                    WearPhoneSupport.openInstallOnWatch(activity, nodeId)
                }
                .setNegativeButton(R.string.wearInstallDismissForever) { _, _ ->
                    Preferences.WEAR_INSTALL_PROMPT_DISMISSED = true
                }
                .setNeutralButton(R.string.cancel, null)
                .show()
        }
    }
}
