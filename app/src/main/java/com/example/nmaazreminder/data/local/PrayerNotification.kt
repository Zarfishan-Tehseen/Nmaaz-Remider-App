package com.example.nmaazreminder.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prayer_notifications")
data class PrayerNotification(
    @PrimaryKey val prayerName: String,
    val isEnabled: Boolean = true,
    val soundName: String = "Adhan",
    val reminderOffset: Int = 5
)