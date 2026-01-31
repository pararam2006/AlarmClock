package com.pararam2006.alarmclock.core.di

import com.pararam2006.alarmclock.ui.ringing.RingingViewModel
import com.pararam2006.alarmclock.ui.shared.AlarmViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { AlarmViewModel(get()) }
    viewModel { (alarmId: Long) -> RingingViewModel(alarmId, get()) }
}
