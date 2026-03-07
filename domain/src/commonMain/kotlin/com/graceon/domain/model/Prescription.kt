package com.graceon.domain.model

import kotlinx.serialization.Serializable

const val RANDOM_VERSE_PROMPT =
    "오늘 하루의 감정이나 고민과 무관하게 누구에게나 힘이 될 수 있는 성경 말씀 하나를 추천해줘."

const val RANDOM_VERSE_DISPLAY_TEXT = "오늘 내게 주시는 말씀"

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
