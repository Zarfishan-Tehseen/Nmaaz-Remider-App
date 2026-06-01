package com.example.nmaazreminder.ui.fragments.setting.notifications

data class PrayerNotificationItem(
    val name: String,
    val arabicName: String,
    val iconDrawableId: Int,
    val adhanSoundName: String,
    val offsetMinutesText: String,
    val isMasterEnabled: Boolean,
    val isItemEnabled: Boolean
)