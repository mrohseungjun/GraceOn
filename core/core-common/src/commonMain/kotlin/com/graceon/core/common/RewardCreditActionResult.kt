package com.graceon.core.common

sealed interface RewardCreditActionResult {
    data class Success(
        val totalRemainingCount: Int
    ) : RewardCreditActionResult

    data class Error(val message: String) : RewardCreditActionResult
}
