package com.example.nmaazreminder.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.batoulapps.adhan.PrayerTimes
import com.example.nmaazreminder.databinding.ActivityMainBinding
import com.example.nmaazreminder.ui.viewmodel.MainViewModel
import com.example.nmaazreminder.ui.prayerdetail.PrayerDetailActivity // General detail activity
import com.example.nmaazreminder.ui.settings.SettingsActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import android.Manifest
import android.location.Geocoder
import com.google.android.gms.location.LocationServices
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.Toast
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var prayerAdapter: PrayerAdapter

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> getUserLocation()
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> getUserLocation()
            else -> Toast.makeText(this, "Location denied. Using default.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvDate.text = viewModel.currentDateString

        setupRecyclerView()
        observePrayerData()

        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        locationPermissionRequest.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    }

    private fun setupRecyclerView() {
        prayerAdapter = PrayerAdapter { prayerItem ->
            // This code runs when the card OR the bell is clicked
            val intent = Intent(this, PrayerDetailActivity::class.java).apply {
                putExtra("PRAYER_NAME", prayerItem.name)
                putExtra("PRAYER_TIME", prayerItem.time)
            }
            startActivity(intent)
        }
        binding.rvPrayers.adapter = prayerAdapter
    }

    private fun observePrayerData() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.prayerState.collect { times ->
                    if (times != null) {
                        updateUi(times)
                    }
                }
            }
        }
    }

    private fun updateUi(times: PrayerTimes) {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())

        binding.apply {
            val next = times.nextPrayer()
            val nextTime = times.timeForPrayer(next)

            // Update Header (Next Prayer)
            tvNextPrayerName.text = next.name
            tvNextPrayerTime.text = if (nextTime != null) sdf.format(nextTime) else "--:--"

            val prayerList = listOf(
                PrayerItem("Fajr", sdf.format(times.fajr)),
                PrayerItem("Dhuhr", sdf.format(times.dhuhr)),
                PrayerItem("Asar", sdf.format(times.asr)),
                PrayerItem("Maghrib", sdf.format(times.maghrib)),
                PrayerItem("Isha", sdf.format(times.isha))
            )

            prayerAdapter.submitList(prayerList)
        }
    }

    private fun getUserLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                location?.let {
                    val lat = it.latitude
                    val lon = it.longitude

                    val cityName = getCityName(lat, lon)

                    binding.tvLocation.text = cityName

                    // SAVE TO ROOM DATABASE
                    // This will now match the ViewModel function perfectly
                    viewModel.updateLocation(cityName, lat, lon)
                }
                } else {
                    // If GPS returns null, try to request a fresh update
                    // or just keep the database's current values.
                    Toast.makeText(this, "Unable to find location. Using last saved.", Toast.LENGTH_SHORT).show()
                }
            }
            } catch (e: SecurityException) { e.printStackTrace() }
    }

    private fun getCityName(lat: Double, lon: Double): String {
        val geocoder = Geocoder(this, Locale.getDefault())
        return try {
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            if (!addresses.isNullOrEmpty()) {
                val city = addresses[0].locality ?: "Unknown City"
                val country = addresses[0].countryName ?: ""
                "$city, $country"
            } else "Sargodha, Pakistan"
        } catch (e: Exception) { "Sargodha, Pakistan" }
    }
}