package com.graceon.feature.worry

import com.graceon.domain.model.Category
import com.graceon.domain.model.DetailWorry
import com.graceon.domain.model.SavedPrescription

/**
 * MVI Contract for Worry Selection Feature
 */
object WorryContract {
    data class DailyUsageUiState(
        val isLoading: Boolean = false,
        val dailyLimit: Int = 1,
        val usedToday: Int = 0,
        val remainingToday: Int = 1,
        val rewardedCredits: Int = 0,
        val rewardedAvailableToday: Int = 0
    )
    
    /**
     * UI State (Immutable)
     */
    data class State(
        val step: Step = Step.Intro,
        val categories: List<Category> = emptyList(),
        val selectedCategory: Category? = null,
        val selectedDetail: DetailWorry? = null,
        val customWorry: String = "",
        val isAiMode: Boolean = false,
        val dailyUsage: DailyUsageUiState = DailyUsageUiState(),
        val recentSavedPrescription: SavedPrescription? = null
    ) {
        enum class Step {
            Intro,
            CategorySelection,
            DetailSelection,
            CustomInput
        }
    }
    
    /**
     * User Intent
     */
    sealed interface Intent {
        data object StartCategoryMode : Intent
        data object StartAiMode : Intent
        data class SelectCategory(val category: Category) : Intent
        data class SelectDetail(val detail: DetailWorry) : Intent
        data class UpdateCustomWorry(val text: String) : Intent
        data object NavigateBack : Intent
        data object ConfirmCustomWorry : Intent
        data object RefreshDailyUsage : Intent
    }
    
    /**
     * Side Effect (One-time events)
     */
    sealed interface Effect {
        data class NavigateToGacha(
            val categoryId: String?,
            val detailId: String?,
            val customWorry: String?,
            val isAiMode: Boolean
        ) : Effect
        data object NavigateBack : Effect
        data class ShowError(val message: String) : Effect
    }
}
