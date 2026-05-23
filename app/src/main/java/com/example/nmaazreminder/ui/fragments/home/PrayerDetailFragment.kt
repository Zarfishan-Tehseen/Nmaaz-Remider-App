package com.example.nmaazreminder.ui.fragments.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.example.nmaazreminder.R
import com.example.nmaazreminder.databinding.FragmentPrayerDetailBinding
import com.example.nmaazreminder.ui.fragments.home.PrayerItem
import com.example.nmaazreminder.ui.viewmodel.PrayerDetailViewModel
import com.example.nmaazreminder.utils.setBounceClickListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import androidx.fragment.app.activityViewModels

@AndroidEntryPoint
class PrayerDetailFragment : Fragment(R.layout.fragment_prayer_detail) {

    private var _binding: FragmentPrayerDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PrayerDetailViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPrayerDetailBinding.bind(view)

        val prayer = arguments?.getParcelable<PrayerItem>("selectedPrayer")

        if (prayer != null) {
            binding.tvDetailTitle.text = prayer.name
            binding.tvDetailTime.text = prayer.time
            setupDynamicTheme(prayer.name)

            Log.d("PRAYER_DEBUG", "Loading data for name: '${prayer.name}'")
            viewModel.loadSettings(prayer.name)
        }

        // Back button with premium bounce interaction
        binding.btnBack.setBounceClickListener {
            findNavController().navigateUp()
        }

        // STEP 1: Define your listener ONCE here, completely outside the collector loop
        binding.switchNotification.setOnCheckedChangeListener { _, isChecked ->
            TransitionManager.beginDelayedTransition(
                binding.layoutSubSettings.parent as ViewGroup,
                AutoTransition()
            )

            // Dynamic layout handling
            binding.layoutSubSettings.visibility = if (isChecked) View.VISIBLE else View.GONE
            binding.tvNotificationStatus.text = if (isChecked) "On — you will be alerted" else "Off — muted"

            // Push status change safely up to Room database
            viewModel.updateNotification(isChecked)
        }

        // Connect Item Pickers with bounce click effects and raw destination IDs
        binding.btnSelectSound.setBounceClickListener {
            navigateToSoundPicker()
        }
        binding.btnPreAlarm.setBounceClickListener {
            navigateToOffsetPicker()
        }

        // STEP 2: Collect database changes reactively and ONLY update values
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.settings.collect { setting ->
                    setting?.let {
                        // Temporarily clear listener ONLY to set the state without triggering loop updates
                        binding.switchNotification.setOnCheckedChangeListener(null)

                        binding.switchNotification.isChecked = it.isEnabled
                        binding.tvCurrentSound.text = it.soundName
                        binding.tvPreAlarmStatus.text = if (it.reminderOffset == 0) {
                            "At time"
                        } else {
                            "${it.reminderOffset} minutes before"
                        }

                        binding.layoutSubSettings.visibility = if (it.isEnabled) View.VISIBLE else View.GONE
                        binding.tvNotificationStatus.text = if (it.isEnabled) "On — you will be alerted" else "Off — muted"

                        // Safely re-apply your permanent switch listener logic
                        rebindSwitchListener()
                    }
                }
            }
        }
    }

    // STEP 3: Helper function to re-link your permanent action callback cleanly
    private fun rebindSwitchListener() {
        binding.switchNotification.setOnCheckedChangeListener { _, isChecked ->
            TransitionManager.beginDelayedTransition(
                binding.layoutSubSettings.parent as ViewGroup,
                AutoTransition()
            )
            binding.layoutSubSettings.visibility = if (isChecked) View.VISIBLE else View.GONE
            binding.tvNotificationStatus.text = if (isChecked) "On — you will be alerted" else "Off — muted"

            viewModel.updateNotification(isChecked)
        }
    }

    // Navigating directly using your destination fragment IDs from nav_graph.xml
    private fun navigateToSoundPicker() {
        findNavController().navigate(R.id.nav_adhan_sound)
    }

    private fun navigateToOffsetPicker() {
        findNavController().navigate(R.id.nav_pre_alarm)
    }

    private fun setupDynamicTheme(prayerName: String) {
        when (prayerName.lowercase().trim()) {
            "fajr" -> {
                binding.tvDetailArabicName.text = "الفجر"
                binding.tvDetailRakahInfo.text = "2 Sunnah · 2 Fard"
                binding.ivDetailIcon.setImageResource(R.drawable.ic_moon)
            }
            "dhuhr" -> {
                binding.tvDetailArabicName.text = "الظهر"
                binding.tvDetailRakahInfo.text = "4 Sunnah · 4 Fard · 2 Sunnah"
                binding.ivDetailIcon.setImageResource(R.drawable.ic_sun)
            }
            "asr" -> {
                binding.tvDetailArabicName.text = "العصر"
                binding.tvDetailRakahInfo.text = "4 Sunnah · 4 Fard"
                binding.ivDetailIcon.setImageResource(R.drawable.ic_sun)
            }
            "maghrib" -> {
                binding.tvDetailArabicName.text = "المغرب"
                binding.tvDetailRakahInfo.text = "3 Fard · 2 Sunnah · 2 Nafl"
                binding.ivDetailIcon.setImageResource(R.drawable.ic_cloud)
            }
            "isha" -> {
                binding.tvDetailArabicName.text = "العشاء"
                binding.tvDetailRakahInfo.text = "4 Fard · 2 Sunnah · 2 Nafl · 3 Witr"
                binding.ivDetailIcon.setImageResource(R.drawable.ic_moon)
            }
            "sunrise" -> {
                binding.tvDetailArabicName.text = "الشروق"
                binding.tvDetailRakahInfo.text = "Sunrise Time Frame"
                binding.ivDetailIcon.setImageResource(R.drawable.ic_sun)
            }
            else -> {
                binding.tvDetailArabicName.text = ""
                binding.tvDetailRakahInfo.text = ""
                binding.ivDetailIcon.setImageResource(R.drawable.ic_sun_cloud_accent)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}