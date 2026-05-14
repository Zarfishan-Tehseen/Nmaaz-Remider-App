package com.example.nmaazreminder.ui.prayerdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nmaazreminder.data.local.PrayerNotification
import com.example.nmaazreminder.data.repository.PrayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrayerDetailViewModel @Inject constructor(
    private val repository: PrayerRepository
) : ViewModel() {

    // 1. We use a StateFlow to hold the current prayer name being viewed
    private val _prayerName = MutableStateFlow<String?>(null)

    // 2. We use flatMapLatest to switch the database observer whenever the prayer name changes
    @OptIn(ExperimentalCoroutinesApi::class)
    val settings: StateFlow<PrayerNotification?> = _prayerName
        .filterNotNull()
        .flatMapLatest { name ->
            repository.getNotificationSetting(name) // This calls your Flow-based repository function
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun loadSettings(prayerName: String) {
        _prayerName.value = prayerName
    }

    // 3. Update functions now just push data to the database.
    // The Flow (settings) will automatically detect the change and update the UI.
    fun updateNotification(isEnabled: Boolean) {
        viewModelScope.launch {
            val current = settings.value ?: PrayerNotification(_prayerName.value ?: return@launch)
            repository.updateNotificationSetting(current.copy(isEnabled = isEnabled))
        }
    }

    fun updateSound(soundName: String) {
        viewModelScope.launch {
            val current = settings.value ?: PrayerNotification(_prayerName.value ?: return@launch)
            repository.updateNotificationSetting(current.copy(soundName = soundName))
        }
    }

    fun updateOffset(minutes: Int) {
        viewModelScope.launch {
            val current = settings.value ?: PrayerNotification(_prayerName.value ?: return@launch)
            repository.updateNotificationSetting(current.copy(reminderOffset = minutes))
        }
    }
}