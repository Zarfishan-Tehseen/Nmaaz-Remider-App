package com.example.nmaazreminder.ui.fragments.setting.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nmaazreminder.R
import com.example.nmaazreminder.databinding.FragmentNotificationSettingsBinding
import com.example.nmaazreminder.ui.fragments.home.PrayerItem
import com.example.nmaazreminder.ui.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint // 🌟 1. CRITICAL: Added entry point for Hilt dependency injection
class NotificationSettingsFragment : Fragment() {

    private var _binding: FragmentNotificationSettingsBinding? = null
    private val binding get() = _binding!!

    // 🌟 2. FIXED: Declared and initialized the missing shared ViewModel instance
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Back button mapping using standard NavController pops
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // Initialize the upgraded ListAdapter with the full-screen navigation callback
        val notificationAdapter = PrayerNotificationAdapter { clickedItem ->
            val prayerParcel = PrayerItem(
                name = clickedItem.name,
                time = "" // Fetched dynamically by Detail ViewModel using the name key
            )
            val bundle = Bundle().apply {
                putParcelable("selectedPrayer", prayerParcel)
            }
            // Navigates cleanly to the whole full-screen standalone fragment window
            findNavController().navigate(R.id.prayerDetailFragment, bundle)
        }

        // Set up your RecyclerView layout rules
        binding.rvPrayerNotifications.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPrayerNotifications.adapter = notificationAdapter

        // Master Action Listener: Updates Room via the freshly declared viewModel
        binding.switchMasterAdhan.setOnClickListener {
            val isChecked = binding.switchMasterAdhan.isChecked
            viewModel.toggleAllNotifications(isChecked)
        }

        // Reactive State Observation Pipeline: Collects live settings changes from Room
        // Reactive State Observation Pipeline: Merging Master status with Real Data entries
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // We use combine to listen to master toggle AND individual item changes together
                kotlinx.coroutines.flow.combine(
                    viewModel.globalSettings,
                    viewModel.prayerNotificationsList
                ) { settings, dbPrayers ->
                    Pair(settings, dbPrayers)
                }.collect { (settings, dbPrayers) ->
                    if (settings != null && dbPrayers.isNotEmpty()) {

                        // Map using real database properties instead of hardcoded static lists
                        val mappedList = dbPrayers.map { dbPrayer ->
                            val prayerName = dbPrayer.prayerName

                            // Convert raw offset integer from database into clean UI display text
                            val offsetText = when (dbPrayer.reminderOffset) {
                                0 -> "on time"
                                else -> "${dbPrayer.reminderOffset} min before"
                            }

                            val iconRes = when (prayerName.lowercase().trim()) {
                                "fajr", "isha" -> R.drawable.ic_moon
                                "dhuhr", "asr" -> R.drawable.ic_sun
                                "maghrib" -> R.drawable.ic_cloud
                                else -> R.drawable.ic_sun
                            }

                            PrayerNotificationItem(
                                name = prayerName,
                                arabicName = when (prayerName.lowercase().trim()) {
                                    "fajr" -> "الفجر"
                                    "dhuhr" -> "الظهر"
                                    "asr" -> "العصر"
                                    "maghrib" -> "المغرب"
                                    "isha" -> "العشاء"
                                    else -> ""
                                },
                                adhanSoundName = dbPrayer.soundName, // 🎯 Real Sound from Database!
                                offsetMinutesText = offsetText,       // 🎯 Real Offset from Database!
                                iconDrawableId = iconRes,
                                isMasterEnabled = settings.isMasterNotificationEnabled,
                                isItemEnabled = dbPrayer.isEnabled
                            )
                        }

                        // Submit clean dynamic list to adapter
                        notificationAdapter.submitList(mappedList)

                        // Sync UI top status bars
                        binding.tvEnabledCounter.text = if (settings.isMasterNotificationEnabled) "5 of 5 enabled" else "0 of 5 enabled"
                        binding.switchMasterAdhan.isChecked = settings.isMasterNotificationEnabled
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}