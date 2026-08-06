package com.jglenn.aviator.sensors.spo2

import android.app.Activity

class SamsungSpo2Gateway : Spo2Gateway {
    override fun start(activity: Activity, onEvent: (Spo2Event) -> Unit) {
        onEvent(Spo2Event.Failure("Samsung SDK is not enabled. Install the AAR and build the samsung flavor."))
    }

    override fun stop() = Unit
}

