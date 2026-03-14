package com.graceon.update

internal enum class AppPlatform {
    Android,
    Ios
}

internal enum class AppUpdateType {
    Optional,
    Required
}

internal data class AppUpdateConfig(
    val latestVersion: String = "",
    val minimumSupportedVersion: String = "",
    val optionalTitle: String = "새 버전이 있어요",
    val optionalMessage: String = "업데이트하고 더 안정적인 GraceOn을 사용해보세요.",
    val requiredTitle: String = "업데이트가 필요해요",
    val requiredMessage: String = "계속 사용하려면 최신 버전으로 업데이트해주세요."
)

internal data class AppUpdatePrompt(
    val type: AppUpdateType,
    val title: String,
    val message: String,
    val targetVersion: String,
    val storeUrl: String
)

internal fun AppUpdateConfig.resolvePrompt(
    currentVersion: String,
    storeUrl: String
): AppUpdatePrompt? {
    if (currentVersion.isBlank() || storeUrl.isBlank()) return null

    val requiredComparison = minimumSupportedVersion.compareAppVersion(currentVersion)
    if (minimumSupportedVersion.isNotBlank() && requiredComparison > 0) {
        return AppUpdatePrompt(
            type = AppUpdateType.Required,
            title = requiredTitle,
            message = requiredMessage,
            targetVersion = minimumSupportedVersion,
            storeUrl = storeUrl
        )
    }

    val latestComparison = latestVersion.compareAppVersion(currentVersion)
    if (latestVersion.isNotBlank() && latestComparison > 0) {
        return AppUpdatePrompt(
            type = AppUpdateType.Optional,
            title = optionalTitle,
            message = optionalMessage,
            targetVersion = latestVersion,
            storeUrl = storeUrl
        )
    }

    return null
}

private fun String.compareAppVersion(currentVersion: String): Int {
    val targetParts = toVersionParts()
    val currentParts = currentVersion.toVersionParts()
    val maxSize = maxOf(targetParts.size, currentParts.size)

    for (index in 0 until maxSize) {
        val targetPart = targetParts.getOrElse(index) { 0 }
        val currentPart = currentParts.getOrElse(index) { 0 }
        if (targetPart != currentPart) {
            return targetPart.compareTo(currentPart)
        }
    }

    return 0
}

private fun String.toVersionParts(): List<Int> =
    Regex("\\d+")
        .findAll(this)
        .map { match -> match.value.toIntOrNull() ?: 0 }
        .toList()
