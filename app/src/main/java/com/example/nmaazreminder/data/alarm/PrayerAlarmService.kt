package com.example.nmaazreminder.data.alarm

import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.IBinder
import com.example.nmaazreminder.R

class PrayerAlarmService : Service() {

    private var mediaPlayer: MediaPlayer? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action

        if (action == "START_ALARM") {
            startAlarm(intent)
        } else if (action == "STOP_ALARM") {
            stopAlarm()
            stopSelf() // Closes the service completely
        }

        return START_NOT_STICKY
    }

    private fun startAlarm(intent: Intent?) {
        if (mediaPlayer?.isPlaying == true) return

        val soundName = intent?.getStringExtra("SOUND_NAME") ?: "Adhan"

        // Map the string label matching your database rows to an actual app file resource id
        val soundResId = if (soundName == "Custom Adhan Name") {
            R.raw.default_sound
        } else {
            // Fallback or system default URI backup fallback path
            R.raw.default_sound
        }

        try {
            mediaPlayer = MediaPlayer().apply {
                // Use setDataSource with open resource handles
                val assetFileDescriptor = applicationContext.resources.openRawResourceFd(soundResId)
                setDataSource(assetFileDescriptor.fileDescriptor, assetFileDescriptor.startOffset, assetFileDescriptor.length)
                assetFileDescriptor.close()

                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // If your resource fails, run your original backup system ringtone logic here...
        }
    }

    private fun stopAlarm() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaPlayer = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAlarm() // Absolute backup safety cleanup
    }
}