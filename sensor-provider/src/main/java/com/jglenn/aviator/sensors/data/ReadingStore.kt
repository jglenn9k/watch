package com.jglenn.aviator.sensors.data

import android.content.Context

data class TimedReading<T>(val value: T, val timestampMillis: Long) {
    fun isStale(nowMillis: Long, maxAgeMillis: Long): Boolean = nowMillis - timestampMillis > maxAgeMillis
}

class ReadingStore(context: Context) {
    private val preferences = context.getSharedPreferences("aviator_readings", Context.MODE_PRIVATE)

    fun heading(): TimedReading<Float>? = floatReading(KEY_HEADING, KEY_HEADING_TIME)
    fun pressure(): TimedReading<Float>? = floatReading(KEY_PRESSURE, KEY_PRESSURE_TIME)

    fun saveHeading(degrees: Float, nowMillis: Long = System.currentTimeMillis()) =
        preferences.edit().putFloat(KEY_HEADING, degrees).putLong(KEY_HEADING_TIME, nowMillis).apply()

    fun savePressure(hPa: Float, nowMillis: Long = System.currentTimeMillis()) =
        preferences.edit().putFloat(KEY_PRESSURE, hPa).putLong(KEY_PRESSURE_TIME, nowMillis).apply()

    private fun floatReading(valueKey: String, timeKey: String): TimedReading<Float>? {
        if (!preferences.contains(valueKey)) return null
        return TimedReading(preferences.getFloat(valueKey, 0f), preferences.getLong(timeKey, 0))
    }

    private companion object {
        const val KEY_HEADING = "heading"
        const val KEY_HEADING_TIME = "heading_time"
        const val KEY_PRESSURE = "pressure"
        const val KEY_PRESSURE_TIME = "pressure_time"
    }
}
