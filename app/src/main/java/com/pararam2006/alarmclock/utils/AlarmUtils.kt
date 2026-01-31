package com.pararam2006.alarmclock.utils

import java.util.Calendar

class AlarmUtils {
    fun calculateTimeInMillis(hour: Int, minute: Int, sec: Int = 0, millisec: Int = 0): Long {
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
}