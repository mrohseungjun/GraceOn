package com.graceon.core.common

fun Throwable.toUserFacingMessage(defaultMessage: String): String {
    val rawMessage = message.orEmpty()
    val normalized = rawMessage.lowercase()
    val className = this::class.simpleName.orEmpty().lowercase()

    return when {
        "api key is missing" in normalized -> "앱 설정이 올바르지 않습니다. 잠시 후 다시 시도해주세요."
        "timeout" in normalized || "timeout" in className -> {
            "응답이 지연되고 있습니다. 네트워크 상태를 확인한 뒤 다시 시도해주세요."
        }
        "unable to resolve host" in normalized ||
            "failed to connect" in normalized ||
            "network is unreachable" in normalized ||
            "no address associated with hostname" in normalized ||
            "offline" in normalized ||
            "host lookup" in normalized ||
            "connectexception" in className ||
            "ioexception" in className -> {
            "인터넷 연결을 확인한 뒤 다시 시도해주세요."
        }
        else -> defaultMessage
    }
}
