package com.example.nmaazreminder.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Query("SELECT * FROM prayer_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<PrayerSettings?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: PrayerSettings)

    @Query("UPDATE prayer_settings SET cityName = :name, latitude = :lat, longitude = :lon WHERE id = 0")
    suspend fun updateLocation(name: String, lat: Double, lon: Double)
}