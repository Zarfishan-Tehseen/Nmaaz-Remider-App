package com.example.nmaazreminder.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nmaazreminder.data.alarm.PrayerAlarmScheduler
import com.example.nmaazreminder.data.local.PrayerNotification
import com.example.nmaazreminder.data.repository.PrayerRepository
import com.example.nmaazreminder.domain.usecase.GetPrayerTimesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrayerDetailViewModel @Inject constructor(
    private val repository: PrayerRepository,
    private val alarmScheduler: PrayerAlarmScheduler,       // 🌟 Injected to reschedule alarms
    private val getPrayerTimesUseCase: GetPrayerTimesUseCase // 🌟 Injected to compute new timestamps
) : ViewModel() {

    private val _prayerName = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val settings: StateFlow<PrayerNotification?> = _prayerName
        .filterNotNull()
        .flatMapLatest { name ->
            repository.getNotificationSetting(name)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentAdhanSound: StateFlow<String> = _prayerName
        .flatMapLatest { name ->
            if (name.isNullOrEmpty() || name == "Global") {
                // Agar global settings mode hai, list ke pehle element (e.g. Fajr) ka sound as dynamic preview use karlein
                repository.getAllNotificationSettings().map { list -> list.firstOrNull()?.soundName ?: "Madinah Adhan" }
            } else {
                // Specific prayer mode
                repository.getNotificationSetting(name).map { it?.soundName ?: "Madinah Adhan" }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "Madinah Adhan"
        )
    fun loadSettings(prayerName: String) {
        _prayerName.value = prayerName
    }

    fun updateNotification(isEnabled: Boolean) {
        viewModelScope.launch {
            val current = settings.value ?: PrayerNotification(_prayerName.value ?: return@launch)
            val updated = current.copy(isEnabled = isEnabled)

            repository.updateNotificationSetting(updated)
            rescheduleAlarmsWithLatestSettings() // 🌟 Trigger background alarm sync
        }
    }

    fun updateSound(soundName: String) {
        viewModelScope.launch {
            val current = settings.value ?: PrayerNotification(_prayerName.value ?: return@launch)
            val updated = current.copy(soundName = soundName)

            repository.updateNotificationSetting(updated)
            rescheduleAlarmsWithLatestSettings() // 🌟 Trigger background alarm sync
        }
    }

    fun updateOffset(minutes: Int) {
        viewModelScope.launch {
            val current = settings.value ?: PrayerNotification(_prayerName.value ?: return@launch)
            val updated = current.copy(reminderOffset = minutes)

            repository.updateNotificationSetting(updated)
            rescheduleAlarmsWithLatestSettings() // 🌟 Trigger background alarm sync
        }
    }

    /**
     * Helper function to extract current location/calculation configuration profiles,
     * generate the raw prayer time structures, and hand them off to the scheduler.
     */
    private suspend fun rescheduleAlarmsWithLatestSettings() {
        // 1. Grab the active global location configurations (City, Latitude, Longitude, etc.)
        val globalSettings = repository.streamPrayerSettings().firstOrNull()

        if (globalSettings != null) {
            // 2. Compute the correct raw base prayer timings using the UseCase
            val rawTimes = getPrayerTimesUseCase.execute(globalSettings)

            // 3. Send them to the scheduler to apply individual user offsets and toggles safely
            alarmScheduler.scheduleAlarms(rawTimes)
        }
    }

    fun updateSoundForSpecificPrayer(prayerName: String, soundName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateSoundForSpecificPrayer(prayerName, soundName)
            rescheduleAlarmsWithLatestSettings()
        }
    }

    fun updateSoundForAllPrayers(soundName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateSoundForAllPrayers(soundName)
            rescheduleAlarmsWithLatestSettings()
        }
    }
}