package com.example.nmaazreminder.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prayer_settings")
data class PrayerSettings(
    @PrimaryKey val id: Int = 0,
    val cityName: String = "Select Location",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val calculationMethod: Int = 0,
    val asrMethod: Int = 0,
    val isDarkMode: Boolean = false
)