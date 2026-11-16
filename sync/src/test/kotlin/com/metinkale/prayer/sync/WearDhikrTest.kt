package com.metinkale.prayer.sync

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class WearDhikrTest {
    @Test
    fun payloadRoundTrip() {
        val payload = WearDhikrPayload(
            generatedAtEpochMillis = 42L,
            dhikrs = listOf(
                WearDhikr(
                    syncId = "a",
                    title = "Tesbih",
                    target = 33,
                    count = 7,
                    color = 0xFF33B5E5.toInt(),
                    updatedAt = 10L
                )
            )
        )

        val decoded = DhikrSyncJson.decode(DhikrSyncJson.encode(payload))

        assertEquals(payload, decoded)
    }

    @Test
    fun mergeUsesNewerItemAndKeepsLocalOnly() {
        val local = listOf(
            WearDhikr(syncId = "a", title = "A", count = 1, updatedAt = 10L),
            WearDhikr(syncId = "local", title = "Local", count = 3, updatedAt = 5L)
        )
        val remote = listOf(
            WearDhikr(syncId = "a", title = "A", count = 2, updatedAt = 20L),
            WearDhikr(syncId = "remote", title = "Remote", count = 4, updatedAt = 1L)
        )

        val merged = mergeWearDhikrs(local, remote).associateBy { it.syncId }

        assertEquals(2, merged.getValue("a").count)
        assertEquals(3, merged.getValue("local").count)
        assertEquals(4, merged.getValue("remote").count)
    }

    @Test
    fun mergeKeepsNewerTombstone() {
        val local = listOf(
            WearDhikr(syncId = "a", title = "A", count = 9, updatedAt = 10L)
        )
        val remote = listOf(
            WearDhikr(
                syncId = "a",
                title = "A",
                count = 0,
                updatedAt = 20L,
                deleted = true
            )
        )

        val merged = mergeWearDhikrs(local, remote)

        assertEquals(1, merged.size)
        assertTrue(merged.single().deleted)
        assertFalse(merged.single().count == 9)
    }
}
