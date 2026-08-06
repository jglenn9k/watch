package com.jglenn.aviator.sensors.ui

import android.app.Activity
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.WindowManager
import com.jglenn.aviator.sensors.complications.HeadingComplicationService
import com.jglenn.aviator.sensors.complications.requestUpdate
import com.jglenn.aviator.sensors.data.AviationFormatter
import com.jglenn.aviator.sensors.data.ReadingStore
import kotlin.math.PI

class CompassActivity : Activity(), SensorEventListener {
    private lateinit var sensors: SensorManager
    private lateinit var compassView: CompassView
    private var rotationSensor: Sensor? = null
    private var heading = 0f
    private var accuracy = SensorManager.SENSOR_STATUS_UNRELIABLE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        sensors = getSystemService(SensorManager::class.java)
        rotationSensor = sensors.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        compassView = CompassView(this)
        setContentView(compassView)
    }

    override fun onResume() {
        super.onResume()
        rotationSensor?.let { sensors.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
            ?: compassView.update(null, SensorManager.SENSOR_STATUS_UNRELIABLE)
    }

    override fun onPause() {
        sensors.unregisterListener(this)
        if (rotationSensor != null) {
            ReadingStore(this).saveHeading(heading)
            requestUpdate(this, HeadingComplicationService::class.java)
        }
        super.onPause()
    }

    override fun onSensorChanged(event: SensorEvent) {
        val matrix = FloatArray(9)
        val orientation = FloatArray(3)
        SensorManager.getRotationMatrixFromVector(matrix, event.values)
        SensorManager.getOrientation(matrix, orientation)
        val raw = (orientation[0] * 180f / PI.toFloat() + 360f) % 360f
        heading = circularSmooth(heading, raw, 0.22f)
        accuracy = event.accuracy
        compassView.update(heading, accuracy)
    }

    override fun onAccuracyChanged(sensor: Sensor?, value: Int) {
        accuracy = value
        compassView.update(heading, accuracy)
    }

    private fun circularSmooth(current: Float, next: Float, amount: Float): Float {
        var delta = (next - current + 540f) % 360f - 180f
        if (current == 0f) delta = 0f
        return if (current == 0f) next else (current + delta * amount + 360f) % 360f
    }
}

private class CompassView(context: android.content.Context) : AviationView(context) {
    private var heading: Float? = null
    private var accuracy = SensorManager.SENSOR_STATUS_UNRELIABLE

    fun update(value: Float?, sensorAccuracy: Int) { heading = value; accuracy = sensorAccuracy; invalidate() }

    override fun onDraw(canvas: android.graphics.Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height / 2f
        val radius = minOf(width, height) * .35f
        panel(canvas, android.graphics.RectF(cx - radius, cy - radius, cx + radius, cy + radius))
        text(canvas, "MAGNETIC", cx, cy - radius - 18f, 22f, cyan)
        val value = heading
        text(canvas, value?.let(AviationFormatter::heading) ?: "NO SENSOR", cx, cy + 18f, 48f)
        paint.color = amber
        paint.style = android.graphics.Paint.Style.FILL
        val path = android.graphics.Path().apply { moveTo(cx, cy - radius + 12); lineTo(cx - 12, cy - radius + 38); lineTo(cx + 12, cy - radius + 38); close() }
        canvas.drawPath(path, paint)
        if (accuracy <= SensorManager.SENSOR_STATUS_ACCURACY_LOW) text(canvas, "Move wrist in a figure 8 to calibrate", cx, cy + radius + 34f, 16f, amber)
    }
}
