package com.example.nmaazreminder.ui.fragments.setting

data class SettingItem(
    val id: Int,
    val title: String,
    val subtitle: String,
    val iconRes: Int,
    val categoryHeader: String? = null, // Set if this item starts a new card block
    val isToggleable: Boolean = false, // True for SwitchCompat, False for Navigation Arrow
    var isChecked: Boolean = false,
    val isLastInGroup: Boolean = false // Helps us determine rounding states dynamically
)