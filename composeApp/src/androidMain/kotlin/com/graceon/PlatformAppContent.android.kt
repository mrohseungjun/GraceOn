package com.graceon

import android.content.Intent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.graceon.ads.AndroidRewardedAdManager
import com.graceon.core.common.RewardedAdResult

@Composable
internal actual fun PlatformAppContent(
    apiBaseUrl: String,
    supabaseAnonKey: String,
    appVersion: String,
    onShareText: (String) -> Unit,
    onToggleDailyVerseNotification: (Boolean) -> Unit,
    onOpenUrl: (String) -> Unit,
    onShowRewardedAd: suspend () -> RewardedAdResult
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
        appVersion = appVersion.ifBlank { BuildConfig.VERSION_NAME },
        onShareText = { text ->
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, text)
                type = "text/plain"
            }
            val chooser = Intent.createChooser(sendIntent, "말씀 공유하기")
            context.startActivity(chooser)
        },
        onToggleDailyVerseNotification = { enabled ->
            DailyVerseNotificationScheduler.update(
                context = context,
                enabled = enabled
            )
        },
        onShowRewardedAd = {
            AndroidRewardedAdManager.show(context)
        }
    )
}
