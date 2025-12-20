package com.graceon.feature.saved

import com.graceon.domain.model.SavedPrescription

/**
 * MVI Contract for Saved Prescriptions Feature
 */
object SavedContract {
    
    data class State(
        val prescriptions: List<SavedPrescription> = emptyList(),
        val isLoading: Boolean = true
    )
    
    sealed interface Intent {
        data class DeletePrescription(val id: String) : Intent
    }
    
    sealed interface Effect {
        data object ShowDeleteSuccess : Effect
        data class ShowError(val message: String) : Effect
    }
}
