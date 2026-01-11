package com.pararam2006.alarmclock.ui.shared

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pararam2006.alarmclock.data.local.dao.AlarmDao
import com.pararam2006.alarmclock.domain.model.Alarm
import com.pararam2006.alarmclock.utils.AlarmManagerScheduler
import com.pararam2006.alarmclock.utils.SnackbarManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

class AlarmViewModel(
    private val scheduler: AlarmManagerScheduler,
    private val dao: AlarmDao,
    private val snackbarManager: SnackbarManager,
) : ViewModel() {
    private val TAG = "AlarmViewModel"
    private val _uiState = MutableStateFlow(AlarmUiState())
    val uiState: StateFlow<AlarmUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            dao.getAllAlarms().collect { alarms ->
                _uiState.update {
                    it.copy(alarms = alarms)
                }
            }
        }
    }

    private fun calculateTimeInMillis(hour: Int, minute: Int, sec: Int = 0, millisec: Int = 0): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, sec)
            set(Calendar.MILLISECOND, millisec)
        }
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        return calendar.timeInMillis
    }

    fun createAlarm(hour: Int, minute: Int, sec: Int = 0, millisec: Int = 0) {
        viewModelScope.launch {
            try {
                // Ищем, есть ли уже будильник на это время в текущем стейте
                val existingAlarm = _uiState.value.alarms.find {
                    it.hour == hour && it.minute == minute
                }

                // Если нашли — обновляем его, если нет — создаем новый (id = 0L)
                val alarmToSave = existingAlarm?.copy(isEnabled = true) ?: Alarm(
                    hour = hour,
                    minute = minute,
                    isEnabled = true,
                    daysOfWeek = emptyList(),
                )

                // Сохраняем в базу. Room заменит запись (REPLACE), если ID совпадет
                val id = dao.insertAlarm(alarmToSave)
                val timeInMillis = calculateTimeInMillis(hour, minute, sec, millisec)

                scheduler.schedule(id, timeInMillis)

                if (existingAlarm != null) {
                    snackbarManager.showMessage("Будильник на это время обновлен")
                } else {
                    snackbarManager.showMessage("Будильник создан!")
                }
            } catch (e: Exception) {
                snackbarManager.showMessage("Ошибка при создании будильника")
                Log.e(TAG, "Ошибка при создании будильника: ${e.message}")
            }
        }
    }

    fun onTimeChanged(
        hour: Int = _uiState.value.hour,
        minute: Int = _uiState.value.minute,
        sec: Int = _uiState.value.sec,
        millisec: Int = _uiState.value.millisec,
    ) {
        _uiState.update {
            it.copy(
                hour = hour,
                minute = minute,
                sec = sec,
                millisec = millisec,
            )
        }
    }

    fun toggleAlarm(alarm: Alarm) {
        val updatedAlarm = alarm.copy(isEnabled = !alarm.isEnabled)

        viewModelScope.launch {
            try {
                dao.updateAlarm(updatedAlarm)

                if (updatedAlarm.isEnabled) {
                    val timeInMillis = calculateTimeInMillis(updatedAlarm.hour, updatedAlarm.minute)
                    scheduler.schedule(updatedAlarm.id, timeInMillis)
                    snackbarManager.showMessage("Будильник включен")
                } else {
                    scheduler.cancel(updatedAlarm)
                    snackbarManager.showMessage("Будильник выключен")
                }
            } catch (e: Exception) {
                snackbarManager.showMessage("Ошибка при обновлении будильника")
                Log.e(TAG, "Ошибка обновления: ${e.message}")
            }
        }
    }

    fun deleteAlarm(alarm: Alarm) {
        viewModelScope.launch {
            try {
                scheduler.cancel(alarm)
                dao.deleteAlarm(alarm)
                snackbarManager.showMessage("Будильник удален")
            } catch (e: Exception) {
                snackbarManager.showMessage("Ошибка при удалении будильника")
                Log.e(TAG, "Ошибка при удалении будильника: ${e.message}")
            }
        }
    }

    fun updateAlarm() {}
}
