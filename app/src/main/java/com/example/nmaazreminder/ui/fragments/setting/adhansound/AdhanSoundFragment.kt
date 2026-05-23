package com.example.nmaazreminder.ui.fragments.setting.adhansound

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels // 🌟 Required Import
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nmaazreminder.R
import com.example.nmaazreminder.databinding.FragmentAdhanSoundBinding
import com.example.nmaazreminder.ui.viewmodel.PrayerDetailViewModel
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AdhanSoundFragment : Fragment(R.layout.fragment_adhan_sound) {

    private var _binding: FragmentAdhanSoundBinding? = null
    private val binding get() = _binding!!

    // 🎯 ALIGNMENT FIX: Share the exact same viewmodel instance using your NavGraph ID
    private val viewModel: PrayerDetailViewModel by activityViewModels()
    private lateinit var soundAdapter: AdhanSoundAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAdhanSoundBinding.bind(view)

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        val soundsList = listOf(
            AdhanSoundItem(1, "Makkah Adhan", "Sheikh Ali Mullah · 3:48"),
            AdhanSoundItem(2, "Madinah Adhan", "Sheikh Abdul Majid · 3:22"),
            AdhanSoundItem(3, "Egyptian Adhan", "Sheikh Mohamed Refat · 4:12"),
            AdhanSoundItem(4, "Al-Aqsa Adhan", "Sheikh Hashem Slaymeh · 3:32"),
            AdhanSoundItem(5, "Turkish Adhan", "Hafız Mustafa Özcan · 4:45"),
            AdhanSoundItem(6, "Soft Chime", "Notification tone · 0:04"),
            AdhanSoundItem(7, "Silent (Vibrate)", "No sound", isSilentOption = true)
        )

        binding.rvAdhanSounds.layoutManager = LinearLayoutManager(requireContext())

        // Initializing index based on currently saved database state safely
        val currentSavedSound = viewModel.settings.value?.soundName ?: "Madinah Adhan"
        val initialIndex = soundsList.indexOfFirst { it.title == currentSavedSound }.coerceAtLeast(0)

        soundAdapter = AdhanSoundAdapter(soundsList, selectedPosition = initialIndex) { trackItem, isPlaying ->
            if (isPlaying) {
                Toast.makeText(requireContext(), "Playing preview: ${trackItem.title}", Toast.LENGTH_SHORT).show()
            }
        }
        binding.rvAdhanSounds.adapter = soundAdapter

        // 🎯 ALIGNMENT FIX: Save selected sound track down into Room via ViewModel directly
        binding.btnSaveSelection.setOnClickListener {
            val selectedTrack = soundAdapter.getSelectedTrack()
            viewModel.updateSound(selectedTrack.title) // Fires update query immediately!
            Toast.makeText(requireContext(), "Saved setting: ${selectedTrack.title}", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}