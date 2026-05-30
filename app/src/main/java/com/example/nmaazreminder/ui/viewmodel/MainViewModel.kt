package com.example.nmaazreminder.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nmaazreminder.data.alarm.PrayerAlarmScheduler
import com.example.nmaazreminder.data.local.PrayerSettings
import com.example.nmaazreminder.data.repository.PrayerRepository
import com.example.nmaazreminder.domain.usecase.GetPrayerTimesUseCase
import com.example.nmaazreminder.utils.LocationFetcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.*

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: PrayerRepository,
    private val getPrayerTimesUseCase: GetPrayerTimesUseCase,
    private val alarmScheduler: PrayerAlarmScheduler,
    private val locationFetcher: LocationFetcher
) : ViewModel() {

    // 🌟 1. BACKING CALENDAR TRACKER FOR DYNAMIC DATE SHIFTING
    private val _selectedDate = MutableStateFlow(Calendar.getInstance())
    val selectedDateState get() = _selectedDate

    val globalSettings = repository.streamPrayerSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // 🌟 2. COMBINED STATE FLOW
    // Listens to database settings changes AND custom calendar shifting simultaneously
    val prayerState = combine(repository.streamPrayerSettings(), _selectedDate) { settings, calendar ->
        if (settings != null) {
            // Modifying setting payload date arguments safely for specific date calculations
            val times = getPrayerTimesUseCase.execute(settings, calendar.time)
            Pair(settings.cityName, times)
        } else {
            null
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // Dynamic label generator matching your exact UI mockup layout specifications
    val currentDateString: String
        get() {
            val sdf = SimpleDateFormat("EEEE, dd MMMM", Locale.getDefault())
            return sdf.format(_selectedDate.value.time)
        }

    val currentHijriDateString: String
        get() {
            return try {
                // 1. Get the current java.util.Calendar instance from state
                val currentSelected = _selectedDate.value

                // 2. Initialize Android ICU IslamicCalendar safely
                val islamicCalendar = android.icu.util.IslamicCalendar.getInstance() as android.icu.util.IslamicCalendar

                // Pass the time from java.util.Calendar directly using timeInMillis property
                islamicCalendar.timeInMillis = currentSelected.timeInMillis

                // 3. Extract accurate components explicitly using ICU constants directly
                val hijriYear = islamicCalendar.get(android.icu.util.Calendar.YEAR)
                val hijriMonth = islamicCalendar.get(android.icu.util.Calendar.MONTH)
                val hijriDay = islamicCalendar.get(android.icu.util.Calendar.DAY_OF_MONTH)

                // 4. Islamic Months representation mapping array
                val islamicMonths = arrayOf(
                    "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' ath-Thani",
                    "Jumada al-Awwal", "Jumada ath-Thani", "Rajab", "Sha'ban",
                    "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
                )

                val monthName = islamicMonths.getOrElse(hijriMonth) { "Dhu al-Hijjah" }

                // Return clean dynamic presentation
                "$hijriDay $monthName $hijriYear AH"
            } catch (e: Exception) {
                e.printStackTrace()
                "1 Dhu al-Hijjah 1447 AH"
            }
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

                val defaultTimes = getPrayerTimesUseCase.execute(defaultSettings, Calendar.getInstance().time)
                alarmScheduler.scheduleAlarms(defaultTimes)
            } else {
                val times = getPrayerTimesUseCase.execute(currentSettings, Calendar.getInstance().time)
                alarmScheduler.scheduleAlarms(times)
            }
        }
    }

    // 🌟 3. DATE NAVIGATION UTILITY API
    fun updateSelectedDate(calendar: Calendar) {
        _selectedDate.value = calendar
    }

    fun shiftDateByDays(days: Int) {
        val currentCalendar = _selectedDate.value.clone() as Calendar
        currentCalendar.add(Calendar.DAY_OF_MONTH, days)
        _selectedDate.value = currentCalendar
    }

    fun resetToToday() {
        _selectedDate.value = Calendar.getInstance()
    }

    fun saveGlobalSettings(newSettings: PrayerSettings) {
        viewModelScope.launch {
            repository.updatePrayerSettings(newSettings)
            val times = getPrayerTimesUseCase.execute(newSettings, _selectedDate.value.time)
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
                        val updatedTimes = getPrayerTimesUseCase.execute(currentSettings, _selectedDate.value.time)
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
                val times = getPrayerTimesUseCase.execute(currentSettings, _selectedDate.value.time)
                alarmScheduler.scheduleAlarms(times)
            }
        }
    }

    fun toggleAllNotifications(isEnabled: Boolean) {
        viewModelScope.launch {
            // Read the current settings safely from the repository stream
            repository.streamPrayerSettings().firstOrNull()?.let { currentSettings ->
                // Create a copy with the updated master notification state
                val updatedSettings = currentSettings.copy(
                    isMasterNotificationEnabled = isEnabled
                )
                // Save it back to Room using your existing save method
                saveGlobalSettings(updatedSettings)
            }
        }
    }
}