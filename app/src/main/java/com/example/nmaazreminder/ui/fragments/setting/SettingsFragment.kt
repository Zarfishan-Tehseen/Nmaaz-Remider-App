package com.example.nmaazreminder.ui.fragments.setting

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nmaazreminder.R
import com.example.nmaazreminder.databinding.FragmentSettingsBinding
import com.example.nmaazreminder.ui.settings.SettingItem
import com.example.nmaazreminder.ui.settings.SettingsAdapter

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    // Decoupled adapter variable to allow signaling item updates inside row click listeners
    private lateinit var settingsAdapter: SettingsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSettingsBinding.bind(view)

        // Changed to mutableListOf so we can modify the isChecked state at runtime
        val settingsList = mutableListOf(
            // --- Group 1: Prayer Calculation ---
            SettingItem(
                1,
                "Location",
                "Karachi, Pakistan",
                R.drawable.ic_location,
                categoryHeader = "PRAYER CALCULATION"
            ),
            SettingItem(2, "Method", "University of Karachi", R.drawable.ic_global),
            SettingItem(3, "Madhab", "Hanafi", R.drawable.ic_ain, isLastInGroup = true),

            // --- Group 2: Reminders ---
            SettingItem(
                4,
                "Notifications",
                "4 of 5 prayers",
                R.drawable.ic_notification,
                categoryHeader = "REMINDERS"
            ),
            SettingItem(
                5,
                "Default Adhan sound",
                "Makkah Adhan",
                R.drawable.ic_volume,
                isLastInGroup = true
            ),

            // --- Group 3: Appearance ---
            SettingItem(
                7,
                "Dark theme",
                "Light & airy",
                R.drawable.ic_sun_cloud_accent,
                categoryHeader = "APPEARANCE",
                isToggleable = true,
                isChecked = false
            ),
            SettingItem(8, "Language", "English + Arabic", R.drawable.ic_global),
            SettingItem(
                9,
                "Show Arabic prayer names",
                "...",
                R.drawable.ic_allah,
                isToggleable = true,
                isChecked = true
            ),
            SettingItem(
                10,
                "Home screen style",
                "List",
                R.drawable.ic_home_style,
                isLastInGroup = true
            ),

            // --- Group 4: About ---
            SettingItem(
                11,
                "Rate Sakinah",
                "Rate us on Play Store",
                R.drawable.ic_star,
                categoryHeader = "ABOUT"
            ),
            SettingItem(12, "Share with family", "Spread the word", R.drawable.ic_share),
            SettingItem(
                13,
                "Privacy",
                "No tracking. No ads.",
                R.drawable.ic_privacy,
                isLastInGroup = true
            )
        )

        settingsAdapter = SettingsAdapter(
            items = settingsList,
            onItemClicked = { item ->
                if (item.isToggleable) {
                    // 1. Manually invert the model's state when clicking anywhere on the item row
                    item.isChecked = !item.isChecked

                    // 2. Find the exact item index position inside the list
                    val index = settingsList.indexOf(item)
                    if (index != -1) {
                        settingsAdapter.notifyItemChanged(index)
                    }

                    // 3. Execute your theme switching or feature toggle logic
                    handleToggleLogic(item.id, item.isChecked)
                } else {
                    // Handle navigation endpoints for non-toggle settings items
                    val destinationId = when (item.id) {
                        1 -> R.id.locationSelectorFragment
                        2 -> R.id.nav_calcmethod
                        3 -> R.id.nav_madhab
                        4 -> R.id.nav_notifications
                        5 -> R.id.nav_adhan_sound
                        else -> null
                    }

                    if (destinationId != null) {
                        findNavController().navigate(destinationId)
                    } else {
                        Toast.makeText(context, "Clicked: ${item.title}", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onToggleChanged = { item, isChecked ->
                // Ensures that interacting directly with the toggle switch widget updates the model layer
                if (item.isChecked != isChecked) {
                    item.isChecked = isChecked
                    handleToggleLogic(item.id, isChecked)
                }
            }
        )

        binding.rvSettings.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = settingsAdapter
        }
    }

    /**
     * Helper function to centralize switch toggle logic (such as saving states to SharedPreferences)
     */
    private fun handleToggleLogic(itemId: Int, isChecked: Boolean) {
        when (itemId) {
            7 -> {
                Toast.makeText(context, "Dark theme changed to: $isChecked", Toast.LENGTH_SHORT).show()
                // TODO: AppCompatDelegate.setDefaultNightMode(...) can be called here to change themes dynamically
            }
            9 -> {
                Toast.makeText(context, "Show Arabic names: $isChecked", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}