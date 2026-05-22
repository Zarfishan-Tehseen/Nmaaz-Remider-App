package com.example.nmaazreminder.ui.fragments.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nmaazreminder.R
import com.example.nmaazreminder.databinding.FragmentNotificationSettingsBinding

class NotificationSettingsFragment : Fragment() {

    private var _binding: FragmentNotificationSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.switchMasterAdhan.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.tvEnabledCounter.text = "5 of 5 enabled"
                Toast.makeText(requireContext(), "All notifications enabled", Toast.LENGTH_SHORT).show()
            } else {
                binding.tvEnabledCounter.text = "0 of 5 enabled"
                Toast.makeText(requireContext(), "All notifications muted", Toast.LENGTH_SHORT).show()
            }
        }

        // Prepare dataset
        val prayerNotifications = listOf(
            PrayerNotificationItem("Fajr", "الفجر", "Madinah Adhan · 10 min before", R.drawable.ic_moon),
            PrayerNotificationItem("Dhuhr", "الظهر", "Makkah Adhan · 5 min before", R.drawable.ic_sun),
            PrayerNotificationItem("Asr", "العصر", "Egyptian Adhan · 5 min before", R.drawable.ic_sun),
            PrayerNotificationItem("Maghrib", "المغرب", "Makkah Adhan · on time", R.drawable.ic_cloud),
            PrayerNotificationItem("Isha", "العشاء", "Silent (Vibrate) · 15 min before", R.drawable.ic_moon)
        )

        // Bind data with our separate adapter implementation
        binding.rvPrayerNotifications.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPrayerNotifications.adapter = PrayerNotificationAdapter(prayerNotifications) { item ->
            Toast.makeText(requireContext(), "Configure alert for ${item.name}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}