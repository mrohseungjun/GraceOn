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

private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_prefs")

actual class ThemePreferences actual constructor(
    platformContext: PlatformContext
) {
    private val context: Context = platformContext.appContext

    private companion object {
        val DARK_THEME_ENABLED = booleanPreferencesKey("dark_theme_enabled")
    }

    actual val isDarkThemeEnabled: Flow<Boolean> =
        context.themeDataStore.data.map { preferences ->
            preferences[DARK_THEME_ENABLED] ?: true
        }

    actual suspend fun setDarkThemeEnabled(enabled: Boolean) {
        context.themeDataStore.edit { preferences ->
            preferences[DARK_THEME_ENABLED] = enabled
        }
    }
}
