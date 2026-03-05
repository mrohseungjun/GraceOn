package com.graceon.feature.gacha

import com.graceon.core.common.Result
import com.graceon.domain.model.WorryContext
import com.graceon.domain.usecase.GeneratePrescriptionUseCase
import com.graceon.feature.gacha.GachaContract
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlin.time.TimeSource

/**
 * ViewModel for Gacha Animation (MVI Pattern)
 */
class GachaViewModel(
    private val generatePrescriptionUseCase: GeneratePrescriptionUseCase,
    private val categoryId: String?,
    private val detailId: String?,
    private val customWorry: String?,
    private val isAiMode: Boolean
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _state = MutableStateFlow(GachaContract.State())
    val state: StateFlow<GachaContract.State> = _state.asStateFlow()

    private val _effect = Channel<GachaContract.Effect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun handleIntent(intent: GachaContract.Intent) {
        when (intent) {
            is GachaContract.Intent.PullLever -> pullLever()
            is GachaContract.Intent.Reset -> reset()
        }
    }

    private fun pullLever() {
        if (_state.value.stage != GachaContract.State.Stage.Idle) return

        scope.launch {
            // Start shaking animation
            _state.value = _state.value.copy(
                stage = GachaContract.State.Stage.Shaking,
                isLoading = true
            )

            // Generate prescription from AI
            val worryContext = WorryContext(
                categoryId = categoryId,
                detailId = detailId,
                customWorry = customWorry,
                isAiMode = isAiMode
            )

            val startTime = TimeSource.Monotonic.markNow()
            val result = generatePrescriptionUseCase(worryContext)
            val elapsed = startTime.elapsedNow().inWholeMilliseconds

            // Ensure minimum animation time (1500ms)
            val remaining = (1500L - elapsed).coerceAtLeast(0L)
            delay(remaining)

            when (result) {
                is Result.Success -> {
                    _state.value = _state.value.copy(
                        prescription = result.data,
                        stage = GachaContract.State.Stage.Dispensing,
                        isLoading = false
                    )

                    // Dispensing animation
                    delay(1000)
                    _state.value = _state.value.copy(stage = GachaContract.State.Stage.Opening)

                    // Opening animation
                    delay(1500)
                    _state.value = _state.value.copy(stage = GachaContract.State.Stage.Complete)

                    // Navigate to result
                    _effect.send(
                        GachaContract.Effect.NavigateToResult(
                            prescription = result.data,
                            categoryId = categoryId,
                            detailId = detailId,
                            customWorry = customWorry,
                            isAiMode = isAiMode
                        )
                    )
                }
                is Result.Error -> {
                    _state.value = _state.value.copy(
                        stage = GachaContract.State.Stage.Idle,
                        isLoading = false,
                        error = result.exception.message
                    )
                    _effect.send(GachaContract.Effect.ShowError("오류가 발생했습니다: ${result.exception.message}"))
                }
                is Result.Loading -> {
                    // Should not happen
                }
            }
        }
    }

    private fun reset() {
        _state.value = GachaContract.State()
    }
}
