package com.jglenn.aviator.sensors.complications

import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.jglenn.aviator.sensors.R
import com.jglenn.aviator.sensors.data.AviationFormatter
import com.jglenn.aviator.sensors.data.ReadingStore
import com.jglenn.aviator.sensors.sensors.PressureSampler
import com.jglenn.aviator.sensors.ui.MainActivity

class PressureComplicationService : SuspendingComplicationDataSourceService() {
    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        val store = ReadingStore(this)
        PressureSampler(this).sample()?.let(store::savePressure)
        return data(store.pressure()?.value)
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData = data(1013.25f)

    private fun data(hPa: Float?): ComplicationData {
        val value = hPa?.let(AviationFormatter::pressure) ?: "--.-- inHg"
        return shortTextData(this, "BARO", value, "Raw ambient pressure $value. Not an altimeter setting.", R.drawable.ic_pressure, MainActivity::class.java)
    }
}
