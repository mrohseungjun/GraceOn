package com.graceon.feature.result

import com.graceon.core.common.Result
import com.graceon.core.common.toUserFacingMessage
import com.graceon.domain.data.CategoryData
import com.graceon.domain.model.Prescription
import com.graceon.domain.model.RANDOM_VERSE_DISPLAY_TEXT
import com.graceon.domain.model.RANDOM_VERSE_PROMPT
import com.graceon.domain.model.SavedPrescription
import com.graceon.domain.model.WorryContext
import com.graceon.domain.usecase.GeneratePrayerUseCase
import com.graceon.domain.usecase.SavePrescriptionUseCase
import kotlin.random.Random
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Result Screen (MVI Pattern)
 */
class ResultViewModel(
    private val generatePrayerUseCase: GeneratePrayerUseCase,
    private val savePrescriptionUseCase: SavePrescriptionUseCase,
    prescription: Prescription,
    private val categoryId: String?,
    private val detailId: String?,
    private val customWorry: String?,
    private val isAiMode: Boolean
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _state = MutableStateFlow(
        ResultContract.State(
            prescription = prescription,
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
            is ResultContract.Intent.SavePrescription -> savePrescription()
            is ResultContract.Intent.Reset -> reset()
        }
    }

    private fun generatePrayer() {
        if (_state.value.isPrayerLoading || _state.value.prayer != null) return

        scope.launch {
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
                    _effect.send(
                        ResultContract.Effect.ShowError(
                            result.exception.toUserFacingMessage("기도문 생성에 실패했습니다. 잠시 후 다시 시도해주세요.")
                        )
                    )
                }
                is Result.Loading -> {}
            }
        }
    }

    private fun sharePrescription() {
        scope.launch {
            val worryText = resolveWorryText()

            val verse = decodeForDisplay(_state.value.prescription.verse)
            val message = decodeForDisplay(_state.value.prescription.message)
            val prayerText = _state.value.prayer?.text?.let(::decodeForDisplay)

            val shareText = """
                [힐링 말씀]
                
                고민: $worryText
                
                $verse
                
                $message
                
                ${prayerText?.let { "\n기도문:\n$it" } ?: ""}
            """.trimIndent()

            _effect.send(ResultContract.Effect.ShareContent(shareText))
        }
    }

    private fun resolveWorryText(): String {
        if (customWorry == RANDOM_VERSE_PROMPT) {
            return RANDOM_VERSE_DISPLAY_TEXT
        }

        customWorry?.let { return decodeForDisplay(it) }

        val category = CategoryData.categories.firstOrNull { it.id == categoryId }

        if (detailId != null) {
            val detailTitle = category
                ?.details
                ?.firstOrNull { it.id == detailId }
                ?.title

            if (!detailTitle.isNullOrBlank()) return detailTitle
        }

        if (!category?.title.isNullOrBlank()) return category.title

        return detailId ?: categoryId ?: "고민"
    }

    private fun decodeForDisplay(value: String): String {
        return value.replace("+", " ")
    }

    private fun reset() {
        scope.launch {
            _effect.send(ResultContract.Effect.NavigateToHome)
        }
    }

    private fun savePrescription() {
        if (_state.value.isSaved) return

        scope.launch {
            val savedPrescription = SavedPrescription(
                id = "${Random.nextLong()}-${Random.nextInt()}",
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
