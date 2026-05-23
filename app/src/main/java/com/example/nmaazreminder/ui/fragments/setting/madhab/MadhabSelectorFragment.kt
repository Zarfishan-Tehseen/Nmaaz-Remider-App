package com.example.nmaazreminder.ui.fragments.setting.madhab

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels // 🌟 Shared ViewModel delegate
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nmaazreminder.R
import com.example.nmaazreminder.databinding.FragmentMadhabSelectorBinding
import com.example.nmaazreminder.databinding.ItemMadhabRowBinding
import com.example.nmaazreminder.ui.viewmodel.MainViewModel
import com.example.nmaazreminder.utils.setBounceClickListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint // 🌟 Critical for Hilt injection support
class MadhabSelectorFragment : Fragment() {
    private var _binding: FragmentMadhabSelectorBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels() // 🌟 Shared state tracking

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMadhabSelectorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setBounceClickListener {
            findNavController().navigateUp()
        }

        val madhabList = listOf(
            MadhabOption(
                id = 1, // Hanafi = 1
                title = "Hanafi",
                description = "Asr: shadow length × 2 of object",
                regions = "Common in South & Central Asia"
            ),
            MadhabOption(
                id = 0, // Shafi'i / Standard = 0
                title = "Shafiʻi · Maliki · Hanbali",
                description = "Asr: shadow length × 1 of object",
                regions = "Common in Arab world, SE Asia"
            )
        )

        binding.rvMadhabMethods.layoutManager = LinearLayoutManager(requireContext())

        // 🌟 Asynchronously observe Room DB settings to map checkmark configurations live
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.globalSettings.collect { settings ->
                    if (settings != null) {
                        // Find position index in our list based on database asrMethod flag
                        val activeAdapterPosition = madhabList.indexOfFirst { it.id == settings.asrMethod }

                        // Fallback block protection
                        val correctedIndex = if (activeAdapterPosition != -1) activeAdapterPosition else 0

                        setupAdapter(madhabList, correctedIndex)
                    }
                }
            }
        }
    }

    private fun setupAdapter(madhabList: List<MadhabOption>, activePosition: Int) {
        val madhabAdapter = MadhabAdapter(madhabList, selectedPosition = activePosition) { selectedMadhab ->

            // 🌟 Save change properties direct into SQLite database
            val currentSettings = viewModel.globalSettings.value
            if (currentSettings != null) {
                val updatedSettings = currentSettings.copy(asrMethod = selectedMadhab.id)
                viewModel.saveGlobalSettings(updatedSettings)

                Toast.makeText(requireContext(), "Juristic method saved to ${selectedMadhab.title}", Toast.LENGTH_SHORT).show()

                // Immediately slide back to Settings screen with layout fully refreshed
                findNavController().navigateUp()
            }
        }
        binding.rvMadhabMethods.adapter = madhabAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
data class MadhabOption(
    val id: Int,
    val title: String,
    val description: String,
    val regions: String
)

class MadhabAdapter(
    private val items: List<MadhabOption>,
    private var selectedPosition: Int,
    private val onMadhabSelected: (MadhabOption) -> Unit
) : RecyclerView.Adapter<MadhabAdapter.MadhabViewHolder>() {

    inner class MadhabViewHolder(val binding: ItemMadhabRowBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MadhabViewHolder {
        val binding = ItemMadhabRowBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MadhabViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MadhabViewHolder, position: Int) {
        val currentItem = items[position]

        holder.binding.apply {
            tvMadhabTitle.text = currentItem.title
            tvMadhabDescription.text = currentItem.description
            tvMadhabRegions.text = currentItem.regions

            // ✨ Enforce custom radio check drawable states matching your visual mockup templates
            if (position == selectedPosition) {
                ivRadioIndicator.setImageResource(R.drawable.ic_radio_selected)
            } else {
                ivRadioIndicator.setImageResource(R.drawable.ic_radio_unselected)
            }

            dividerLine.visibility = if (position == items.size - 1) View.GONE else View.VISIBLE

            // ✨ Scale-down bounce click micro-interaction integration
            rootItemLayout.setBounceClickListener {
                if (selectedPosition != holder.adapterPosition) {
                    val oldPosition = selectedPosition
                    selectedPosition = holder.adapterPosition

                    notifyItemChanged(oldPosition)
                    notifyItemChanged(selectedPosition)

                    onMadhabSelected(currentItem)
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size
}