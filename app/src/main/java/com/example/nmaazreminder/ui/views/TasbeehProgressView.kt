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

    // ⚪ Inactive Background Bead Paint Brush
    private val inactivePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E0DCD3")
        style = Paint.Style.FILL
    }

    // 🟢 Active Foreground Bead Paint Brush
    private val activePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#0D3E31")
        style = Paint.Style.FILL
    }

    fun updateProgress(count: Int, limit: Int) {
        this.currentCount = count
        this.maxLimit = limit
        invalidate() // Clears canvas and redraws elements instantly
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f

        // Dynamic padding translation to match layout boundaries cleanly
        val radius = (width / 2f) - dpToPx(30f)

        // Dynamic slot limits based on chosen target button parameters
        val totalBeadSlots =  maxLimit

        // Bead size scaling profile: 33 gets large beads, 100 gets sleek small beads
        val beadRadius = when {
            maxLimit <= 33 -> dpToPx(4.5f)
            maxLimit <= 100 -> dpToPx(2.5f)
            else -> dpToPx(1.0f)
        }

        val activeDotsCount = currentCount

        for (i in 0 until totalBeadSlots) {
            // -90 degrees locks starting bead explicitly at 12 o'clock top center
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

    // Single unified helper method for handling layout pixel densities safely
    private fun dpToPx(dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }
}