package com.graceon.feature.saved

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale

private fun formatTimestamp(timestamp: Long, pattern: String): String {
    val formatter = NSDateFormatter().apply {
        locale = NSLocale.currentLocale
        dateFormat = pattern
    }
    val unixSeconds = timestamp.toDouble() / 1000.0
    val appleReferenceOffsetSeconds = 978_307_200.0
    val date = NSDate(timeIntervalSinceReferenceDate = unixSeconds - appleReferenceOffsetSeconds)
    return formatter.stringFromDate(date)
}

internal actual fun formatDate(timestamp: Long): String = formatTimestamp(timestamp, "yyyy.MM.dd")

internal actual fun formatSectionDate(timestamp: Long): String = formatTimestamp(timestamp, "yyyy년 MM월 dd일")
