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
}