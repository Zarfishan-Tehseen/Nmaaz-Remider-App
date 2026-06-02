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

    // 1️⃣ Mode 1 (Specific): Detail fragment se sirf ek namaz ka sound badalne ke liye
    @Query("UPDATE prayer_notifications SET soundName = :sound WHERE prayerName = :prayerName")
    suspend fun updateSoundForSpecificPrayer(prayerName: String, sound: String)

    // 2️⃣ Mode 2 (Global): Settings screen se SARI namazon ka sound ek sath badalne ke liye
    @Query("UPDATE prayer_notifications SET soundName = :sound")
    suspend fun updateSoundForAllPrayers(sound: String)
}