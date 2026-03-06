package com.graceon

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
internal actual fun PlatformAppContent(apiKey: String) {
    val context = LocalContext.current
    val dependencies = remember(context) {
        createGraceOnAndroidDependencies(
            context = context,
            apiKey = apiKey.ifBlank { BuildConfig.GEMINI_API_KEY }
        )
    }

    GraceOnRoot(
        dependencies = dependencies,
        onShareText = { text ->
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, text)
                type = "text/plain"
            }
            val chooser = Intent.createChooser(sendIntent, "말씀 공유하기")
            context.startActivity(chooser)
        }
    )
}
