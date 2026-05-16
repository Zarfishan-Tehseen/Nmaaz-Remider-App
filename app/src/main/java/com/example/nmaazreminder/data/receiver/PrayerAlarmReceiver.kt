package com.example.nmaazreminder.data.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.nmaazreminder.data.alarm.PrayerAlarmService

class PrayerAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action

        // 1. If it's a swipe event, send a message to stop the service directly
        if (action == "com.example.nmaazreminder.ACTION_STOP_ALARM") {
            val stopIntent = Intent(context, PrayerAlarmService::class.java).apply {
                this.action = "STOP_ALARM"
            }
            context.startService(stopIntent)
            return
        }

        // 2. Otherwise, start the alarm audio via Service
        val startServiceIntent = Intent(context, PrayerAlarmService::class.java).apply {
            this.action = "START_ALARM"
        }
        context.startService(startServiceIntent)

        // 3. Display the notification visual
        val prayerName = intent.getStringExtra("PRAYER_NAME") ?: "Prayer"
        showNotification(context, prayerName)
    }

    private fun showNotification(context: Context, prayerName: String) {
        val channelId = "prayer_alerts"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Prayer Reminders",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Setup the swipe intent that talks back to this receiver
        val swipeIntent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            action = "com.example.nmaazreminder.ACTION_STOP_ALARM"
        }

        val swipePendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            swipeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Time for $prayerName")
            .setContentText("It is time for $prayerName prayer.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDeleteIntent(swipePendingIntent) // Triggers on swipe away
            .setAutoCancel(true)
            .build()

        notificationManager.notify(prayerName.hashCode(), notification)
    }
}