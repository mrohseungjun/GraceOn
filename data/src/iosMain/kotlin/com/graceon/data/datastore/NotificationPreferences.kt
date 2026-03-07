package com.graceon.data.datastore

import com.graceon.data.repository.PlatformContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import platform.Foundation.NSUserDefaults

actual class NotificationPreferences actual constructor(platformContext: PlatformContext) {
    private val userDefaults = NSUserDefaults.standardUserDefaults
    private val key = "daily_verse_notification_enabled"
    private val state = MutableStateFlow(userDefaults.boolForKey(key))

    actual val isDailyVerseNotificationEnabled: Flow<Boolean> = state

    actual suspend fun setDailyVerseNotificationEnabled(enabled: Boolean) {
        userDefaults.setBool(enabled, forKey = key)
        state.value = enabled
    }
}
