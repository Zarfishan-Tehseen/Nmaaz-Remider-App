package com.example.nmaazreminder.ui.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.sin

class PrayerDialView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Paint for the main tracking circle ring
    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#D4CFC3")
        style = Paint.Style.STROKE
        strokeWidth = 3f
        // Creates the clean dash/dotted pattern matching your mockup design
        pathEffect = DashPathEffect(floatArrayOf(6f, 8f), 0f)
    }

    // Paint for hour/minute dial tick dashes
    private val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#A0A39F")
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    // Paint for the text labels (Fajr, Dhuhr, etc.)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#6E726E")
        textSize = 34f // Adjust scale relative to target device density profile
        textAlign = Paint.Align.CENTER
    }

    // Paint for the active prayer highlight accent indicator point
    private val activeDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#CBB393")
        style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f
        // Leave a 60px margin buffer on sides to prevent text labels clipping off-screen
        val radius = (Math.min(width, height) / 2f) - 60f

        // 1. Draw the underlying dashed tracking circle ring
        canvas.drawCircle(centerX, centerY, radius, circlePaint)

        // 2. Draw static ornamental clock tick marks (Every 30 degrees for layout structure)
        for (angleDegrees in 0 until 360 step 30) {
            val angleRadians = Math.toRadians(angleDegrees.toDouble())

            // Outer point of the line segment intersecting the circle perimeter
            val startX = (centerX + radius * cos(angleRadians)).toFloat()
            val startY = (centerY + radius * sin(angleRadians)).toFloat()

            // Inner point creating a small tick length variant
            val endX = (centerX + (radius - 12f) * cos(angleRadians)).toFloat()
            val endY = (centerY + (radius - 12f) * sin(angleRadians)).toFloat()

            canvas.drawLine(startX, startY, endX, endY, tickPaint)
        }

        // 3. Optional visual placeholder dots for main prayer positions (e.g., 45, 120, 180, 270, 330 degrees)
        // These can later be dynamically bound to times via custom functions
        drawPrayerNode(canvas, centerX, centerY, radius, 140.0, "Fajr")
        drawPrayerNode(canvas, centerX, centerY, radius, 260.0, "Dhuhr")
        drawPrayerNode(canvas, centerX, centerY, radius, 330.0, "Asr", isActive = true)
        drawPrayerNode(canvas, centerX, centerY, radius, 20.0, "Maghrib")
        drawPrayerNode(canvas, centerX, centerY, radius, 70.0, "Isha")
    }

    /**
     * Helper to compute structural coordinates and draw prayer labels and connection nodes onto the canvas
     */
    private fun drawPrayerNode(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        radius: Float,
        angleDegrees: Double,
        label: String,
        isActive: Boolean = false
    ) {
        val radians = Math.toRadians(angleDegrees)

        // Node dot center coordinates intersecting the ring line perfectly
        val dotX = (cx + radius * cos(radians)).toFloat()
        val dotY = (cy + radius * sin(radians)).toFloat()

        if (isActive) {
            // Draw an highlighted ring for active state matching Asr selection mockup
            canvas.drawCircle(dotX, dotY, 20f, activeDotPaint)
            canvas.drawCircle(dotX, dotY, 8f, Paint().apply { color = Color.parseColor("#1E3A34"); isAntiAlias = true })
        } else {
            canvas.drawCircle(dotX, dotY, 10f, Paint().apply { color = Color.parseColor("#1E3A34"); isAntiAlias = true })
        }

        // Push text labels further out away from node circle slightly to make it readable
        val textDistance = radius + 40f
        val textX = (cx + textDistance * cos(radians)).toFloat()
        val textY = (cy + textDistance * sin(radians)).toFloat() + 10f // Offset y font baseline variance

        textPaint.color = if (isActive) Color.parseColor("#CBB393") else Color.parseColor("#6E726E")
        canvas.drawText(label, textX, textY, textPaint)
    }
}