package com.pararam2006.alarmclock.core.di

import androidx.room.Room
import com.pararam2006.alarmclock.data.local.AlarmDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import kotlin.jvm.java

val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AlarmDatabase::class.java,
            "alarm_database"
        ).build()
    }

    single { get<AlarmDatabase>().alarmDao() }
}