package com.pararam2006.alarmclock.domain.model

import android.os.Build

data class PermissionRequirement(
    val id: String,
    val title: String,
    val description: String,
    val permission: String? = null,
    val onAction: () -> Unit,
    val minSdk: Int = Build.VERSION_CODES.BASE,
    val isGranted: Boolean = false
)
