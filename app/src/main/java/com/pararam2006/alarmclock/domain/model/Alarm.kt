package com.pararam2006.alarmclock.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val hour: Int,
    val minute: Int,
    val isEnabled: Boolean,
    val isDeleted: Boolean = false,
    val daysOfWeek: List<Int>,
)
