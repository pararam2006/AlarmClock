package com.pararam2006.alarmclock.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.pararam2006.alarmclock.core.service.AlarmService

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getLongExtra("ALARM_ID", -1L)

        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            putExtra("ALARM_ID", alarmId)
        }

        // Запускаем сервис как Foreground
        ContextCompat.startForegroundService(context, serviceIntent)
    }
}