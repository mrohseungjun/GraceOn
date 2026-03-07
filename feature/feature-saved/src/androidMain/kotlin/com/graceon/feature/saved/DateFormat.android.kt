package com.graceon.feature.saved

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal actual fun formatDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

internal actual fun formatSectionDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault())
    return formatter.format(Date(timestamp))
}
