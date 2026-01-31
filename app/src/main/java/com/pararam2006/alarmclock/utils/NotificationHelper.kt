package com.pararam2006.alarmclock.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.pararam2006.alarmclock.R
import com.pararam2006.alarmclock.core.service.AlarmService.Companion.CHANNEL_ID
import com.pararam2006.alarmclock.ui.ringing.RingingActivity

class NotificationHelper(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun createNotification(alarmId: Long): Notification {
        val fullScreenIntent = Intent(context, RingingActivity::class.java).apply {
            putExtra("ALARM_ID", alarmId)
            flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            alarmId.toInt(),
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Будильник!")
            .setContentText("Пора просыпаться")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        // Проверка разрешения именно на Android 14+ через NotificationManager
        val canUseFullScreen = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            notificationManager.canUseFullScreenIntent()
        } else {
            true // До Android 14 разрешение дается при установке
        }

        if (canUseFullScreen) {
            builder.setFullScreenIntent(fullScreenPendingIntent, true)
        }

        return builder.build()
    }

    fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            "Alarm Service Channel",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Channel for Alarm Clock"
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        notificationManager.createNotificationChannel(serviceChannel)
    }
}
