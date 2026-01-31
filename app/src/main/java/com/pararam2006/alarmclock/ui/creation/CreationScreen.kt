package com.pararam2006.alarmclock.ui.creation

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.pararam2006.alarmclock.R
import com.pararam2006.alarmclock.ui.theme.Dimens
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreationScreen(
    onAlarmCreation: (Int, Int) -> Unit,
    onNavigateToMain: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timePickerState = rememberTimePickerState(
        initialHour = LocalDateTime.now().hour,
        initialMinute = LocalDateTime.now().minute + 1,
        is24Hour = true
    )

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize(),
    ) {
        TimePicker(state = timePickerState)

        Spacer(modifier = Modifier.height(Dimens.spacingLarge))

        Button(
            onClick = {
                try {
                    onAlarmCreation(timePickerState.hour, timePickerState.minute)
                    onNavigateToMain()
                } catch (e: Exception) {
                    Log.d("CreationScreen", e.message.toString())
                }
            }
        ) { Text(text = stringResource(R.string.creation_screen_create_button_text)) }
    }
}
