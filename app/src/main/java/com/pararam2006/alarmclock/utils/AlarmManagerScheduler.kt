package com.pararam2006.alarmclock.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.pararam2006.alarmclock.domain.model.Alarm

class AlarmManagerScheduler(private val context: Context) {
    val TAG = "AlarmManagerScheduler"
    val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(alarmId: Long, timeInMillis: Long) {
        try {
            //Создание Intent
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("ALARM_ID", alarmId)
            }

            //Обертка в PendingIntent
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                alarmId.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val isCanScheduleExactAlarms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                manager.canScheduleExactAlarms()
            } else {
                true
            }

            //Планирование события
            if (isCanScheduleExactAlarms) {
                manager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent
                )
            } else {
                Log.d("MainScreen", "Нет разрашения на точные будильники")
                openAlarmSettings()
            }
        } catch (e: Exception) {
            Log.e("MainScreen", e.message.toString())
        }
    }

    fun cancel(alarm: Alarm) {
        try {
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                alarm.id.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            manager.cancel(pendingIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при отмене будильника: ${e.message}")
        }
    }

    fun openAlarmSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
}