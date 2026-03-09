package com.graceon.domain.util

internal fun currentTimeMillis(): Long = kotlin.time.Clock.System.now().toEpochMilliseconds()
