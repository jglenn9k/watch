package com.jglenn9k.aviator.sensors.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

class PressureSampler(context: Context) {
    private val sensorManager = context.getSystemService(SensorManager::class.java)

    suspend fun sample(timeoutMillis: Long = 1_500): Float? = withTimeoutOrNull(timeoutMillis) {
        suspendCancellableCoroutine { continuation ->
            val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)
            if (sensor == null) {
                continuation.resume(null)
                return@suspendCancellableCoroutine
            }
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    sensorManager.unregisterListener(this)
                    if (continuation.isActive) continuation.resume(event.values.firstOrNull())
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }
            continuation.invokeOnCancellation { sensorManager.unregisterListener(listener) }
            val registered = sensorManager.registerListener(
                listener,
                sensor,
                SensorManager.SENSOR_DELAY_NORMAL,
                Handler(Looper.getMainLooper()),
            )
            if (!registered && continuation.isActive) continuation.resume(null)
        }
    }
}
