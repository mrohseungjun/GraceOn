package com.graceon.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

actual class PlatformContext(val appContext: Context)

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "saved_prescriptions")
private val PRESCRIPTIONS_KEY = stringPreferencesKey("prescriptions")

private class AndroidSavedPrescriptionStorage(
    context: Context
) : SavedPrescriptionStorage {
    private val dataStore = context.dataStore

    override val prescriptionsJsonFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[PRESCRIPTIONS_KEY] ?: "[]"
    }

    override suspend fun updatePrescriptionsJson(json: String) {
        dataStore.edit { preferences ->
            preferences[PRESCRIPTIONS_KEY] = json
        }
    }
}

internal actual fun createSavedPrescriptionStorage(platformContext: PlatformContext): SavedPrescriptionStorage {
    return AndroidSavedPrescriptionStorage(platformContext.appContext)
}
