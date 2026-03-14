package com.graceon

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.graceon.ads.AndroidRewardedAdManager
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
    val context = LocalContext.current
    val dependencies = remember(context) {
        createGraceOnAndroidDependencies(
            context = context,
            apiBaseUrl = apiBaseUrl.ifBlank { BuildConfig.GRACEON_API_BASE_URL },
            supabaseAnonKey = supabaseAnonKey.ifBlank { BuildConfig.SUPABASE_ANON_KEY }
        )
    }

    LaunchedEffect(context) {
        AndroidRewardedAdManager.preload(context)
    }

    GraceOnRoot(
        dependencies = dependencies,
        appPlatform = AppPlatform.Android,
        appVersion = appVersion.ifBlank { BuildConfig.VERSION_NAME },
        storeUrl = BuildConfig.ANDROID_APP_STORE_URL,
        onShareText = { text ->
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, text)
                type = "text/plain"
            }
            val chooser = Intent.createChooser(sendIntent, "말씀 공유하기")
            context.startActivity(chooser)
        },
        onOpenUrl = { url ->
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        },
        onToggleDailyVerseNotification = { enabled ->
            DailyVerseNotificationScheduler.update(
                context = context,
                enabled = enabled
            )
        },
        onShowRewardedAd = {
            AndroidRewardedAdManager.show(context)
        },
        onInlineAdPlacementChanged = onInlineAdPlacementChanged
    )
}
