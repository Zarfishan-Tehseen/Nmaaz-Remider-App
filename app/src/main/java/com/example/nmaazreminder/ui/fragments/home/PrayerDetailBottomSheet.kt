package com.example.nmaazreminder.ui.fragments.home

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
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
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PrayerDetailBottomSheet : BottomSheetDialogFragment() {

    private var _binding: FragmentPrayerDetailBinding? = null
    private val binding get() = _binding!!

    // Link safely to your shared activity view model scope
    private val viewModel: PrayerDetailViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPrayerDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Parse incoming bundled structural configurations
        val prayer = arguments?.getParcelable<PrayerItem>("selectedPrayer")

        if (prayer != null) {
            binding.tvDetailTitle.text = prayer.name
            binding.tvDetailTime.text = prayer.time
            setupDynamicTheme(prayer.name)

            Log.d("PRAYER_DEBUG", "BottomSheet loading name: '${prayer.name}'")
            viewModel.loadSettings(prayer.name)
        }

        binding.btnBack.setBounceClickListener {
            dismiss()
        }

//        // 🌟 RESTORED: Click listeners for sound and pre-alarm offset pickers
//        binding.btnSelectSound.setBounceClickListener {
//            findNavController().navigate(R.id.nav_adhan_sound)
//        }
//        binding.btnPreAlarm.setBounceClickListener {
//            findNavController().navigate(R.id.nav_pre_alarm)
//        }

        // Configure Switch Notification Listener
        binding.switchNotification.setOnCheckedChangeListener { _, isChecked ->
            TransitionManager.beginDelayedTransition(
                binding.layoutSubSettings.parent as ViewGroup,
                AutoTransition()
            )
            binding.layoutSubSettings.visibility = if (isChecked) View.VISIBLE else View.GONE
            binding.tvNotificationStatus.text = if (isChecked) "On — you will be alerted" else "Off — muted"

            viewModel.updateNotification(isChecked)
        }

        // Safe pipeline reactive collector flow streams
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.settings.collect { setting ->
                    if (setting != null) {
                        Log.d("PRAYER_DEBUG", "BottomSheet Room Match: isEnabled=${setting.isEnabled}")
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
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        dialog.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout?

            bottomSheet?.let { container ->
                val behavior = BottomSheetBehavior.from(container)

                container.setBackgroundColor(android.graphics.Color.TRANSPARENT)

                // 🌟 THE FIX: Get total screen height in pixels
                val displayMetrics = resources.displayMetrics
                val screenHeight = displayMetrics.heightPixels

                // 🌟 Calculate a safe 70% height of the screen (Perfect half-ish view)
                val dynamicTargetHeight = (screenHeight * 0.70).toInt()

                // 🌟 Force the container frame to lock onto this exact 70% height
                val params = container.layoutParams
                params.height = dynamicTargetHeight
                container.layoutParams = params

                // 🌟 Lock behavior bounds so it doesn't collapse down to 0dp
                behavior.peekHeight = dynamicTargetHeight
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true

                // Trigger a UI recalculation pass
                container.requestLayout()
            }
        }
        return dialog
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}