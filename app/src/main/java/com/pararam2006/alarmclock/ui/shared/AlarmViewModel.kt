package com.pararam2006.alarmclock.ui.shared

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pararam2006.alarmclock.data.local.repository.AlarmRepository
import com.pararam2006.alarmclock.domain.model.Alarm
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class AlarmViewModel(
    private val repository: AlarmRepository
) : ViewModel() {

    val uiState: StateFlow<AlarmUiState> = repository.getAllAlarms()
        .map { alarms ->
            AlarmUiState(
                alarms = alarms.sortedBy { it.hour * 60 + it.minute },
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AlarmUiState(isLoading = true)
        )

    fun createAlarm(hour: Int, minute: Int) = repository.createAlarm(hour, minute)

    fun toggleAlarm(alarm: Alarm) = repository.toggleAlarm(alarm)

    fun deleteAlarm(alarm: Alarm) = repository.deleteAlarm(alarm)

    fun updateAlarm(alarm: Alarm, hour: Int, minute: Int) =
        repository.updateAlarm(alarm, hour, minute)

}
