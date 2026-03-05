package com.graceon.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    var currentScreen by remember { mutableStateOf(startDestination) }
    var worryArgs by remember { mutableStateOf<WorryArgs?>(null) }
    var resultArgs by remember { mutableStateOf<ResultArgs?>(null) }

    when (currentScreen) {
        Screen.ONBOARDING -> {
            OnboardingScreen(
                onComplete = {
                    onOnboardingComplete()
                    currentScreen = Screen.WORRY
                }
            )
        }

        Screen.WORRY -> {
            val viewModel = remember { WorryViewModel() }
            WorryScreen(
                viewModel = viewModel,
                onNavigateToGacha = { categoryId, detailId, customWorry, isAiMode ->
                    worryArgs = WorryArgs(categoryId, detailId, customWorry, isAiMode)
                    currentScreen = Screen.GACHA
                },
                onNavigateBack = {},
                onNavigateToSaved = { currentScreen = Screen.SAVED }
            )
        }

        Screen.GACHA -> {
            val args = worryArgs ?: WorryArgs(null, null, null, false)
            val viewModel = remember(args) {
                GachaViewModel(
                    generatePrescriptionUseCase = dependencies.generatePrescriptionUseCase,
                    categoryId = args.categoryId,
                    detailId = args.detailId,
                    customWorry = args.customWorry,
                    isAiMode = args.isAiMode
                )
            }

            GachaScreen(
                viewModel = viewModel,
                onNavigateToResult = { prescription, categoryId, detailId, customWorry, isAiMode ->
                    resultArgs = ResultArgs(
                        prescription = prescription,
                        categoryId = categoryId,
                        detailId = detailId,
                        customWorry = customWorry,
                        isAiMode = isAiMode
                    )
                    currentScreen = Screen.RESULT
                }
            )
        }

        Screen.RESULT -> {
            val args = resultArgs
            if (args == null) {
                currentScreen = Screen.WORRY
                return
            }

            val viewModel = remember(args) {
                ResultViewModel(
                    generatePrayerUseCase = dependencies.generatePrayerUseCase,
                    savePrescriptionUseCase = dependencies.savePrescriptionUseCase,
                    prescription = args.prescription,
                    categoryId = args.categoryId,
                    detailId = args.detailId,
                    customWorry = args.customWorry,
                    isAiMode = args.isAiMode
                )
            }
            ResultScreen(
                viewModel = viewModel,
                onShareText = onShareText,
                onShareImage = onShareImage,
                onNavigateHome = {
                    currentScreen = Screen.WORRY
                }
            )
        }

        Screen.SAVED -> {
            val viewModel = remember {
                SavedViewModel(
                    getSavedPrescriptionsUseCase = dependencies.getSavedPrescriptionsUseCase,
                    deletePrescriptionUseCase = dependencies.deletePrescriptionUseCase
                )
            }
            SavedScreen(
                viewModel = viewModel,
                onNavigateBack = { currentScreen = Screen.WORRY }
            )
        }
    }
}
