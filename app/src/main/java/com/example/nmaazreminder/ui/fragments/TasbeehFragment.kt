package com.example.nmaazreminder.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.nmaazreminder.R
import com.example.nmaazreminder.databinding.FragmentTasbeehBinding

data class DhikrPhrase(
    val arabic: String,
    val transliteration: String,
    val translation: String
)

class TasbeehFragment : Fragment(R.layout.fragment_tasbeeh) {

    private var _binding: FragmentTasbeehBinding? = null
    private val binding get() = _binding!!

    // State Variables
    private var currentCount = 0
    private var maxLimit = 33
    private var currentPhraseIndex = 0
    private var absoluteTotalCount = 0
    private var completedCycles = 0

    private val phrasesList = listOf(
        DhikrPhrase("سُبْحَانَ اللهِ", "Subhān Allāh", "Glory be to Allah"),
        DhikrPhrase("الْحَمْدُ لِلَّهِ", "Alhamdu lillāh", "Praise be to Allah"),
        DhikrPhrase("اللهُ أَكْبَرُ", "Allāhu Akbar", "Allah is the Greatest"),
        DhikrPhrase("لَا إِلٰهَ إِلَّا اللهُ", "Lā ilāha illallāh", "There is no god but Allah"),
        DhikrPhrase("أَسْتَغْفِرُ اللهَ", "Astaghfirullāh", "I seek Allah's forgiveness")
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTasbeehBinding.bind(view)

        updatePhraseDisplay()
        updateStatsDisplay()

        // Main Tap Counter Area Click Action
        binding.layoutCounterClicker.setOnClickListener {
            incrementCounter()
        }

        // Phrase Card Click Listener to Loop Phrases
        binding.cardPhraseSelector.setOnClickListener {
            currentPhraseIndex = (currentPhraseIndex + 1) % phrasesList.size
            updatePhraseDisplay()
            resetCurrentSessionOnly()
        }

        // Reset Button Click
        binding.btnReset.setOnClickListener {
            resetCurrentSessionOnly()
        }

        // Target Limit Button Listeners
        binding.btnTarget33.setOnClickListener { changeTargetLimit(33, binding.btnTarget33) }
        binding.btnTarget99.setOnClickListener { changeTargetLimit(99, binding.btnTarget99) }
        binding.btnTarget100.setOnClickListener { changeTargetLimit(100, binding.btnTarget100) }
        binding.btnTarget500.setOnClickListener { changeTargetLimit(500, binding.btnTarget500) }
    }

    private fun incrementCounter() {
        if (currentCount < maxLimit) {
            currentCount++
            absoluteTotalCount++

            // Update digit strings
            binding.tvCounterDigits.text = currentCount.toString()

            // Calculate the percentage mapping for the indicator
            val progressPercent = ((currentCount.toFloat() / maxLimit.toFloat()) * 100).toInt()
            binding.circularProgress.setProgress(progressPercent, true)

            if (currentCount == maxLimit) {
                completedCycles++
                currentCount = 0

                // Brief handler delay makes the "complete" transition smooth
                binding.circularProgress.postDelayed({
                    binding.tvCounterDigits.text = "0"
                    binding.circularProgress.setProgress(0, false)
                }, 150)
            }
            updateStatsDisplay()
        }
    }

    private fun changeTargetLimit(newLimit: Int, clickedView: TextView) {
        maxLimit = newLimit
        binding.tvCurrentLimit.text = "OF $maxLimit"
        currentCount = 0
        binding.tvCounterDigits.text = "0"
        binding.circularProgress.setProgress(0, false)

        // Revert all selector buttons to unselected state drawables
        val unselectedBg = ContextCompat.getDrawable(requireContext(), R.drawable.bg_target_unselected)
        val darkTextColor = ContextCompat.getColor(requireContext(), R.color.bottom_nav_icon_color) // or #2E312F

        val buttonsList = listOf(binding.btnTarget33, binding.btnTarget99, binding.btnTarget100, binding.btnTarget500)
        buttonsList.forEach { btn ->
            btn.background = unselectedBg
            btn.setTextColor(darkTextColor)
        }

        // Apply active design states to chosen button anchor context
        clickedView.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_target_selected)
        clickedView.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
    }

    private fun updatePhraseDisplay() {
        val currentDhikr = phrasesList[currentPhraseIndex]
        binding.tvArabicPhrase.text = currentDhikr.arabic
        binding.tvTransliteration.text = currentDhikr.transliteration
        binding.tvEnglishTranslation.text = currentDhikr.translation
        binding.tvPhraseIndex.text = "TAP TO CHANGE (${currentPhraseIndex + 1}/${phrasesList.size})"
    }

    private fun updateStatsDisplay() {
        binding.tvTodayStats.text = "Today: $completedCycles cycles · $absoluteTotalCount total"
    }

    private fun resetCurrentSessionOnly() {
        currentCount = 0
        binding.tvCounterDigits.text = "0"
        binding.circularProgress.setProgress(0, true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}