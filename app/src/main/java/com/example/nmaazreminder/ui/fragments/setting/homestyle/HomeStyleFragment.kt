package com.example.nmaazreminder.ui.fragments.setting.homestyle

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nmaazreminder.R
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.nmaazreminder.databinding.FragmentHomeStyleBinding
import com.example.nmaazreminder.ui.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import com.example.nmaazreminder.ui.fragments.setting.homestyle.HomeStyleAdapter
import com.example.nmaazreminder.ui.fragments.setting.homestyle.HomeStyleItem
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeStyleFragment : Fragment(R.layout.fragment_home_style) {

    private var _binding: FragmentHomeStyleBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeStyleBinding.bind(view)

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // Populating mockup data variants directly matching screen specs
        val styleList = listOf(
            HomeStyleItem(0, "list", "List", "Hero countdown card with prayer list below", R.drawable.ic_home_style),
            HomeStyleItem(1, "day_dial", "Day-dial", "A 24h circular dial with prayers on the arc", R.drawable.ic_home_style), // Replace with your dial thumbnail icon later
            HomeStyleItem(2, "mihrab_arch", "Mihrab arch", "Ornamental full-bleed with Islamic arch framing", R.drawable.ic_home_style) // Replace with your arch thumbnail icon later
        )

        // 🌟 3. Observe the live database state to checkmark the currently selected style
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.globalSettings.collect { settings ->
                    settings?.let {
                        val currentSavedStyle = it.selectedHomeStyle

                        val homeStyleAdapter = HomeStyleAdapter(
                            items = styleList,
                            selectedPosition = currentSavedStyle,
                            onStyleSelected = { selectedStyle ->
                                val updatedSettings = it.copy(selectedHomeStyle = selectedStyle.id)
                                viewModel.saveGlobalSettings(updatedSettings)
                                Toast.makeText(
                                    context,
                                    "${selectedStyle.title} layout activated!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                        binding.rvHomeStyles.adapter = homeStyleAdapter
                    }
                }
            }
        }
        binding.rvHomeStyles.layoutManager = LinearLayoutManager(context)

        }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}