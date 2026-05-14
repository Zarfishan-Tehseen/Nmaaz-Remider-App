package com.example.nmaazreminder.ui.prayerdetail

import android.os.Bundle
import androidx.activity.viewModels // Add this for 'by viewModels()'
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.nmaazreminder.databinding.ActivityPrayerDetailBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PrayerDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrayerDetailBinding

    private val viewModel: PrayerDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrayerDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prayerName = intent.getStringExtra("PRAYER_NAME") ?: "Fajr"
        val prayerTime = intent.getStringExtra("PRAYER_TIME") ?: "--:--"

        binding.tvDetailPrayerName.text = prayerName
        binding.tvPrayerTime.text = prayerTime

        setupDynamicLabels(prayerName)

        viewModel.loadSettings(prayerName)

        binding.btnBack.setOnClickListener {
            finish()
        }

        //  Observe the data flow
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.settings.collect { data ->
                    data?.let {
                        // Check your XML ID here. It might be binding.switch1 or similar
                        // based on your previous XML snippet
                        binding.notificationSwitch.isChecked = it.isEnabled

                        binding.tvSoundValue.text = it.soundName
                        binding.tvOffsetValue.text = if (it.reminderOffset == 0) "At time"
                        else "${it.reminderOffset} min before"
                    }
                }
            }
        }

        binding.notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateNotification(isChecked)
        }

        binding.layoutSound.setOnClickListener { showSoundPicker() }
        binding.layoutOffset.setOnClickListener { showOffsetPicker() }
    }

    private fun setupDynamicLabels(name: String) {
        binding.tvPrayerSubtitle.text = when (name) {
            "Fajr" -> "Dawn Prayer"
            "Zuhr" -> "Noon Prayer"
            "Asr" -> "Afternoon Prayer"
            "Maghrib" -> "Sunset Prayer"
            "Isha" -> "Night Prayer"
            else -> "Daily Prayer"
        }
    }

    private fun showSoundPicker() {
        val sounds = arrayOf("Adhan 1", "Adhan 2", "Soft Chime", "None")

        MaterialAlertDialogBuilder(this)
            .setTitle("Select Notification Sound")
            .setItems(sounds) { _, which ->
                val selectedSound = sounds[which]
                viewModel.updateSound(selectedSound)
                // The UI will update automatically because we are observing the Flow
            }
            .show()
    }

    private fun showOffsetPicker() {
        val options = arrayOf("At time", "5 min before", "10 min before", "15 min before")
        val values = intArrayOf(0, 5, 10, 15) // Minutes to save in DB

        MaterialAlertDialogBuilder(this)
            .setTitle("Select Reminder Offset")
            .setItems(options) { _, which ->
                viewModel.updateOffset(values[which])
            }
            .show()
    }
}