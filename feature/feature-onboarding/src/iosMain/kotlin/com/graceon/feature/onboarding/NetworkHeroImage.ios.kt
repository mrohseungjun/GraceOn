package com.graceon.feature.onboarding

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Image
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class)
@Composable
internal actual fun NetworkHeroImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier
) {
    val imageBitmap by produceState<ImageBitmap?>(initialValue = null, url) {
        value = withContext(Dispatchers.Default) {
            runCatching {
                val nsUrl = NSURL.URLWithString(url) ?: return@runCatching null
                val data = NSData.dataWithContentsOfURL(nsUrl) ?: return@runCatching null
                val bytes = ByteArray(data.length.toInt())
                bytes.usePinned { pinned ->
                    memcpy(pinned.addressOf(0), data.bytes, data.length)
                }
                Image.makeFromEncoded(bytes).toComposeImageBitmap()
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
