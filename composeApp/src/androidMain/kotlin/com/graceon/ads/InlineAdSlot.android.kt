package com.graceon.ads

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.graceon.BuildConfig
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import android.view.ViewGroup

internal actual object InlineAdSlotRenderer {
    actual val isEnabled: Boolean = true
    private val bannerCache = mutableMapOf<InlineAdPlacement, CachedBanner>()
    actual fun currentLoadState(placement: InlineAdPlacement): InlineAdLoadState =
        bannerCache[placement]?.loadState ?: InlineAdLoadState.Loading

    @Composable
    actual fun Banner(
        placement: InlineAdPlacement,
        modifier: Modifier,
        onLoadStateChanged: (InlineAdLoadState) -> Unit
    ) {
        val context = LocalContext.current
        val currentOnLoadStateChanged = rememberUpdatedState(onLoadStateChanged)
        val adUnitId = when (placement) {
            InlineAdPlacement.HomeFeed -> BuildConfig.ADMOB_HOME_BANNER_AD_UNIT_ID
            InlineAdPlacement.ResultContent -> BuildConfig.ADMOB_RESULT_BANNER_AD_UNIT_ID
        }

        if (adUnitId.isBlank()) return

        val availableWidthDp = (LocalConfiguration.current.screenWidthDp - 40).coerceAtLeast(320)
        val adSize = remember(availableWidthDp) {
            AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, availableWidthDp)
        }
        val cachedBanner = remember(placement, adUnitId, availableWidthDp) {
            getOrCreateBanner(
                context = context,
                placement = placement,
                adUnitId = adUnitId,
                adSize = adSize,
                widthDp = availableWidthDp
            )
        }

        DisposableEffect(cachedBanner) {
            cachedBanner.onLoadStateChanged = { state ->
                currentOnLoadStateChanged.value(state)
            }
            currentOnLoadStateChanged.value(cachedBanner.loadState)

            if (!cachedBanner.hasActiveLoad) {
                cachedBanner.hasActiveLoad = true
                cachedBanner.loadState = InlineAdLoadState.Loading
                currentOnLoadStateChanged.value(InlineAdLoadState.Loading)
                cachedBanner.adView.loadAd(AdRequest.Builder().build())
            }

            onDispose {
                cachedBanner.onLoadStateChanged = null
            }
        }

        AndroidView(
            modifier = modifier,
            factory = {
                cachedBanner.adView.also { adView ->
                    (adView.parent as? ViewGroup)?.removeView(adView)
                }
            }
        )
    }

    private fun getOrCreateBanner(
        context: android.content.Context,
        placement: InlineAdPlacement,
        adUnitId: String,
        adSize: AdSize,
        widthDp: Int
    ): CachedBanner {
        val cachedBanner = bannerCache[placement]
        if (cachedBanner != null && cachedBanner.adUnitId == adUnitId && cachedBanner.widthDp == widthDp) {
            return cachedBanner
        }

        cachedBanner?.adView?.destroy()

        val newBanner = CachedBanner(
            adView = AdView(context).apply {
                this.adUnitId = adUnitId
                setAdSize(adSize)
            },
            adUnitId = adUnitId,
            widthDp = widthDp
        )

        newBanner.adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                newBanner.hasActiveLoad = false
                newBanner.loadState = InlineAdLoadState.Loaded
                newBanner.onLoadStateChanged?.invoke(InlineAdLoadState.Loaded)
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                newBanner.hasActiveLoad = false
                newBanner.loadState = InlineAdLoadState.Failed
                newBanner.onLoadStateChanged?.invoke(InlineAdLoadState.Failed)
            }
        }

        bannerCache[placement] = newBanner
        return newBanner
    }

    private class CachedBanner(
        val adView: AdView,
        val adUnitId: String,
        val widthDp: Int,
        var loadState: InlineAdLoadState = InlineAdLoadState.Loading,
        var hasActiveLoad: Boolean = false,
        var onLoadStateChanged: ((InlineAdLoadState) -> Unit)? = null
    )
}
