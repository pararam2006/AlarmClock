package com.pararam2006.alarmclock.core.navigation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.pararam2006.alarmclock.R
import com.pararam2006.alarmclock.ui.creation.CreationScreen
import com.pararam2006.alarmclock.ui.main.MainScreen
import com.pararam2006.alarmclock.ui.permissions.PermissionsScreen
import com.pararam2006.alarmclock.ui.redacting.RedactingScreen
import com.pararam2006.alarmclock.ui.shared.AlarmViewModel
import com.pararam2006.alarmclock.utils.PermissionManager
import com.pararam2006.alarmclock.utils.SnackbarManager
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootNavGraph() {
    val permissionManager: PermissionManager = koinInject()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        permissionManager.refresh()
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        permissionManager.refresh()
    }

    LaunchedEffect(launcher) {
        permissionManager.updateNotificationAction { permission ->
            launcher.launch(permission)
        }
    }

    val requirements by permissionManager.requirements.collectAsStateWithLifecycle()

    val isAllGranted by remember {
        derivedStateOf {
            requirements.isNotEmpty() && requirements.filter { it.id != "battery" }
                .all { it.isGranted }
        }
    }

    if (!isAllGranted) {
        PermissionsScreen(
            requirements = requirements,
        )
    } else {
        AppMainContent()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppMainContent() {
    val navController = rememberNavController()
    val vm: AlarmViewModel = koinViewModel()
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val snackbarManager: SnackbarManager = koinInject()
    val snackbarHostState = remember { SnackbarHostState() }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val snackbarActionLabel = stringResource(R.string.snackbar_action_label)

    LaunchedEffect(Unit) {
        snackbarManager.messages.collect { message ->
            snackbarHostState.showSnackbar(
                message = message,
                actionLabel = snackbarActionLabel
            )
        }
    }

    val title = when {
        currentDestination?.hasRoute<Route.Main>() == true -> stringResource(R.string.main_screen_title)
        currentDestination?.hasRoute<Route.Creation>() == true -> stringResource(R.string.creation_screen_title)
        currentDestination?.hasRoute<Route.Redacting>() == true -> stringResource(R.string.redacting_screen_title)
        else -> stringResource(R.string.unkown_screen_title)
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(title) }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            when {
                currentDestination?.hasRoute<Route.Main>() == true -> {
                    FloatingActionButton(onClick = { navController.navigate(Route.Creation) }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.main_screen_fab_desc)
                        )
                    }
                }

                currentDestination?.hasRoute<Route.Creation>() == true -> {
                    FloatingActionButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.creation_screen_fab_desc)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Route.Main,
        ) {
            composable<Route.Main> {
                MainScreen(
                    uiState = uiState,
                    onEnabledChange = { alarm -> vm.toggleAlarm(alarm) },
                    onDeleteAlarm = { alarm -> vm.deleteAlarm(alarm) },
                    modifier = Modifier.padding(innerPadding),
                    onNavigateToRedacting = { alarm ->
                        navController.navigate(Route.Redacting(alarm.id))
                    },
                )
            }

            composable<Route.Creation> {
                CreationScreen(
                    onAlarmCreation = { hour, minute -> vm.createAlarm(hour, minute) },
                    onNavigateToMain = { navController.popBackStack() },
                    modifier = Modifier.padding(innerPadding),
                )
            }

            composable<Route.Redacting> { backStackEntry ->
                val route: Route.Redacting = backStackEntry.toRoute()
                val alarm = uiState.alarms.find { it.id == route.alarmId }

                if (alarm != null) {
                    RedactingScreen(
                        onNavigateToMain = {
                            navController.popBackStack()
                        },
                        onAlarmUpdate = { hour, minute ->
                            vm.updateAlarm(alarm, hour, minute)
                        },
                        alarm = alarm,
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }
}
