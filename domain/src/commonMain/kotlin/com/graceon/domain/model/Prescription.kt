package com.graceon.domain.model

import kotlinx.serialization.Serializable

/**
 * AI 말씀전 결과 모델
 */
@Serializable
data class Prescription(
    val verse: String,
    val message: String
)

/**
 * 기도문 모델
 */
data class Prayer(
    val text: String
)

/**
 * 사용자 고민 컨텍스트
 */
data class WorryContext(
    val categoryId: String? = null,
    val detailId: String? = null,
    val customWorry: String? = null,
    val isAiMode: Boolean = false
) {
    fun toPromptText(): String {
        return if (isAiMode && customWorry != null) {
            customWorry
        } else {
            "Category: $categoryId, Detail: $detailId"
        }
    }
}
