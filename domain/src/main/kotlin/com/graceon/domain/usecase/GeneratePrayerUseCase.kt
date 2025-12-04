package com.graceon.domain.usecase

import com.graceon.core.common.Result
import com.graceon.domain.model.Prayer
import com.graceon.domain.model.WorryContext
import com.graceon.domain.repository.PrescriptionRepository

/**
 * 기도문 생성 UseCase
 */
class GeneratePrayerUseCase(
    private val repository: PrescriptionRepository
) {
    suspend operator fun invoke(
        worryContext: WorryContext,
        verse: String
    ): Result<Prayer> {
        return repository.generatePrayer(worryContext, verse)
    }
}
