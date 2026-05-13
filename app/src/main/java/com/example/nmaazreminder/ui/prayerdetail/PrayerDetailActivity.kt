package com.example.nmaazreminder.ui.prayerdetail

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.nmaazreminder.databinding.ActivityPrayerDetailBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PrayerDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrayerDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrayerDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prayerName = intent.getStringExtra("PRAYER_NAME") ?: "Prayer"
        val prayerTime = intent.getStringExtra("PRAYER_TIME") ?: "--:--"

        binding.tvDetailPrayerName.text = prayerName
        binding.tvPrayerTime.text = prayerTime

        setupDynamicLabels(prayerName)

        binding.btnBack.setOnClickListener {
            finish() // Goes back to MainActivity
        }
    }

    private fun setupDynamicLabels(name: String) {
        // You can use a 'when' block to change the subtitle text
        // based on the prayer name passed from the RecyclerView
        binding.tvPrayerSubtitle.text = when (name) {
            "Fajr" -> "Dawn Prayer"
            "Zuhr" -> "Noon Prayer"
            "Asr" -> "Afternoon Prayer"
            "Maghrib" -> "Sunset Prayer"
            "Isha" -> "Night Prayer"
            else -> "Daily Prayer"
        }
    }
}