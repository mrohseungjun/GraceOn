package com.graceon.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GraceOnScaffold(
    title: String? = null,
    titleContent: @Composable (() -> Unit)? = null,
    onNavigateBack: (() -> Unit)? = null,
    snackbarHostState: SnackbarHostState? = null,
    snackbarAlignment: Alignment = Alignment.BottomCenter,
    snackbarPadding: PaddingValues = PaddingValues(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 28.dp),
    backgroundBrush: Brush? = null,
    centerAlignedTopBar: Boolean = true,
    topBarContainerColor: Color = Color.Transparent,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    val resolvedTitle: @Composable () -> Unit = {
        when {
            titleContent != null -> titleContent()
            title != null -> {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                if (title != null || onNavigateBack != null) {
                    if (centerAlignedTopBar) {
                        CenterAlignedTopAppBar(
                            title = resolvedTitle,
                            navigationIcon = {
                                if (onNavigateBack != null) {
                                    IconButton(onClick = onNavigateBack) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "뒤로가기"
                                        )
                                    }
                                }
                            },
                            actions = actions,
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = topBarContainerColor,
                                titleContentColor = MaterialTheme.colorScheme.onBackground,
                                navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                                actionIconContentColor = MaterialTheme.colorScheme.onBackground
                            )
                        )
                    } else {
                        TopAppBar(
                            title = resolvedTitle,
                            navigationIcon = {
                                if (onNavigateBack != null) {
                                    IconButton(onClick = onNavigateBack) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "뒤로가기"
                                        )
                                    }
                                }
                            },
                            actions = actions,
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = topBarContainerColor,
                                titleContentColor = MaterialTheme.colorScheme.onBackground,
                                navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                                actionIconContentColor = MaterialTheme.colorScheme.onBackground
                            )
                        )
                    }
                }
            },
            snackbarHost = {},
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            if (backgroundBrush == null) {
                content(paddingValues)
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundBrush)
                ) {
                    content(paddingValues)
                }
            }
        }

        snackbarHostState?.let { hostState ->
            SnackbarHost(
                hostState = hostState,
                modifier = Modifier
                    .align(snackbarAlignment)
                    .padding(snackbarPadding)
            ) { data ->
                Snackbar(
                    snackbarData = data,
                    modifier = Modifier.widthIn(max = 520.dp),
                    shape = RoundedCornerShape(18.dp),
                    containerColor = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.96f),
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                    actionColor = MaterialTheme.colorScheme.primary,
                    dismissActionContentColor = MaterialTheme.colorScheme.inverseOnSurface
                )
            }
        }
    }
}
