package com.graceon.data.datastore

import com.graceon.data.repository.PlatformContext
import kotlinx.coroutines.flow.Flow

expect class NotificationPreferences(platformContext: PlatformContext) {
    val isDailyVerseNotificationEnabled: Flow<Boolean>
    suspend fun setDailyVerseNotificationEnabled(enabled: Boolean)
}
