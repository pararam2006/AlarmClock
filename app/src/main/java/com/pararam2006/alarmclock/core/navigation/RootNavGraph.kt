package com.pararam2006.alarmclock.core.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pararam2006.alarmclock.ui.creation.CreationScreen
import com.pararam2006.alarmclock.ui.main.MainScreen
import com.pararam2006.alarmclock.ui.redacting.RedactingScreen
import com.pararam2006.alarmclock.ui.shared.AlarmViewModel
import com.pararam2006.alarmclock.utils.SnackbarManager
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootNavGraph() {
    val navController = rememberNavController()
    val vm: AlarmViewModel = koinViewModel()
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val title = when {
        currentDestination?.hasRoute<Route.Main>() == true -> "Мои будильники"
        currentDestination?.hasRoute<Route.Creation>() == true -> "Новый будильник"
        currentDestination?.hasRoute<Route.Redacting>() == true -> "Редактирование"
        else -> "Будильник"
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarManager: SnackbarManager = koinInject()

    LaunchedEffect(Unit) {
        snackbarManager.messages.collect { message ->
            snackbarHostState.showSnackbar(
                message = message,
                actionLabel = "OK"
            )
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(title) }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            when {
                currentDestination?.hasRoute<Route.Main>() == true -> {
                    FloatingActionButton(onClick = { navController.navigate(Route.Creation) }) {
                        Icon(Icons.Default.Add, contentDescription = "Добавить")
                    }
                }

                currentDestination?.hasRoute<Route.Creation>() == true -> {
                    FloatingActionButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController,
            startDestination = Route.Main,
        ) {
            composable<Route.Main> {
                MainScreen(
                    uiState = uiState,
                    onEnabledChange = { alarm -> vm.toggleAlarm(alarm) },
                    onDeleteAlarm = { alarm -> vm.deleteAlarm(alarm) },
                    modifier = Modifier.padding(innerPadding),
                )
            }

            composable<Route.Creation> {
                CreationScreen(
                    uiState = uiState,
                    onAlarmCreation = { hour, minute ->
                        vm.createAlarm(hour, minute)
                    },
                    onNavigateToMain = { navController.popBackStack() },
                    onTimePickerStateChange = { hour, minute ->
                        vm.onTimeChanged(hour = hour, minute = minute)
                    },
                    modifier = Modifier.padding(innerPadding),
                )
            }

            composable<Route.Redacting> {
                RedactingScreen()
            }
        }
    }
}
