package com.graceon.domain.repository

import com.graceon.core.common.Result
import com.graceon.domain.model.Prayer
import com.graceon.domain.model.Prescription
import com.graceon.domain.model.WorryContext

/**
 * 처방전 Repository 인터페이스 (Clean Architecture)
 */
interface PrescriptionRepository {
    
    /**
     * AI로부터 처방전(말씀 + 메시지) 생성
     */
    suspend fun generatePrescription(worryContext: WorryContext): Result<Prescription>
    
    /**
     * AI로부터 기도문 생성
     */
    suspend fun generatePrayer(
        worryContext: WorryContext,
        verse: String
    ): Result<Prayer>
}
