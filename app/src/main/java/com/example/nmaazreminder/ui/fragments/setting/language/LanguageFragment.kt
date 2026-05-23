package com.example.nmaazreminder.ui.fragments.setting.language

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nmaazreminder.R
import com.example.nmaazreminder.databinding.FragmentLanguageBinding
import com.example.nmaazreminder.ui.fragments.setting.language.LanguageAdapter
import com.example.nmaazreminder.ui.fragments.setting.language.LanguageItem

class LanguageFragment : Fragment(R.layout.fragment_language) {

    private var _binding: FragmentLanguageBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLanguageBinding.bind(view)

        // Setup safe back navigation tracking
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // Language lists populated directly from your visual layout mock spec sheets
        val languageList = listOf(
            LanguageItem(1, "en", "English", "Fajr · Dhuhr · Asr...", "Fajr"),
            LanguageItem(2, "en_ar", "English + Arabic", "Fajr · الفجر", "الفجر · Fajr"),
            LanguageItem(3, "ar", "العربية", "Arabic interface", "الفجر"),
            LanguageItem(4, "ur", "اُردُو", "Urdu interface", "فجر", isLastInGroup = true)
        )

        // TODO: Load saved selectedPosition preference index dynamically from SharedPreferences here
        val initialSelectedPosition = 1

        val languageAdapter = LanguageAdapter(
            items = languageList,
            selectedPosition = initialSelectedPosition,
            onLanguageSelected = { selectedLanguage ->
                Toast.makeText(context, "Selected: ${selectedLanguage.title}", Toast.LENGTH_SHORT).show()
                // TODO: Store chosen locale values safely inside SharedPreferences or Local App Configuration managers
            }
        )

        binding.rvLanguages.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = languageAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}