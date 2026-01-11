package com.pararam2006.alarmclock.ui.shared

import com.pararam2006.alarmclock.domain.model.Alarm

data class AlarmUiState(
    val alarms: List<Alarm> = emptyList(),

    val hour: Int = 0,
    val minute: Int = 0,
    val sec: Int = 0,
    val millisec: Int = 0,

    val creationHour: Int = 0,
    val creationMinute: Int = 0,
    val creationSec: Int = 0,
    val creationMillisec: Int = 0,

    val updateHour: Int = 0,
    val updateMinute: Int = 0,
    val updateSec: Int = 0,
    val updateMillisec: Int = 0,
)
