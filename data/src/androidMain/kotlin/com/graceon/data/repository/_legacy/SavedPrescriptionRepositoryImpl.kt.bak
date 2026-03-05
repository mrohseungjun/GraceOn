package com.graceon.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.graceon.domain.model.SavedPrescription
import com.graceon.domain.repository.SavedPrescriptionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "saved_prescriptions")

/**
 * SavedPrescriptionRepository 구현체 (DataStore 사용)
 */
class SavedPrescriptionRepositoryImpl(
    private val context: Context
) : SavedPrescriptionRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    companion object {
        private val PRESCRIPTIONS_KEY = stringPreferencesKey("prescriptions")
    }

    override fun getSavedPrescriptions(): Flow<List<SavedPrescription>> {
        return context.dataStore.data.map { preferences ->
            val jsonString = preferences[PRESCRIPTIONS_KEY] ?: "[]"
            try {
                json.decodeFromString<List<SavedPrescription>>(jsonString)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    override suspend fun savePrescription(prescription: SavedPrescription) {
        context.dataStore.edit { preferences ->
            val currentList = try {
                val jsonString = preferences[PRESCRIPTIONS_KEY] ?: "[]"
                json.decodeFromString<List<SavedPrescription>>(jsonString).toMutableList()
            } catch (e: Exception) {
                mutableListOf()
            }
            
            // 중복 체크 (같은 verse가 있으면 업데이트)
            val existingIndex = currentList.indexOfFirst { it.verse == prescription.verse }
            if (existingIndex >= 0) {
                currentList[existingIndex] = prescription
            } else {
                currentList.add(0, prescription) // 최신 것이 맨 위로
            }
            
            preferences[PRESCRIPTIONS_KEY] = json.encodeToString(currentList)
        }
    }

    override suspend fun deletePrescription(id: String) {
        context.dataStore.edit { preferences ->
            val currentList = try {
                val jsonString = preferences[PRESCRIPTIONS_KEY] ?: "[]"
                json.decodeFromString<List<SavedPrescription>>(jsonString).toMutableList()
            } catch (e: Exception) {
                mutableListOf()
            }
            
            currentList.removeAll { it.id == id }
            preferences[PRESCRIPTIONS_KEY] = json.encodeToString(currentList)
        }
    }

    override suspend fun isPrescriptionSaved(verse: String): Boolean {
        val prescriptions = context.dataStore.data.first()
        val jsonString = prescriptions[PRESCRIPTIONS_KEY] ?: "[]"
        return try {
            val list = json.decodeFromString<List<SavedPrescription>>(jsonString)
            list.any { it.verse == verse }
        } catch (e: Exception) {
            false
        }
    }
}
