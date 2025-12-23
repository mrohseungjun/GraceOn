package com.graceon.domain.usecase

import com.graceon.core.common.Result
import com.graceon.domain.model.Prescription
import com.graceon.domain.model.WorryContext
import com.graceon.domain.repository.PrescriptionRepository

/**
 * 말씀전 생성 UseCase
 */
class GeneratePrescriptionUseCase(
    private val repository: PrescriptionRepository
) {
    suspend operator fun invoke(worryContext: WorryContext): Result<Prescription> {
        return repository.generatePrescription(worryContext)
    }
}
