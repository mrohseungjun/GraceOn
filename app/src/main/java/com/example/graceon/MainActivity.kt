package com.example.graceon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.graceon.core.ui.theme.GraceOnTheme
import com.graceon.data.datastore.OnboardingPreferences
import com.graceon.navigation.NavGraph
import com.graceon.navigation.Screen
import kotlinx.coroutines.launch

/**
 * Main Activity - Entry point of GraceOn App
 */
class MainActivity : ComponentActivity() {
    
    private lateinit var onboardingPreferences: OnboardingPreferences
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        onboardingPreferences = OnboardingPreferences(this)
        
        setContent {
            val isOnboardingCompleted by onboardingPreferences.isOnboardingCompleted.collectAsState(initial = null)
            
            GraceOnTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (isOnboardingCompleted) {
                        null -> {
                            // Loading state
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                        else -> {
                            val navController = rememberNavController()
                            val coroutineScope = rememberCoroutineScope()
                            val startDestination = if (isOnboardingCompleted == true) {
                                Screen.Worry.route
                            } else {
                                Screen.Onboarding.route
                            }
                            NavGraph(
                                navController = navController,
                                startDestination = startDestination,
                                onOnboardingComplete = {
                                    coroutineScope.launch {
                                        onboardingPreferences.setOnboardingCompleted()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}