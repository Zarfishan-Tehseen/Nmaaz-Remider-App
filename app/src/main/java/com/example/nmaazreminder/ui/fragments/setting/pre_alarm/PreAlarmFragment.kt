package com.example.nmaazreminder.ui.fragments.setting.pre_alarm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.nmaazreminder.R
import com.example.nmaazreminder.databinding.FragmentPreAlarmBinding
import com.example.nmaazreminder.ui.viewmodel.PrayerDetailViewModel
import com.example.nmaazreminder.utils.setBounceClickListener
import com.google.android.material.chip.Chip
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PreAlarmFragment : Fragment(R.layout.fragment_pre_alarm) {

    private var _binding: FragmentPreAlarmBinding? = null
    private val binding get() = _binding!!

    // 🎯 ALIGNMENT FIX: Shared ViewModel lifecycle access
    private val viewModel: PrayerDetailViewModel by activityViewModels()
    private var selectedMinutes = 5

    private val presetOptions = listOf(
        "On time" to 0, "5 min" to 5, "10 min" to 10, "15 min" to 15,
        "20 min" to 20, "30 min" to 30, "45 min" to 45, "60 min" to 60
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPreAlarmBinding.bind(view)

        // Read active settings live from your database state stream
        val currentPrayerNotification = viewModel.settings.value
        val currentPrayerName = currentPrayerNotification?.prayerName ?: "Prayer"
        selectedMinutes = currentPrayerNotification?.reminderOffset ?: 0

        binding.tvReminderContext.text = "$currentPrayerName reminder"

        binding.btnBack.setBounceClickListener {
            findNavController().navigateUp()
        }

        // 🎯 ALIGNMENT FIX: Save structural offset integers straight up to Room
        binding.btnSaveSettings.setBounceClickListener {
            viewModel.updateOffset(selectedMinutes)
            Toast.makeText(context, "Saved: $selectedMinutes minutes pre-alarm for $currentPrayerName", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }

        setupPresetChips()
        setupSliderLogic()
        updateUiState(selectedMinutes)
    }

    private fun setupPresetChips() {
        binding.cgPresetsGroup.removeAllViews()
        presetOptions.forEach { (label, value) ->
            val chip = LayoutInflater.from(context).inflate(
                R.layout.layout_custom_preset_chip, binding.cgPresetsGroup, false
            ) as Chip

            chip.text = label
            chip.tag = value
            chip.id = View.generateViewId()

            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedMinutes = value
                    binding.sbTimeSlider.progress = value
                    binding.tvSelectedMinutesDisplay.text = value.toString()
                }
            }
            binding.cgPresetsGroup.addView(chip)
        }
    }

    private fun setupSliderLogic() {
        binding.sbTimeSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) { updateUiState(progress) }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun updateUiState(minutes: Int) {
        selectedMinutes = minutes
        binding.tvSelectedMinutesDisplay.text = minutes.toString()
        binding.sbTimeSlider.progress = minutes

        for (i in 0 until binding.cgPresetsGroup.childCount) {
            val chip = binding.cgPresetsGroup.getChildAt(i) as Chip
            if (chip.tag == minutes) {
                chip.isChecked = true
                return
            }
        }
        binding.cgPresetsGroup.clearCheck()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}