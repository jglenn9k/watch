package com.jglenn.aviator.sensors.data

import org.junit.Assert.assertEquals
import org.junit.Test

class AviationFormatterTest {
    @Test fun normalizesHeadingAcrossNorth() {
        assertEquals(0, AviationFormatter.normalizeHeading(360f))
        assertEquals(359, AviationFormatter.normalizeHeading(-1f))
        assertEquals(1, AviationFormatter.normalizeHeading(721f))
    }

    @Test fun formatsHeadingAsThreeDigitsAndCardinal() {
        assertEquals("000° N", AviationFormatter.heading(0f))
        assertEquals("090° E", AviationFormatter.heading(90f))
        assertEquals("274° W", AviationFormatter.heading(274f))
        assertEquals("225° SW", AviationFormatter.heading(225f))
    }

    @Test fun convertsStandardPressureToInchesOfMercury() {
        assertEquals("29.92 inHg", AviationFormatter.pressure(1013.25f))
    }

    @Test fun formatsSpo2Percent() {
        assertEquals("98%", AviationFormatter.spo2(98))
    }
}

