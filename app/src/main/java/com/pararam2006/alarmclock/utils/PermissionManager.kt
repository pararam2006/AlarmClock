package com.pararam2006.alarmclock.utils

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.AppOpsManager
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.os.Process
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.pararam2006.alarmclock.domain.model.PermissionRequirement
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PermissionManager(
    private val context: Context,
    private val scheduler: AlarmManagerScheduler
) {
    private var notificationLauncher: ((String) -> Unit)? = null
    private var notificationRequestCount = 0

    private val _requirements = MutableStateFlow<List<PermissionRequirement>>(emptyList())
    val requirements: StateFlow<List<PermissionRequirement>> = _requirements.asStateFlow()

    init {
        refresh(context)
    }

    fun refresh(callerContext: Context = context) {
        _requirements.value = fetchRequirements(callerContext)
    }

    fun updateNotificationAction(onStandardRequest: (String) -> Unit) {
        notificationLauncher = onStandardRequest
        refresh(context)
    }

    private fun fetchRequirements(callerContext: Context): List<PermissionRequirement> {
        val list = mutableListOf<PermissionRequirement>()

        // Уведомления
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            val granted = ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
            list.add(
                PermissionRequirement(
                    id = "notifications",
                    title = "Уведомления",
                    description = "Чтобы видеть активный будильник в шторке",
                    permission = permission,
                    onAction = {
                        if (granted) scheduler.openNotificationSettings()
                        else {
                            val activity = callerContext.findActivity()
                            val shouldShowRationale = activity?.let {
                                ActivityCompat.shouldShowRequestPermissionRationale(
                                    it,
                                    permission
                                )
                            } ?: false
                            if (shouldShowRationale || notificationRequestCount == 0) {
                                notificationRequestCount++
                                notificationLauncher?.invoke(permission)
                            } else scheduler.openNotificationSettings()
                        }
                    },
                    minSdk = Build.VERSION_CODES.TIRAMISU,
                    isGranted = granted
                )
            )
        }

        // Точные будильники
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            list.add(
                PermissionRequirement(
                    id = "exact_alarm",
                    title = "Точные будильники",
                    description = "Гарантирует срабатывание вовремя.",
                    onAction = { scheduler.requestExactAlarmPermission() },
                    minSdk = Build.VERSION_CODES.S,
                    isGranted = alarmManager.canScheduleExactAlarms()
                )
            )
        }

        // 3. Батарея
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        list.add(
            PermissionRequirement(
                id = "battery",
                title = "Работа в фоне",
                description = "Чтобы будильник не 'засыпал' ночью.",
                onAction = { scheduler.requestIgnoreBatteryOptimizations() },
                isGranted = powerManager.isIgnoringBatteryOptimizations(context.packageName)
            )
        )

        // Специфичные для Xiaomi (MIUI)
        if (Build.MANUFACTURER.equals("Xiaomi", ignoreCase = true)) {
            // Экран блокировки (10020)
            list.add(
                PermissionRequirement(
                    id = "miui_lock_screen",
                    title = "Экран блокировки (Xiaomi)",
                    description = "Разрешите 'Экран блокировки'",
                    onAction = { scheduler.openXiaomiSpecificSettings() },
                    isGranted = checkMiuiOp(10020)
                )
            )

            // Запуск в фоне / Всплывающие окна (10021)
            list.add(
                PermissionRequirement(
                    id = "miui_bg_start",
                    title = "Запуск из фона (Xiaomi)",
                    description = "Разрешите 'Отображать всплывающие окна, когда приложение запущено в фоновом режиме'",
                    onAction = { scheduler.openXiaomiSpecificSettings() },
                    isGranted = checkMiuiOp(10021)
                )
            )
        }

        return list
    }

    private fun checkMiuiOp(op: Int): Boolean {
        return try {
            val ops = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val method = ops.javaClass.getMethod(
                "checkOpNoThrow",
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
                String::class.java
            )
            val result = method.invoke(ops, op, Process.myUid(), context.packageName) as Int
            result == AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            false
        }
    }

    fun isAllCriticalGranted(): Boolean {
        val current = fetchRequirements(context)
        return current.filter { it.id != "battery" }.all { it.isGranted }
    }

    private fun Context.findActivity(): Activity? {
        var context = this
        while (context is ContextWrapper) {
            if (context is Activity) return context
            context = context.baseContext
        }
        return null
    }
}
