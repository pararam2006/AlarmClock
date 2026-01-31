package com.pararam2006.alarmclock.ui.main

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.pararam2006.alarmclock.R
import com.pararam2006.alarmclock.domain.model.Alarm
import com.pararam2006.alarmclock.ui.shared.AlarmUiState
import com.pararam2006.alarmclock.ui.theme.AlarmClockTheme
import com.pararam2006.alarmclock.ui.theme.Dimens
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    uiState: AlarmUiState,
    onEnabledChange: (Alarm) -> Unit,
    onDeleteAlarm: (Alarm) -> Unit,
    onNavigateToRedacting: (Alarm) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (uiState.isLoading) {
        Box(modifier = modifier.fillMaxSize()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    } else {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier.fillMaxSize()
        ) {
            if (uiState.alarms.isEmpty()) {
                Text(
                    text = stringResource(R.string.main_screen_no_alarms_text),
                    style = MaterialTheme.typography.titleLarge
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(Dimens.spacingSmall),
                    modifier = Modifier
                        .padding(horizontal = Dimens.lazyColumnHorizontalPadding)
                ) {
                    items(uiState.alarms, key = { it.id }) { alarm ->
                        AlarmCard(
                            alarm = alarm,
                            onToggle = onEnabledChange,
                            onClick = { onNavigateToRedacting(alarm) },
                            onLongClick = onDeleteAlarm,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AlarmCard(
    alarm: Alarm,
    modifier: Modifier = Modifier,
    onToggle: (Alarm) -> Unit = {},
    onClick: () -> Unit = {},
    onLongClick: (Alarm) -> Unit = {},
) {
    Card(
        modifier = modifier.combinedClickable(
            onClick = onClick,
            onLongClick = { onLongClick(alarm) },
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(all = Dimens.cardPadding)
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
                    style = MaterialTheme.typography.headlineLarge,
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Switch(
                checked = alarm.isEnabled,
                onCheckedChange = { onToggle(alarm) }
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
        )
    }
}
