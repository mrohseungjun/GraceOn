package com.graceon.domain.usecase

import com.graceon.domain.model.SavedPrescription
import com.graceon.domain.repository.SavedPrescriptionRepository
import kotlinx.coroutines.flow.Flow

/**
 * 저장된 처방전 목록 조회 UseCase
 */
class GetSavedPrescriptionsUseCase(
    private val repository: SavedPrescriptionRepository
) {
    operator fun invoke(): Flow<List<SavedPrescription>> {
        return repository.getSavedPrescriptions()
    }
}
