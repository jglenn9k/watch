package com.jglenn.aviator.sensors.ui

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.jglenn.aviator.sensors.R
import com.jglenn.aviator.sensors.complications.Spo2ComplicationService
import com.jglenn.aviator.sensors.complications.requestUpdate
import com.jglenn.aviator.sensors.data.ReadingStore
import com.jglenn.aviator.sensors.spo2.SamsungSpo2Gateway
import com.jglenn.aviator.sensors.spo2.Spo2Event
import com.jglenn.aviator.sensors.spo2.Spo2Gateway

class Spo2Activity : Activity() {
    private lateinit var status: TextView
    private lateinit var measure: Button
    private var gateway: Spo2Gateway? = null
    private val timeoutHandler = Handler(Looper.getMainLooper())
    private val timeout = Runnable {
        gateway?.stop()
        gateway = null
        showFailure("Measurement timed out. Keep still and try again.")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        status = textView("Place the watch snugly above your wrist bone.", 20f, Color.WHITE)
        measure = Button(this).apply { text = "START 30s MEASUREMENT"; setOnClickListener { ensurePermissionAndMeasure() } }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(34, 28, 34, 48)
            addView(textView("BLOOD OXYGEN", 24f, Color.rgb(0, 229, 255)))
            addView(status)
            addView(measure, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
            addView(textView(getString(R.string.fitness_disclaimer), 14f, Color.LTGRAY))
        }
        setContentView(ScrollView(this).apply { addView(content) })
        ReadingStore(this).spo2()?.let { status.text = "Last reading: ${it.value}%\nTap start for a new measurement." }
    }

    private fun requiredPermission(): String =
        if (Build.VERSION.SDK_INT >= 36) "android.permission.health.READ_OXYGEN_SATURATION" else Manifest.permission.BODY_SENSORS

    private fun ensurePermissionAndMeasure() {
        val permission = requiredPermission()
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), REQUEST_HEALTH)
        } else startMeasurement()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_HEALTH && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) startMeasurement()
        else showFailure("Sensor permission denied. Permission is required only while you explicitly measure SpO₂.")
    }

    private fun startMeasurement() {
        measure.isEnabled = false
        gateway?.stop()
        val newGateway = SamsungSpo2Gateway()
        gateway = newGateway
        timeoutHandler.postDelayed(timeout, 32_000)
        newGateway.start(this, ::onEvent)
    }

    private fun onEvent(event: Spo2Event) = runOnUiThread {
        when (event) {
            is Spo2Event.Progress -> status.text = event.message
            is Spo2Event.Success -> {
                timeoutHandler.removeCallbacks(timeout)
                ReadingStore(this).saveSpo2(event.percent)
                requestUpdate(this, Spo2ComplicationService::class.java)
                status.text = "${event.percent}%\nMeasurement complete"
                measure.isEnabled = true
                gateway = null
            }
            is Spo2Event.Failure -> {
                timeoutHandler.removeCallbacks(timeout)
                gateway?.stop()
                gateway = null
                showFailure(event.message)
                event.resolution?.invoke()
            }
        }
    }

    private fun showFailure(message: String) {
        status.text = message
        measure.isEnabled = true
    }

    private fun textView(value: String, size: Float, color: Int) = TextView(this).apply {
        text = value
        textSize = size
        setTextColor(color)
        gravity = Gravity.CENTER
        setPadding(4, 14, 4, 14)
    }

    override fun onDestroy() {
        timeoutHandler.removeCallbacks(timeout)
        gateway?.stop()
        super.onDestroy()
    }

    private companion object { const val REQUEST_HEALTH = 400 }
}
