package com.graceon.data.repository

import com.graceon.domain.model.SavedPrescription
import com.graceon.domain.repository.SavedPrescriptionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

expect class PlatformContext

internal interface SavedPrescriptionStorage {
    val prescriptionsJsonFlow: Flow<String>
    suspend fun updatePrescriptionsJson(json: String)
}

internal expect fun createSavedPrescriptionStorage(platformContext: PlatformContext): SavedPrescriptionStorage

/**
 * SavedPrescriptionRepository 공용 구현체.
 * 저장소는 expect/actual로 플랫폼별 분기한다.
 */
class SavedPrescriptionRepositoryImpl(
    platformContext: PlatformContext
) : SavedPrescriptionRepository {

    private val storage = createSavedPrescriptionStorage(platformContext)

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    override fun getSavedPrescriptions(): Flow<List<SavedPrescription>> {
        return storage.prescriptionsJsonFlow.map { jsonString ->
            try {
                json.decodeFromString<List<SavedPrescription>>(jsonString)
            } catch (_: Exception) {
                emptyList()
            }
        }
    }

    override suspend fun savePrescription(prescription: SavedPrescription) {
        val currentList = getSavedPrescriptions().first().toMutableList()

        val existingIndex = currentList.indexOfFirst { it.verse == prescription.verse }
        if (existingIndex >= 0) {
            currentList[existingIndex] = prescription
        } else {
            currentList.add(0, prescription)
        }

        storage.updatePrescriptionsJson(json.encodeToString(currentList))
    }

    override suspend fun deletePrescription(id: String) {
        val currentList = getSavedPrescriptions().first().toMutableList()
        currentList.removeAll { it.id == id }
        storage.updatePrescriptionsJson(json.encodeToString(currentList))
    }

    override suspend fun isPrescriptionSaved(verse: String): Boolean {
        return getSavedPrescriptions().first().any { it.verse == verse }
    }

    suspend fun clearAll() {
        storage.updatePrescriptionsJson("[]")
    }
}
