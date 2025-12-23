package com.graceon.domain.usecase

import com.graceon.domain.repository.SavedPrescriptionRepository

/**
 * 말씀전 삭제 UseCase
 */
class DeletePrescriptionUseCase(
    private val repository: SavedPrescriptionRepository
) {
    suspend operator fun invoke(id: String) {
        repository.deletePrescription(id)
    }
}
