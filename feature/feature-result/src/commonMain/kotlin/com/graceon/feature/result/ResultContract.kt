package com.graceon.feature.result

import com.graceon.domain.model.Prayer
import com.graceon.domain.model.Prescription

/**
 * MVI Contract for Result Feature
 */
object ResultContract {
    
    data class State(
        val prescription: Prescription,
        val prayer: Prayer? = null,
        val isPrayerLoading: Boolean = false,
        val isSaved: Boolean = false,
        val categoryId: String? = null,
        val detailId: String? = null,
        val customWorry: String? = null,
        val isAiMode: Boolean = false
    )
    
    sealed interface Intent {
        data object GeneratePrayer : Intent
        data object SharePrescription : Intent
        data object SavePrescription : Intent
        data object Reset : Intent
    }
    
    sealed interface Effect {
        data class ShareContent(val text: String) : Effect
        data object NavigateToHome : Effect
        data object ShowSaveSuccess : Effect
        data class ShowError(val message: String) : Effect
    }
}
