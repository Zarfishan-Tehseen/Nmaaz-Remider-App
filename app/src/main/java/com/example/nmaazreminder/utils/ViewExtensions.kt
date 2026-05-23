package com.example.nmaazreminder.utils

import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.drawable.GradientDrawable
import android.view.MotionEvent
import android.view.View

// Converts DP integer values into exact system pixels
val Int.toPx: Float
    get() = (this * Resources.getSystem().displayMetrics.density)

/**
 * Dynamically updates a view's background shape with precise corner rounding configurations
 */
fun View.setRoundedCorners(
    backgroundColor: Int,
    topLeft: Float = 0f,
    topRight: Float = 0f,
    bottomRight: Float = 0f,
    bottomLeft: Float = 0f
) {
    val shape = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        setColor(backgroundColor)
        // Corner array expects X and Y radius for each of the 4 corners
        cornerRadii = floatArrayOf(
            topLeft, topLeft,
            topRight, topRight,
            bottomRight, bottomRight,
            bottomLeft, bottomLeft
        )
    }
    this.background = shape
}

/**
 * Applies a premium fluid scale bounce animation when a view is pressed.
 * It shrinks the row item slightly on down-touch and bounces back smoothly when released.
 */
@SuppressLint("ClickableViewAccessibility")
fun View.setBounceClickListener(onClick: () -> Unit) {
    this.setOnTouchListener { v, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Shrink slightly to 96% scale when finger presses down
                v.animate()
                    .scaleX(0.96f)
                    .scaleY(0.96f)
                    .setDuration(100)
                    .start()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                // Return to original size when finger is released
                v.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .withEndAction {
                        // Fire the actual click action ONLY after animation finishes cleanly if not canceled
                        if (event.action == MotionEvent.ACTION_UP) {
                            onClick()
                        }
                    }
                    .start()
            }
        }
        true
    }
}