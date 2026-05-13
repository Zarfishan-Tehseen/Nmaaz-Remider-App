package com.example.nmaazreminder.data.repository

import com.example.nmaazreminder.data.local.PrayerSettings
import com.example.nmaazreminder.data.local.SettingsDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PrayerRepository @Inject constructor(
    private val settingsDao: SettingsDao
) {
    fun streamPrayerSettings(): Flow<PrayerSettings?> = settingsDao.getSettings()

    suspend fun updatePrayerSettings(settings: PrayerSettings) {
        settingsDao.saveSettings(settings)
    }
    suspend fun updateUserCoordinates(name: String, lat: Double, lon: Double) {
        settingsDao.updateLocation(name, lat, lon)
    }
}
