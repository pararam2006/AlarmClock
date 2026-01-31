package com.pararam2006.alarmclock.ui.shared

import com.pararam2006.alarmclock.domain.model.Alarm

data class AlarmUiState(
    val alarms: List<Alarm> = emptyList(),
    val isLoading: Boolean = true,
)
