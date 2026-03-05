package com.graceon.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import platform.Foundation.NSUserDefaults

actual class PlatformContext

private const val PRESCRIPTIONS_KEY = "saved_prescriptions_json"

private class IosSavedPrescriptionStorage : SavedPrescriptionStorage {
    private val userDefaults = NSUserDefaults.standardUserDefaults
    private val state = MutableStateFlow(userDefaults.stringForKey(PRESCRIPTIONS_KEY) ?: "[]")

    override val prescriptionsJsonFlow: Flow<String> = state

    override suspend fun updatePrescriptionsJson(json: String) {
        userDefaults.setObject(json, forKey = PRESCRIPTIONS_KEY)
        state.value = json
    }
}

internal actual fun createSavedPrescriptionStorage(platformContext: PlatformContext): SavedPrescriptionStorage {
    return IosSavedPrescriptionStorage()
}
