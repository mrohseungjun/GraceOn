package com.graceon.domain.model

data class DailyFreeUsage(
    val dailyLimit: Int,
    val usedToday: Int,
    val remainingToday: Int,
    val rewardedCredits: Int = 0,
    val rewardedAvailableToday: Int = 0
)
