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

private val Context.onboardingDataStore: DataStore<Preferences> by preferencesDataStore(name = "onboarding_prefs")

actual class OnboardingPreferences actual constructor(
    platformContext: PlatformContext
) {
    private val context: Context = platformContext.appContext

    private companion object {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }

    actual val isOnboardingCompleted: Flow<Boolean> = context.onboardingDataStore.data.map { preferences ->
        preferences[ONBOARDING_COMPLETED] ?: false
    }

    actual suspend fun setOnboardingCompleted() {
        context.onboardingDataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = true
        }
    }
}
