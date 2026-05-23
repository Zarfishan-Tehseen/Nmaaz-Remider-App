package com.example.nmaazreminder.ui.fragments.setting.method

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels // 🌟 Shared ViewModel delegate
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nmaazreminder.R
import com.example.nmaazreminder.databinding.FragmentCalculationMethodBinding
import com.example.nmaazreminder.ui.viewmodel.MainViewModel
import com.example.nmaazreminder.utils.setBounceClickListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint // 🌟 Critical for Hilt injection support
class CalculationMethodFragment : Fragment(R.layout.fragment_calculation_method) {

    private var _binding: FragmentCalculationMethodBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels() // 🌟 Shared state tracking
    private lateinit var methodAdapter: MethodAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCalculationMethodBinding.bind(view)

        binding.btnBack.setBounceClickListener {
            findNavController().navigateUp()
        }

        val calculationMethods = listOf(
            MethodItem(1, "University of Islamic Sciences, Karachi", "Fajr: 18.0° | Isha: 18.0°"),
            MethodItem(2, "Islamic Society of North America (ISNA)", "Fajr: 15.0° | Isha: 15.0°"),
            MethodItem(3, "Muslim World League (MWL)", "Fajr: 18.0° | Isha: 17.0°"),
            MethodItem(4, "Umm al-Qura University, Makkah", "Fajr: 18.5° | Isha: 90 min after Maghrib"),
            MethodItem(5, "Egyptian General Authority of Survey", "Fajr: 19.5° | Isha: 17.5°"),
            MethodItem(6, "Tehran Institute of Geophysics", "Fajr: 17.7° | Isha: 14.0°"),
            MethodItem(7, "Gulf Region", "Fajr: 19.5° | Isha: 90 min after Maghrib")
        )

        // 🌟 Asynchronously observe Room DB settings to select the accurate radio bubble
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.globalSettings.collect { settings ->
                    if (settings != null) {
                        // DB id is 1-based, Adapter needs 0-based layout positions
                        val activeAdapterIndex = (settings.calculationMethod - 1).coerceAtLeast(0)

                        setupRecyclerView(calculationMethods, activeAdapterIndex)
                    }
                }
            }
        }
    }

    private fun setupRecyclerView(methodsList: List<MethodItem>, activeIndex: Int) {
        methodAdapter = MethodAdapter(
            items = methodsList,
            selectedPosition = activeIndex
        ) { selectedMethod ->

            // 🌟 Save the selection to Room DB by copying current state configuration row
            val currentSettings = viewModel.globalSettings.value
            if (currentSettings != null) {
                val updatedSettings = currentSettings.copy(calculationMethod = selectedMethod.id)
                viewModel.saveGlobalSettings(updatedSettings)

                Toast.makeText(requireContext(), "Calculation method updated!", Toast.LENGTH_SHORT).show()

                // Immediately slide back to Settings screen with layout fully refreshed
                findNavController().navigateUp()
            }
        }

        binding.rvCalculationMethods.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = methodAdapter
            setHasFixedSize(true)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}