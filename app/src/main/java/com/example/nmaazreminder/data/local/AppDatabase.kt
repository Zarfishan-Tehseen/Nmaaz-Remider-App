package com.example.nmaazreminder.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [PrayerSettings::class], version = 1)
abstract class AppDatabase : RoomDatabase() { // room handle kry ga how to save files
    abstract fun settingsDao(): SettingsDao
}