package com.example.nmaazreminder.ui.fragments.home

import android.Manifest
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.batoulapps.adhan.Prayer
import com.batoulapps.adhan.PrayerTimes
import com.example.nmaazreminder.R
import com.example.nmaazreminder.databinding.FragmentHomeBinding
import com.example.nmaazreminder.ui.fragments.home.choosedate.ChooseDateBottomSheetFragment
import com.example.nmaazreminder.ui.viewmodel.MainViewModel
import com.example.nmaazreminder.ui.views.PrayerDialView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()
    private var countDownTimer: CountDownTimer? = null

    // Global cache for calculated prayer boundaries
    private var currentTimesData: PrayerTimes? = null

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted || coarseLocationGranted) {
            viewModel.fetchAndSaveCurrentLocation()
            Toast.makeText(requireContext(), "Fetching your location details...", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(
                requireContext(),
                "Location permission denied. Please select your location manually.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        // Smooth animations between layout flips
        binding.homeViewFlipper.setInAnimation(requireContext(), android.R.anim.slide_in_left)
        binding.homeViewFlipper.setOutAnimation(requireContext(), android.R.anim.slide_out_right)

        // Wire up your unique TextView Custom Segmented Switch Click Listeners
        binding.btnToggleList.setOnClickListener { switchHomeScreenVersion(0) }
        binding.btnToggleDial.setOnClickListener { switchHomeScreenVersion(1) }
        binding.btnToggleArch.setOnClickListener { switchHomeScreenVersion(2) }

        // Setup Location Click
        binding.layoutLocationSelector.setOnClickListener {
            findNavController().navigate(R.id.locationSelectorFragment)
        }

        // Setup Date Click sheet transitions inside layoutList sub-view safely via lookup
        val tvCalendarGregorian = binding.homeViewFlipper.findViewById<TextView>(R.id.tv_calendar_gregorian)
        val tvCalendarHijri = binding.homeViewFlipper.findViewById<TextView>(R.id.tv_calendar_hijri)

        val onDateTextClickListener = View.OnClickListener {
            val chooseDateSheet = ChooseDateBottomSheetFragment()
            chooseDateSheet.show(childFragmentManager, "ChooseDateBottomSheet")
        }
        tvCalendarGregorian?.setOnClickListener(onDateTextClickListener)
        tvCalendarHijri?.setOnClickListener(onDateTextClickListener)

        // List Screen date controls (handled from main layout overlay safely)
        val btnPrev = view.findViewById<View>(R.id.btn_date_prev)
        val btnNext = view.findViewById<View>(R.id.btn_date_next)
        val btnToday = view.findViewById<View>(R.id.btn_date_today)

        btnPrev?.setOnClickListener { viewModel.shiftDateByDays(-1) }
        btnNext?.setOnClickListener { viewModel.shiftDateByDays(1) }
        btnToday?.setOnClickListener { viewModel.resetToToday() }

        // State collection pipeline
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.prayerState.collect { dataPair ->
                    if (dataPair != null) {
                        val (cityName, times) = dataPair

                        // Update Static Header elements
                        binding.tvCurrentLocation.text = cityName

                        // Safely look up list date fields and apply texts
                        binding.homeViewFlipper.findViewById<TextView>(R.id.tv_calendar_gregorian)?.text = viewModel.currentDateString
                        binding.homeViewFlipper.findViewById<TextView>(R.id.tv_calendar_hijri)?.text = viewModel.currentHijriDateString

                        // Cache instance
                        currentTimesData = times

                        // Re-trigger core clock computations
                        updatePrayerUI(times)

                        // Synchronize whichever flipper sub-layout is active right now
                        renderVersionContent(binding.homeViewFlipper.displayedChild)
                    }
                }
            }
        }
    }

    private fun switchHomeScreenVersion(index: Int) {
        binding.homeViewFlipper.displayedChild = index

        // Update selection UI highlight colors dynamically
        val activeColor = android.graphics.Color.WHITE
        val inactiveColor = android.graphics.Color.parseColor("#6E726E")

        binding.btnToggleList.setTextColor(if (index == 0) activeColor else inactiveColor)
        binding.btnToggleList.setBackgroundResource(if (index == 0) R.drawable.bg_segmented_active else 0)

        binding.btnToggleDial.setTextColor(if (index == 1) activeColor else inactiveColor)
        binding.btnToggleDial.setBackgroundResource(if (index == 1) R.drawable.bg_segmented_active else 0)

        binding.btnToggleArch.setTextColor(if (index == 2) activeColor else inactiveColor)
        binding.btnToggleArch.setBackgroundResource(if (index == 2) R.drawable.bg_segmented_active else 0)

        // Force layout refresh loop
        renderVersionContent(index)
    }

    private fun renderVersionContent(index: Int) {
        val times = currentTimesData ?: return
        val fullList = getFormattedPrayerList(times)
        val filteredList = fullList.filter { !it.name.equals("Sunrise", ignoreCase = true) }

        // Clear and match view targets using current index specific layouts
        val currentView = binding.homeViewFlipper.getChildAt(index) ?: return

        when (index) {
            0 -> {
                val rvList = currentView.findViewById<RecyclerView>(R.id.rv_prayer_list)
                if (rvList != null) {
                    val adapter = PrayerAdapter(0) { item -> handlePrayerClick(item) }
                    rvList.layoutManager = LinearLayoutManager(requireContext())
                    rvList.adapter = adapter
                    adapter.submitList(fullList)
                }
            }
            1 -> {
                currentView.findViewById<TextView>(R.id.tv_dial_day_name)?.text =
                    SimpleDateFormat("EEEE", Locale.getDefault()).format(Date()).uppercase(Locale.getDefault())
                currentView.findViewById<TextView>(R.id.tv_dial_gregorian_date)?.text = viewModel.currentDateString

                currentView.findViewById<View>(R.id.btn_dial_date_prev)?.setOnClickListener { viewModel.shiftDateByDays(-1) }
                currentView.findViewById<View>(R.id.btn_dial_date_next)?.setOnClickListener { viewModel.shiftDateByDays(1) }

                // 🎯 Targeted directly from current active Flipper view context
                val rvDial = currentView.findViewById<RecyclerView>(R.id.rv_prayer_list)
                if (rvDial != null) {
                    val adapter = PrayerAdapter(1) { item -> handlePrayerClick(item) }
                    rvDial.layoutManager = object : androidx.recyclerview.widget.GridLayoutManager(requireContext(), 5) {
                        override fun canScrollHorizontally(): Boolean = false
                    }
                    rvDial.adapter = adapter
                    adapter.submitList(filteredList)
                }
            }
            2 -> {
                val rvArch = currentView.findViewById<RecyclerView>(R.id.rvArchPrayerList)
                    ?: currentView.findViewById<RecyclerView>(R.id.rv_prayer_list)

                if (rvArch != null) {
                    val adapter = PrayerAdapter(1) { item -> handlePrayerClick(item) }
                    rvArch.layoutManager = object : androidx.recyclerview.widget.GridLayoutManager(requireContext(), 5) {
                        override fun canScrollHorizontally(): Boolean = false
                    }
                    rvArch.adapter = adapter
                    adapter.submitList(filteredList)
                }
            }
        }
    }

    private fun handlePrayerClick(clickedPrayer: PrayerItem) {
        if (clickedPrayer.name.equals("Sunrise", ignoreCase = true)) return

        val bundle = Bundle().apply { putParcelable("selectedPrayer", clickedPrayer) }
        val bottomSheet = PrayerDetailBottomSheet().apply { arguments = bundle }
        bottomSheet.show(parentFragmentManager, "PrayerDetailBottomSheet")
    }

    private fun updatePrayerUI(times: PrayerTimes) {
        val nextPrayer = times.nextPrayer()

        // 🎯 If nextPrayer is NONE (user shifted date), default to displaying Fajr for that selected day
        val resolvedPrayer = if (nextPrayer == Prayer.NONE) Prayer.FAJR else nextPrayer
        val nextPrayerTime: Date? = times.timeForPrayer(resolvedPrayer)

        if (nextPrayerTime == null) {
            Log.e("HomeFragment", "Prayer time calculation returned null for: $resolvedPrayer")
            return
        }

        val timeFormatter = SimpleDateFormat("h:mm a", Locale.getDefault())

        val (englishName, arabicName) = when (resolvedPrayer) {
            Prayer.FAJR -> "Fajr" to "الفجر"
            Prayer.SUNRISE -> "Sunrise" to "الشروق"
            Prayer.DHUHR -> "Dhuhr" to "الظهر"
            Prayer.ASR -> "Asr" to "العصر"
            Prayer.MAGHRIB -> "Maghrib" to "المغرب"
            Prayer.ISHA -> "Isha" to "العشاء"
            else -> "Fajr" to "الفجر"
        }

        // 1. Update standard List Screen Header elements with selected day's target prayer
        binding.homeViewFlipper.findViewById<TextView>(R.id.tv_next_prayer_title)?.text = englishName
        binding.homeViewFlipper.findViewById<TextView>(R.id.tv_next_prayer_arabic)?.text = arabicName
        binding.homeViewFlipper.findViewById<TextView>(R.id.tv_next_prayer_time)?.text = timeFormatter.format(nextPrayerTime)

        // 2. Update Arch View layout headers if visible
        binding.homeViewFlipper.findViewById<TextView>(R.id.tvArchNextPrayerTitle)?.text = englishName.uppercase(Locale.getDefault())
        binding.homeViewFlipper.findViewById<TextView>(R.id.tvArchNextPrayerArabic)?.text = arabicName
        binding.homeViewFlipper.findViewById<TextView>(R.id.tvArchNextPrayerTime)?.text = timeFormatter.format(nextPrayerTime)

        // 3. Update Dial Hero card layout explicitly
        binding.homeViewFlipper.findViewById<TextView>(R.id.tv_dial_target_time)?.text = timeFormatter.format(nextPrayerTime)

        binding.homeViewFlipper.getChildAt(1)?.findViewById<PrayerDialView>(R.id.prayer_custom_dial_view)?.setActivePrayer(englishName)

        // ⏳ Smart Countdown Handler for selected dates
        val currentTimeMs = System.currentTimeMillis()
        val targetTimeMs = nextPrayerTime.time

        if (nextPrayer == Prayer.NONE) {
            // User is viewing another day (or all prayers for today ended). Stop ticking and show a clean 00:00:00 or static boundary.
            countDownTimer?.cancel()
            val zeroStr = "00"

            // Clear List layout countdowns
            binding.homeViewFlipper.findViewById<TextView>(R.id.tv_countdown_hours)?.text = zeroStr
            binding.homeViewFlipper.findViewById<TextView>(R.id.tv_countdown_minutes)?.text = zeroStr
            binding.homeViewFlipper.findViewById<TextView>(R.id.tv_countdown_seconds)?.text = zeroStr

            // Clear Arch layout countdowns
            binding.homeViewFlipper.findViewById<TextView>(R.id.tvArchCountdownHours)?.text = zeroStr
            binding.homeViewFlipper.findViewById<TextView>(R.id.tvArchCountdownMinutes)?.text = zeroStr
            binding.homeViewFlipper.findViewById<TextView>(R.id.tvArchCountdownSeconds)?.text = zeroStr

            // Clear Dial tracker text
            binding.homeViewFlipper.findViewById<TextView>(R.id.tv_dial_countdown_time)?.text = "00h 00m 00s"
        } else {
            // It's the active current day! Fire up the real-time live clock computation
            var remainingMs = targetTimeMs - currentTimeMs
            if (remainingMs < 0) {
                remainingMs += 24 * 60 * 60 * 1000
            }
            startPrayerCountdown(remainingMs)
        }
    }

    private fun getFormattedPrayerList(times: PrayerTimes): List<PrayerItem> {
        val timeFormatter = SimpleDateFormat("h:mm a", Locale.getDefault())
        return listOf(
            PrayerItem("Fajr", timeFormatter.format(times.timeForPrayer(Prayer.FAJR))),
            PrayerItem("Sunrise", timeFormatter.format(times.timeForPrayer(Prayer.SUNRISE))),
            PrayerItem("Dhuhr", timeFormatter.format(times.timeForPrayer(Prayer.DHUHR))),
            PrayerItem("Asr", timeFormatter.format(times.timeForPrayer(Prayer.ASR))),
            PrayerItem("Maghrib", timeFormatter.format(times.timeForPrayer(Prayer.MAGHRIB))),
            PrayerItem("Isha", timeFormatter.format(times.timeForPrayer(Prayer.ISHA)))
        )
    }

    private fun startPrayerCountdown(totalMs: Long) {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(totalMs, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val hours = (millisUntilFinished / (1000 * 60 * 60)) % 24
                val minutes = (millisUntilFinished / (1000 * 60)) % 60
                val seconds = (millisUntilFinished / 1000) % 60

                val hrStr = String.format(Locale.getDefault(), "%02d", hours)
                val minStr = String.format(Locale.getDefault(), "%02d", minutes)
                val secStr = String.format(Locale.getDefault(), "%02d", seconds)

                // 1. Safe refresh for standard List countdown text elements via lookup
                binding.homeViewFlipper.findViewById<TextView>(R.id.tv_countdown_hours)?.text = hrStr
                binding.homeViewFlipper.findViewById<TextView>(R.id.tv_countdown_minutes)?.text = minStr
                binding.homeViewFlipper.findViewById<TextView>(R.id.tv_countdown_seconds)?.text = secStr

                // 2. Refresh Arch Screen text countdown clock references
                binding.homeViewFlipper.findViewById<TextView>(R.id.tvArchCountdownHours)?.text = hrStr
                binding.homeViewFlipper.findViewById<TextView>(R.id.tvArchCountdownMinutes)?.text = minStr
                binding.homeViewFlipper.findViewById<TextView>(R.id.tvArchCountdownSeconds)?.text = secStr

                // 3. Refresh Dial layout flat text tracker element
                binding.homeViewFlipper.findViewById<TextView>(R.id.tv_dial_countdown_time)?.text = "${hrStr}h ${minStr}m ${secStr}s"
            }

            override fun onFinish() {
                val zeroStr = "00"
                binding.homeViewFlipper.findViewById<TextView>(R.id.tv_countdown_hours)?.text = zeroStr
                binding.homeViewFlipper.findViewById<TextView>(R.id.tv_countdown_minutes)?.text = zeroStr
                binding.homeViewFlipper.findViewById<TextView>(R.id.tv_countdown_seconds)?.text = zeroStr
            }
        }.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer?.cancel()
        _binding = null
    }
}