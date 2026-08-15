package com.jglenn.aviator.sensors.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.jglenn.aviator.sensors.complications.PressureComplicationService
import com.jglenn.aviator.sensors.complications.requestUpdate
import com.jglenn.aviator.sensors.data.AviationFormatter
import com.jglenn.aviator.sensors.data.ReadingStore
import com.jglenn.aviator.sensors.sensors.PressureSampler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MainActivity : Activity() {
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private lateinit var pressureValue: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(36, 28, 36, 48)
            addView(label("AVIATOR SENSORS", 22f, Color.rgb(0, 229, 255)))
            pressureValue = label("BARO  --.-- inHg", 28f, Color.WHITE)
            addView(pressureValue)
            addView(label("Raw ambient pressure — not QNH or an altimeter setting", 14f, Color.LTGRAY))
            addView(button("REFRESH BARO") { refreshPressure() })
            addView(button("LIVE COMPASS") { startActivity(Intent(this@MainActivity, CompassActivity::class.java)) })
            addView(label("Assign Samsung Health Blood oxygen to SpO₂ and Samsung/Google Weather to WX in the watch-face editor.", 14f, Color.LTGRAY))
        }
        setContentView(ScrollView(this).apply { addView(content) })
        showCachedPressure()
        refreshPressure()
    }

    private fun refreshPressure() {
        pressureValue.text = "BARO  sampling…"
        scope.launch {
            val sample = PressureSampler(this@MainActivity).sample()
            if (sample == null) {
                pressureValue.text = "BARO  unavailable"
            } else {
                ReadingStore(this@MainActivity).savePressure(sample)
                pressureValue.text = "BARO  ${AviationFormatter.pressure(sample)}"
                requestUpdate(this@MainActivity, PressureComplicationService::class.java)
            }
        }
    }

    private fun showCachedPressure() {
        ReadingStore(this).pressure()?.let { pressureValue.text = "BARO  ${AviationFormatter.pressure(it.value)}" }
    }

    private fun label(value: String, size: Float, color: Int) = TextView(this).apply {
        text = value
        textSize = size
        setTextColor(color)
        gravity = Gravity.CENTER
        setPadding(6, 12, 6, 12)
    }

    private fun button(value: String, action: () -> Unit) = Button(this).apply {
        text = value
        setOnClickListener { action() }
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins(0, 6, 0, 6) }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
