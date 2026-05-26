package com.example.nmaazreminder.ui.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.sin

class TasbeehProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var currentCount = 0
    private var maxLimit = 33

    private val inactivePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E0DCD3")
        style = Paint.Style.FILL
    }

    private val activePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#0D3E31")
        style = Paint.Style.FILL
    }

    fun updateProgress(count: Int, limit: Int) {
        this.currentCount = count
        this.maxLimit = limit
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f
        val radius = (width / 2f) - dpToPx(25f) // Optimizing bounds edge space

        // 🌟 STRATEGY CONFIGURATOR: If limit is 500, draw 100 beads to keep layout crisp.
        val totalBeadSlots = if (maxLimit == 500) 100 else maxLimit

        // 🌟 CONDITIONAL ACCENT RATIO: 1 green bead for every 5 clicks when limit is 500
        val activeDotsCount = if (maxLimit == 500) {
            currentCount / 5
        } else {
            currentCount
        }

        // Adjust bead radius sizes dynamically to stay readable
        val beadRadius = when {
            maxLimit <= 33 -> dpToPx(5f)
            maxLimit <= 100 -> dpToPx(3f)
            else -> dpToPx(3f) // For 500 limit using 100 beads configuration
        }

        for (i in 0 until totalBeadSlots) {
            val angleDegrees = -90f + (i * (360f / totalBeadSlots))
            val angleRadians = Math.toRadians(angleDegrees.toDouble())

            val dotX = centerX + radius * cos(angleRadians).toFloat()
            val dotY = centerY + radius * sin(angleRadians).toFloat()

            if (i < activeDotsCount) {
                canvas.drawCircle(dotX, dotY, beadRadius, activePaint)
            } else {
                canvas.drawCircle(dotX, dotY, beadRadius, inactivePaint)
            }
        }
    }

    private fun dpToPx(dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }
}