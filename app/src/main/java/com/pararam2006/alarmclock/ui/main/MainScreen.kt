package com.pararam2006.alarmclock.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pararam2006.alarmclock.R
import com.pararam2006.alarmclock.domain.model.Alarm
import com.pararam2006.alarmclock.ui.shared.AlarmUiState
import com.pararam2006.alarmclock.ui.theme.AlarmClockTheme
import com.pararam2006.alarmclock.ui.theme.Dimens
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    uiState: AlarmUiState,
    onEnabledChange: (Alarm) -> Unit,
    onDeleteAlarmRequest: (Alarm) -> Unit,
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
                            onLongClick = {
                                onDeleteAlarmRequest(alarm)
                            },
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
    val daysString = remember(alarm.daysOfWeek) {
        if (alarm.daysOfWeek.isEmpty()) {
            "Без повторов"
        } else if (alarm.daysOfWeek.size == 7) {
            "Ежедневно"
        } else {
            val order = listOf(
                Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
                Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY
            )
            alarm.daysOfWeek.sortedBy { order.indexOf(it) }
                .joinToString(" ") { day ->
                    when (day) {
                        Calendar.MONDAY -> "Пн"
                        Calendar.TUESDAY -> "Вт"
                        Calendar.WEDNESDAY -> "Ср"
                        Calendar.THURSDAY -> "Чт"
                        Calendar.FRIDAY -> "Пт"
                        Calendar.SATURDAY -> "Сб"
                        Calendar.SUNDAY -> "Вс"
                        else -> ""
                    }
                }
        }
    }

    val swipeToDismissBoxState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) {
                onLongClick(alarm)
            }

            it != SwipeToDismissBoxValue.StartToEnd
        }
    )

    SwipeToDismissBox(
        state = swipeToDismissBoxState,
        backgroundContent = {
            when (swipeToDismissBoxState.dismissDirection) {
                SwipeToDismissBoxValue.EndToStart -> {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Удалить",
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Red)
                            .wrapContentSize(Alignment.CenterEnd)
                            .padding(12.dp),
                        tint = Color.White,
                    )
                }

                else -> {}
            }
        },
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
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
                    verticalArrangement = Arrangement.Center,
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
                    Text(
                        text = daysString,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
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
