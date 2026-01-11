package com.pararam2006.alarmclock.ui.creation

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.pararam2006.alarmclock.ui.shared.AlarmUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreationScreen(
    uiState: AlarmUiState,
    onAlarmCreation: (Int, Int) -> Unit,
    onNavigateToMain: () -> Unit,
    onTimePickerStateChange: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val timePickerState = rememberTimePickerState(is24Hour = true)

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize(),
    ) {
        TimePicker(state = timePickerState)

        LaunchedEffect(timePickerState.hour, timePickerState.minute) {
            onTimePickerStateChange(timePickerState.hour, timePickerState.minute)
        }

        Button(
            onClick = {
                try {
                    onAlarmCreation(timePickerState.hour, timePickerState.minute)
                    onNavigateToMain()
                } catch (e: Exception) {
                    Log.d("CreationScreen", e.message.toString())
                }
            }
        ) { Text("Создать будильник") }
    }
}