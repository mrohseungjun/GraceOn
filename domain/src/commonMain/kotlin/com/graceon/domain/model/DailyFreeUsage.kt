package com.graceon.domain.model

data class DailyFreeUsage(
    val dailyLimit: Int,
    val usedToday: Int,
    val remainingToday: Int
)
