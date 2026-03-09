package com.example.graceon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.graceon.App
import com.graceon.core.network.handleSupabaseAuthCallbackUrl

/**
 * Main Activity - ㅇEntry point of GraceOn App
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        intent?.dataString?.let(::handleSupabaseAuthCallbackUrl)

        setContent {
            App()
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.dataString?.let(::handleSupabaseAuthCallbackUrl)
    }
}
