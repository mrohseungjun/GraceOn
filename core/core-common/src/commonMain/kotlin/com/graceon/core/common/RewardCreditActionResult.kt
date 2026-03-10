package com.graceon.core.common

sealed interface RewardCreditActionResult {
    data class Success(
        val rewardedCredits: Int,
        val rewardedAvailableToday: Int
    ) : RewardCreditActionResult

    data class Error(val message: String) : RewardCreditActionResult
}
