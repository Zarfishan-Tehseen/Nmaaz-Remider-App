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
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // 🌟 Make sure 'globalSettings' or 'prayerNotificationsList' is open and public in MainViewModel
                viewModel.globalSettings.collect { settings ->
                    settings?.let {
                        // For demonstration matching your manual dataset design pattern:
                        val staticList = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")

                        val mappedList = staticList.map { prayerName ->
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
                                // Using fallbacks from your local settings schema dynamically
                                statusText = if (settings.isMasterNotificationEnabled) "Adhan · Active" else "Muted",                                iconDrawableId = when (prayerName.lowercase().trim()) {
                                    "fajr", "isha" -> R.drawable.ic_moon
                                    "dhuhr", "asr" -> R.drawable.ic_sun
                                    "maghrib" -> R.drawable.ic_cloud
                                    else -> R.drawable.ic_sun
                                },
                                isEnabled = settings.isMasterNotificationEnabled
                            )
                        }

                        // Submit the clean collection to the adapter
                        notificationAdapter.submitList(mappedList)

                        // Sync layout master state counter badges dynamically
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