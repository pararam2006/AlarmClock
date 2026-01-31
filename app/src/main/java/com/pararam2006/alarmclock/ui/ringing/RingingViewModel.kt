package com.pararam2006.alarmclock.ui.ringing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pararam2006.alarmclock.data.local.repository.AlarmRepository
import com.pararam2006.alarmclock.domain.model.Alarm
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RingingViewModel(
    private val alarmId: Long,
    private val repository: AlarmRepository
) : ViewModel() {
    private val _alarm = MutableStateFlow<Alarm?>(null)
    val alarm: StateFlow<Alarm?> = _alarm.asStateFlow()

    init {
        viewModelScope.launch {
            _alarm.value = repository.getAlarmById(alarmId)
        }
    }

    fun cancel() = repository.disableAlarm(alarmId)
    fun snooze() = repository.snooze(alarmId)
}
