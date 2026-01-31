package com.pararam2006.alarmclock.ui.ringing

import android.app.KeyguardManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.pararam2006.alarmclock.R
import com.pararam2006.alarmclock.core.service.AlarmService
import com.pararam2006.alarmclock.ui.theme.AlarmClockTheme
import com.pararam2006.alarmclock.ui.theme.Dimens
import com.pararam2006.alarmclock.ui.theme.RingingTimeStyle
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RingingActivity : ComponentActivity() {

    private val viewModel: RingingViewModel by viewModel {
        parametersOf(intent.getLongExtra("ALARM_ID", -1L))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setShowWhenLocked(true)
        setTurnScreenOn(true)

        val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        keyguardManager.requestDismissKeyguard(this, null)

        enableEdgeToEdge()
        setContent {
            AlarmClockTheme {
                RingingScreen(
                    onDismiss = {
                        viewModel.cancel()
                        stopAlarmService()
                        finish()
                    },
                    onSnooze = {
                        viewModel.snooze()
                        stopAlarmService()
                        finish()
                    }
                )
            }
        }
    }

    @Composable
    private fun RingingScreen(
        onDismiss: () -> Unit,
        onSnooze: () -> Unit
    ) {
        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Dimens.paddingExtraLarge),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = Dimens.paddingHuge)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(Dimens.iconSizeLarge),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(Dimens.spacingLarge))
                    Text(
                        text = currentTime,
                        style = RingingTimeStyle,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = stringResource(R.string.ringing_screen_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(Dimens.spacingMedium)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimens.buttonHeight),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.ringing_screen_shutdown_button_text),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    Button(
                        onClick = onSnooze,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimens.buttonHeight),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.ringing_screen_snooze_button_text),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }

    private fun stopAlarmService() {
        val serviceIntent = Intent(this, AlarmService::class.java)
        stopService(serviceIntent)
    }
}
