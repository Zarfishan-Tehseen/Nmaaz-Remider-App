package com.example.nmaazreminder.data.repository

import com.example.nmaazreminder.data.local.PrayerNotification
import com.example.nmaazreminder.data.local.PrayerDao
import com.example.nmaazreminder.data.local.PrayerSettings
import com.example.nmaazreminder.data.local.SettingsDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PrayerRepository @Inject constructor(
    private val settingsDao: SettingsDao,
    private val prayerDao: PrayerDao
) {
    fun streamPrayerSettings(): Flow<PrayerSettings?> = settingsDao.getSettings()

    suspend fun updatePrayerSettings(settings: PrayerSettings) {
        settingsDao.saveSettings(settings)
    }
    suspend fun updateUserCoordinates(name: String, lat: Double, lon: Double) {
        settingsDao.updateLocation(name, lat, lon)
    }

    // 1. Get settings for a specific prayer as a Flow
    fun getNotificationSetting(prayerName: String): Flow<PrayerNotification?> {
        return prayerDao.getSettingForPrayer(prayerName)
    }

    // 2. A generic update function
    suspend fun updateNotificationSetting(setting: PrayerNotification) {
        prayerDao.updatePrayerSetting(setting)
    }
}
