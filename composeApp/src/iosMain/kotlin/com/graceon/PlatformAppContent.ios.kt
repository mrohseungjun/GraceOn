package com.graceon

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.graceon.core.common.RewardedAdResult
import com.graceon.update.AppPlatform

@Composable
internal actual fun PlatformAppContent(
    apiBaseUrl: String,
    supabaseAnonKey: String,
    appVersion: String,
    onShareText: (String) -> Unit,
    onToggleDailyVerseNotification: (Boolean) -> Unit,
    onOpenUrl: (String) -> Unit,
    onShowRewardedAd: suspend () -> RewardedAdResult,
    onInlineAdPlacementChanged: (String?) -> Unit
) {
    val dependencies = remember(apiBaseUrl, supabaseAnonKey, onOpenUrl) {
        createGraceOnIosDependencies(
            apiBaseUrl = apiBaseUrl,
            supabaseAnonKey = supabaseAnonKey,
            openUrl = onOpenUrl
        )
    }
    GraceOnRoot(
        dependencies = dependencies,
        appPlatform = AppPlatform.Ios,
        appVersion = appVersion,
        storeUrl = IOS_APP_STORE_URL.ifBlank { "https://apps.apple.com/kr/search?term=GraceOn" },
        onShareText = onShareText,
        onOpenUrl = onOpenUrl,
        onToggleDailyVerseNotification = onToggleDailyVerseNotification,
        onShowRewardedAd = onShowRewardedAd,
        onInlineAdPlacementChanged = onInlineAdPlacementChanged
    )
}

private val IOS_APP_STORE_URL: String
    get() = platform.Foundation.NSBundle.mainBundle.objectForInfoDictionaryKey("IOS_APP_STORE_URL") as? String ?: ""
