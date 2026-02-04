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

    fun createAlarm(hour: Int, minute: Int, daysOfWeek: List<Int>) {
        repositoryScope.launch {
            val alarms = alarmDao.getAllAlarms().firstOrNull() ?: emptyList()
            val existingAlarm = alarms.find { it.hour == hour && it.minute == minute && it.daysOfWeek == daysOfWeek }

            val alarmToSave = existingAlarm?.copy(isEnabled = true, isDeleted = false) ?: Alarm(
                hour = hour,
                minute = minute,
                isEnabled = true,
                daysOfWeek = daysOfWeek,
                isDeleted = false
            )

            val id = alarmDao.insertAlarm(alarmToSave)
            val timeInMillis = alarmUtils.calculateTimeInMillis(hour, minute, daysOfWeek)

            scheduler.schedule(id, timeInMillis)

            if (existingAlarm != null) {
                snackbarManager.showMessage("Будильник на это время обновлен")
            } else {
                Log.i(TAG, "Будильник с id $id создан!")
            }
        }
    }

    fun updateAlarm(alarm: Alarm, hour: Int, minute: Int, daysOfWeek: List<Int>) {
        repositoryScope.launch {
            val updatedAlarm = alarm.copy(hour = hour, minute = minute, isEnabled = true, daysOfWeek = daysOfWeek, isDeleted = false)
            alarmDao.updateAlarm(updatedAlarm)

            val timeInMillis = alarmUtils.calculateTimeInMillis(hour, minute, daysOfWeek)
            scheduler.schedule(updatedAlarm.id, timeInMillis)

            Log.i(TAG, "Будильник с id ${updatedAlarm.id} обновлен на $hour:$minute")
        }
    }

    fun toggleAlarm(alarm: Alarm) {
        repositoryScope.launch {
            val updatedAlarm = alarm.copy(isEnabled = !alarm.isEnabled)
            alarmDao.updateAlarm(updatedAlarm)

            if (updatedAlarm.isEnabled) {
                val timeInMillis = alarmUtils.calculateTimeInMillis(updatedAlarm.hour, updatedAlarm.minute, updatedAlarm.daysOfWeek)
                scheduler.schedule(updatedAlarm.id, timeInMillis)
                Log.i(TAG, "Будильник с id ${updatedAlarm.id} включен")
            } else {
                scheduler.cancel(updatedAlarm)
                Log.i(TAG, "Будильник с id ${updatedAlarm.id} выключен")
            }
        }
    }

    fun softDeleteAlarm(alarm: Alarm) {
        repositoryScope.launch {
            scheduler.cancel(alarm)
            alarmDao.updateAlarm(alarm.copy(isDeleted = true))
            Log.i(TAG, "Будильник с id ${alarm.id} помечен как удаленный")
            snackbarManager.showMessageWithAction("Будильник удален", "Отмена") {
                undoSoftDeleteAlarm(alarm)
            }
        }
    }

    fun undoSoftDeleteAlarm(alarm: Alarm) {
        repositoryScope.launch {
            val restoredAlarm = alarm.copy(isDeleted = false)
            alarmDao.updateAlarm(restoredAlarm)
            if (restoredAlarm.isEnabled) {
                val timeInMillis = alarmUtils.calculateTimeInMillis(restoredAlarm.hour, restoredAlarm.minute, restoredAlarm.daysOfWeek)
                scheduler.schedule(restoredAlarm.id, timeInMillis)
            }
            Log.i(TAG, "Будильник с id ${alarm.id} восстановлен")
        }
    }

    fun hardDeleteAlarm(alarm: Alarm) {
        repositoryScope.launch {
            alarmDao.deleteAlarm(alarm)
            Log.i(TAG, "Будильник с id ${alarm.id} окончательно удален")
        }
    }

    fun snooze(alarmId: Long) {
        repositoryScope.launch {
            val alarm = alarmDao.getAlarmById(alarmId)
            if (alarm != null) {
                val calendar = Calendar.getInstance().apply { add(Calendar.MINUTE, 5) }
                val newHour = calendar.get(Calendar.HOUR_OF_DAY)
                val newMinute = calendar.get(Calendar.MINUTE)

                val alarms = alarmDao.getAllAlarms().firstOrNull() ?: emptyList()
                val conflict = alarms.find { it.hour == newHour && it.minute == newMinute && it.id != alarmId }

                if (conflict != null) {
                    snackbarManager.showMessage("Нельзя отложить: на это время уже есть будильник")
                    return@launch
                }

                val snoozedAlarm = alarm.copy(
                    hour = newHour,
                    minute = newMinute,
                    isEnabled = true
                )
                alarmDao.updateAlarm(snoozedAlarm)

                scheduler.schedule(alarmId, calendar.timeInMillis)
                Log.i(TAG, "Будильник $alarmId отложен на 5 минут")
            }
        }
    }

    fun disableAlarm(alarmId: Long) {
        repositoryScope.launch {
            val alarm = alarmDao.getAlarmById(alarmId)
            if (alarm != null) {
                if (alarm.daysOfWeek.isEmpty()) {
                    val disabledAlarm = alarm.copy(isEnabled = false)
                    alarmDao.updateAlarm(disabledAlarm)
                    scheduler.cancel(disabledAlarm)
                    Log.i(TAG, "Разовый будильник $alarmId выключен")
                } else {
                    val nextTime = alarmUtils.calculateTimeInMillis(alarm.hour, alarm.minute, alarm.daysOfWeek)
                    scheduler.schedule(alarm.id, nextTime)
                    Log.i(TAG, "Повторяющийся будильник $alarmId перепланирован")
                }
            }
        }
    }

    suspend fun deleteAllDeletedAlarms() = alarmDao.deleteAllDeletedAlarms()
}
