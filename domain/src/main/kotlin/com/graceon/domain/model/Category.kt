package com.graceon.domain.model

/**
 * 고민 카테고리 도메인 모델
 */
data class Category(
    val id: String,
    val title: String,
    val iconType: IconType,
    val colorType: ColorType,
    val details: List<DetailWorry>
)

data class DetailWorry(
    val id: String,
    val title: String,
    val defaultVerse: String
)

enum class IconType {
    BRIEFCASE,  // 진로/직장
    USER,       // 인간관계
    SUN,        // 삶/미래
    HEART       // 신앙/마음
}

enum class ColorType {
    BLUE,
    PINK,
    YELLOW,
    PURPLE
}
