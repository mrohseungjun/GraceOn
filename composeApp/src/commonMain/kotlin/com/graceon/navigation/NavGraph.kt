package com.graceon.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.graceon.GraceOnDependencies
import com.graceon.domain.model.Prescription
import com.graceon.feature.gacha.GachaScreen
import com.graceon.feature.gacha.GachaViewModel
import com.graceon.feature.onboarding.OnboardingScreen
import com.graceon.feature.profile.ProfileScreen
import com.graceon.feature.result.ResultScreen
import com.graceon.feature.result.ResultViewModel
import com.graceon.feature.saved.SavedScreen
import com.graceon.feature.saved.SavedViewModel
import com.graceon.feature.worry.WorryScreen
import com.graceon.feature.worry.WorryViewModel

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
    data object Onboarding : NavEntry
    data object Worry : NavEntry
    data class Gacha(val args: WorryArgs) : NavEntry
    data class Result(val args: ResultArgs) : NavEntry
    data object Saved : NavEntry
    data object Profile : NavEntry
}

internal object Screen {
    const val ONBOARDING = "onboarding"
    const val WORRY = "worry"
    const val GACHA = "gacha"
    const val RESULT = "result"
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
    startDestination: String = Screen.ONBOARDING,
    appVersion: String = "",
    onShareText: (String) -> Unit = {},
    isDailyVerseNotificationEnabled: Boolean = false,
    isDarkThemeEnabled: Boolean = true,
    onToggleDailyVerseNotification: (Boolean) -> Unit = {},
    onToggleDarkTheme: (Boolean) -> Unit = {},
    onOnboardingComplete: () -> Unit = {}
) {
    val worryViewModel = remember { WorryViewModel() }
    val savedViewModel = remember {
        SavedViewModel(
            getSavedPrescriptionsUseCase = dependencies.getSavedPrescriptionsUseCase,
            deletePrescriptionUseCase = dependencies.deletePrescriptionUseCase
        )
    }
    val initialEntry = remember(startDestination) {
        when (startDestination) {
            Screen.WORRY -> NavEntry.Worry
            Screen.SAVED -> NavEntry.Saved
            Screen.PROFILE -> NavEntry.Profile
            else -> NavEntry.Onboarding
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
        onBack = ::popBackStack
    )

    AnimatedContent(
        targetState = backStack.last(),
        transitionSpec = {
            navigationDirection.toContentTransform()
        },
        label = "nav_graph_transition"
    ) { entry ->
        when (entry) {
            NavEntry.Onboarding -> {
                OnboardingScreen(
                    onComplete = {
                        onOnboardingComplete()
                        replaceRoot(NavEntry.Worry)
                    }
                )
            }

            NavEntry.Worry -> {
                WorryScreen(
                    viewModel = worryViewModel,
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
                        categoryId = entry.args.categoryId,
                        detailId = entry.args.detailId,
                        customWorry = entry.args.customWorry,
                        isAiMode = entry.args.isAiMode
                    )
                }

                GachaScreen(
                    viewModel = viewModel,
                    onNavigateBack = ::popBackStack,
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
                    onNavigateBack = ::popBackStack,
                    onNavigateToSaved = { navigate(NavEntry.Saved) },
                    onNavigateToProfile = { replaceRoot(NavEntry.Profile) },
                    onShareText = onShareText,
                    onRetry = { retryFromResult(entry.args) },
                    onNavigateHome = ::popToWorry
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
                    onNavigateHome = { replaceRoot(NavEntry.Worry) }
                )
            }

            NavEntry.Profile -> {
                ProfileScreen(
                    isDailyVerseNotificationEnabled = isDailyVerseNotificationEnabled,
                    isDarkThemeEnabled = isDarkThemeEnabled,
                    appVersion = appVersion,
                    onToggleDailyVerseNotification = onToggleDailyVerseNotification,
                    onToggleDarkTheme = onToggleDarkTheme,
                    onNavigateHome = { replaceRoot(NavEntry.Worry) },
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
