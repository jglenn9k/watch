package com.jglenn9k.aviator.sensors.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TimedReadingTest {
    @Test fun becomesStaleOnlyAfterMaximumAge() {
        val reading = TimedReading(42, 1_000L)
        assertFalse(reading.isStale(2_000L, 1_000L))
        assertTrue(reading.isStale(2_001L, 1_000L))
    }
}
