package com.metinkale.prayer.wear

import android.content.Context

object WearPhoneSupport {
    suspend fun watchWithoutAppNodeId(context: Context): String? = null

    suspend fun isWatchAppInstalled(context: Context): Boolean = false

    fun openInstallOnWatch(context: Context, nodeId: String) = Unit
}
