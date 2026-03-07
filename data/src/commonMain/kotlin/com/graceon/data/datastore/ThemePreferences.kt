package com.graceon.data.datastore

import com.graceon.data.repository.PlatformContext
import kotlinx.coroutines.flow.Flow

expect class ThemePreferences(platformContext: PlatformContext) {
    val isDarkThemeEnabled: Flow<Boolean>
    suspend fun setDarkThemeEnabled(enabled: Boolean)
}
