package com.example.nmaazreminder.ui.fragments.setting.homestyle

data class HomeStyleItem(
    val id: Int,
    val styleKey: String, // e.g., "list", "day_dial", "mihrab_arch"
    val title: String,
    val subtitle: String,
    val illustrationRes: Int // Your custom illustration drawables
)