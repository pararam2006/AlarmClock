package com.pararam2006.alarmclock.ui.redacting

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.pararam2006.alarmclock.R
import com.pararam2006.alarmclock.domain.model.Alarm
import com.pararam2006.alarmclock.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RedactingScreen(
    modifier: Modifier = Modifier,
    onNavigateToMain: () -> Unit,
    onAlarmUpdate: (Int, Int) -> Unit,
    alarm: Alarm,
) {
    val timePickerState = rememberTimePickerState(
        initialHour = alarm.hour,
        initialMinute = alarm.minute,
        is24Hour = true,
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
                    if (timePickerState.hour != alarm.hour || timePickerState.minute != alarm.minute) {
                        onAlarmUpdate(timePickerState.hour, timePickerState.minute)
                        onNavigateToMain()
                    } else {
                        onNavigateToMain()
                    }
                } catch (e: Exception) {
                    Log.d("RedactingScreen", e.message.toString())
                }
            }
        ) { 
            Text(
                text = stringResource(R.string.redacting_screen_redacting_button_text),
                style = MaterialTheme.typography.labelLarge
            ) 
        }
    }
}
