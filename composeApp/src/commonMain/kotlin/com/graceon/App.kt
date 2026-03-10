package com.graceon

import androidx.compose.runtime.Composable
import com.graceon.core.common.RewardedAdResult

@Composable
fun App(
    apiBaseUrl: String = "",
    supabaseAnonKey: String = "",
    appVersion: String = "",
    onShareText: (String) -> Unit = {},
    onToggleDailyVerseNotification: (Boolean) -> Unit = {},
    onOpenUrl: (String) -> Unit = {},
    onShowRewardedAd: suspend () -> RewardedAdResult = { RewardedAdResult.Failed("리워드 광고를 사용할 수 없습니다.") }
) {
    PlatformAppContent(
        apiBaseUrl = apiBaseUrl,
        supabaseAnonKey = supabaseAnonKey,
        appVersion = appVersion,
        onShareText = onShareText,
        onToggleDailyVerseNotification = onToggleDailyVerseNotification,
        onOpenUrl = onOpenUrl,
        onShowRewardedAd = onShowRewardedAd
    )
}

@Composable
internal expect fun PlatformAppContent(
    apiBaseUrl: String,
    supabaseAnonKey: String,
    appVersion: String,
    onShareText: (String) -> Unit,
    onToggleDailyVerseNotification: (Boolean) -> Unit,
    onOpenUrl: (String) -> Unit,
    onShowRewardedAd: suspend () -> RewardedAdResult
)
