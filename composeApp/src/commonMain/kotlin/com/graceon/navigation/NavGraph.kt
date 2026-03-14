package com.graceon.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.graceon.GraceOnDependencies
import com.graceon.ads.GraceOnInlineAdSlot
import com.graceon.ads.InlineAdPlacement
import com.graceon.core.common.RewardCreditActionResult
import com.graceon.core.common.Result
import com.graceon.core.common.RewardedAdResult
import com.graceon.core.common.toUserFacingMessage
import com.graceon.domain.model.Prescription
import com.graceon.feature.gacha.GachaScreen
import com.graceon.feature.gacha.GachaViewModel
import com.graceon.feature.onboarding.LoginScreen
import com.graceon.feature.profile.ProfileScreen
import com.graceon.feature.result.ResultScreen
import com.graceon.feature.result.ResultViewModel
import com.graceon.feature.saved.SavedScreen
import com.graceon.feature.saved.SavedViewModel
import com.graceon.feature.worry.WorryScreen
import com.graceon.feature.worry.WorryViewModel
import com.graceon.feature.worry.WorryContract

private data class WorryArgs(
    val categoryId: String?,
    val detailId: String?,
    val customWorry: String?,
    val isAiMode: Boolean
)

private data class ResultArgs(
    val prescription: Prescription,
    val categoryId: String?,
    val detailId: String?,
    val customWorry: String?,
    val isAiMode: Boolean
)

private sealed interface NavEntry {
    data object Login : NavEntry
    data object Worry : NavEntry
    data class Gacha(val args: WorryArgs) : NavEntry
    data class Result(val args: ResultArgs) : NavEntry
    data object Saved : NavEntry
    data object Profile : NavEntry
}

internal object Screen {
    const val LOGIN = "login"
    const val WORRY = "worry"
    const val SAVED = "saved"
    const val PROFILE = "profile"
}

private enum class NavigationDirection {
    Forward,
    Backward,
    Replace
}

@Composable
internal fun NavGraph(
    dependencies: GraceOnDependencies,
    startDestination: String = Screen.LOGIN,
    initialDailyUsage: WorryContract.DailyUsageUiState? = null,
    appVersion: String = "",
    onShareText: (String) -> Unit = {},
    isDailyVerseNotificationEnabled: Boolean = false,
    isDarkThemeEnabled: Boolean = true,
    onToggleDailyVerseNotification: (Boolean) -> Unit = {},
    onToggleDarkTheme: (Boolean) -> Unit = {},
    onLoginComplete: () -> Unit = {},
    onLogout: () -> Unit = {},
    onShowRewardedAd: suspend () -> RewardedAdResult = { RewardedAdResult.Failed("리워드 광고를 사용할 수 없습니다.") },
    onInlineAdPlacementChanged: (String?) -> Unit = {}
) {
    val worryViewModel = remember(initialDailyUsage) {
        WorryViewModel(
            getDailyFreeUsageUseCase = dependencies.getDailyFreeUsageUseCase,
            getSavedPrescriptionsUseCase = dependencies.getSavedPrescriptionsUseCase,
            initialDailyUsage = initialDailyUsage
        )
    }
    val savedViewModel = remember {
        SavedViewModel(
            getSavedPrescriptionsUseCase = dependencies.getSavedPrescriptionsUseCase,
            deletePrescriptionUseCase = dependencies.deletePrescriptionUseCase
        )
    }
    var profileEmail by remember { mutableStateOf<String?>(null) }
    var profileRemainingToday by remember { mutableStateOf(0) }
    var profileDailyLimit by remember { mutableStateOf(1) }
    var profileUsedToday by remember { mutableStateOf(0) }
    var profileRewardedCredits by remember { mutableStateOf(0) }
    var profileRewardedAvailableToday by remember { mutableStateOf(0) }

    suspend fun refreshProfileRewardUsage() {
        when (val result = dependencies.getDailyFreeUsageUseCase()) {
            is Result.Success -> {
                profileDailyLimit = result.data.dailyLimit
                profileUsedToday = result.data.usedToday
                profileRemainingToday = result.data.remainingToday
                profileRewardedCredits = result.data.rewardedCredits
                profileRewardedAvailableToday = result.data.rewardedAvailableToday
                worryViewModel.syncDailyUsage(result.data)
            }
            else -> Unit
        }
    }

    suspend fun refreshProfileSummary() {
        profileEmail = dependencies.getCurrentUserEmail()
        refreshProfileRewardUsage()
    }
    val initialEntry = remember(startDestination) {
        when (startDestination) {
            Screen.WORRY -> NavEntry.Worry
            Screen.SAVED -> NavEntry.Saved
            Screen.PROFILE -> NavEntry.Profile
            else -> NavEntry.Login
        }
    }
    val backStack = remember(initialEntry) { mutableStateListOf(initialEntry) }
    var navigationDirection by remember { mutableStateOf(NavigationDirection.Replace) }

    fun navigate(entry: NavEntry) {
        navigationDirection = NavigationDirection.Forward
        backStack += entry
    }

    fun replaceRoot(entry: NavEntry) {
        navigationDirection = NavigationDirection.Replace
        backStack.clear()
        backStack += entry
    }

    fun navigateHome() {
        worryViewModel.handleIntent(com.graceon.feature.worry.WorryContract.Intent.NavigateBack)
        worryViewModel.handleIntent(com.graceon.feature.worry.WorryContract.Intent.NavigateBack)
        worryViewModel.handleIntent(com.graceon.feature.worry.WorryContract.Intent.NavigateBack)
        replaceRoot(NavEntry.Worry)
    }

    fun popBackStack() {
        if (backStack.size > 1) {
            navigationDirection = NavigationDirection.Backward
            backStack.removeAt(backStack.lastIndex)
        }
    }

    fun popToWorry() {
        navigationDirection = NavigationDirection.Backward
        while (backStack.size > 1 && backStack.last() !is NavEntry.Worry) {
            backStack.removeAt(backStack.lastIndex)
        }
    }

    fun retryFromResult(args: ResultArgs) {
        navigationDirection = NavigationDirection.Replace
        while (backStack.lastOrNull() is NavEntry.Result || backStack.lastOrNull() is NavEntry.Gacha) {
            backStack.removeAt(backStack.lastIndex)
        }
        backStack += NavEntry.Gacha(
            WorryArgs(
                categoryId = args.categoryId,
                detailId = args.detailId,
                customWorry = args.customWorry,
                isAiMode = args.isAiMode
            )
        )
    }

    PlatformBackHandler(
        enabled = backStack.size > 1,
        onBack = {
            when (backStack.lastOrNull()) {
                is NavEntry.Result -> popToWorry()
                else -> popBackStack()
            }
        }
    )

    AnimatedContent(
        targetState = backStack.last(),
        transitionSpec = {
            navigationDirection.toContentTransform()
        },
        label = "nav_graph_transition"
    ) { entry ->
        when (entry) {
            NavEntry.Login -> {
                LoginScreen(
                    onSignIn = { email, password ->
                        dependencies.signInWithEmail(email, password)
                        onLoginComplete()
                        replaceRoot(NavEntry.Worry)
                    },
                    onSignUp = { email, password ->
                        val signedIn = dependencies.signUpWithEmail(email, password)
                        if (signedIn) {
                            onLoginComplete()
                            replaceRoot(NavEntry.Worry)
                        }
                        signedIn
                    },
                    onGoogleLogin = {
                        dependencies.signInWithGoogle()
                        onLoginComplete()
                        replaceRoot(NavEntry.Worry)
                    },
                    onResendConfirmationEmail = { email ->
                        dependencies.resendConfirmationEmail(email)
                    },
                    onSendPasswordResetEmail = { email ->
                        dependencies.sendPasswordResetEmail(email)
                    }
                )
            }

            NavEntry.Worry -> {
                WorryScreen(
                    viewModel = worryViewModel,
                    inlineAdSlot = {
                        GraceOnInlineAdSlot(placement = InlineAdPlacement.HomeFeed)
                    },
                    onInlineAdPlacementChanged = onInlineAdPlacementChanged,
                    onNavigateToGacha = { categoryId, detailId, customWorry, isAiMode ->
                        navigate(
                            NavEntry.Gacha(
                                WorryArgs(categoryId, detailId, customWorry, isAiMode)
                            )
                        )
                    },
                    onNavigateBack = ::popBackStack,
                    onNavigateToSaved = { navigate(NavEntry.Saved) },
                    onNavigateToProfile = { navigate(NavEntry.Profile) }
                )
            }

            is NavEntry.Gacha -> {
                val viewModel = remember(entry.args) {
                    GachaViewModel(
                        generatePrescriptionUseCase = dependencies.generatePrescriptionUseCase,
                        grantRewardedCreditUseCase = dependencies.grantRewardedCreditUseCase,
                        categoryId = entry.args.categoryId,
                        detailId = entry.args.detailId,
                        customWorry = entry.args.customWorry,
                        isAiMode = entry.args.isAiMode
                    )
                }

                GachaScreen(
                    viewModel = viewModel,
                    onNavigateBack = ::popBackStack,
                    onShowRewardedAd = onShowRewardedAd,
                    onNavigateToResult = { prescription, categoryId, detailId, customWorry, isAiMode ->
                        navigate(
                            NavEntry.Result(
                                ResultArgs(
                                    prescription = prescription,
                                    categoryId = categoryId,
                                    detailId = detailId,
                                    customWorry = customWorry,
                                    isAiMode = isAiMode
                                )
                            )
                        )
                    }
                )
            }

            is NavEntry.Result -> {
                val viewModel = remember(entry.args) {
                    ResultViewModel(
                        generatePrayerUseCase = dependencies.generatePrayerUseCase,
                        savePrescriptionUseCase = dependencies.savePrescriptionUseCase,
                        prescription = entry.args.prescription,
                        categoryId = entry.args.categoryId,
                        detailId = entry.args.detailId,
                        customWorry = entry.args.customWorry,
                        isAiMode = entry.args.isAiMode
                    )
                }
                ResultScreen(
                    viewModel = viewModel,
                    inlineAdSlot = {
                        GraceOnInlineAdSlot(placement = InlineAdPlacement.ResultContent)
                    },
                    onInlineAdPlacementChanged = onInlineAdPlacementChanged,
                    onNavigateBack = ::popToWorry,
                    onNavigateToSaved = { navigate(NavEntry.Saved) },
                    onNavigateToProfile = { replaceRoot(NavEntry.Profile) },
                    onShareText = onShareText,
                    onRetry = { retryFromResult(entry.args) },
                    onNavigateHome = ::navigateHome
                )
            }

            NavEntry.Saved -> {
                SavedScreen(
                    viewModel = savedViewModel,
                    onNavigateBack = ::popBackStack,
                    onShareText = onShareText,
                    onNavigateToWord = {
                        worryViewModel.handleIntent(com.graceon.feature.worry.WorryContract.Intent.StartCategoryMode)
                        replaceRoot(NavEntry.Worry)
                    },
                    onNavigateToProfile = { replaceRoot(NavEntry.Profile) },
                    onNavigateHome = ::navigateHome
                )
            }

            NavEntry.Profile -> {
                LaunchedEffect(Unit) {
                    refreshProfileSummary()
                }

                ProfileScreen(
                    currentUserEmail = profileEmail,
                    remainingCount = profileRemainingToday + profileRewardedCredits,
                    dailyLimit = profileDailyLimit,
                    usedToday = profileUsedToday,
                    isDailyVerseNotificationEnabled = isDailyVerseNotificationEnabled,
                    isDarkThemeEnabled = isDarkThemeEnabled,
                    appVersion = appVersion,
                    rewardedCredits = profileRewardedCredits,
                    rewardedAvailableToday = profileRewardedAvailableToday,
                    onToggleDailyVerseNotification = onToggleDailyVerseNotification,
                    onToggleDarkTheme = onToggleDarkTheme,
                    onWatchRewardAd = {
                        when (val adResult = onShowRewardedAd()) {
                            RewardedAdResult.RewardEarned -> {
                                when (val grantResult = dependencies.grantRewardedCreditUseCase()) {
                                    is Result.Success -> {
                                        refreshProfileSummary()
                                        RewardCreditActionResult.Success(
                                            totalRemainingCount = profileRemainingToday + profileRewardedCredits
                                        )
                                    }
                                    is Result.Error -> {
                                        RewardCreditActionResult.Error(
                                            grantResult.exception.toUserFacingMessage("광고 보상을 반영하지 못했습니다. 잠시 후 다시 시도해주세요.")
                                        )
                                    }
                                    Result.Loading -> RewardCreditActionResult.Error("광고 보상을 처리하는 중입니다.")
                                }
                            }
                            RewardedAdResult.Dismissed -> {
                                RewardCreditActionResult.Error("광고 시청이 완료되지 않아 추가 횟수가 지급되지 않았습니다.")
                            }
                            is RewardedAdResult.Failed -> {
                                RewardCreditActionResult.Error(adResult.message)
                            }
                        }
                    },
                    onLogout = {
                        replaceRoot(NavEntry.Login)
                        onLogout()
                    },
                    onNavigateHome = ::navigateHome,
                    onNavigateToWord = {
                        worryViewModel.handleIntent(com.graceon.feature.worry.WorryContract.Intent.StartCategoryMode)
                        replaceRoot(NavEntry.Worry)
                    },
                    onNavigateToSaved = { replaceRoot(NavEntry.Saved) }
                )
            }
        }
    }
}

private fun NavigationDirection.toContentTransform(): ContentTransform {
    return when (this) {
        NavigationDirection.Forward -> {
            (slideInHorizontally { fullWidth -> fullWidth / 4 } + fadeIn()) togetherWith
                (slideOutHorizontally { fullWidth -> -fullWidth / 6 } + fadeOut())
        }
        NavigationDirection.Backward -> {
            (slideInHorizontally { fullWidth -> -fullWidth / 4 } + fadeIn()) togetherWith
                (slideOutHorizontally { fullWidth -> fullWidth / 6 } + fadeOut())
        }
        NavigationDirection.Replace -> {
            fadeIn() togetherWith fadeOut()
        }
    }
}
