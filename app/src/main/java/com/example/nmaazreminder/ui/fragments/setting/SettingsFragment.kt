package com.example.nmaazreminder.ui.fragments.setting

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nmaazreminder.R
import com.example.nmaazreminder.data.local.PrayerSettings
import com.example.nmaazreminder.databinding.FragmentSettingsBinding
import com.example.nmaazreminder.ui.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    // 🌟 Shared ViewModel delegate
    private val viewModel: MainViewModel by activityViewModels()

    private lateinit var settingsAdapter: SettingsAdapter
    private val settingsList = mutableListOf<SettingItem>()
    private var currentSettings: PrayerSettings? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSettingsBinding.bind(view)

        setupRecyclerView()

        // 🌟 Asynchronously listen to the database to update row components dynamically
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.globalSettings.collect { settings ->
                    if (settings != null) {
                        currentSettings = settings
                        populateSettingsList(settings)
                    }
                }
            }
        }
    }

    private fun setupRecyclerView() {
        settingsAdapter = SettingsAdapter(
            items = settingsList,
            onItemClicked = { item ->
                if (item.isToggleable) {
                    item.isChecked = !item.isChecked
                    val index = settingsList.indexOf(item)
                    if (index != -1) {
                        settingsAdapter.notifyItemChanged(index)
                    }
                    handleToggleLogic(item.id, item.isChecked)
                } else {
                    val destinationId = when (item.id) {
                        1 -> R.id.locationSelectorFragment
                        2 -> R.id.nav_calcmethod
                        3 -> R.id.nav_madhab
                        4 -> R.id.nav_notifications
                        5 -> R.id.nav_adhan_sound
                        8 -> R.id.nav_language
                        10 -> R.id.nav_home_style
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
     * Rebuilds the dataset dynamically using accurate values fetched direct from Room DB
     */
    private fun populateSettingsList(settings: PrayerSettings) {
        settingsList.clear()

        // Map calculation code constants back to human readable names
        val methodSubtitle = when (settings.calculationMethod) {
            1 -> "University of Islamic Sciences, Karachi"
            2 -> "Islamic Society of North America (ISNA)"
            3 -> "Muslim World League (MWL)"
            else -> "University of Islamic Sciences, Karachi"
        }

        val madhabSubtitle = if (settings.asrMethod == 1) "Hanafi" else "Shafi / Maliki / Hanbali"

        settingsList.addAll(
            listOf(
                // --- Group 1: Prayer Calculation ---
                SettingItem(1, "Location", settings.cityName, R.drawable.ic_location, categoryHeader = "PRAYER CALCULATION"),
                SettingItem(2, "Method", methodSubtitle, R.drawable.ic_global),
                SettingItem(3, "Madhab", madhabSubtitle, R.drawable.ic_ain, isLastInGroup = true),

                // --- Group 2: Reminders ---
                SettingItem(4, "Notifications", "4 of 5 prayers", R.drawable.ic_notification, categoryHeader = "REMINDERS"),
                SettingItem(5, "Default Adhan sound", "Makkah Adhan", R.drawable.ic_speaker, isLastInGroup = true),

                // --- Group 3: Appearance ---
                SettingItem(7, "Dark theme", "System default theme config", R.drawable.ic_sun_cloud_accent, categoryHeader = "APPEARANCE", isToggleable = true, isChecked = false),
                SettingItem(8, "Language", "English + Arabic", R.drawable.ic_global),
                SettingItem(9, "Show Arabic prayer names", "...", R.drawable.ic_allah, isToggleable = true, isChecked = true),
                SettingItem(10, "Home screen style", "List", R.drawable.ic_home_style, isLastInGroup = true),

                // --- Group 4: About ---
                SettingItem(11, "Rate Sakinah", "Rate us on Play Store", R.drawable.ic_star, categoryHeader = "ABOUT"),
                SettingItem(12, "Share with family", "Spread the word", R.drawable.ic_share),
                SettingItem(13, "Privacy", "No tracking. No ads.", R.drawable.ic_privacy, isLastInGroup = true)
            )
        )

        settingsAdapter.notifyDataSetChanged()
    }

    private fun handleToggleLogic(itemId: Int, isChecked: Boolean) {
        currentSettings?.let { settings ->
            when (itemId) {
                7 -> {
                    Toast.makeText(context, "Dark theme changed to: $isChecked", Toast.LENGTH_SHORT).show()
                    // Example configuration change update mapped to model parameters
                    // val updated = settings.copy(isDarkMode = isChecked)
                    // viewModel.saveGlobalSettings(updated)
                }
                9 -> {
                    Toast.makeText(context, "Show Arabic names: $isChecked", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}