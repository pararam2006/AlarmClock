package com.pararam2006.alarmclock.ui.redacting

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.pararam2006.alarmclock.R
import com.pararam2006.alarmclock.domain.model.Alarm
import com.pararam2006.alarmclock.ui.theme.Dimens
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RedactingScreen(
    modifier: Modifier = Modifier,
    onNavigateToMain: () -> Unit,
    onAlarmUpdate: (Int, Int, List<Int>) -> Unit,
    alarm: Alarm,
) {
    val timePickerState = rememberTimePickerState(
        initialHour = alarm.hour,
        initialMinute = alarm.minute,
        is24Hour = true,
    )

    val selectedDays = remember { 
        mutableStateListOf<Int>().apply { addAll(alarm.daysOfWeek) } 
    }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize(),
    ) {
        TimePicker(state = timePickerState)

        Spacer(modifier = Modifier.height(Dimens.spacingLarge))

        DaysOfWeekPeeker(
            selectedDays = selectedDays,
            onDayClick = { day ->
                if (selectedDays.contains(day)) {
                    selectedDays.remove(day)
                } else {
                    selectedDays.add(day)
                }
            },
            modifier = Modifier.padding(horizontal = Dimens.paddingMedium)
        )

        Spacer(modifier = Modifier.height(Dimens.spacingLarge))

        Button(
            onClick = {
                try {
                    onAlarmUpdate(timePickerState.hour, timePickerState.minute, selectedDays.toList())
                    onNavigateToMain()
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

@Composable
private fun DaysOfWeekPeeker(
    selectedDays: List<Int>,
    onDayClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val days = listOf(
        Calendar.MONDAY,
        Calendar.TUESDAY,
        Calendar.WEDNESDAY,
        Calendar.THURSDAY,
        Calendar.FRIDAY,
        Calendar.SATURDAY,
        Calendar.SUNDAY
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        days.forEach { day ->
            val isSelected = selectedDays.contains(day)
            val dayName = when (day) {
                Calendar.MONDAY -> "Пн"
                Calendar.TUESDAY -> "Вт"
                Calendar.WEDNESDAY -> "Ср"
                Calendar.THURSDAY -> "Чт"
                Calendar.FRIDAY -> "Пт"
                Calendar.SATURDAY -> "Сб"
                Calendar.SUNDAY -> "Вс"
                else -> ""
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary 
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .clickable { onDayClick(day) }
            ) {
                Text(
                    text = dayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary 
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
