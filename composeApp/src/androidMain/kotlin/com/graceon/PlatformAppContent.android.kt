package com.graceon

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
internal actual fun PlatformAppContent(
    apiBaseUrl: String,
    appVersion: String,
    onShareText: (String) -> Unit,
    onToggleDailyVerseNotification: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val dependencies = remember(context) {
        createGraceOnAndroidDependencies(
            context = context,
            apiBaseUrl = apiBaseUrl.ifBlank { BuildConfig.GRACEON_API_BASE_URL }
        )
    }

    GraceOnRoot(
        dependencies = dependencies,
        appVersion = appVersion.ifBlank { BuildConfig.VERSION_NAME },
        onShareText = { text ->
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, text)
                type = "text/plain"
            }
            val chooser = Intent.createChooser(sendIntent, "말씀 공유하기")
            context.startActivity(chooser)
        },
        onToggleDailyVerseNotification = { enabled ->
            DailyVerseNotificationScheduler.update(
                context = context,
                enabled = enabled
            )
        }
    )
}
