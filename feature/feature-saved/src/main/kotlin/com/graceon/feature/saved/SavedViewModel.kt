package com.graceon.feature.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.graceon.domain.usecase.DeletePrescriptionUseCase
import com.graceon.domain.usecase.GetSavedPrescriptionsUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Saved Prescriptions Screen (MVI Pattern)
 */
class SavedViewModel(
    private val getSavedPrescriptionsUseCase: GetSavedPrescriptionsUseCase,
    private val deletePrescriptionUseCase: DeletePrescriptionUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SavedContract.State())
    val state: StateFlow<SavedContract.State> = _state.asStateFlow()

    private val _effect = Channel<SavedContract.Effect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        loadPrescriptions()
    }

    private fun loadPrescriptions() {
        viewModelScope.launch {
            getSavedPrescriptionsUseCase().collect { prescriptions ->
                _state.value = _state.value.copy(
                    prescriptions = prescriptions,
                    isLoading = false
                )
            }
        }
    }

    fun handleIntent(intent: SavedContract.Intent) {
        when (intent) {
            is SavedContract.Intent.DeletePrescription -> deletePrescription(intent.id)
        }
    }

    private fun deletePrescription(id: String) {
        viewModelScope.launch {
            deletePrescriptionUseCase(id)
            _effect.send(SavedContract.Effect.ShowDeleteSuccess)
        }
    }
}
