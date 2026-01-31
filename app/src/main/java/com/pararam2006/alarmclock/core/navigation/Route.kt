package com.pararam2006.alarmclock.core.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Route {
    @Serializable
    data object Permissions : Route()

    @Serializable
    data object Main : Route()

    @Serializable
    data object Creation : Route()

    @Serializable
    data class Redacting(
        val alarmId: Long,
    ) : Route()
}
