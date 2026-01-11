package com.pararam2006.alarmclock.core.di

import org.koin.dsl.module

val appModule = module {
    includes(
        repositoryModule,
        viewModelModule,
        utilsModule,
        databaseModule,
    )
}