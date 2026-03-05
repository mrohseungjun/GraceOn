package com.graceon

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.graceon.navigation.NavGraph
import com.graceon.navigation.Screen
import kotlinx.coroutines.launch

@Composable
internal fun GraceOnRoot(
    dependencies: GraceOnDependencies,
    onShareText: (String) -> Unit = {},
    onShareImage: () -> Unit = {}
) {
    val isOnboardingCompleted by dependencies.onboardingPreferences
        .isOnboardingCompleted
        .collectAsState(initial = null)

    when (isOnboardingCompleted) {
        null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        else -> {
            val coroutineScope = rememberCoroutineScope()
            val startDestination = if (isOnboardingCompleted == true) {
                Screen.WORRY
            } else {
                Screen.ONBOARDING
            }

            NavGraph(
                startDestination = startDestination,
                dependencies = dependencies,
                onShareText = onShareText,
                onShareImage = onShareImage,
                onOnboardingComplete = {
                    coroutineScope.launch {
                        dependencies.onboardingPreferences.setOnboardingCompleted()
                    }
                }
            )
        }
    }
}
