package com.example.nmaazreminder.ui.fragments.tasbeeh

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.nmaazreminder.R
import com.example.nmaazreminder.databinding.FragmentTasbeehBinding

data class DhikrPhrase(
    val arabic: String,
    val transliteration: String,
    val translation: String
)

fun View.animateBounceClick(onAnimationEnd: () -> Unit) {
    this.animate()
        .scaleX(0.94f)
        .scaleY(0.94f)
        .setDuration(60)
        .withEndAction {
            this.animate()
                .scaleX(1.0f)
                .scaleY(1.0f)
                .setDuration(120)
                .withEndAction {
                    onAnimationEnd()
                }
                .start()
        }
        .start()
}

class TasbeehFragment : Fragment(R.layout.fragment_tasbeeh) {

    private var _binding: FragmentTasbeehBinding? = null
    private val binding get() = _binding!!

    // State Variables
    private var currentCount = 0
    private var maxLimit = 33
    private var currentPhraseIndex = 0
    private var absoluteTotalCount = 0
    private var completedCycles = 0

    // 🌟 SharedPreferences Instance
    private lateinit var sharedPreferences: SharedPreferences

    private val phrasesList = listOf(
        DhikrPhrase("سُبْحَانَ اللَّهِ", "Subhān Allāh", "Glory be to Allah"),
        DhikrPhrase("الْحَمْدُ لِلَّهِ", "Alhamdu lillāh", "Praise be to Allah"),
        DhikrPhrase("اللهُ أَكْبَرُ", "Allāhu Akbar", "Allah is the Greatest"),
        DhikrPhrase("لَا إِلٰهَ إِلَّا اللهُ", "Lā ilāha illallāh", "There is no god but Allah"),
        DhikrPhrase("أَسْتَغْفِرُ اللهَ", "Astaghfirullāh", "I seek Allah's forgiveness")
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTasbeehBinding.bind(view)

        // Initialize SharedPreferences storage file
        sharedPreferences = requireContext().getSharedPreferences("tasbeeh_prefs", Context.MODE_PRIVATE)

        // 🌟 RESTORE STATE: Load the last active phrase index and count upon launching the screen
        currentPhraseIndex = sharedPreferences.getInt("last_phrase_index", 0)
        loadSavedCountForCurrentPhrase()

        updatePhraseDisplay()
        updateStatsDisplay()

        // Main Tap Counter Area Click Action
        binding.layoutCounterClicker.setOnClickListener {
            binding.layoutCounterClicker.animateBounceClick {
                incrementCounter()
            }
        }

        // Phrase Card Click Listener to Loop Phrases
        binding.cardPhraseSelector.setOnClickListener {
            binding.cardPhraseSelector.animateBounceClick {
                // 1. SAVE current count for the active phrase BEFORE switching away
                saveCountForCurrentPhrase(currentCount)

                // 2. Advance to the next phrase index layout link
                currentPhraseIndex = (currentPhraseIndex + 1) % phrasesList.size

                // 3. Save the active index tracker globally so the app remembers where you left off next time it opens
                sharedPreferences.edit().putInt("last_phrase_index", currentPhraseIndex).apply()

                // 4. LOAD the previously saved count state for the newly selected phrase
                loadSavedCountForCurrentPhrase()

                updatePhraseDisplay()
            }
        }

        // Reset Button Click
        binding.btnReset.setOnClickListener {
            binding.btnReset.animateBounceClick {
                resetCurrentSessionOnly()
            }
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // Target Limit Button Listeners
        binding.btnTarget33.setOnClickListener { binding.btnTarget33.animateBounceClick { changeTargetLimit(33, binding.btnTarget33) } }
        binding.btnTarget99.setOnClickListener { binding.btnTarget99.animateBounceClick { changeTargetLimit(99, binding.btnTarget99) } }
        binding.btnTarget100.setOnClickListener { binding.btnTarget100.animateBounceClick { changeTargetLimit(100, binding.btnTarget100) } }
        binding.btnTarget500.setOnClickListener { binding.btnTarget500.animateBounceClick { changeTargetLimit(500, binding.btnTarget500) } }
    }

    private fun incrementCounter() {
        if (currentCount < maxLimit) {
            currentCount++
            absoluteTotalCount++

            binding.tvCounterDigits.text = currentCount.toString()
            binding.beadProgressIndicator.updateProgress(currentCount, maxLimit)

            // 🌟 REALTIME SAVE: Update the count in storage dynamically on every single click
            saveCountForCurrentPhrase(currentCount)

            if (currentCount == maxLimit) {
                completedCycles++
                currentCount = 0

                // Tasbeeh completed! Clear the saved progress record for this phrase
                saveCountForCurrentPhrase(0)

                binding.beadProgressIndicator.postDelayed({
                    binding.tvCounterDigits.text = "0"
                    binding.beadProgressIndicator.updateProgress(0, maxLimit)
                }, 200)
            }
            updateStatsDisplay()
        }
    }

    /**
     * Helper to write current count to SharedPreferences using the unique transliteration text label as the key
     */
    private fun saveCountForCurrentPhrase(count: Int) {
        val currentPhraseKey = phrasesList[currentPhraseIndex].transliteration
        sharedPreferences.edit().putInt(currentPhraseKey, count).apply()
    }
    private fun loadSavedCountForCurrentPhrase() {
        val currentPhraseKey = phrasesList[currentPhraseIndex].transliteration
        // Default to 0 if the user has never clicked or already finished this phrase before
        currentCount = sharedPreferences.getInt(currentPhraseKey, 0)

        // Push the values directly to the UI layouts elements
        binding.tvCounterDigits.text = currentCount.toString()
        binding.beadProgressIndicator.updateProgress(currentCount, maxLimit)
    }

    private fun changeTargetLimit(newLimit: Int, clickedView: TextView) {
        maxLimit = newLimit

        binding.tvCurrentLimit.text = "OF $maxLimit"

        // When changing target limits manually, check if saved session fits the new bounds
        if (currentCount >= maxLimit) {
            currentCount = 0
            saveCountForCurrentPhrase(0)
        }

        binding.tvCounterDigits.text = currentCount.toString()
        binding.beadProgressIndicator.updateProgress(currentCount, maxLimit)

        val unselectedBg = ContextCompat.getDrawable(requireContext(), R.drawable.bg_target_unselected)
        val darkTextColor = ContextCompat.getColor(requireContext(), R.color.bottom_nav_icon_color)

        val buttonsList = listOf(binding.btnTarget33, binding.btnTarget99, binding.btnTarget100, binding.btnTarget500)
        buttonsList.forEach { btn ->
            btn.background = unselectedBg
            btn.setTextColor(darkTextColor)
        }

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
        saveCountForCurrentPhrase(0) // Clear storage records for this phrase
        binding.tvCounterDigits.text = "0"
        binding.beadProgressIndicator.updateProgress(0, maxLimit)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}