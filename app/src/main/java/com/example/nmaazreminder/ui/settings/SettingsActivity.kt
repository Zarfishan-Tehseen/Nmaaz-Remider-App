package com.example.nmaazreminder.ui.settings

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.nmaazreminder.data.local.PrayerSettings
import com.example.nmaazreminder.databinding.ActivitySettingsBinding
import com.example.nmaazreminder.ui.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRowTitles()
        setupClickListeners()
        observeSettings()
    }

    private fun setupRowTitles() {
        binding.apply {
            methodKarachi.tvTitle.text = "University of Islamic Sciences, Karachi"
            methodMWL.tvTitle.text = "Muslim World League"
            methodNorthAmerica.tvTitle.text = "North America (ISNA)"

            itemHanafi.tvTitle.text = "Hanafi (Standard)"
            itemShafi.tvTitle.text = "Shafi, Maliki, Hanbali"
        }
    }

    private fun observeSettings() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.settingsState.collect { settings ->
                    settings?.let {
                        updateSelectionVisuals(it)
                    }
                }
            }
        }
    }

    private fun updateSelectionVisuals(settings: PrayerSettings) {
        binding.apply {
            // 1. Update Calculation Method UI
            val calcRows = listOf(methodKarachi, methodMWL, methodNorthAmerica)
            calcRows.forEachIndexed { index, row ->
                val isSelected = (index + 1) == settings.calculationMethod
                row.apply {
                    root.setBackgroundColor(if (isSelected) Color.parseColor("#F1FBF5") else Color.TRANSPARENT)
                    ivCheck.visibility = if (isSelected) View.VISIBLE else View.GONE
                    tvTitle.setTextColor(if (isSelected) Color.parseColor("#00695C") else Color.parseColor("#2D3436"))
                }
            }

            // 2. Update Asr Method UI
            val asrRows = listOf(itemHanafi, itemShafi)
            asrRows.forEachIndexed { index, row ->
                val isSelected = (index + 1) == settings.asrMethod
                row.apply {
                    root.setBackgroundColor(if (isSelected) Color.parseColor("#F1FBF5") else Color.TRANSPARENT)
                    ivCheck.visibility = if (isSelected) View.VISIBLE else View.GONE
                    tvTitle.setTextColor(if (isSelected) Color.parseColor("#00695C") else Color.parseColor("#2D3436"))
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            methodKarachi.root.setOnClickListener { updateAndSave(calc = 1) }
            methodMWL.root.setOnClickListener { updateAndSave(calc = 2) }
            methodNorthAmerica.root.setOnClickListener { updateAndSave(calc = 3) }

            itemHanafi.root.setOnClickListener { updateAndSave(asr = 1) }
            itemShafi.root.setOnClickListener { updateAndSave(asr = 2) }
        }
    }

    private fun updateAndSave(calc: Int? = null, asr: Int? = null) {
        lifecycleScope.launch {
            val current = viewModel.settingsState.value
            val updatedSettings = PrayerSettings(
                id = 1,
                latitude = current?.latitude ?: 32.074,
                longitude = current?.longitude ?: 72.686,
                calculationMethod = calc ?: current?.calculationMethod ?: 1,
                asrMethod = asr ?: current?.asrMethod ?: 1
            )
            viewModel.saveSettings(updatedSettings)
            Toast.makeText(this@SettingsActivity, "Settings Saved", Toast.LENGTH_SHORT).show()
        }
    }
}