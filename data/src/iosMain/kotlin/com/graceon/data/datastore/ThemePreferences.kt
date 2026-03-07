package com.graceon.data.datastore

import com.graceon.data.repository.PlatformContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import platform.Foundation.NSUserDefaults

actual class ThemePreferences actual constructor(platformContext: PlatformContext) {
    private val userDefaults = NSUserDefaults.standardUserDefaults
    private val key = "dark_theme_enabled"
    private val state = MutableStateFlow(
        if (userDefaults.objectForKey(key) == null) true else userDefaults.boolForKey(key)
    )

    actual val isDarkThemeEnabled: Flow<Boolean> = state

    actual suspend fun setDarkThemeEnabled(enabled: Boolean) {
        userDefaults.setBool(enabled, forKey = key)
        state.value = enabled
    }
}
