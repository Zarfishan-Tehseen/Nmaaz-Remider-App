package com.example.nmaazreminder.ui.fragments.home

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.example.nmaazreminder.R
import com.example.nmaazreminder.databinding.FragmentPrayerDetailBinding
import com.example.nmaazreminder.ui.viewmodel.PrayerDetailViewModel
import com.example.nmaazreminder.utils.setBounceClickListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PrayerDetailFragment : Fragment(R.layout.fragment_prayer_detail) {

    private var _binding: FragmentPrayerDetailBinding? = null
    private val binding get() = _binding!!

    // 🌟 RESTORED ORIGINAL SCOPE: Yeh line dono screens ki configuration ko handle karegi bina crash kiye!
    private val viewModel: PrayerDetailViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPrayerDetailBinding.bind(view)

        // 🎨 DUAL-MODE UI HANDLING
        val isBottomSheet = parentFragment is PrayerDetailBottomSheet
        if (!isBottomSheet) {
            // Agar full screen khuli hai settings se, toh background solid cream kar do
            view.setBackgroundColor(android.graphics.Color.parseColor("#F7F5F0"))
            val params = view.layoutParams
            params.height = ViewGroup.LayoutParams.MATCH_PARENT
            view.layoutParams = params
        }

        // 📦 Parse data safely from bundle paths
        val directArgs = arguments?.getParcelable<PrayerItem>("selectedPrayer")
        val parentArgs = parentFragment?.arguments?.getParcelable<PrayerItem>("selectedPrayer")
        val prayer = directArgs ?: parentArgs

        if (prayer != null) {
            binding.tvDetailTitle.text = prayer.name
            binding.tvDetailTime.text = prayer.time
            setupDynamicTheme(prayer.name)

            Log.d("PRAYER_DEBUG", "Loading data for name: '${prayer.name}'")
            viewModel.loadSettings(prayer.name)
        }

        // Back button action setup base profile rules
        binding.btnBack.setBounceClickListener {
            if (isBottomSheet) {
                (parentFragment as PrayerDetailBottomSheet).dismiss()
            } else {
                findNavController().navigateUp()
            }
        }

        // Link listener once securely
        binding.switchNotification.setOnCheckedChangeListener { _, isChecked ->
            TransitionManager.beginDelayedTransition(
                binding.layoutSubSettings.parent as ViewGroup,
                AutoTransition()
            )
            binding.layoutSubSettings.visibility = if (isChecked) View.VISIBLE else View.GONE
            binding.tvNotificationStatus.text = if (isChecked) "On — you will be alerted" else "Off — muted"

            viewModel.updateNotification(isChecked)
        }

        binding.btnSelectSound.setBounceClickListener { navigateToSoundPicker() }
        binding.btnPreAlarm.setBounceClickListener { navigateToOffsetPicker() }

        // 🔄 Reactive Collector Loop with Room State Fallbacks
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.settings.collect { setting ->
                    if (setting != null) {
                        Log.d("PRAYER_DEBUG", "Database match found: isEnabled=${setting.isEnabled}")
                        binding.switchNotification.setOnCheckedChangeListener(null)

                        binding.switchNotification.isChecked = setting.isEnabled
                        binding.tvCurrentSound.text = setting.soundName
                        binding.tvPreAlarmStatus.text = if (setting.reminderOffset == 0) {
                            "At time"
                        } else {
                            "${setting.reminderOffset} minutes before"
                        }

                        binding.layoutSubSettings.visibility = if (setting.isEnabled) View.VISIBLE else View.GONE
                        binding.tvNotificationStatus.text = if (setting.isEnabled) "On — you will be alerted" else "Off — muted"

                        rebindSwitchListener()
                    } else {
                        Log.w("PRAYER_DEBUG", "Database Row Empty — Showing layout defaults safely")
                    }
                }
            }
        }
    }

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

    private fun navigateToSoundPicker() {
        val currentPrayerName = viewModel.settings.value?.prayerName ?: "Fajr"

        val bundle = Bundle().apply {
            putString("prayerName", currentPrayerName)
        }
        findNavController().navigate(R.id.nav_adhan_sound, bundle)    }
    private fun navigateToOffsetPicker() { findNavController().navigate(R.id.nav_pre_alarm) }

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