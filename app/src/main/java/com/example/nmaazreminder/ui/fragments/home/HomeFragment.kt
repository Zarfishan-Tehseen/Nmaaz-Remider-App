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
import kotlinx.coroutines.flow.collectLatest
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

    private var currentTimesData: PrayerTimes? = null

    // 🌟 KEEP SINGLE INSTANCES OF ADAPTERS TO AVOID UI THREAD BLOCKING / FREEZING
    private var listAdapter: PrayerAdapter? = null
    private var dialAdapter: PrayerAdapter? = null
    private var archAdapter: PrayerAdapter? = null

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

        // Wire up Segmented Switch Click Listeners
        binding.btnToggleList.setOnClickListener { updateHomeStyleInDatabase(0) }
        binding.btnToggleDial.setOnClickListener { updateHomeStyleInDatabase(1) }
        binding.btnToggleArch.setOnClickListener { updateHomeStyleInDatabase(2) }

        // Setup Location Click
        binding.layoutLocationSelector.setOnClickListener {
            findNavController().navigate(R.id.locationSelectorFragment)
        }

        // Setup Date Click sheet transitions safely via lookup
        val tvCalendarGregorian = binding.homeViewFlipper.findViewById<TextView>(R.id.tv_calendar_gregorian)
        val tvCalendarHijri = binding.homeViewFlipper.findViewById<TextView>(R.id.tv_calendar_hijri)

        val onDateTextClickListener = View.OnClickListener {
            val chooseDateSheet = ChooseDateBottomSheetFragment()
            chooseDateSheet.show(childFragmentManager, "ChooseDateBottomSheet")
        }
        tvCalendarGregorian?.setOnClickListener(onDateTextClickListener)
        tvCalendarHijri?.setOnClickListener(onDateTextClickListener)

        // List Screen date controls
        val btnPrev = view.findViewById<View>(R.id.btn_date_prev)
        val btnNext = view.findViewById<View>(R.id.btn_date_next)
        val btnToday = view.findViewById<View>(R.id.btn_date_today)

        btnPrev?.setOnClickListener { viewModel.shiftDateByDays(-1) }
        btnNext?.setOnClickListener { viewModel.shiftDateByDays(1) }
        btnToday?.setOnClickListener { viewModel.resetToToday() }

        // 🌟 1. PIPELINE PIPES PIPING: PRAYER DATA COLLECTION
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.prayerState.collect { dataPair ->
                    if (dataPair != null) {
                        val (cityName, times) = dataPair

                        binding.tvCurrentLocation.text = cityName
                        binding.homeViewFlipper.findViewById<TextView>(R.id.tv_calendar_gregorian)?.text = viewModel.currentDateString
                        binding.homeViewFlipper.findViewById<TextView>(R.id.tv_calendar_hijri)?.text = viewModel.currentHijriDateString

                        currentTimesData = times
                        updatePrayerUI(times)

                        // Render content on the current layout variant smoothly
                        renderVersionContent(binding.homeViewFlipper.displayedChild)
                    }
                }
            }
        }

        // 🌟 2. PIPELINE PIPES PIPING: GLOBAL SETTINGS DISPATCHER (Layout Synchronizer)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.globalSettings.collectLatest { settings ->
                    settings?.let {
                        val savedStyleIndex = it.selectedHomeStyle

                        // CRITICAL FILTER GUARD: Only flip if index actually changed! Avoids blocking loops!
                        if (binding.homeViewFlipper.displayedChild != savedStyleIndex) {
                            applyStyleUiHighlight(savedStyleIndex)
                            renderVersionContent(savedStyleIndex)
                        }
                    }
                }
            }
        }
    }

    private fun renderVersionContent(index: Int) {
        val times = currentTimesData ?: return
        val fullList = getFormattedPrayerList(times)
        val filteredList = fullList.filter { !it.name.equals("Sunrise", ignoreCase = true) }

        val currentView = binding.homeViewFlipper.getChildAt(index) ?: return

        when (index) {
            0 -> {
                val rvList = currentView.findViewById<RecyclerView>(R.id.rv_prayer_list)
                if (rvList != null) {
                    // 🌟 REFACTOR: If adapter is null, create it. Otherwise, reuse it and submit list!
                    if (listAdapter == null) {
                        listAdapter = PrayerAdapter(0) { item -> handlePrayerClick(item) }
                        rvList.layoutManager = LinearLayoutManager(requireContext())
                        rvList.adapter = listAdapter
                    }
                    listAdapter?.submitList(fullList)
                }
            }
            1 -> {
                currentView.findViewById<TextView>(R.id.tv_dial_day_name)?.text =
                    SimpleDateFormat("EEEE", Locale.getDefault()).format(Date()).uppercase(Locale.getDefault())
                currentView.findViewById<TextView>(R.id.tv_dial_gregorian_date)?.text = viewModel.currentDateString

                currentView.findViewById<View>(R.id.btn_dial_date_prev)?.setOnClickListener { viewModel.shiftDateByDays(-1) }
                currentView.findViewById<View>(R.id.btn_dial_date_next)?.setOnClickListener { viewModel.shiftDateByDays(1) }

                val rvDial = currentView.findViewById<RecyclerView>(R.id.rv_prayer_list)
                if (rvDial != null) {
                    if (dialAdapter == null) {
                        dialAdapter = PrayerAdapter(1) { item -> handlePrayerClick(item) }
                        rvDial.layoutManager = object : androidx.recyclerview.widget.GridLayoutManager(requireContext(), 5) {
                            override fun canScrollHorizontally(): Boolean = false
                        }
                        rvDial.adapter = dialAdapter
                    }
                    dialAdapter?.submitList(filteredList)
                }
            }
            2 -> {
                val rvArch = currentView.findViewById<RecyclerView>(R.id.rvArchPrayerList)
                    ?: currentView.findViewById<RecyclerView>(R.id.rv_prayer_list)

                if (rvArch != null) {
                    if (archAdapter == null) {
                        archAdapter = PrayerAdapter(1) { item -> handlePrayerClick(item) }
                        rvArch.layoutManager = object : androidx.recyclerview.widget.GridLayoutManager(requireContext(), 5) {
                            override fun canScrollHorizontally(): Boolean = false
                        }
                        rvArch.adapter = archAdapter
                    }
                    archAdapter?.submitList(filteredList)
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

        binding.homeViewFlipper.findViewById<TextView>(R.id.tv_next_prayer_title)?.text = englishName
        binding.homeViewFlipper.findViewById<TextView>(R.id.tv_next_prayer_arabic)?.text = arabicName
        binding.homeViewFlipper.findViewById<TextView>(R.id.tv_next_prayer_time)?.text = timeFormatter.format(nextPrayerTime)

        binding.homeViewFlipper.findViewById<TextView>(R.id.tvArchNextPrayerTitle)?.text = englishName.uppercase(Locale.getDefault())
        binding.homeViewFlipper.findViewById<TextView>(R.id.tvArchNextPrayerArabic)?.text = arabicName
        binding.homeViewFlipper.findViewById<TextView>(R.id.tvArchNextPrayerTime)?.text = timeFormatter.format(nextPrayerTime)

        binding.homeViewFlipper.findViewById<TextView>(R.id.tv_dial_target_time)?.text = timeFormatter.format(nextPrayerTime)
        binding.homeViewFlipper.getChildAt(1)?.findViewById<PrayerDialView>(R.id.prayer_custom_dial_view)?.setActivePrayer(englishName)

        val currentTimeMs = System.currentTimeMillis()
        val targetTimeMs = nextPrayerTime.time

        if (nextPrayer == Prayer.NONE) {
            countDownTimer?.cancel()
            val zeroStr = "00"

            binding.homeViewFlipper.findViewById<TextView>(R.id.tv_countdown_hours)?.text = zeroStr
            binding.homeViewFlipper.findViewById<TextView>(R.id.tv_countdown_minutes)?.text = zeroStr
            binding.homeViewFlipper.findViewById<TextView>(R.id.tv_countdown_seconds)?.text = zeroStr

            binding.homeViewFlipper.findViewById<TextView>(R.id.tvArchCountdownHours)?.text = zeroStr
            binding.homeViewFlipper.findViewById<TextView>(R.id.tvArchCountdownMinutes)?.text = zeroStr
            binding.homeViewFlipper.findViewById<TextView>(R.id.tvArchCountdownSeconds)?.text = zeroStr

            binding.homeViewFlipper.findViewById<TextView>(R.id.tv_dial_countdown_time)?.text = "00h 00m 00s"
        } else {
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

                binding.homeViewFlipper.findViewById<TextView>(R.id.tv_countdown_hours)?.text = hrStr
                binding.homeViewFlipper.findViewById<TextView>(R.id.tv_countdown_minutes)?.text = minStr
                binding.homeViewFlipper.findViewById<TextView>(R.id.tv_countdown_seconds)?.text = secStr

                binding.homeViewFlipper.findViewById<TextView>(R.id.tvArchCountdownHours)?.text = hrStr
                binding.homeViewFlipper.findViewById<TextView>(R.id.tvArchCountdownMinutes)?.text = minStr
                binding.homeViewFlipper.findViewById<TextView>(R.id.tvArchCountdownSeconds)?.text = secStr

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

    private fun updateHomeStyleInDatabase(styleIndex: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.globalSettings.value?.let { currentSettings ->
                // Avoid redundant DB writes if same tab clicked repeatedly
                if (currentSettings.selectedHomeStyle != styleIndex) {
                    val updated = currentSettings.copy(selectedHomeStyle = styleIndex)
                    viewModel.saveGlobalSettings(updated)
                }
            }
        }
    }

    private fun applyStyleUiHighlight(index: Int) {
        binding.homeViewFlipper.displayedChild = index

        val activeColor = android.graphics.Color.WHITE
        val inactiveColor = android.graphics.Color.parseColor("#6E726E")

        binding.btnToggleList.setTextColor(if (index == 0) activeColor else inactiveColor)
        binding.btnToggleList.setBackgroundResource(if (index == 0) R.drawable.bg_segmented_active else 0)

        binding.btnToggleDial.setTextColor(if (index == 1) activeColor else inactiveColor)
        binding.btnToggleDial.setBackgroundResource(if (index == 1) R.drawable.bg_segmented_active else 0)

        binding.btnToggleArch.setTextColor(if (index == 2) activeColor else inactiveColor)
        binding.btnToggleArch.setBackgroundResource(if (index == 2) R.drawable.bg_segmented_active else 0)
    }

    override fun onDestroyView() {
        countDownTimer?.cancel()
        countDownTimer = null
        currentTimesData = null
        listAdapter = null
        dialAdapter = null
        archAdapter = null
        _binding = null
        super.onDestroyView()
    }
}