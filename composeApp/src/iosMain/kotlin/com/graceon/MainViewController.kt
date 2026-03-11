package com.graceon

import androidx.compose.ui.window.ComposeUIViewController
import com.graceon.core.common.RewardedAdResult
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

typealias RewardedAdPresenter = (callback: (String, String?) -> Unit) -> Unit

fun MainViewController(
    apiBaseUrl: String,
    supabaseAnonKey: String,
    appVersion: String,
    onShareText: (String) -> Unit,
    onToggleDailyVerseNotification: (Boolean) -> Unit,
    onOpenUrl: (String) -> Unit,
    onShowRewardedAd: RewardedAdPresenter
) = ComposeUIViewController {
    App(
        apiBaseUrl = apiBaseUrl,
        supabaseAnonKey = supabaseAnonKey,
        appVersion = appVersion,
        onShareText = onShareText,
        onToggleDailyVerseNotification = onToggleDailyVerseNotification,
        onOpenUrl = onOpenUrl,
        onShowRewardedAd = {
            suspendCancellableCoroutine { continuation ->
                onShowRewardedAd { status, message ->
                    val result = when (status) {
                        "earned" -> RewardedAdResult.RewardEarned
                        "dismissed" -> RewardedAdResult.Dismissed
                        else -> RewardedAdResult.Failed(
                            message ?: "광고를 표시하지 못했습니다. 잠시 후 다시 시도해주세요."
                        )
                    }
                    continuation.resume(result)
                }
            }
        }
    )
}
