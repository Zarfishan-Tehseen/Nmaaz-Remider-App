package com.example.nmaazreminder.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nmaazreminder.data.local.PrayerNotification
import com.example.nmaazreminder.data.local.PrayerSettings
import com.example.nmaazreminder.data.repository.PrayerRepository
import com.example.nmaazreminder.domain.usecase.GetPrayerTimesUseCase
import com.example.nmaazreminder.ui.main.PrayerItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.flow.firstOrNull

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: PrayerRepository,
    private val getPrayerTimesUseCase: GetPrayerTimesUseCase
) : ViewModel() {

    // Database Flow into a UI State Flow
    val prayerState = repository.streamPrayerSettings().map { settings ->
        if (settings != null) {
            getPrayerTimesUseCase.execute(settings)
        } else {
            null
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val currentDateString: String
        get() {
            val sdf = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault())
            return sdf.format(Date())
        }

    init {
        viewModelScope.launch {
            // We only want to check the database ONCE at startup
            val currentSettings = repository.streamPrayerSettings().firstOrNull()

            if (currentSettings == null) {
                repository.updatePrayerSettings(
                    PrayerSettings(
                        id = 1,
                        latitude = 32.074,
                        longitude = 72.686,
                        calculationMethod = 1, // Karachi
                        asrMethod = 1 // Hanafi
                    )
                )
            }
        }
    }
    fun updateLocation(name: String, lat: Double, lon: Double) {
        viewModelScope.launch {
            repository.updateUserCoordinates(name, lat, lon)
        }
    }
}