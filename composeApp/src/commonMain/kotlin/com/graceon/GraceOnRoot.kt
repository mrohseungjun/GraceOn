package com.graceon

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.graceon.core.common.RewardedAdResult
import com.graceon.core.common.Result
import com.graceon.feature.worry.WorryContract
import com.graceon.core.ui.theme.GraceOnTheme
import com.graceon.navigation.NavGraph
import com.graceon.navigation.Screen
import com.graceon.update.AppPlatform
import com.graceon.update.AppUpdatePrompt
import com.graceon.update.AppUpdateType
import com.graceon.update.resolvePrompt
import kotlinx.coroutines.launch

@Composable
internal fun GraceOnRoot(
    dependencies: GraceOnDependencies,
    appPlatform: AppPlatform,
    appVersion: String,
    storeUrl: String,
    onShareText: (String) -> Unit = {},
    onOpenUrl: (String) -> Unit = {},
    onToggleDailyVerseNotification: (Boolean) -> Unit = {},
    onShowRewardedAd: suspend () -> RewardedAdResult = { RewardedAdResult.Failed("리워드 광고를 사용할 수 없습니다.") },
    onInlineAdPlacementChanged: (String?) -> Unit = {}
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
    val resolvedStartDestination = remember { mutableStateOf<String?>(null) }
    val initialDailyUsageState = remember { mutableStateOf<WorryContract.DailyUsageUiState?>(null) }
    val appUpdatePrompt = remember { mutableStateOf<AppUpdatePrompt?>(null) }

    LaunchedEffect(dependencies, appPlatform, appVersion, storeUrl) {
        if (appVersion.isBlank()) return@LaunchedEffect

        when (val result = dependencies.getAppUpdateConfig(appPlatform)) {
            is Result.Success -> {
                appUpdatePrompt.value = result.data.resolvePrompt(
                    currentVersion = appVersion,
                    storeUrl = storeUrl
                )
            }
            is Result.Error, Result.Loading -> Unit
        }
    }

    LaunchedEffect(isAuthenticated) {
        when (isAuthenticated) {
            null -> {
                resolvedStartDestination.value = null
                initialDailyUsageState.value = null
            }

            false -> {
                initialDailyUsageState.value = null
                resolvedStartDestination.value = Screen.LOGIN
            }

            true -> {
                resolvedStartDestination.value = null
                val preloadedUsage = when (val result = dependencies.getDailyFreeUsageUseCase()) {
                    is Result.Success -> WorryContract.DailyUsageUiState(
                        isLoading = false,
                        dailyLimit = result.data.dailyLimit,
                        usedToday = result.data.usedToday,
                        remainingToday = result.data.remainingToday,
                        rewardedCredits = result.data.rewardedCredits,
                        rewardedAvailableToday = result.data.rewardedAvailableToday
                    )
                    else -> WorryContract.DailyUsageUiState(isLoading = false)
                }
                initialDailyUsageState.value = preloadedUsage
                resolvedStartDestination.value = Screen.WORRY
            }
        }
    }

    GraceOnTheme(darkTheme = isDarkThemeEnabled) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            when (val startDestination = resolvedStartDestination.value) {
                null -> {
                    GraceOnStartupScreen()
                }

                else -> {
                    val coroutineScope = rememberCoroutineScope()

                    NavGraph(
                        startDestination = startDestination,
                        initialDailyUsage = initialDailyUsageState.value,
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
                        },
                        onShowRewardedAd = onShowRewardedAd,
                        onInlineAdPlacementChanged = onInlineAdPlacementChanged
                    )
                }
            }
        }

        appUpdatePrompt.value?.let { prompt ->
            AppUpdateDialog(
                prompt = prompt,
                onUpdate = { onOpenUrl(prompt.storeUrl) },
                onDismiss = {
                    if (prompt.type == AppUpdateType.Optional) {
                        appUpdatePrompt.value = null
                    }
                }
            )
        }
    }
}

@Composable
private fun GraceOnStartupScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF05070A)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            Text(
                text = "GraceOn",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.4.sp
                ),
                color = Color(0xFFF8EFE4)
            )
        }
    }
}

@Composable
private fun AppUpdateDialog(
    prompt: AppUpdatePrompt,
    onUpdate: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            if (prompt.type == AppUpdateType.Optional) {
                onDismiss()
            }
        },
        title = {
            Text(text = prompt.title)
        },
        text = {
            Text(
                text = "${prompt.message}\n\n대상 버전: ${prompt.targetVersion}"
            )
        },
        confirmButton = {
            TextButton(onClick = onUpdate) {
                Text("업데이트")
            }
        },
        dismissButton = if (prompt.type == AppUpdateType.Optional) {
            {
                TextButton(onClick = onDismiss) {
                    Text("나중에")
                }
            }
        } else {
            null
        }
    )
}
