package com.pararam2006.alarmclock.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.pararam2006.alarmclock.core.service.AlarmService
import java.time.LocalDateTime

class AlarmReceiver(
    val alarmService: AlarmService
) : BroadcastReceiver() {
    val TAG = "MainScreen"

    override fun onReceive(context: Context?, intent: Intent?) {
        //TODO Логика срабатывания будильника

        Log.d(TAG, "Будильник сработал в ${LocalDateTime.now()}")
    }

}