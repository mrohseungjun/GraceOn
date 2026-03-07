package com.graceon.feature.saved

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

private fun formatTimestamp(timestamp: Long, includeKoreanSuffix: Boolean): String {
    val localDateTime = Instant.fromEpochMilliseconds(timestamp)
        .toLocalDateTime(TimeZone.currentSystemDefault())

    val year = localDateTime.year.toString().padStart(4, '0')
    val month = localDateTime.monthNumber.toString().padStart(2, '0')
    val day = localDateTime.dayOfMonth.toString().padStart(2, '0')

    return if (includeKoreanSuffix) {
        "${year}년 ${month}월 ${day}일"
    } else {
        "$year.$month.$day"
    }
}

internal actual fun formatDate(timestamp: Long): String = formatTimestamp(timestamp, includeKoreanSuffix = false)

internal actual fun formatSectionDate(timestamp: Long): String = formatTimestamp(timestamp, includeKoreanSuffix = true)
