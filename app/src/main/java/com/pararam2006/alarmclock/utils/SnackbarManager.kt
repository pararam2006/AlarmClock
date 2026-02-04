package com.pararam2006.alarmclock.utils

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

data class SnackbarMessage(
    val message: String,
    val actionLabel: String? = null,
    val onAction: (() -> Unit)? = null
)

class SnackbarManager {
    private val _messages = MutableSharedFlow<SnackbarMessage>()
    val messages = _messages.asSharedFlow()

    suspend fun showMessage(message: String) {
        _messages.emit(SnackbarMessage(message))
    }

    suspend fun showMessageWithAction(message: String, actionLabel: String, onAction: () -> Unit) {
        _messages.emit(SnackbarMessage(message, actionLabel, onAction))
    }
}