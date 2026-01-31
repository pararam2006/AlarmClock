package com.pararam2006.alarmclock.core.di

import com.pararam2006.alarmclock.data.local.repository.AlarmRepository
import org.koin.dsl.module

val repositoryModule = module {
    single { AlarmRepository(get(), get(), get(), get()) }
}
