package com.example.nmaazreminder.ui.fragments.home

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nmaazreminder.R
import com.example.nmaazreminder.databinding.FragmentLocationSelectorBinding
import com.example.nmaazreminder.ui.fragments.home.LocationAdapter
import com.example.nmaazreminder.ui.fragments.home.LocationItem

class LocationSelectorFragment : Fragment(R.layout.fragment_location_selector) {

    private var _binding: FragmentLocationSelectorBinding? = null
    private val binding get() = _binding!!
    private lateinit var locationAdapter: LocationAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLocationSelectorBinding.bind(view)

        // Close layout safely on arrow navigation click
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        setupRecyclerView()
        loadDefaultCities()
    }

    private fun setupRecyclerView() {
        locationAdapter = LocationAdapter { clickedLocation ->
            // Handle what happens when a user selects a city
            Toast.makeText(requireContext(), "Selected: ${clickedLocation.cityName}", Toast.LENGTH_SHORT).show()

            // TODO: Pass choice back to viewModel and navigate back home
            findNavController().navigateUp()
        }

        binding.rvLocationSuggestions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = locationAdapter
        }
    }

    private fun loadDefaultCities() {
        // Mock dataset matching your "ALL CITIES" row visual template
        val citiesList = listOf(
            LocationItem("Karachi", "Sindh, Pakistan"),
            LocationItem("Lahore", "Punjab, Pakistan"),
            LocationItem("Islamabad", "Capital Territory, Pakistan"),
            LocationItem("Faisalabad", "Punjab, Pakistan"),
            LocationItem("Rawalpindi", "Punjab, Pakistan"),
            LocationItem("Multan", "Punjab, Pakistan")
        )

        locationAdapter.submitList(citiesList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}