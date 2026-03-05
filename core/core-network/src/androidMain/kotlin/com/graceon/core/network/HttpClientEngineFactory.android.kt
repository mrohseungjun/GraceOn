package com.graceon.core.network

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.android.Android

internal actual fun platformHttpClientEngineFactory(): HttpClientEngineFactory<*> = Android
