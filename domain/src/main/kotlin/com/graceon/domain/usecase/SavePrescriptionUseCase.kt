package com.graceon.domain.usecase

import com.graceon.domain.model.SavedPrescription
import com.graceon.domain.repository.SavedPrescriptionRepository

/**
 * 처방전 저장 UseCase
 */
class SavePrescriptionUseCase(
    private val repository: SavedPrescriptionRepository
) {
    suspend operator fun invoke(prescription: SavedPrescription) {
        repository.savePrescription(prescription)
    }
}
