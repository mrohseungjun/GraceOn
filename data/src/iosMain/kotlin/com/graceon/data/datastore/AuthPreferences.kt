package com.graceon.data.datastore

import com.graceon.data.repository.PlatformContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import platform.Foundation.NSUserDefaults

actual class AuthPreferences actual constructor(platformContext: PlatformContext) {
    private val userDefaults = NSUserDefaults.standardUserDefaults
    private val key = "auth_completed"
    private val state = MutableStateFlow(userDefaults.boolForKey(key))

    actual val isAuthenticated: Flow<Boolean> = state

    actual suspend fun setAuthenticated() {
        userDefaults.setBool(true, forKey = key)
        state.value = true
    }

    actual suspend fun resetAuthenticated() {
        userDefaults.setBool(false, forKey = key)
        state.value = false
    }
}
