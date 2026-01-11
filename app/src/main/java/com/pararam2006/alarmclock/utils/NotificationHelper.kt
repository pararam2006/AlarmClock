package com.pararam2006.alarmclock.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.content.ContextCompat.getSystemService

class NotificationHelper(context: Context) {
    /*TODO Создание каналов уведомлений и сборки
       самого уведомления будильника
     */
    private val notificationManager =
        getSystemService(context, NotificationManager::class.java) as NotificationManager

    init {
        createChannels()
    }

    private fun createChannels() {
        val defaultChannel = NotificationChannel(
            "default alarms",
            "Обычные будильники",
            NotificationManager.IMPORTANCE_HIGH
        )

        notificationManager.createNotificationChannel(defaultChannel)
    }

    fun showNotification() {

    }
}