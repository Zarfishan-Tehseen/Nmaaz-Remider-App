package com.example.nmaazreminder.ui.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.sin

class PrayerDialView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // 🎨 Paints Definition
    private val dashedCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#C3C7C2")
        style = Paint.Style.STROKE
        strokeWidth = 3f
        pathEffect = DashPathEffect(floatArrayOf(8f, 12f), 0f)
    }

    private val innerTrackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E0DCD3")
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    private val activeArcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1E3A34") // Dark Green color matching reference
        style = Paint.Style.STROKE
        strokeWidth = 5f
        strokeCap = Paint.Cap.ROUND
    }

    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }

    private val needlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1E3A34")
        style = Paint.Style.STROKE
        strokeWidth = 6f
        strokeCap = Paint.Cap.ROUND
    }

    // Coordinates variables
    private var centerX = 0f
    private var centerY = 0f
    private var outerRadius = 0f
    private var innerRadius = 0f

    // 🗺️ Mocking structural values for beautiful render framework configuration
    private val prayerAngles = floatArrayOf(140f, 80f, 30f, -25f, -85f) // Fajr, Dhuhr, Asr, Maghrib, Isha angles
    private val prayerNamesEnglish = arrayOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
    private val prayerNamesArabic = arrayOf("الفجر", "الظهر", "العصر", "المغرب", "العشاء")

    private var activeIndex = 2 // Defaulting to ASR matching reference display

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = w / 2f
        centerY = h / 2f

        // Match exact bounds layout
        outerRadius = (w.coerceAtMost(h) / 2f) * 0.82f
        innerRadius = outerRadius * 0.86f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 1. Draw the clean Outer Custom Ticks Ring
        canvas.drawCircle(centerX, centerY, outerRadius, dashedCirclePaint)

        // 2. Draw standard layout static Tick Markers along the outer ring
        for (angle in 0 until 360 step 15) {
            val rad = Math.toRadians(angle.toDouble())
            val startX = centerX + (outerRadius - 8f) * cos(rad).toFloat()
            val startY = centerY + (outerRadius - 8f) * sin(rad).toFloat()
            val endX = centerX + (outerRadius + 8f) * cos(rad).toFloat()
            val endY = centerY + (outerRadius + 8f) * sin(rad).toFloat()

            // Highlight explicit anchor nodes matching layout
            if (angle % 90 == 0) {
                dashedCirclePaint.strokeWidth = 5f
                canvas.drawLine(startX, startY, endX, endY, dashedCirclePaint)
                dashedCirclePaint.strokeWidth = 3f
            } else {
                canvas.drawLine(startX, startY, endX, endY, dashedCirclePaint)
            }
        }

        // 3. Draw Inner base ring circle track
        canvas.drawCircle(centerX, centerY, innerRadius, innerTrackPaint)

        // 4. Draw Active Sweep Track (From current active baseline up to active prayer node)
        val startSweepAngle = prayerAngles[0] // Starts from Fajr node boundary
        val targetSweepAngle = prayerAngles[activeIndex]
        val sweepDelta = if (targetSweepAngle < startSweepAngle) {
            (targetSweepAngle - startSweepAngle)
        } else {
            (targetSweepAngle - startSweepAngle) - 360f
        }

        val arcBounds = RectF(centerX - innerRadius, centerY - innerRadius, centerX + innerRadius, centerY + innerRadius)
        canvas.drawArc(arcBounds, startSweepAngle, sweepDelta, false, activeArcPaint)

        // 5. Draw Pointers, Text Layout Elements, and dynamic state items
        for (i in prayerAngles.indices) {
            val angleRad = Math.toRadians(prayerAngles[i].toDouble())
            val itemX = centerX + innerRadius * cos(angleRad).toFloat()
            val itemY = centerY + innerRadius * sin(angleRad).toFloat()

            // Text formatting configuration layout parameters
            textPaint.textSize = 34f
            textPaint.typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)

            if (i == activeIndex) {
                dotPaint.color = Color.parseColor("#D4A373") // Golden highlight dot
                textPaint.color = Color.parseColor("#D4A373")
                canvas.drawCircle(itemX, itemY, 14f, dotPaint) // Highlighted ring outer dot
            } else {
                dotPaint.color = Color.parseColor("#1E3A34")
                textPaint.color = Color.parseColor("#5A5E5A")
                canvas.drawCircle(itemX, itemY, 10f, dotPaint)
            }

            // Draw Prayer Label Texts matching boundaries offset configuration
            val textOffsetRadius = innerRadius + 45f
            val labelX = centerX + textOffsetRadius * cos(angleRad).toFloat()
            val labelY = centerY + textOffsetRadius * sin(angleRad).toFloat() + 10f
            canvas.drawText(prayerNamesEnglish[i], labelX, labelY, textPaint)
        }

        // 🌟 6. DRAW NEEDLE (Perfect reference line drawing transformation)
        val activeRad = Math.toRadians(prayerAngles[activeIndex].toDouble())
        // Start needle slightly offset from the exact center point circle boundary
        val needleStartX = centerX + 25f * cos(activeRad).toFloat()
        val needleStartY = centerY + 25f * sin(activeRad).toFloat()
        val needleEndX = centerX + (innerRadius - 10f) * cos(activeRad).toFloat()
        val needleEndY = centerY + (innerRadius - 10f) * sin(activeRad).toFloat()
        canvas.drawLine(needleStartX, needleStartY, needleEndX, needleEndY, needlePaint)

        // 🌟 7. DRAW CENTER METADATA HUD BLOCK (TEXT INSIDE DIAL)
        // Draw standard small pivot circle center connector node
        dotPaint.color = Color.parseColor("#1E3A34")
        canvas.drawCircle(centerX, centerY, 15f, dotPaint)

        // "NEXT" Title marker text representation
        textPaint.color = Color.parseColor("#A0A39F")
        textPaint.textSize = 28f
        textPaint.letterSpacing = 0.08f
        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        canvas.drawText("NEXT", centerX, centerY - 65f, textPaint)

        // Target Prayer Title English
        textPaint.color = Color.parseColor("#1E3A34")
        textPaint.textSize = 58f
        textPaint.letterSpacing = 0.02f
        textPaint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        canvas.drawText(prayerNamesEnglish[activeIndex], centerX, centerY - 5f, textPaint)

        // Target Prayer Title Arabic text
        textPaint.color = Color.parseColor("#1E3A34")
        textPaint.textSize = 48f
        textPaint.letterSpacing = 0.0f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(prayerNamesArabic[activeIndex], centerX, centerY + 60f, textPaint)
    }

    // Dynamic setter to control dial center focus values asynchronously from fragment tracking loops
    fun setActivePrayer(prayerName: String) {
        val mappedIndex = prayerNamesEnglish.indexOfFirst { it.equals(prayerName, ignoreCase = true) }
        if (mappedIndex != -1) {
            this.activeIndex = mappedIndex
            invalidate() // Re-draw interface view container updates safely
        }
    }
}