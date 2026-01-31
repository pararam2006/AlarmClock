package com.pararam2006.alarmclock.core.di

import com.pararam2006.alarmclock.utils.AlarmManagerScheduler
import com.pararam2006.alarmclock.utils.AlarmUtils
import com.pararam2006.alarmclock.utils.NotificationHelper
import com.pararam2006.alarmclock.utils.PermissionManager
import com.pararam2006.alarmclock.utils.SnackbarManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val utilsModule = module {
    single { AlarmManagerScheduler(androidContext()) }
    single { SnackbarManager() }
    single { PermissionManager(
        androidContext(),
        scheduler = get()
    ) }
    single { NotificationHelper(androidContext()) }
    single { AlarmUtils() }
}