package com.graceon.feature.worry

import com.graceon.domain.data.CategoryData
import com.graceon.domain.model.Category
import com.graceon.domain.model.RANDOM_VERSE_PROMPT
import com.graceon.domain.usecase.GetDailyFreeUsageUseCase
import com.graceon.feature.worry.WorryContract
import com.graceon.core.common.Result
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
 * ViewModel for Worry Selection (MVI Pattern)
 */
class WorryViewModel(
    private val getDailyFreeUsageUseCase: GetDailyFreeUsageUseCase
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _state = MutableStateFlow(WorryContract.State())
    val state: StateFlow<WorryContract.State> = _state.asStateFlow()

    private val _effect = Channel<WorryContract.Effect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        loadCategories()
        refreshDailyUsage()
    }

    fun handleIntent(intent: WorryContract.Intent) {
        when (intent) {
            is WorryContract.Intent.StartCategoryMode -> startCategoryMode()
            is WorryContract.Intent.StartAiMode -> startAiMode()
            is WorryContract.Intent.SelectCategory -> selectCategory(intent.category)
            is WorryContract.Intent.SelectDetail -> selectDetail(intent.detail)
            is WorryContract.Intent.UpdateCustomWorry -> updateCustomWorry(intent.text)
            is WorryContract.Intent.NavigateBack -> navigateBack()
            is WorryContract.Intent.ConfirmCustomWorry -> confirmCustomWorry()
            is WorryContract.Intent.RefreshDailyUsage -> refreshDailyUsage()
        }
    }

    private fun loadCategories() {
        _state.value = _state.value.copy(
            categories = CategoryData.categories
        )
    }

    private fun startCategoryMode() {
        _state.value = _state.value.copy(
            step = WorryContract.State.Step.CategorySelection,
            isAiMode = false
        )
    }

    private fun startAiMode() {
        scope.launch {
            _effect.send(
                WorryContract.Effect.NavigateToGacha(
                    categoryId = null,
                    detailId = null,
                    customWorry = RANDOM_VERSE_PROMPT,
                    isAiMode = true
                )
            )
        }
    }

    private fun selectCategory(category: Category) {
        if (category.id == CategoryData.CUSTOM_CATEGORY_ID) {
            _state.value = _state.value.copy(
                selectedCategory = category,
                selectedDetail = null,
                customWorry = "",
                step = WorryContract.State.Step.CustomInput,
                isAiMode = true
            )
            return
        }

        _state.value = _state.value.copy(
            selectedCategory = category,
            selectedDetail = null,
            step = WorryContract.State.Step.DetailSelection
        )
    }

    private fun selectDetail(detail: com.graceon.domain.model.DetailWorry) {
        _state.value = _state.value.copy(
            selectedDetail = detail
        )
        
        // Navigate to Gacha
        scope.launch {
            _effect.send(
                WorryContract.Effect.NavigateToGacha(
                    categoryId = _state.value.selectedCategory?.id,
                    detailId = detail.id,
                    customWorry = null,
                    isAiMode = false
                )
            )
        }
    }

    private fun updateCustomWorry(text: String) {
        _state.value = _state.value.copy(customWorry = text)
    }

    private fun confirmCustomWorry() {
        val worry = _state.value.customWorry.trim()
        if (worry.isEmpty()) {
            scope.launch {
                _effect.send(WorryContract.Effect.ShowError("고민을 입력해주세요!"))
            }
            return
        }

        scope.launch {
            _effect.send(
                WorryContract.Effect.NavigateToGacha(
                    categoryId = null,
                    detailId = null,
                    customWorry = worry,
                    isAiMode = true
                )
            )
        }
    }

    private fun navigateBack() {
        when (_state.value.step) {
            WorryContract.State.Step.Intro -> {
                // Do nothing or exit app
            }
            WorryContract.State.Step.CategorySelection -> {
                _state.value = _state.value.copy(step = WorryContract.State.Step.Intro)
            }
            WorryContract.State.Step.CustomInput -> {
                _state.value = _state.value.copy(
                    step = if (_state.value.selectedCategory?.id == CategoryData.CUSTOM_CATEGORY_ID) {
                        WorryContract.State.Step.CategorySelection
                    } else {
                        WorryContract.State.Step.Intro
                    },
                    customWorry = "",
                    selectedCategory = if (_state.value.selectedCategory?.id == CategoryData.CUSTOM_CATEGORY_ID) null else _state.value.selectedCategory
                )
            }
            WorryContract.State.Step.DetailSelection -> {
                _state.value = _state.value.copy(
                    step = WorryContract.State.Step.CategorySelection,
                    selectedCategory = null
                )
            }
        }
    }

    private fun refreshDailyUsage() {
        if (_state.value.dailyUsage.isLoading) return

        scope.launch {
            _state.value = _state.value.copy(
                dailyUsage = _state.value.dailyUsage.copy(isLoading = true)
            )

            when (val result = getDailyFreeUsageUseCase()) {
                is Result.Success -> {
                    _state.value = _state.value.copy(
                        dailyUsage = WorryContract.DailyUsageUiState(
                            isLoading = false,
                            dailyLimit = result.data.dailyLimit,
                            usedToday = result.data.usedToday,
                            remainingToday = result.data.remainingToday
                        )
                    )
                }
                is Result.Error -> {
                    _state.value = _state.value.copy(
                        dailyUsage = _state.value.dailyUsage.copy(isLoading = false)
                    )
                }
                Result.Loading -> Unit
            }
        }
    }
}
