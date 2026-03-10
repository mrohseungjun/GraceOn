package com.graceon.domain.usecase

import com.graceon.core.common.Result
import com.graceon.domain.repository.PrescriptionRepository

class GrantRewardedCreditUseCase(
    private val repository: PrescriptionRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return repository.grantRewardedCredit()
    }
}
