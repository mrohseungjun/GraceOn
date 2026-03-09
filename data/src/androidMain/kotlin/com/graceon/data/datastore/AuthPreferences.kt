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

private val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

actual class AuthPreferences actual constructor(
    platformContext: PlatformContext
) {
    private val context: Context = platformContext.appContext

    private companion object {
        val AUTH_COMPLETED = booleanPreferencesKey("auth_completed")
    }

    actual val isAuthenticated: Flow<Boolean> = context.authDataStore.data.map { preferences ->
        preferences[AUTH_COMPLETED] ?: false
    }

    actual suspend fun setAuthenticated() {
        context.authDataStore.edit { preferences ->
            preferences[AUTH_COMPLETED] = true
        }
    }

    actual suspend fun resetAuthenticated() {
        context.authDataStore.edit { preferences ->
            preferences[AUTH_COMPLETED] = false
        }
    }
}
