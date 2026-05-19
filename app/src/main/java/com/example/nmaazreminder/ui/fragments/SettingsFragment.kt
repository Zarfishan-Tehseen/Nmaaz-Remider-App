package com.example.nmaazreminder.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nmaazreminder.R
import com.example.nmaazreminder.databinding.FragmentSettingsBinding
import com.example.nmaazreminder.ui.settings.SettingItem
import com.example.nmaazreminder.ui.settings.SettingsAdapter

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSettingsBinding.bind(view)

        val settingsList = listOf(
            // --- Group 1: Prayer Calculation ---
            SettingItem(1, "Location", "Karachi, Pakistan", R.drawable.ic_location, categoryHeader = "PRAYER CALCULATION"),
            SettingItem(2, "Method", "University of Karachi", R.drawable.ic_global),
            SettingItem(3, "Madhab", "Hanafi", R.drawable.ic_ain, isLastInGroup = true),

            // --- Group 2: Reminders ---
            SettingItem(4, "Notifications", "4 of 5 prayers", R.drawable.ic_notification, categoryHeader = "REMINDERS"),
            // Changed to isLastInGroup = true since it's now the last item in this card block
            SettingItem(5, "Default Adhan sound", "Makkah Adhan", R.drawable.ic_volume, isLastInGroup = true),

            // --- Group 3: Appearance ---
            SettingItem(7, "Dark theme", "Light & airy", R.drawable.ic_sun_cloud_accent, categoryHeader = "APPEARANCE", isToggleable = true, isChecked = false),
            SettingItem(8, "Language", "English + Arabic", R.drawable.ic_global),
            SettingItem(9, "Show Arabic prayer names", "...", R.drawable.ic_allah, isToggleable = true, isChecked = true),
            SettingItem(10, "Home screen style", "List", R.drawable.ic_home_style, isLastInGroup = true),

            // --- Group 4: About ---
            SettingItem(11, "Rate Sakinah", "Rate us on Play Store", R.drawable.ic_star, categoryHeader = "ABOUT"),
            SettingItem(12, "Share with family", "Spread the word", R.drawable.ic_share),
            SettingItem(13, "Privacy", "No tracking. No ads.", R.drawable.ic_privacy, isLastInGroup = true)
        )

        // Attach adapter implementations to RecyclerView setup
        val settingsAdapter = SettingsAdapter(
            items = settingsList,
            onItemClicked = { item ->
                Toast.makeText(context, "Navigating to ${item.title}", Toast.LENGTH_SHORT).show()
            },
            onToggleChanged = { item, isChecked ->
                Toast.makeText(context, "${item.title} toggled: $isChecked", Toast.LENGTH_SHORT).show()
            }
        )

        binding.rvSettings.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = settingsAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}