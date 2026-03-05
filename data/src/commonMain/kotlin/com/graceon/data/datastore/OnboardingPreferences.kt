package com.graceon.data.datastore

import com.graceon.data.repository.PlatformContext
import kotlinx.coroutines.flow.Flow

expect class OnboardingPreferences(platformContext: PlatformContext) {
    val isOnboardingCompleted: Flow<Boolean>
    suspend fun setOnboardingCompleted()
}
