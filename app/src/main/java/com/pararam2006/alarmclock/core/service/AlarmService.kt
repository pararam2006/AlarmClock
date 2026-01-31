package com.pararam2006.alarmclock.core.service

import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import com.pararam2006.alarmclock.utils.NotificationHelper
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AlarmService : Service(), KoinComponent {
    private val notificationHelper: NotificationHelper by inject()
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
        Log.e(TAG, "CoroutineExceptionHandler got $exception")
    }
    private val serviceScope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO + coroutineExceptionHandler)
    private val TAG = "AlarmService"

    companion object {
        const val CHANNEL_ID = "alarm_service_channel"
        const val NOTIFICATION_ID = 1001
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        notificationHelper.createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val alarmId = intent?.getLongExtra("ALARM_ID", -1L) ?: -1L

        startForeground(NOTIFICATION_ID, notificationHelper.createNotification(alarmId))
        startAlarm()

        return START_STICKY
    }

    private fun startAlarm() {
        val urisToTry = listOf(
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE),
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        )

        var mpStarted = false
        for (uri in urisToTry) {
            if (uri == null) continue
            try {
                mediaPlayer?.release()
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(applicationContext, uri)
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    isLooping = true
                    prepare()
                    start()
                }
                mpStarted = true
                Log.i(TAG, "Alarm started with URI: $uri")
                break
            } catch (e: Exception) {
                Log.w(TAG, "Failed to play URI $uri: ${e.message}")
            }
        }

        if (!mpStarted) {
            Log.e(TAG, "Could not play any alarm sounds")
        }

        try {
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager =
                    getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(VIBRATOR_SERVICE) as Vibrator
            }
            val pattern = longArrayOf(0, 500, 500)
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } catch (e: Exception) {
            Log.e(TAG, "Error starting vibration: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        vibrator?.cancel()
        serviceScope.cancel()
    }
}
