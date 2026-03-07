package com.graceon.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.graceon.data.repository.PlatformContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.notificationDataStore: DataStore<Preferences> by preferencesDataStore(name = "notification_prefs")

actual class NotificationPreferences actual constructor(
    platformContext: PlatformContext
) {
    private val context: Context = platformContext.appContext

    private companion object {
        val DAILY_VERSE_NOTIFICATION_ENABLED = booleanPreferencesKey("daily_verse_notification_enabled")
    }

    actual val isDailyVerseNotificationEnabled: Flow<Boolean> =
        context.notificationDataStore.data.map { preferences ->
            preferences[DAILY_VERSE_NOTIFICATION_ENABLED] ?: false
        }

    actual suspend fun setDailyVerseNotificationEnabled(enabled: Boolean) {
        context.notificationDataStore.edit { preferences ->
            preferences[DAILY_VERSE_NOTIFICATION_ENABLED] = enabled
        }
    }
}
