package com.graceon.core.common

sealed interface RewardedAdResult {
    data object RewardEarned : RewardedAdResult
    data object Dismissed : RewardedAdResult
    data class Failed(val message: String) : RewardedAdResult
}
