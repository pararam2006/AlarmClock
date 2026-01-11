package com.pararam2006.alarmclock.ui.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pararam2006.alarmclock.domain.model.Alarm
import com.pararam2006.alarmclock.ui.shared.AlarmUiState
import com.pararam2006.alarmclock.ui.theme.AlarmClockTheme
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    uiState: AlarmUiState,
    onEnabledChange: (Alarm) -> Unit,
    onDeleteAlarm: (Alarm) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        if (uiState.alarms.isEmpty()) {
            Text(
                text = "У вас пока нет будильников",
                fontSize = 18.sp
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .padding(horizontal = 16.dp)
            ) {
                items(uiState.alarms, key = { it.id }) { alarm ->
                    AlarmCard(
                        alarm = alarm,
                        onToggleAlarm = { onEnabledChange(alarm) },
                        onDeleteAlarm = { onDeleteAlarm(alarm) },
                    )
                }
            }
        }
    }
}

@Composable
private fun AlarmCard(
    alarm: Alarm,
    onToggleAlarm: (Alarm) -> Unit,
    onDeleteAlarm: (Alarm) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.clickable {
            onDeleteAlarm(alarm)
        }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(all = 16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceAround,
            ) {
                Text(
                    text = String.format(
                        Locale.getDefault(),
                        "%02d:%02d",
                        alarm.hour,
                        alarm.minute
                    ),
                    fontSize = 32.sp,
                )
                Text(
                    text = "Дни повтора",
                    fontSize = 16.sp,
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Switch(
                checked = alarm.isEnabled,
                onCheckedChange = { onToggleAlarm(alarm) }
            )
        }
    }
}


@Preview
@Composable
private fun AlarmCardPreview() {
    AlarmClockTheme {
        AlarmCard(
            alarm = Alarm(
                id = 0,
                hour = 7,
                minute = 5,
                isEnabled = true,
                daysOfWeek = emptyList()
            ),
            onToggleAlarm = {},
            onDeleteAlarm = {},
        )
    }
}