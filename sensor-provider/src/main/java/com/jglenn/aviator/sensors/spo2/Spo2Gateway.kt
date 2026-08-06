package com.jglenn.aviator.sensors.spo2

import android.app.Activity

sealed interface Spo2Event {
    data class Progress(val message: String) : Spo2Event
    data class Success(val percent: Int) : Spo2Event
    data class Failure(val message: String, val resolution: (() -> Unit)? = null) : Spo2Event
}

interface Spo2Gateway {
    fun start(activity: Activity, onEvent: (Spo2Event) -> Unit)
    fun stop()
}

