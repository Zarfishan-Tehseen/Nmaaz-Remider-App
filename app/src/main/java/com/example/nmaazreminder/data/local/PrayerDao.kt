package com.example.nmaazreminder.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PrayerDao {
    @Query("SELECT * FROM prayer_notifications WHERE prayerName = :prayerName")
    fun getSettingForPrayer(prayerName: String): Flow<PrayerNotification?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updatePrayerSetting(settings: PrayerNotification)

    @Query("UPDATE prayer_notifications SET isEnabled = :isEnabled")
    suspend fun updateAllPrayersEnabledStatus(isEnabled: Boolean)

    @Query("""
    SELECT * FROM prayer_notifications 
    ORDER BY CASE prayerName
        WHEN 'Fajr' THEN 1
        WHEN 'Dhuhr' THEN 2
        WHEN 'Asr' THEN 3
        WHEN 'Maghrib' THEN 4
        WHEN 'Isha' THEN 5
        ELSE 6 
    END ASC
""")
    fun getAllSettings(): Flow<List<PrayerNotification>>
}