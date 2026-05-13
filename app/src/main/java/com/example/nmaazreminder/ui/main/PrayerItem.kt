package com.example.nmaazreminder.ui.main

data class PrayerItem(
    val name: String,
    val time: String,
    val isNotificationEnabled: Boolean = false
)