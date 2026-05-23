package com.example.nmaazreminder.ui.fragments.setting.homestyle

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nmaazreminder.R
import com.example.nmaazreminder.databinding.FragmentHomeStyleBinding
import com.example.nmaazreminder.ui.fragments.setting.homestyle.HomeStyleAdapter
import com.example.nmaazreminder.ui.fragments.setting.homestyle.HomeStyleItem

class HomeStyleFragment : Fragment(R.layout.fragment_home_style) {

    private var _binding: FragmentHomeStyleBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeStyleBinding.bind(view)

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // Populating mockup data variants directly matching screen specs
        val styleList = listOf(
            HomeStyleItem(1, "list", "List", "Hero countdown card with prayer list below", R.drawable.ic_home_style),
            HomeStyleItem(2, "day_dial", "Day-dial", "A 24h circular dial with prayers on the arc", R.drawable.ic_home_style), // Replace with your dial thumbnail icon later
            HomeStyleItem(3, "mihrab_arch", "Mihrab arch", "Ornamental full-bleed with Islamic arch framing", R.drawable.ic_home_style) // Replace with your arch thumbnail icon later
        )

        // TODO: Pull default selected style index state configuration key layer dynamically via SharedPreferences
        val initialSelectedPosition = 0

        val homeStyleAdapter = HomeStyleAdapter(
            items = styleList,
            selectedPosition = initialSelectedPosition,
            onStyleSelected = { selectedStyle ->
                Toast.makeText(context, "Layout Set: ${selectedStyle.title}", Toast.LENGTH_SHORT)
                    .show()
                // TODO: Save choice to SharedPreferences to reload the home fragment UI dynamically
            }
        )

        binding.rvHomeStyles.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = homeStyleAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}