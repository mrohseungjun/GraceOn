package com.graceon.feature.onboarding

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

@Composable
internal actual fun NetworkHeroImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier
) {
    val imageBitmap by produceState<androidx.compose.ui.graphics.ImageBitmap?>(initialValue = null, url) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                URL(url).openStream().use { stream ->
                    BitmapFactory.decodeStream(stream)?.asImageBitmap()
                }
            }.getOrNull()
        }
    }

    imageBitmap?.let { bitmap ->
        Image(
            bitmap = bitmap,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    }
}
