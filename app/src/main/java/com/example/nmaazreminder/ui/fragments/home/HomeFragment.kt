package com.example.nmaazreminder.ui.fragments.home

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.batoulapps.adhan.Prayer
import com.batoulapps.adhan.PrayerTimes
import com.example.nmaazreminder.R
import com.example.nmaazreminder.databinding.FragmentHomeBinding
import com.example.nmaazreminder.ui.fragments.home.PrayerAdapter
import com.example.nmaazreminder.ui.fragments.home.PrayerItem
import com.example.nmaazreminder.ui.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by viewModels()
    private var countDownTimer: CountDownTimer? = null
    private lateinit var prayerAdapter: PrayerAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        // 🔄 Navigate ONLY when clicking on the RecyclerView list rows
        prayerAdapter = PrayerAdapter { clickedPrayer ->

            if (clickedPrayer.name.equals("Sunrise", ignoreCase = true)) {
                // Do absolutely nothing — this makes it unclickable!
                return@PrayerAdapter
            }

            val bundle = Bundle().apply {
                putParcelable("selectedPrayer", clickedPrayer)
            }
            // Direct ID navigation using destination ID instead of action
            findNavController().navigate(R.id.prayerDetailFragment, bundle)
        }
        binding.layoutLocationSelector.setOnClickListener {
            findNavController().navigate(R.id.locationSelectorFragment)
        }

        binding.rvPrayerList.adapter = prayerAdapter
        binding.tvCalendarGregorian.text = viewModel.currentDateString

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.prayerState.collect { dataPair ->
                    if (dataPair != null) {
                        val (cityName, times) = dataPair
                        binding.tvCurrentLocation.text = cityName

                        updatePrayerUI(times)
                        submitPrayerList(times)
                    }
                }
            }
        }
    }

    private fun updatePrayerUI(times: PrayerTimes) {
        val nextPrayer = times.nextPrayer()
        val nextPrayerTime: Date = times.timeForPrayer(nextPrayer)
        val timeFormatter = SimpleDateFormat("h:mm a", Locale.getDefault())

        val (englishName, arabicName) = when (nextPrayer) {
            Prayer.FAJR -> "Fajr" to "الفجر"
            Prayer.SUNRISE -> "Sunrise" to "الشروق"
            Prayer.DHUHR -> "Dhuhr" to "الظهر"
            Prayer.ASR -> "Asr" to "العصر"
            Prayer.MAGHRIB -> "Maghrib" to "المغرب"
            Prayer.ISHA -> "Isha" to "العشاء"
            else -> "Fajr" to "الفجر"
        }

        binding.tvNextPrayerTitle.text = englishName
        binding.tvNextPrayerArabic.text = arabicName
        binding.tvNextPrayerTime.text = timeFormatter.format(nextPrayerTime)

        // 🛑 (Card click listener logic completely removed from here)

        val currentTimeMs = System.currentTimeMillis()
        var targetTimeMs = nextPrayerTime.time

        if (targetTimeMs < currentTimeMs) {
            targetTimeMs += 24 * 60 * 60 * 1000
        }

        startPrayerCountdown(targetTimeMs - currentTimeMs)
    }

    private fun submitPrayerList(times: PrayerTimes) {
        val timeFormatter = SimpleDateFormat("h:mm a", Locale.getDefault())

        val list = listOf(
            PrayerItem("Fajr", timeFormatter.format(times.timeForPrayer(Prayer.FAJR))),
            PrayerItem("Sunrise", timeFormatter.format(times.timeForPrayer(Prayer.SUNRISE))),
            PrayerItem("Dhuhr", timeFormatter.format(times.timeForPrayer(Prayer.DHUHR))),
            PrayerItem("Asr", timeFormatter.format(times.timeForPrayer(Prayer.ASR))),
            PrayerItem("Maghrib", timeFormatter.format(times.timeForPrayer(Prayer.MAGHRIB))),
            PrayerItem("Isha", timeFormatter.format(times.timeForPrayer(Prayer.ISHA)))
        )

        prayerAdapter.submitList(list)
    }

    private fun startPrayerCountdown(totalMs: Long) {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(totalMs, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val hours = (millisUntilFinished / (1000 * 60 * 60)) % 24
                val minutes = (millisUntilFinished / (1000 * 60)) % 60
                val seconds = (millisUntilFinished / 1000) % 60

                binding.tvCountdownHours.text = String.Companion.format(Locale.getDefault(), "%02d", hours)
                binding.tvCountdownMinutes.text = String.Companion.format(Locale.getDefault(), "%02d", minutes)
                binding.tvCountdownSeconds.text = String.Companion.format(Locale.getDefault(), "%02d", seconds)
            }

            override fun onFinish() {
                binding.tvCountdownHours.text = "00"
                binding.tvCountdownMinutes.text = "00"
                binding.tvCountdownSeconds.text = "00"
            }
        }.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer?.cancel()
        _binding = null
    }
}