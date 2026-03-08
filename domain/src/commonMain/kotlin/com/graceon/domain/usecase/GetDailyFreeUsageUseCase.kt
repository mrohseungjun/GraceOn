package com.graceon.domain.usecase

import com.graceon.core.common.Result
import com.graceon.domain.model.DailyFreeUsage
import com.graceon.domain.repository.PrescriptionRepository

class GetDailyFreeUsageUseCase(
    private val repository: PrescriptionRepository
) {
    suspend operator fun invoke(): Result<DailyFreeUsage> {
        return repository.getDailyFreeUsage()
    }
}
