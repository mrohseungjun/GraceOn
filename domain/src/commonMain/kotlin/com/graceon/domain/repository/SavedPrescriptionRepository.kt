package com.graceon.domain.repository

import com.graceon.domain.model.SavedPrescription
import kotlinx.coroutines.flow.Flow

/**
 * 저장된 말씀전 Repository 인터페이스
 */
interface SavedPrescriptionRepository {
    
    /**
     * 모든 저장된 말씀전 조회
     */
    fun getSavedPrescriptions(): Flow<List<SavedPrescription>>
    
    /**
     * 말씀전 저장
     */
    suspend fun savePrescription(prescription: SavedPrescription)
    
    /**
     * 말씀전 삭제
     */
    suspend fun deletePrescription(id: String)
    
    /**
     * 말씀전이 저장되어 있는지 확인
     */
    suspend fun isPrescriptionSaved(verse: String): Boolean
}
