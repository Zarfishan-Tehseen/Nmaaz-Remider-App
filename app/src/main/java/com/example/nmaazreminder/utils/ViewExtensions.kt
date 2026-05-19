package com.example.nmaazreminder.utils

import android.content.res.Resources
import android.graphics.drawable.GradientDrawable
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