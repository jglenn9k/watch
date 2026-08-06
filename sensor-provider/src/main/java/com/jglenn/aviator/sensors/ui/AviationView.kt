package com.jglenn.aviator.sensors.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.View

open class AviationView(context: Context) : View(context) {
    protected val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD) }
    protected val cyan = Color.rgb(0, 229, 255)
    protected val amber = Color.rgb(255, 171, 0)

    protected fun text(canvas: Canvas, value: String, x: Float, y: Float, size: Float, color: Int = Color.WHITE, align: Paint.Align = Paint.Align.CENTER) {
        paint.style = Paint.Style.FILL
        paint.textSize = size
        paint.color = color
        paint.textAlign = align
        canvas.drawText(value, x, y, paint)
    }

    protected fun panel(canvas: Canvas, bounds: RectF) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        paint.color = cyan
        canvas.drawRoundRect(bounds, 18f, 18f, paint)
    }
}

