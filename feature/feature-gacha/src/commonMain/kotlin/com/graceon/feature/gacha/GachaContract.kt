package com.graceon.feature.gacha

import com.graceon.domain.model.Prescription

/**
 * MVI Contract for Gacha Feature
 */
object GachaContract {
    
    data class State(
        val stage: Stage = Stage.Idle,
        val prescription: Prescription? = null,
        val isLoading: Boolean = false,
        val error: String? = null
    ) {
        enum class Stage {
            Idle,
            Shaking,
            Dispensing,
            Opening,
            Complete
        }
    }
    
    sealed interface Intent {
        data object PullLever : Intent
        data object Reset : Intent
        data object RewardAdCompleted : Intent
    }
    
    sealed interface Effect {
        data class NavigateToResult(
            val prescription: Prescription,
            val categoryId: String?,
            val detailId: String?,
            val customWorry: String?,
            val isAiMode: Boolean
        ) : Effect
        data class ShowError(val message: String) : Effect
        data class ShowRewardAdOffer(val message: String) : Effect
    }
}
