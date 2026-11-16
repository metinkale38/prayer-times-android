package com.metinkale.prayer.sync

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class WearTimesPayloadTest {
    private val payload = WearTimesPayload(
        generatedAtEpochMillis = 1L,
        city = WearCity(
            id = 1,
            name = "Test",
            source = "Calc",
            timezone = 0.0,
            latitude = 0.0,
            longitude = 0.0
        ),
        days = listOf(
            WearDayTimes(
                date = "2026-11-05",
                fajr = "05:24",
                sun = "07:13",
                dhuhr = "12:07",
                asr = "14:20",
                maghrib = "16:50",
                ishaa = "18:26"
            )
        )
    )

    @Test
    fun `round trip keeps payload`() {
        assertEquals(payload, SyncJson.decode(SyncJson.encode(payload)))
    }

    @Test
    fun `unknown schema is rejected`() {
        val encoded = SyncJson.encode(payload.copy(schemaVersion = 99))

        assertNull(SyncJson.decode(encoded))
    }
}
