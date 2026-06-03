package com.example.nmaazreminder.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.batoulapps.adhan.Prayer
import com.batoulapps.adhan.PrayerTimes
import com.example.nmaazreminder.data.alarm.PrayerAlarmScheduler
import com.example.nmaazreminder.data.local.PrayerNotification
import com.example.nmaazreminder.data.local.PrayerSettings
import com.example.nmaazreminder.data.repository.PrayerRepository
import com.example.nmaazreminder.domain.usecase.GetPrayerTimesUseCase
import com.example.nmaazreminder.ui.fragments.home.PrayerCountdownState
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

    // 1. BACKING CALENDAR TRACKER FOR DYNAMIC DATE SHIFTING
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

    // 2. COMBINED STATE FLOW
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

    // NEW: THE DYNAMIC QAZA & GAP TIMER PIPELINE ENGINE
    val dynamicCountdownState = combine(prayerState, globalSettings) { statePair, settings ->
        if (statePair == null || settings == null) null
        else {
            val (_, times) = statePair
            calculateDynamicPrayerState(times, settings)
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

    private fun calculateDynamicPrayerState(times: PrayerTimes, settings: PrayerSettings): PrayerCountdownState {
        val currentTime = System.currentTimeMillis()
        val timeFormatter = SimpleDateFormat("h:mm a", Locale.getDefault())

        // Fetch core timestamps from Adhan API
        val fajrStart = times.timeForPrayer(Prayer.FAJR).time
        val sunriseStart = times.timeForPrayer(Prayer.SUNRISE).time
        val dhuhrStart = times.timeForPrayer(Prayer.DHUHR).time
        val asrStart = times.timeForPrayer(Prayer.ASR).time
        val maghribStart = times.timeForPrayer(Prayer.MAGHRIB).time
        val ishaStart = times.timeForPrayer(Prayer.ISHA).time

        //  Apply Fiqh Subtracting Offsets
        val fajrQazaLimit = sunriseStart - (10 * 60 * 1000)      // -10 mins from Sunrise
        val dhuhrQazaLimit = asrStart - (10 * 60 * 1000)         // -10 mins from Asr
        val asrMakroohLimit = maghribStart - (25 * 60 * 1000)   // -25 mins from Maghrib
        val asrQazaLimit = maghribStart - (10 * 60 * 1000)       // -10 mins from Maghrib
        val maghribQazaLimit = ishaStart - (10 * 60 * 1000)      // -10 mins from Isha

        //  Handle Isha Midnight Cross-over Calculation
        val tomorrowCalendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
        val tomorrowTimes = getPrayerTimesUseCase.execute(settings, tomorrowCalendar.time)
        val tomorrowFajrStart = tomorrowTimes.timeForPrayer(Prayer.FAJR).time
        val ishaQazaLimit = tomorrowFajrStart - (10 * 60 * 1000) // -10 mins from Tomorrow's Fajr

        // TRIGGER THE MATCHING TREE
        return when {
            // 1. FAJR TIMELINE
            currentTime in fajrStart until fajrQazaLimit -> {
                PrayerCountdownState("Fajr", "الفجر", timeFormatter.format(Date(fajrStart)), "Next: Sunrise at ${timeFormatter.format(Date(sunriseStart))}", fajrQazaLimit)
            }
            currentTime in fajrQazaLimit until sunriseStart -> {
                PrayerCountdownState("Fajr Qaza", "الفجر", timeFormatter.format(Date(fajrStart)), "Next: Dhuhr at ${timeFormatter.format(Date(dhuhrStart))}", 0L)
            }
            // 💡 GAP WINDOW: Sunrise to Dhuhr Start
            currentTime in sunriseStart until dhuhrStart -> {
                PrayerCountdownState("Sunrise", "الشروق", timeFormatter.format(Date(sunriseStart)), "Next: Dhuhr at ${timeFormatter.format(Date(dhuhrStart))}", 0L)
            }

            // 2. DHUHR TIMELINE
            currentTime in dhuhrStart until dhuhrQazaLimit -> {
                PrayerCountdownState("Dhuhr", "الظهر", timeFormatter.format(Date(dhuhrStart)), "Next: Asr at ${timeFormatter.format(Date(asrStart))}", dhuhrQazaLimit)
            }
            // 💡 GAP WINDOW: Zuhr Qaza to Asr Start
            currentTime in dhuhrQazaLimit until asrStart -> {
                PrayerCountdownState("Dhuhr Qaza", "الظهر", timeFormatter.format(Date(dhuhrStart)), "Next: Asr at ${timeFormatter.format(Date(asrStart))}", 0L)
            }

            // 3. ASR TIMELINE
            currentTime in asrStart until asrMakroohLimit -> {
                PrayerCountdownState("Asr", "العصر", timeFormatter.format(Date(asrStart)), "Next: Maghrib at ${timeFormatter.format(Date(maghribStart))}", asrMakroohLimit)
            }
            currentTime in asrMakroohLimit until asrQazaLimit -> {
                PrayerCountdownState("Asr (Makrooh)", "العصر", timeFormatter.format(Date(asrStart)), "Next: Maghrib at ${timeFormatter.format(Date(maghribStart))}", asrQazaLimit, isMakrooh = true)
            }
            // 💡 GAP WINDOW: Asr Qaza to Maghrib Start
            currentTime in asrQazaLimit until maghribStart -> {
                PrayerCountdownState("Asr Qaza", "العصر", timeFormatter.format(Date(asrStart)), "Next: Maghrib at ${timeFormatter.format(Date(maghribStart))}", 0L)
            }

            // 4. MAGHRIB TIMELINE
            currentTime in maghribStart until maghribQazaLimit -> {
                PrayerCountdownState("Maghrib", "المغرب", timeFormatter.format(Date(maghribStart)), "Next: Isha at ${timeFormatter.format(Date(ishaStart))}", maghribQazaLimit)
            }
            // 💡 GAP WINDOW: Maghrib Qaza to Isha Start
            currentTime in maghribQazaLimit until ishaStart -> {
                PrayerCountdownState("Maghrib Qaza", "المغرب", timeFormatter.format(Date(maghribStart)), "Next: Isha at ${timeFormatter.format(Date(ishaStart))}", 0L)
            }

            // 5. ISHA TIMELINE (Handles Midnight Transition)
            currentTime >= ishaStart || currentTime < tomorrowFajrStart -> {
                if (currentTime in ishaStart until ishaQazaLimit || currentTime < ishaQazaLimit - (24 * 60 * 60 * 1000)) {
                    // Normal Isha Period
                    PrayerCountdownState("Isha", "العشاء", timeFormatter.format(Date(ishaStart)), "Next: Fajr at ${timeFormatter.format(Date(tomorrowFajrStart))}", ishaQazaLimit)
                } else {
                    // 💡 GAP WINDOW: Isha Qaza to Tomorrow's Fajr Start
                    PrayerCountdownState("Isha Qaza", "العشاء", timeFormatter.format(Date(ishaStart)), "Next: Fajr at ${timeFormatter.format(Date(tomorrowFajrStart))}", 0L)
                }
            }

            // Fallback Guard
            else -> {
                PrayerCountdownState("Fajr", "الفجر", timeFormatter.format(Date(fajrStart)), "Next: Sunrise at ${timeFormatter.format(Date(sunriseStart))}", fajrQazaLimit)
            }
        }
    }

    // Dynamic refreshing callback triggered by fragment on countdown completions
    fun refreshCurrentState() {
        val currentCalendar = _selectedDate.value.clone() as Calendar
        _selectedDate.value = currentCalendar
    }
    //  3. DATE NAVIGATION UTILITY API
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