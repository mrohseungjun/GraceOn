package com.graceon.data.datastore

import com.graceon.data.repository.PlatformContext
import kotlinx.coroutines.flow.Flow

expect class AuthPreferences(platformContext: PlatformContext) {
    val isAuthenticated: Flow<Boolean>
    suspend fun setAuthenticated()
    suspend fun resetAuthenticated()
}
