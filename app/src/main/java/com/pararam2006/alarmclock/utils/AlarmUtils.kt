package com.pararam2006.alarmclock.utils

import java.util.Calendar

class AlarmUtils {
    /**
     * Рассчитывает время в миллисекундах для следующего срабатывания будильника.
     * @param daysOfWeek список дней из Calendar (1 - воскресенье, 2 - понедельник...)
     */
    fun calculateTimeInMillis(hour: Int, minute: Int, daysOfWeek: List<Int> = emptyList()): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val now = Calendar.getInstance()

        if (daysOfWeek.isEmpty()) {
            // Логика для разового будильника
            if (calendar.timeInMillis <= now.timeInMillis) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
        } else {
            // Логика для повторяющегося будильника
            // Ищем ближайший день из списка, начиная с сегодняшнего
            var daysUntilAlarm = 8 // Максимум через неделю
            val currentDayOfWeek = now.get(Calendar.DAY_OF_WEEK)

            for (targetDay in daysOfWeek) {
                var diff = targetDay - currentDayOfWeek
                if (diff < 0) diff += 7 // День на следующей неделе
                
                // Если день сегодня, но время уже прошло
                if (diff == 0 && calendar.timeInMillis <= now.timeInMillis) {
                    diff = 7
                }
                
                if (diff < daysUntilAlarm) {
                    daysUntilAlarm = diff
                }
            }
            
            if (daysUntilAlarm != 8) {
                calendar.add(Calendar.DAY_OF_YEAR, daysUntilAlarm)
            }
        }
        
        return calendar.timeInMillis
    }
}
