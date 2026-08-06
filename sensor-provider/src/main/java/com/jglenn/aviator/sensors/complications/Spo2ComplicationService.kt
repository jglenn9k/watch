package com.jglenn.aviator.sensors.complications

import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.jglenn.aviator.sensors.R
import com.jglenn.aviator.sensors.data.AviationFormatter
import com.jglenn.aviator.sensors.data.ReadingStore
import com.jglenn.aviator.sensors.ui.Spo2Activity

class Spo2ComplicationService : SuspendingComplicationDataSourceService() {
    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? = data(false)
    override fun getPreviewData(type: ComplicationType): ComplicationData = data(true)

    private fun data(preview: Boolean): ComplicationData {
        val reading = ReadingStore(this).spo2()
        val value = if (preview) "98%" else reading?.let { AviationFormatter.spo2(it.value) } ?: "--%"
        val stale = !preview && reading?.isStale(System.currentTimeMillis(), 24 * 60 * 60_000L) == true
        return shortTextData(this, "SpO₂", if (stale) "$value*" else value, "Last blood oxygen reading $value${if (stale) ", older than 24 hours" else ""}", R.drawable.ic_spo2, Spo2Activity::class.java)
    }
}
