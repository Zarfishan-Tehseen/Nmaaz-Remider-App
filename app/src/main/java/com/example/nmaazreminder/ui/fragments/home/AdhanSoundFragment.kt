package com.example.nmaazreminder.ui.fragments.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nmaazreminder.ui.fragments.setting.AdhanSoundItem
import com.example.nmaazreminder.databinding.FragmentAdhanSoundBinding
import com.example.nmaazreminder.ui.fragments.setting.AdhanSoundAdapter

class AdhanSoundFragment : Fragment() {

    private var _binding: FragmentAdhanSoundBinding? = null
    private val binding get() = _binding!!
    private lateinit var soundAdapter: AdhanSoundAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdhanSoundBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // 1. Prepare raw collection objects data mapping profiles array metrics matching design
        val soundsList = listOf(
            AdhanSoundItem(1, "Makkah Adhan", "Sheikh Ali Mullah · 3:48"),
            AdhanSoundItem(2, "Madinah Adhan", "Sheikh Abdul Majid · 3:22"),
            AdhanSoundItem(3, "Egyptian Adhan", "Sheikh Mohamed Refat · 4:12"),
            AdhanSoundItem(4, "Al-Aqsa Adhan", "Sheikh Hashem Slaymeh · 3:32"),
            AdhanSoundItem(5, "Turkish Adhan", "Hafız Mustafa Özcan · 4:45"),
            AdhanSoundItem(6, "Soft Chime", "Notification tone · 0:04"),
            AdhanSoundItem(7, "Silent (Vibrate)", "No sound", isSilentOption = true)
        )

        // 2. Map structural components elements parameters binding views directly layout sets
        binding.rvAdhanSounds.layoutManager = LinearLayoutManager(requireContext())

        // Initializing with index 2 ("Egyptian Adhan") selected by default matching your picture template choice wrapper card
        soundAdapter = AdhanSoundAdapter(soundsList, selectedPosition = 2) { trackItem, isPlaying ->
            if (isPlaying) {
                // TODO: Initialize your MediaPlayer asset instances to start audio preview tracks metrics streams here
                Toast.makeText(requireContext(), "Playing preview: ${trackItem.title}", Toast.LENGTH_SHORT).show()
            } else {
                // TODO: Stop sound player instances elements safely logic pipelines tracking loop systems
            }
        }
        binding.rvAdhanSounds.adapter = soundAdapter

        // 3. Persistent operational processing event save trigger button actions
        binding.btnSaveSelection.setOnClickListener {
            val selectedTrack = soundAdapter.getSelectedTrack()
            // TODO: Persist configuration preferences data blocks locally via SharedPreferences/DataStore frameworks
            Toast.makeText(requireContext(), "Saved setting: ${selectedTrack.title}", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}