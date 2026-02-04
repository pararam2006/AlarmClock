package com.pararam2006.alarmclock.ui.shared

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pararam2006.alarmclock.data.local.repository.AlarmRepository
import com.pararam2006.alarmclock.domain.model.Alarm
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AlarmViewModel(
    private val repository: AlarmRepository
) : ViewModel() {
    init {
        try {
            viewModelScope.launch {
                deleteAllDeletedAlarms()
            }
        } catch (e: Exception) {
            Log.e("AlarmViewModel", "Ошибка во время удаления задач:\n${e.message}")
        }
    }

    val uiState: StateFlow<AlarmUiState> = repository.getAllAlarms()
        .map { alarms ->
            AlarmUiState(alarms = alarms, isLoading = false)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AlarmUiState(isLoading = true)
        )

    fun createAlarm(hour: Int, minute: Int, daysOfWeek: List<Int>) = repository.createAlarm(hour, minute, daysOfWeek)

    fun toggleAlarm(alarm: Alarm) = repository.toggleAlarm(alarm)

    fun softDeleteAlarm(alarm: Alarm) = repository.softDeleteAlarm(alarm)

    fun updateAlarm(alarm: Alarm, hour: Int, minute: Int, daysOfWeek: List<Int>) = repository.updateAlarm(alarm, hour, minute, daysOfWeek)

    suspend fun deleteAllDeletedAlarms() = repository.deleteAllDeletedAlarms()
}
