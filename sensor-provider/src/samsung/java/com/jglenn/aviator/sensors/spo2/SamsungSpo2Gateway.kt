package com.jglenn.aviator.sensors.spo2

import android.app.Activity
import com.samsung.android.service.health.tracking.ConnectionListener
import com.samsung.android.service.health.tracking.HealthTracker
import com.samsung.android.service.health.tracking.HealthTrackerException
import com.samsung.android.service.health.tracking.HealthTrackingService
import com.samsung.android.service.health.tracking.data.DataPoint
import com.samsung.android.service.health.tracking.data.HealthTrackerType
import com.samsung.android.service.health.tracking.data.ValueKey

class SamsungSpo2Gateway : Spo2Gateway {
    private var service: HealthTrackingService? = null
    private var tracker: HealthTracker? = null
    private var callback: ((Spo2Event) -> Unit)? = null

    private val trackerListener = object : HealthTracker.TrackerEventListener {
        override fun onDataReceived(dataPoints: MutableList<DataPoint>) {
            dataPoints.forEach { point ->
                val status = point.getValue(ValueKey.SpO2Set.STATUS)
                when (status) {
                    2 -> {
                        val value = point.getValue(ValueKey.SpO2Set.SPO2)
                        if (value in 1..100) callback?.invoke(Spo2Event.Success(value))
                        stop()
                    }
                    -6 -> fail("Measurement timed out. Keep still and try again.")
                    -5 -> fail("Signal quality is low. Tighten the watch above the wrist bone.")
                    -4 -> fail("Movement detected. Rest your arm and keep still.")
                    else -> callback?.invoke(Spo2Event.Progress("Measuring… keep still ($status)"))
                }
            }
        }

        override fun onFlushCompleted() = Unit
        override fun onError(error: HealthTracker.TrackerError) {
            callback?.invoke(Spo2Event.Failure("Sensor error: ${error.name.replace('_', ' ').lowercase()}"))
            stop()
        }
    }

    override fun start(activity: Activity, onEvent: (Spo2Event) -> Unit) {
        callback = onEvent
        onEvent(Spo2Event.Progress("Connecting to Samsung Health Platform…"))
        val connection = object : ConnectionListener {
            override fun onConnectionSuccess() {
                try {
                    val connected = service ?: return
                    if (!connected.trackingCapability.supportHealthTrackerTypes.contains(HealthTrackerType.SPO2_ON_DEMAND)) {
                        onEvent(Spo2Event.Failure("SpO₂ tracking is not supported on this watch."))
                        return
                    }
                    tracker = connected.getHealthTracker(HealthTrackerType.SPO2_ON_DEMAND)
                    tracker?.setEventListener(trackerListener)
                    onEvent(Spo2Event.Progress("Measuring… rest your arm and keep still for up to 30 seconds."))
                } catch (error: HealthTrackerException) {
                    onEvent(Spo2Event.Failure("Health Platform error: ${error.errorCode}", if (error.hasResolution()) ({ error.resolve(activity) }) else null))
                }
            }

            override fun onConnectionEnded() {
                onEvent(Spo2Event.Failure("Health Platform connection ended."))
            }

            override fun onConnectionFailed(error: HealthTrackerException) {
                onEvent(Spo2Event.Failure("Could not connect to Health Platform: ${error.errorCode}", if (error.hasResolution()) ({ error.resolve(activity) }) else null))
            }
        }
        service = HealthTrackingService(connection, activity)
        service?.connectService()
    }

    override fun stop() {
        tracker?.unsetEventListener()
        tracker = null
        callback = null
        service?.disconnectService()
        service = null
    }

    private fun fail(message: String) {
        callback?.invoke(Spo2Event.Failure(message))
        stop()
    }
}
