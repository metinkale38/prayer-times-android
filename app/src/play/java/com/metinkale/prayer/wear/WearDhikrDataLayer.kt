package com.metinkale.prayer.wear

import android.content.Context
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.DataMap
import com.google.android.gms.wearable.PutDataRequest
import com.google.android.gms.wearable.Wearable
import com.metinkale.prayer.R
import com.metinkale.prayer.dhikr.data.Dhikr
import com.metinkale.prayer.dhikr.data.DhikrDao
import com.metinkale.prayer.dhikr.data.DhikrDatabase
import com.metinkale.prayer.sync.DhikrSyncConstants
import com.metinkale.prayer.sync.DhikrSyncJson
import com.metinkale.prayer.sync.WearDhikr
import com.metinkale.prayer.sync.WearDhikrPayload
import com.metinkale.prayer.sync.mergeWearDhikrs
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal object WearDhikrDataLayer {
    suspend fun pushPhoneState(context: Context) = withContext(Dispatchers.IO) {
        val dao = DhikrDatabase.getDatabase(context.applicationContext).dhikrDao() ?: return@withContext
        ensureDefaultDhikr(context, dao)
        val payload = WearDhikrPayload(
            generatedAtEpochMillis = System.currentTimeMillis(),
            dhikrs = dao.getAllDhikrsForSync().map(::toWear)
        )
        put(context.applicationContext, payload)
    }

    private fun ensureDefaultDhikr(context: Context, dao: DhikrDao) {
        if (dao.getAllDhikrsForSync().isNotEmpty()) return

        val now = System.currentTimeMillis()
        dao.addDhikr(
            Dhikr().apply {
                syncId = UUID.randomUUID().toString()
                title = context.getString(R.string.tasbih)
                max = 33
                position = 0
                updatedAt = now
            }
        )
    }

    suspend fun mergeWearState(context: Context, payload: WearDhikrPayload) = withContext(Dispatchers.IO) {
        val dao = DhikrDatabase.getDatabase(context.applicationContext).dhikrDao() ?: return@withContext
        val local = dao.getAllDhikrsForSync().map(::toWear)
        val merged = mergeWearDhikrs(local, payload.dhikrs)

        merged.forEach { dhikr ->
            if (dhikr.syncId.isBlank()) return@forEach
            val existing = dao.getDhikrBySyncId(dhikr.syncId)
            if (existing == null) {
                dao.addDhikr(fromWear(dhikr))
            } else {
                existing.title = dhikr.title
                existing.max = dhikr.target.coerceAtLeast(1)
                existing.value = dhikr.count.coerceAtLeast(0)
                existing.color = dhikr.color
                existing.position = dhikr.position
                existing.updatedAt = dhikr.updatedAt
                existing.isDeleted = dhikr.deleted
                dao.saveDhikr(existing)
            }
        }

        runCatching { pushPhoneState(context) }
    }

    private fun toWear(dhikr: Dhikr): WearDhikr = WearDhikr(
        syncId = dhikr.syncId.ifBlank { "legacy-${dhikr.id}" },
        title = dhikr.title ?: "",
        target = dhikr.max,
        count = dhikr.value,
        position = dhikr.position,
        color = dhikr.color,
        updatedAt = dhikr.updatedAt,
        deleted = dhikr.isDeleted
    )

    private fun fromWear(dhikr: WearDhikr): Dhikr = Dhikr().apply {
        syncId = dhikr.syncId
        title = dhikr.title
        max = dhikr.target.coerceAtLeast(1)
        value = dhikr.count.coerceAtLeast(0)
        color = dhikr.color
        position = dhikr.position
        updatedAt = dhikr.updatedAt
        isDeleted = dhikr.deleted
    }

    private fun put(context: Context, payload: WearDhikrPayload) {
        val data = DataMap().apply {
            putString(DhikrSyncConstants.KEY_JSON, DhikrSyncJson.encode(payload))
        }.toByteArray()
        val request = PutDataRequest.create(DhikrSyncConstants.PHONE_DATA_PATH).setData(data)
        Tasks.await(
            Wearable.getDataClient(context).putDataItem(request),
            30,
            TimeUnit.SECONDS
        )
    }
}
