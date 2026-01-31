package com.pararam2006.alarmclock.data.local.repository

import android.util.Log
import com.pararam2006.alarmclock.data.local.dao.AlarmDao
import com.pararam2006.alarmclock.domain.model.Alarm
import com.pararam2006.alarmclock.utils.AlarmManagerScheduler
import com.pararam2006.alarmclock.utils.AlarmUtils
import com.pararam2006.alarmclock.utils.SnackbarManager
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Calendar

class AlarmRepository(
    private val alarmDao: AlarmDao,
    private val scheduler: AlarmManagerScheduler,
    private val alarmUtils: AlarmUtils,
    private val snackbarManager: SnackbarManager
) {
    private val TAG = "AlarmRepository"
    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
        Log.e(TAG, "Repository Error: ${exception.message}", exception)
        repositoryScope.launch {
            snackbarManager.showMessage("Ошибка при работе с базой данных") 
        }
    }

    private val repositoryScope: CoroutineScope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO + coroutineExceptionHandler
    )

    fun getAllAlarms(): Flow<List<Alarm>> = alarmDao.getAllAlarms()

    suspend fun getAlarmById(id: Long): Alarm? = alarmDao.getAlarmById(id)

    fun createAlarm(hour: Int, minute: Int) {
        repositoryScope.launch {
            val alarms = alarmDao.getAllAlarms().firstOrNull() ?: emptyList()
            val existingAlarm = alarms.find { it.hour == hour && it.minute == minute }

            val alarmToSave = existingAlarm?.copy(isEnabled = true) ?: Alarm(
                hour = hour,
                minute = minute,
                isEnabled = true,
                daysOfWeek = emptyList(),
            )

            val id = alarmDao.insertAlarm(alarmToSave)
            val timeInMillis = alarmUtils.calculateTimeInMillis(hour, minute)

            scheduler.schedule(id, timeInMillis)

            if (existingAlarm != null) {
                snackbarManager.showMessage("Будильник на это время обновлен")
            } else {
                Log.i(TAG, "Будильник с id $id создан!")
            }
        }
    }

    fun updateAlarm(alarm: Alarm, hour: Int, minute: Int) {
        repositoryScope.launch {
            val updatedAlarm = alarm.copy(hour = hour, minute = minute, isEnabled = true)
            alarmDao.updateAlarm(updatedAlarm)

            val timeInMillis = alarmUtils.calculateTimeInMillis(hour, minute)
            scheduler.schedule(updatedAlarm.id, timeInMillis)

            Log.i(TAG, "Будильник с id ${updatedAlarm.id} обновлен на $hour:$minute")
        }
    }

    fun toggleAlarm(alarm: Alarm) {
        repositoryScope.launch {
            val updatedAlarm = alarm.copy(isEnabled = !alarm.isEnabled)
            alarmDao.updateAlarm(updatedAlarm)

            if (updatedAlarm.isEnabled) {
                val timeInMillis = alarmUtils.calculateTimeInMillis(updatedAlarm.hour, updatedAlarm.minute)
                scheduler.schedule(updatedAlarm.id, timeInMillis)
                Log.i(TAG, "Будильник с id ${updatedAlarm.id} включен")
            } else {
                scheduler.cancel(updatedAlarm)
                Log.i(TAG, "Будильник с id ${updatedAlarm.id} выключен")
            }
        }
    }

    fun deleteAlarm(alarm: Alarm) {
        repositoryScope.launch {
            scheduler.cancel(alarm)
            alarmDao.deleteAlarm(alarm)
            Log.i(TAG, "Будильник с id ${alarm.id} удален")
        }
    }

    fun snooze(alarmId: Long) {
        repositoryScope.launch {
            val alarm = alarmDao.getAlarmById(alarmId)
            if (alarm != null) {
                val calendar = Calendar.getInstance().apply { add(Calendar.MINUTE, 5) }
                val newHour = calendar.get(Calendar.HOUR_OF_DAY)
                val newMinute = calendar.get(Calendar.MINUTE)

                // Проверяем, нет ли уже будильника на это время
                val alarms = alarmDao.getAllAlarms().firstOrNull() ?: emptyList()
                val conflict = alarms.find { it.hour == newHour && it.minute == newMinute && it.id != alarmId }

                if (conflict != null) {
                    snackbarManager.showMessage("Нельзя отложить: на это время уже есть будильник")
                    Log.w(TAG, "Ошибка откладывания: будильник на $newHour:$newMinute уже существует")
                    return@launch
                }

                val snoozedAlarm = alarm.copy(
                    hour = newHour,
                    minute = newMinute,
                    isEnabled = true
                )
                alarmDao.updateAlarm(snoozedAlarm)

                scheduler.schedule(alarmId, calendar.timeInMillis)
                Log.i(TAG, "Будильник $alarmId отложен на 5 минут до $newHour:$newMinute")
            }
        }
    }

    fun disableAlarm(alarmId: Long) {
        repositoryScope.launch {
            val alarm = alarmDao.getAlarmById(alarmId)
            if (alarm != null) {
                val disabledAlarm = alarm.copy(isEnabled = false)
                alarmDao.updateAlarm(disabledAlarm)
                scheduler.cancel(disabledAlarm)
                Log.i(TAG, "Будильник $alarmId выключен")
            }
        }
    }
}
