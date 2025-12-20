package com.graceon.feature.result

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.graceon.core.common.Result
import com.graceon.domain.model.Prescription
import com.graceon.domain.model.SavedPrescription
import com.graceon.domain.model.WorryContext
import com.graceon.domain.usecase.GeneratePrayerUseCase
import com.graceon.domain.usecase.SavePrescriptionUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.UUID

/**
 * ViewModel for Result Screen (MVI Pattern)
 */
class ResultViewModel(
    private val generatePrayerUseCase: GeneratePrayerUseCase,
    private val savePrescriptionUseCase: SavePrescriptionUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val prescriptionJson: String = savedStateHandle["prescription"] ?: ""
    private val categoryId: String? = savedStateHandle["categoryId"]
    private val detailId: String? = savedStateHandle["detailId"]
    private val customWorry: String? = savedStateHandle["customWorry"]
    private val isAiMode: Boolean = savedStateHandle["isAiMode"] ?: false

    private val _state = MutableStateFlow(
        ResultContract.State(
            prescription = Json.decodeFromString<Prescription>(prescriptionJson),
            categoryId = categoryId,
            detailId = detailId,
            customWorry = customWorry,
            isAiMode = isAiMode
        )
    )
    val state: StateFlow<ResultContract.State> = _state.asStateFlow()

    private val _effect = Channel<ResultContract.Effect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun handleIntent(intent: ResultContract.Intent) {
        when (intent) {
            is ResultContract.Intent.GeneratePrayer -> generatePrayer()
            is ResultContract.Intent.SharePrescription -> sharePrescription()
            is ResultContract.Intent.ShareAsImage -> shareAsImage()
            is ResultContract.Intent.SavePrescription -> savePrescription()
            is ResultContract.Intent.Reset -> reset()
        }
    }

    private fun generatePrayer() {
        if (_state.value.isPrayerLoading || _state.value.prayer != null) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isPrayerLoading = true)

            val worryContext = WorryContext(
                categoryId = categoryId,
                detailId = detailId,
                customWorry = customWorry,
                isAiMode = isAiMode
            )

            val result = generatePrayerUseCase(
                worryContext = worryContext,
                verse = _state.value.prescription.verse
            )

            when (result) {
                is Result.Success -> {
                    _state.value = _state.value.copy(
                        prayer = result.data,
                        isPrayerLoading = false
                    )
                }
                is Result.Error -> {
                    _state.value = _state.value.copy(isPrayerLoading = false)
                    _effect.send(ResultContract.Effect.ShowError("기도문 생성에 실패했습니다."))
                }
                is Result.Loading -> {}
            }
        }
    }

    private fun sharePrescription() {
        viewModelScope.launch {
            val worryText = customWorry ?: detailId ?: "고민"
            val shareText = """
                [💊 하늘 약국] 처방전
                
                고민: $worryText
                
                ${_state.value.prescription.verse}
                
                ${_state.value.prescription.message}
                
                ${_state.value.prayer?.let { "\n기도문:\n${it.text}" } ?: ""}
            """.trimIndent()

            _effect.send(ResultContract.Effect.ShareContent(shareText))
        }
    }

    private fun reset() {
        viewModelScope.launch {
            _effect.send(ResultContract.Effect.NavigateToHome)
        }
    }

    private fun shareAsImage() {
        viewModelScope.launch {
            _effect.send(ResultContract.Effect.ShareAsImage)
        }
    }

    private fun savePrescription() {
        if (_state.value.isSaved) return

        viewModelScope.launch {
            val savedPrescription = SavedPrescription(
                id = UUID.randomUUID().toString(),
                verse = _state.value.prescription.verse,
                message = _state.value.prescription.message,
                prayer = _state.value.prayer?.text,
                categoryId = categoryId
            )
            
            savePrescriptionUseCase(savedPrescription)
            _state.value = _state.value.copy(isSaved = true)
            _effect.send(ResultContract.Effect.ShowSaveSuccess)
        }
    }
}
