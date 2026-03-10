package com.graceon.ads

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import com.graceon.BuildConfig
import com.graceon.core.common.RewardedAdResult
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

internal object AndroidRewardedAdManager {
    private var rewardedAd: RewardedAd? = null
    private var isLoading = false

    suspend fun show(context: Context): RewardedAdResult {
        val activity = context.findActivity()
            ?: return RewardedAdResult.Failed("광고를 표시할 화면을 찾지 못했습니다.")

        return suspendCancellableCoroutine { continuation ->
            fun loadAndShow() {
                val adUnitId = BuildConfig.ADMOB_REWARDED_AD_UNIT_ID
                if (adUnitId.isBlank()) {
                    continuation.resume(RewardedAdResult.Failed("리워드 광고 설정이 비어 있습니다."))
                    return
                }

                isLoading = true
                RewardedAd.load(
                    activity,
                    adUnitId,
                    AdRequest.Builder().build(),
                    object : RewardedAdLoadCallback() {
                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                            rewardedAd = null
                            isLoading = false
                            continuation.resume(
                                RewardedAdResult.Failed("광고를 불러오지 못했습니다. 잠시 후 다시 시도해주세요.")
                            )
                        }

                        override fun onAdLoaded(loadedAd: RewardedAd) {
                            rewardedAd = loadedAd
                            isLoading = false
                            showLoadedAd(activity, loadedAd, continuation::resume)
                        }
                    }
                )
            }

            val currentAd = rewardedAd
            if (currentAd != null) {
                showLoadedAd(activity, currentAd, continuation::resume)
            } else if (!isLoading) {
                loadAndShow()
            } else {
                continuation.resume(RewardedAdResult.Failed("광고를 준비 중입니다. 잠시 후 다시 시도해주세요."))
            }
        }
    }

    private fun showLoadedAd(
        activity: Activity,
        ad: RewardedAd,
        resume: (RewardedAdResult) -> Unit
    ) {
        var rewardEarned = false
        rewardedAd = null
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                preload(activity)
                resume(if (rewardEarned) RewardedAdResult.RewardEarned else RewardedAdResult.Dismissed)
            }

            override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                preload(activity)
                resume(RewardedAdResult.Failed("광고를 표시하지 못했습니다. 잠시 후 다시 시도해주세요."))
            }
        }

        ad.show(activity) {
            rewardEarned = true
        }
    }

    fun preload(context: Context) {
        if (rewardedAd != null || isLoading) return
        val activity = context.findActivity() ?: return
        isLoading = true
        RewardedAd.load(
            activity,
            BuildConfig.ADMOB_REWARDED_AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    rewardedAd = null
                    isLoading = false
                }

                override fun onAdLoaded(loadedAd: RewardedAd) {
                    rewardedAd = loadedAd
                    isLoading = false
                }
            }
        )
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
