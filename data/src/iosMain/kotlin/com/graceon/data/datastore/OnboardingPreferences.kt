package com.graceon.data.datastore

import com.graceon.data.repository.PlatformContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import platform.Foundation.NSUserDefaults

actual class OnboardingPreferences actual constructor(platformContext: PlatformContext) {
    private val userDefaults = NSUserDefaults.standardUserDefaults
    private val key = "onboarding_completed"
    private val state = MutableStateFlow(userDefaults.boolForKey(key))

    actual val isOnboardingCompleted: Flow<Boolean> = state

    actual suspend fun setOnboardingCompleted() {
        userDefaults.setBool(true, forKey = key)
        state.value = true
    }
}
