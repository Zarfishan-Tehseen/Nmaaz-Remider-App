package com.example.nmaazreminder.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nmaazreminder.data.alarm.PrayerAlarmScheduler
import com.example.nmaazreminder.data.local.PrayerNotification
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

    val prayerNotificationsList = repository.getAllNotificationSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 🌟 2. COMBINED STATE FLOW
    // Listens to database settings changes AND custom calendar shifting simultaneously
    val prayerState = combine(repository.streamPrayerSettings(), _selectedDate) { settings, calendar ->
        settings?.let {
            val times = getPrayerTimesUseCase.execute(it, calendar.time)
            Pair(it.cityName, times)
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

            repository.getAllNotificationSettings().firstOrNull()?.let { currentList ->
                if (currentList.size < 5) {
                    val staticPrayers = listOf(
                        PrayerNotification(prayerName = "Fajr", isEnabled = true, soundName = "Madinah Adhan", reminderOffset = 10),
                        PrayerNotification(
                            prayerName = "Dhuhr",
                            isEnabled = true,
                            soundName = "Makkah Adhan",
                            reminderOffset = 5
                        ),
                        PrayerNotification(prayerName = "Asr", isEnabled = true, soundName = "Egyptian Adhan", reminderOffset = 5),
                        PrayerNotification(prayerName = "Maghrib", isEnabled = true, soundName = "Makkah Adhan", reminderOffset = 0),
                        PrayerNotification(prayerName = "Isha", isEnabled = true, soundName = "Silent (Vibrate)", reminderOffset = 15)
                    )

                    // Save missing items to Room natively using single inserts or loop mapping
                    staticPrayers.forEach { defaultPrayer ->
                        // Sirf wohi insert karega jo database mein pehle se nahi hai
                        if (currentList.none { it.prayerName.equals(defaultPrayer.prayerName, ignoreCase = true) }) {
                            repository.updateNotificationSetting(defaultPrayer)
                        }
                    }
                }
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
            // 1. Update the master setting table row
            repository.updateMasterNotification(isEnabled)

            // 2. Update all individual prayer notification rows in the database to match this status
            repository.updateAllPrayersEnabledStatus(isEnabled)
        }
    }
}