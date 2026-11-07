package com.metinkale.prayer.wear

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.wear.remote.interactions.RemoteActivityHelper
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Wearable
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object WearPhoneSupport {
    private const val CAPABILITY = "prayer_times_watch"
    private const val PLAY_STORE_URL =
        "https://play.google.com/store/apps/details?id=com.metinkale.prayer"

    suspend fun watchWithoutAppNodeId(context: Context): String? = withContext(Dispatchers.IO) {
        runCatching {
            val appContext = context.applicationContext
            val nodes = Tasks.await(
                Wearable.getNodeClient(appContext).connectedNodes,
                5,
                TimeUnit.SECONDS
            )
            if (nodes.isEmpty()) return@runCatching null

            val capability = Tasks.await(
                Wearable.getCapabilityClient(appContext).getCapability(
                    CAPABILITY,
                    CapabilityClient.FILTER_REACHABLE
                ),
                5,
                TimeUnit.SECONDS
            )
            val installedNodeIds = capability.nodes.map { it.id }.toSet()
            nodes.firstOrNull { it.id !in installedNodeIds }?.id
        }.getOrNull()
    }

    suspend fun isWatchAppInstalled(context: Context): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val capability = Tasks.await(
                Wearable.getCapabilityClient(context.applicationContext).getCapability(
                    CAPABILITY,
                    CapabilityClient.FILTER_ALL
                ),
                5,
                TimeUnit.SECONDS
            )
            capability.nodes.isNotEmpty()
        }.getOrDefault(false)
    }

    fun openInstallOnWatch(context: Context, nodeId: String) {
        val targetIntent = Intent(Intent.ACTION_VIEW)
            .setData(Uri.parse(PLAY_STORE_URL))
            .addCategory(Intent.CATEGORY_BROWSABLE)
        val helper = RemoteActivityHelper(context.applicationContext)
        val future = runCatching {
            helper.startRemoteActivity(targetIntent, nodeId)
        }.getOrNull() ?: return

        future.addListener({
            runCatching { future.get() }.onFailure {
                runCatching {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_STORE_URL))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }
            }
        }, ContextCompat.getMainExecutor(context))
    }
}
