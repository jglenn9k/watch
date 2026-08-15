package com.jglenn.aviator.sensors.sensors

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
import kotlin.math.PI

class HeadingSampler(context: Context) {
    private val sensorManager = context.getSystemService(SensorManager::class.java)

    suspend fun sample(timeoutMillis: Long = 1_500): Float? = withTimeoutOrNull(timeoutMillis) {
        suspendCancellableCoroutine { continuation ->
            val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            if (sensor == null) {
                continuation.resume(null)
                return@suspendCancellableCoroutine
            }
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    val matrix = FloatArray(9)
                    val orientation = FloatArray(3)
                    SensorManager.getRotationMatrixFromVector(matrix, event.values)
                    SensorManager.getOrientation(matrix, orientation)
                    val heading = (orientation[0] * 180f / PI.toFloat() + 360f) % 360f
                    sensorManager.unregisterListener(this)
                    if (continuation.isActive) continuation.resume(heading)
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
