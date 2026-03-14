package com.graceon.core.network

import kotlinx.serialization.Serializable

@Serializable
data class AppUpdateConfigResponse(
    val latestVersion: String = "",
    val minimumSupportedVersion: String = "",
    val optionalTitle: String = "새 버전이 있어요",
    val optionalMessage: String = "업데이트하고 더 안정적인 GraceOn을 사용해보세요.",
    val requiredTitle: String = "업데이트가 필요해요",
    val requiredMessage: String = "계속 사용하려면 최신 버전으로 업데이트해주세요."
)
