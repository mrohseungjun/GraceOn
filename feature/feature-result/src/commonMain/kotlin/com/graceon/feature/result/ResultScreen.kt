package com.graceon.feature.result

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.shadow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.graceon.core.ui.component.GraceOnAmbientBackground
import com.graceon.core.ui.component.GraceOnBottomBar
import com.graceon.core.ui.component.GraceOnBottomTab
import com.graceon.core.ui.component.GraceOnScaffold
import com.graceon.core.ui.theme.GlassBorder
import com.graceon.core.ui.theme.GlassSurface
import com.graceon.core.ui.theme.GlassSurfaceStrong
import com.graceon.core.ui.theme.Primary

@Composable
fun ResultScreen(
    viewModel: ResultViewModel,
    onNavigateBack: () -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateToSaved: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onShareText: (String) -> Unit = {},
    onRetry: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ResultContract.Effect.ShareContent -> onShareText(effect.text)
                is ResultContract.Effect.NavigateToHome -> onNavigateHome()
                is ResultContract.Effect.ShowSaveSuccess -> snackbarHostState.showSnackbar("말씀이 저장되었습니다")
                is ResultContract.Effect.ShowError -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    GraceOnScaffold(
        title = "오늘의 말씀",
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState,
        backgroundBrush = Brush.verticalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.background,
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
            )
        ),
        topBarContainerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            GraceOnAmbientBackground()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                VerseHeroCard(state = state)
                InsightCard(
                    title = "AI의 한마디",
                    text = state.prescription.message.replace("+", " ")
                )
                AnimatedVisibility(
                    visible = state.prayer != null,
                    enter = expandVertically() + fadeIn()
                ) {
                    InsightCard(
                        title = "기도문",
                        text = state.prayer?.text.orEmpty()
                    )
                }
                ResultActions(
                    state = state,
                    onGeneratePrayer = { viewModel.handleIntent(ResultContract.Intent.GeneratePrayer) },
                    onSave = { viewModel.handleIntent(ResultContract.Intent.SavePrescription) },
                    onShare = { viewModel.handleIntent(ResultContract.Intent.SharePrescription) },
                    onRetry = onRetry,
                    onReset = { viewModel.handleIntent(ResultContract.Intent.Reset) }
                )
            }

            GraceOnBottomBar(
                activeTab = GraceOnBottomTab.Word,
                onHomeClick = onNavigateHome,
                onWordClick = {},
                onSavedClick = onNavigateToSaved,
                onProfileClick = onNavigateToProfile,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            )
        }
    }
}

@Composable
private fun VerseHeroCard(state: ResultContract.State) {
    val isLightTheme = MaterialTheme.colorScheme.background.luminance() > 0.5f
    val verseParts = state.prescription.verse.split("(")
    val verseText = verseParts.getOrNull(0)?.trim()?.replace("+", " ") ?: state.prescription.verse
    val verseReference = verseParts.getOrNull(1)?.replace(")", "")?.trim()?.replace("+", " ").orEmpty()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isLightTheme) 14.dp else 4.dp,
                shape = RoundedCornerShape(32.dp),
                clip = false
            ),
        color = GlassSurfaceStrong,
        shape = RoundedCornerShape(32.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isLightTheme) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.78f) else GlassBorder
        )
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Primary.copy(alpha = 0.22f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "\"",
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.92f)
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = verseText,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            lineHeight = 38.sp
                        )
                        if (verseReference.isNotBlank()) {
                            Spacer(modifier = Modifier.height(18.dp))
                            Text(
                                text = verseReference,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f)
                            )
                        }
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(999.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Text(
                            text = if (state.isAiMode) "AI 맞춤 말씀" else "Daily Grace Card",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InsightCard(
    title: String,
    text: String
) {
    val isLightTheme = MaterialTheme.colorScheme.background.luminance() > 0.5f
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isLightTheme) 8.dp else 2.dp,
                shape = RoundedCornerShape(24.dp),
                clip = false
            ),
        color = GlassSurface,
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isLightTheme) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f) else GlassBorder
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 28.sp
            )
        }
    }
}

@Composable
private fun ResultActions(
    state: ResultContract.State,
    onGeneratePrayer: () -> Unit,
    onSave: () -> Unit,
    onShare: () -> Unit,
    onRetry: () -> Unit,
    onReset: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (state.prayer == null) {
            Button(
                onClick = onGeneratePrayer,
                enabled = !state.isPrayerLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                if (state.isPrayerLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(22.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("기도문 만들기", fontWeight = FontWeight.Bold)
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onSave,
                enabled = !state.isSaved,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.isSaved) Primary.copy(alpha = 0.22f) else GlassSurfaceStrong,
                    contentColor = MaterialTheme.colorScheme.onBackground,
                    disabledContainerColor = Primary.copy(alpha = 0.22f),
                    disabledContentColor = MaterialTheme.colorScheme.onBackground
                )
            ) {
                Icon(
                    imageVector = if (state.isSaved) Icons.Default.Check else Icons.Default.Bookmark,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.size(6.dp))
                Text(if (state.isSaved) "저장됨" else "저장")
            }

            OutlinedButton(
                onClick = onShare,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(999.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onBackground)
            ) {
                Icon(Icons.Default.IosShare, contentDescription = null)
                Spacer(modifier = Modifier.size(6.dp))
                Text("공유")
            }
        }

        OutlinedButton(
            onClick = onRetry,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(999.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onBackground)
        ) {
            Icon(Icons.Default.Create, contentDescription = null)
            Spacer(modifier = Modifier.size(6.dp))
            Text("다시 받기")
        }

        Button(
            onClick = onReset,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(999.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = GlassSurfaceStrong,
                contentColor = MaterialTheme.colorScheme.onBackground
            )
        ) {
            Icon(Icons.Default.Home, contentDescription = null)
            Spacer(modifier = Modifier.size(6.dp))
            Text("홈으로 돌아가기", fontWeight = FontWeight.SemiBold)
        }
    }
}
