package com.example.nmaazreminder.ui.fragments.home

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PrayerItem(
    val name: String,
    val time: String,
    val isNotificationEnabled: Boolean = true
): Parcelable