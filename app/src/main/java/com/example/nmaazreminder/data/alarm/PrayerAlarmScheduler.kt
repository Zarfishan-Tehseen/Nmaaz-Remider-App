package com.example.nmaazreminder.data.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.batoulapps.adhan.PrayerTimes
import com.example.nmaazreminder.data.receiver.PrayerAlarmReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PrayerAlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleAlarms(times: PrayerTimes) {
        val prayers = listOf(
            "Fajr" to times.fajr,
            "Dhuhr" to times.dhuhr,
            "Asar" to times.asr,
            "Maghrib" to times.maghrib,
            "Isha" to times.isha
        )

        prayers.forEach { (name, date) ->
            if (date.after(java.util.Date())) { // Only schedule future prayers
                val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
                    putExtra("PRAYER_NAME", name)
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    name.hashCode(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                // Set exact alarm
                try {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        date.time,
                        pendingIntent
                    )
                } catch (e: SecurityException) {
                    // If exact alarm permission is denied, fallback to a normal alarm
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        date.time,
                        pendingIntent
                    )
                }
            }
        }
    }
}