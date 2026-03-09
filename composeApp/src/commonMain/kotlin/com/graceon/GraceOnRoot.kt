package com.graceon

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.graceon.core.ui.theme.GraceOnTheme
import com.graceon.navigation.NavGraph
import com.graceon.navigation.Screen
import kotlinx.coroutines.launch

@Composable
internal fun GraceOnRoot(
    dependencies: GraceOnDependencies,
    appVersion: String,
    onShareText: (String) -> Unit = {},
    onToggleDailyVerseNotification: (Boolean) -> Unit = {}
) {
    val isAuthenticated by dependencies.authPreferences
        .isAuthenticated
        .collectAsState(initial = null)
    val isDailyVerseNotificationEnabled by dependencies.notificationPreferences
        .isDailyVerseNotificationEnabled
        .collectAsState(initial = false)
    val isDarkThemeEnabled by dependencies.themePreferences
        .isDarkThemeEnabled
        .collectAsState(initial = true)

    GraceOnTheme(darkTheme = isDarkThemeEnabled) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            when (isAuthenticated) {
                null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                else -> {
                    val coroutineScope = rememberCoroutineScope()
                    val startDestination = if (isAuthenticated == true) {
                        Screen.WORRY
                    } else {
                        Screen.LOGIN
                    }

                    NavGraph(
                        startDestination = startDestination,
                        dependencies = dependencies,
                        appVersion = appVersion,
                        onShareText = onShareText,
                        isDailyVerseNotificationEnabled = isDailyVerseNotificationEnabled,
                        isDarkThemeEnabled = isDarkThemeEnabled,
                        onToggleDailyVerseNotification = { enabled ->
                            coroutineScope.launch {
                                dependencies.notificationPreferences.setDailyVerseNotificationEnabled(enabled)
                            }
                            onToggleDailyVerseNotification(enabled)
                        },
                        onToggleDarkTheme = { enabled ->
                            coroutineScope.launch {
                                dependencies.themePreferences.setDarkThemeEnabled(enabled)
                            }
                        },
                        onLoginComplete = {
                            coroutineScope.launch {
                                dependencies.authPreferences.setAuthenticated()
                            }
                        },
                        onLogout = {
                            coroutineScope.launch {
                                dependencies.logout()
                            }
                        }
                    )
                }
            }
        }
    }
}
