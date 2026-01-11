package com.pararam2006.alarmclock.core.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Route {
    @Serializable
    object Main : Route()

    @Serializable
    object Creation : Route()

    @Serializable
    object Redacting : Route()
}