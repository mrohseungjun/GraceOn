package com.graceon.domain.repository

import com.graceon.domain.model.SavedPrescription
import kotlinx.coroutines.flow.Flow

/**
 * 저장된 처방전 Repository 인터페이스
 */
interface SavedPrescriptionRepository {
    
    /**
     * 모든 저장된 처방전 조회
     */
    fun getSavedPrescriptions(): Flow<List<SavedPrescription>>
    
    /**
     * 처방전 저장
     */
    suspend fun savePrescription(prescription: SavedPrescription)
    
    /**
     * 처방전 삭제
     */
    suspend fun deletePrescription(id: String)
    
    /**
     * 처방전이 저장되어 있는지 확인
     */
    suspend fun isPrescriptionSaved(verse: String): Boolean
}
