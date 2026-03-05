package com.graceon.core.common

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal actual fun defaultMainDispatcher(): CoroutineDispatcher = Dispatchers.Main
internal actual fun defaultIoDispatcher(): CoroutineDispatcher = Dispatchers.Default
internal actual fun defaultDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default
