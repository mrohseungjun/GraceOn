package com.graceon.core.common

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Dispatcher provider for dependency injection
 */
interface DispatcherProvider {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
}

class DefaultDispatcherProvider : DispatcherProvider {
    override val main: CoroutineDispatcher = defaultMainDispatcher()
    override val io: CoroutineDispatcher = defaultIoDispatcher()
    override val default: CoroutineDispatcher = defaultDefaultDispatcher()
}

internal expect fun defaultMainDispatcher(): CoroutineDispatcher
internal expect fun defaultIoDispatcher(): CoroutineDispatcher
internal expect fun defaultDefaultDispatcher(): CoroutineDispatcher
