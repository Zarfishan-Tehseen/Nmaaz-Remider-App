package com.example.nmaazreminder.data.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.batoulapps.adhan.PrayerTimes
import com.example.nmaazreminder.data.repository.PrayerRepository
import com.example.nmaazreminder.data.receiver.PrayerAlarmReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.firstOrNull
import java.util.Date
import javax.inject.Inject

class PrayerAlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: PrayerRepository // 🌟 Inject the repository to read user settings
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    suspend fun scheduleAlarms(times: PrayerTimes) {
        val prayers = listOf(
            "Fajr" to times.fajr,
            "Dhuhr" to times.dhuhr,
            "Asar" to times.asr,
            "Maghrib" to times.maghrib,
            "Isha" to times.isha
        )

        prayers.forEach { (name, rawDate) ->
            // 1. Fetch the user's customized database settings for this specific prayer
            val dbNotification = repository.getNotificationSetting(name).firstOrNull()

            // 2. If user disabled notifications for this prayer, skip scheduling entirely!
            if (dbNotification != null && !dbNotification.isEnabled) {
                cancelAlarm(name)
                return@forEach
            }

            // 3. Apply the custom reminder offset (subtracting minutes converted to milliseconds)
            val offsetMinutes = dbNotification?.reminderOffset ?: 0
            val targetTimeInMillis = rawDate.time - (offsetMinutes * 60 * 1000)

            if (targetTimeInMillis > System.currentTimeMillis()) { // Only schedule for the future
                val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
                    putExtra("PRAYER_NAME", name)
                    putExtra("SOUND_NAME", dbNotification?.soundName ?: "Adhan")
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    name.hashCode(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                try {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        targetTimeInMillis,
                        pendingIntent
                    )
                } catch (e: SecurityException) {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        targetTimeInMillis,
                        pendingIntent
                    )
                }
            }
        }
    }

    private fun cancelAlarm(prayerName: String) {
        val intent = Intent(context, PrayerAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            prayerName.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }
}