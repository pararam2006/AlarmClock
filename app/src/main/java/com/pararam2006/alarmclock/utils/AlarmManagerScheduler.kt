package com.pararam2006.alarmclock.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.net.toUri
import com.pararam2006.alarmclock.domain.model.Alarm

class AlarmManagerScheduler(private val context: Context) {
    val TAG = "AlarmManagerScheduler"
    val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(alarmId: Long, timeInMillis: Long) {
        try {
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("ALARM_ID", alarmId)
            }

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

            if (isCanScheduleExactAlarms) {
                manager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent
                )
            } else {
                Log.d("MainScreen", "Нет разрашения на точные будильники")
                openExactAlarmSettings()
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

    // Открывает именно настройки уведомлений конкретно для вашего приложения
    fun openNotificationSettings() {
        val intent =
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun requestOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            "package:${context.packageName}".toUri()
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun requestIgnoreBatteryOptimizations() {
        val intent = Intent(
            Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
            "package:${context.packageName}".toUri()
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun requestExactAlarmPermission() {
        openExactAlarmSettings()
    }

    private fun openExactAlarmSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    fun openXiaomiSpecificSettings() {
        val manufacturer = Build.MANUFACTURER
        if (manufacturer.equals("Xiaomi", ignoreCase = true)) {
            try {
                // Пытаемся открыть специфическое меню разрешений MIUI
                val intent = Intent("miui.intent.action.APP_PERM_EDITOR").apply {
                    setClassName(
                        "com.miui.securitycenter",
                        "com.miui.permcenter.permissions.PermissionsEditorActivity"
                    )
                    putExtra("extra_pkgname", context.packageName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                return // Если успешно открыли, выходим
            } catch (e: Exception) {
                Log.e(TAG, "Не удалось открыть специфические настройки Xiaomi: ${e.message}")
            }
        }

        // Если это не Xiaomi или произошла ошибка, открываем общие настройки приложения
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
