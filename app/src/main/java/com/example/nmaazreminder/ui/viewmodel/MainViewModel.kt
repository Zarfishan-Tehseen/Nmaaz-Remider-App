package com.example.nmaazreminder.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nmaazreminder.data.alarm.PrayerAlarmScheduler
import com.example.nmaazreminder.data.local.PrayerSettings
import com.example.nmaazreminder.data.repository.PrayerRepository
import com.example.nmaazreminder.domain.usecase.GetPrayerTimesUseCase
import com.example.nmaazreminder.utils.LocationFetcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.text.SimpleDateFormat
import java.util.*

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: PrayerRepository,
    private val getPrayerTimesUseCase: GetPrayerTimesUseCase,
    private val alarmScheduler: PrayerAlarmScheduler,
    private val locationFetcher: LocationFetcher
) : ViewModel() {

    // 🌟 Expose the raw database stream so our settings screen can observe selections live
    val globalSettings = repository.streamPrayerSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val prayerState = repository.streamPrayerSettings().map { settings ->
        if (settings != null) {
            val times = getPrayerTimesUseCase.execute(settings)
            Pair(settings.cityName, times)
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
            val currentSettings = repository.streamPrayerSettings().firstOrNull()

            if (currentSettings == null) {
                val defaultSettings = PrayerSettings(
                    id = 1,
                    cityName = "Sargodha",
                    latitude = 32.074,
                    longitude = 72.686,
                    calculationMethod = 1, // Karachi
                    asrMethod = 1 // Hanafi
                )
                repository.updatePrayerSettings(defaultSettings)

                val defaultTimes = getPrayerTimesUseCase.execute(defaultSettings)
                alarmScheduler.scheduleAlarms(defaultTimes)
            } else {
                val times = getPrayerTimesUseCase.execute(currentSettings)
                alarmScheduler.scheduleAlarms(times)
            }
        }
    }

    /**
     * Helper function to save updated configuration structural properties
     * (Calculation methods, Madhab rules, etc.) directly into Room.
     */
    fun saveGlobalSettings(newSettings: PrayerSettings) {
        viewModelScope.launch {
            repository.updatePrayerSettings(newSettings)

            // Re-sync background system alarms whenever structural formulas shift
            val times = getPrayerTimesUseCase.execute(newSettings)
            alarmScheduler.scheduleAlarms(times)
        }
    }

    fun fetchAndSaveCurrentLocation() {
        viewModelScope.launch {
            try {
                val location = locationFetcher.getCurrentCoordinates()
                if (location != null) {
                    val lat = location.latitude
                    val lon = location.longitude
                    val cityName = locationFetcher.getCityName(lat, lon)

                    repository.updateUserCoordinates(cityName, lat, lon)

                    val currentSettings = repository.streamPrayerSettings().firstOrNull()
                    if (currentSettings != null) {
                        val updatedTimes = getPrayerTimesUseCase.execute(currentSettings)
                        alarmScheduler.scheduleAlarms(updatedTimes)
                    }
                }
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }

    fun updateLocation(name: String, lat: Double, lon: Double) {
        viewModelScope.launch {
            repository.updateUserCoordinates(name, lat, lon)

            val currentSettings = repository.streamPrayerSettings().firstOrNull()
            if (currentSettings != null) {
                val times = getPrayerTimesUseCase.execute(currentSettings)
                alarmScheduler.scheduleAlarms(times)
            }
        }
    }
}