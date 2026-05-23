package com.example.nmaazreminder.ui.fragments.setting.location

import android.Manifest
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels // 🌟 Shared ViewModel delegate
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nmaazreminder.R
import com.example.nmaazreminder.databinding.FragmentLocationSelectorBinding
import com.example.nmaazreminder.ui.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LocationSelectorFragment : Fragment(R.layout.fragment_location_selector) {

    private var _binding: FragmentLocationSelectorBinding? = null
    private val binding get() = _binding!!

    // 🌟 Accessing your shared MainViewModel
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var locationAdapter: LocationAdapter

    // 🌟 Location Permission Launcher for "Use my current location"
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted || coarseLocationGranted) {
            Toast.makeText(requireContext(), "Fetching current GPS location...", Toast.LENGTH_SHORT).show()
            viewModel.fetchAndSaveCurrentLocation()
            findNavController().navigateUp() // Navigate back to Dashboard home
        } else {
            Toast.makeText(
                requireContext(),
                "Permission denied. Please select a city manually from the list.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLocationSelectorBinding.bind(view)

        // Close layout safely on arrow navigation click
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // 🌟 Hook up "Use my current location" card layout click listener
        binding.cardBtnGps.setOnClickListener {
            checkAndRequestPermissions()
        }

        setupRecyclerView()
        loadDefaultCities()
    }

    private fun setupRecyclerView() {
        locationAdapter = LocationAdapter { clickedLocation ->
            // 🌟 1. Save chosen coordinates directly into Room Database via ViewModel
            viewModel.updateLocation(
                name = clickedLocation.cityName,
                lat = clickedLocation.latitude,
                lon = clickedLocation.longitude
            )

            Toast.makeText(requireContext(), "Location updated to ${clickedLocation.cityName}", Toast.LENGTH_SHORT).show()

            // 🌟 2. Pop back to Home Dashboard instantly
            findNavController().navigateUp()
        }

        binding.rvLocationSuggestions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = locationAdapter
        }
    }

    private fun loadDefaultCities() {
        // Precise geographic values for major cities in Pakistan
        val citiesList = listOf(
            LocationItem("Karachi", "Sindh, Pakistan", 24.8607, 67.0011),
            LocationItem("Lahore", "Punjab, Pakistan", 31.5204, 74.3587),
            LocationItem("Islamabad", "Capital Territory, Pakistan", 33.6844, 73.0479),
            LocationItem("Faisalabad", "Punjab, Pakistan", 31.4504, 73.1350),
            LocationItem("Rawalpindi", "Punjab, Pakistan", 33.5651, 73.0169),
            LocationItem("Multan", "Punjab, Pakistan", 30.1575, 71.5249),
            LocationItem("Sargodha", "Punjab, Pakistan", 32.0740, 72.6860)
        )

        locationAdapter.submitList(citiesList)
    }

    private fun checkAndRequestPermissions() {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}