package com.jglenn9k.aviator.sensors.complications

import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.jglenn9k.aviator.sensors.R
import com.jglenn9k.aviator.sensors.data.AviationFormatter
import com.jglenn9k.aviator.sensors.data.ReadingStore
import com.jglenn9k.aviator.sensors.sensors.HeadingSampler
import com.jglenn9k.aviator.sensors.ui.CompassActivity

class HeadingComplicationService : SuspendingComplicationDataSourceService() {
    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        val store = ReadingStore(this)
        HeadingSampler(this).sample()?.let(store::saveHeading)
        return data(false)
    }
    override fun getPreviewData(type: ComplicationType): ComplicationData = data(true)

    private fun data(preview: Boolean): ComplicationData {
        val reading = ReadingStore(this).heading()
        val value = if (preview) "274° W" else reading?.let { AviationFormatter.heading(it.value) } ?: "---"
        val stale = !preview && reading?.isStale(System.currentTimeMillis(), 15 * 60_000L) == true
        return shortTextData(this, "HDG", if (stale) "$value*" else value, "Magnetic heading $value${if (stale) ", stale" else ""}", R.drawable.ic_heading, CompassActivity::class.java)
    }
}
