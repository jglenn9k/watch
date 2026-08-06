package com.jglenn.aviator.sensors.data

import java.util.Locale
import kotlin.math.roundToInt

object AviationFormatter {
    const val HPA_TO_INHG = 0.0295299830714

    fun normalizeHeading(degrees: Float): Int = ((degrees.roundToInt() % 360) + 360) % 360

    fun cardinal(degrees: Float): String {
        val points = arrayOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
        return points[((normalizeHeading(degrees) + 22) / 45) % points.size]
    }

    fun heading(degrees: Float): String = "%03d° %s".format(
        Locale.US,
        normalizeHeading(degrees),
        cardinal(degrees),
    )

    fun pressure(hPa: Float): String = "%.2f inHg".format(Locale.US, hPa * HPA_TO_INHG)

    fun spo2(percent: Int): String = "$percent%"
}

