package com.graceon.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import com.graceon.GraceOnDependencies
import com.graceon.domain.model.Prescription
import com.graceon.feature.gacha.GachaScreen
import com.graceon.feature.gacha.GachaViewModel
import com.graceon.feature.onboarding.OnboardingScreen
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
}

internal object Screen {
    const val ONBOARDING = "onboarding"
    const val WORRY = "worry"
    const val GACHA = "gacha"
    const val RESULT = "result"
    const val SAVED = "saved"
}

@Composable
internal fun NavGraph(
    dependencies: GraceOnDependencies,
    startDestination: String = Screen.ONBOARDING,
    onShareText: (String) -> Unit = {},
    onShareImage: () -> Unit = {},
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
            else -> NavEntry.Onboarding
        }
    }
    val backStack = remember(initialEntry) { mutableStateListOf(initialEntry) }

    fun navigate(entry: NavEntry) {
        backStack += entry
    }

    fun replaceRoot(entry: NavEntry) {
        backStack.clear()
        backStack += entry
    }

    fun popBackStack() {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
        }
    }

    fun popToWorry() {
        while (backStack.size > 1 && backStack.last() !is NavEntry.Worry) {
            backStack.removeAt(backStack.lastIndex)
        }
    }

    PlatformBackHandler(
        enabled = backStack.size > 1,
        onBack = ::popBackStack
    )

    when (val entry = backStack.last()) {
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
                onNavigateToSaved = { navigate(NavEntry.Saved) }
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
                onShareText = onShareText,
                onShareImage = onShareImage,
                onNavigateHome = ::popToWorry
            )
        }

        NavEntry.Saved -> {
            SavedScreen(
                viewModel = savedViewModel,
                onNavigateBack = ::popBackStack
            )
        }
    }
}
