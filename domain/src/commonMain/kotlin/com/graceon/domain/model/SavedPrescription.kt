package com.graceon.domain.model

import com.graceon.domain.util.currentTimeMillis
import kotlinx.serialization.Serializable

/**
 * 저장된 말씀전 모델
 */
@Serializable
data class SavedPrescription(
    val id: String,
    val verse: String,
    val message: String,
    val prayer: String? = null,
    val categoryId: String? = null,
    val savedAt: Long = currentTimeMillis()
)
