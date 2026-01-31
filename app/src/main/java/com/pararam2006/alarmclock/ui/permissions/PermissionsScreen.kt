package com.pararam2006.alarmclock.ui.permissions

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.pararam2006.alarmclock.R
import com.pararam2006.alarmclock.domain.model.PermissionRequirement
import com.pararam2006.alarmclock.ui.theme.AlarmClockTheme
import com.pararam2006.alarmclock.ui.theme.Dimens

@Composable
fun PermissionsScreen(
    requirements: List<PermissionRequirement>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Dimens.paddingLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.permissions_screen_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = Dimens.paddingMedium)
        )

        Text(
            text = stringResource(R.string.permissions_screen_list_title),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = Dimens.paddingLarge)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingMedium)
        ) {
            items(requirements) { requirement ->
                PermissionItem(requirement = requirement)
                HorizontalDivider(modifier = Modifier.padding(top = Dimens.paddingIntermediate))
            }
        }

        Text(
            text = stringResource(R.string.permissions_screen_warning),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(top = Dimens.paddingMedium)
        )
    }
}

@Composable
private fun PermissionItem(requirement: PermissionRequirement) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = requirement.title,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = requirement.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }

        if (requirement.isGranted) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = stringResource(R.string.permissions_screen_check_icon_desc),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = Dimens.paddingMedium)
            )
        } else {
            Button(
                onClick = requirement.onAction,
                modifier = Modifier.padding(start = Dimens.paddingSmall)
            ) {
                Text(
                    text = stringResource(R.string.permissions_screen_give_button_text),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PermissionsScreenPreview() {
    AlarmClockTheme {
        PermissionsScreen(
            requirements = listOf(
                PermissionRequirement(
                    id = "notifications",
                    title = "Уведомления",
                    description = "Чтобы видеть активный будильник в шторке",
                    onAction = {},
                    minSdk = Build.VERSION_CODES.TIRAMISU,
                    isGranted = false
                ),
                PermissionRequirement(
                    id = "exact_alarm",
                    title = "Точные будильники",
                    description = "Гарантирует срабатывание вовремя.",
                    onAction = {},
                    minSdk = Build.VERSION_CODES.S,
                    isGranted = true
                ),
                PermissionRequirement(
                    id = "battery",
                    title = "Работа в фоне",
                    description = "Чтобы будильник не 'засыпал' ночью.",
                    onAction = {},
                    isGranted = true
                ),
            ),
        )
    }
}
