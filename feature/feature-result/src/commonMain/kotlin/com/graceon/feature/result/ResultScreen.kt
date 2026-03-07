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
    onShareText: (String) -> Unit = {},
    onShareImage: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ResultContract.Effect.ShareContent -> onShareText(effect.text)
                is ResultContract.Effect.ShareAsImage -> onShareImage()
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
                Color(0xFF07131E)
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
                    onShareImage = { viewModel.handleIntent(ResultContract.Intent.ShareAsImage) },
                    onReset = { viewModel.handleIntent(ResultContract.Intent.Reset) }
                )
            }

            GraceOnBottomBar(
                activeTab = GraceOnBottomTab.Word,
                onHomeClick = onNavigateHome,
                onWordClick = {},
                onSavedClick = onNavigateToSaved,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            )
        }
    }
}

@Composable
private fun VerseHeroCard(state: ResultContract.State) {
    val verseParts = state.prescription.verse.split("(")
    val verseText = verseParts.getOrNull(0)?.trim()?.replace("+", " ") ?: state.prescription.verse
    val verseReference = verseParts.getOrNull(1)?.replace(")", "")?.trim()?.replace("+", " ").orEmpty()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = GlassSurfaceStrong,
        shape = RoundedCornerShape(32.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF1E293B),
                                Color(0xFF0F172A),
                                Color(0xFF05070A)
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
                        color = Color.White.copy(alpha = 0.92f)
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = verseText,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = Color.White,
                            lineHeight = 38.sp
                        )
                        if (verseReference.isNotBlank()) {
                            Spacer(modifier = Modifier.height(18.dp))
                            Text(
                                text = verseReference,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White.copy(alpha = 0.82f)
                            )
                        }
                    }

                    Surface(
                        color = Color.White.copy(alpha = 0.10f),
                        shape = RoundedCornerShape(999.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
                    ) {
                        Text(
                            text = if (state.isAiMode) "AI 맞춤 말씀" else "Daily Grace Card",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
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
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = GlassSurface,
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
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
    onShareImage: () -> Unit,
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
                    containerColor = Color.White,
                    contentColor = Color.Black
                )
            ) {
                if (state.isPrayerLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(22.dp),
                        strokeWidth = 2.dp,
                        color = Color.Black
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
                    contentColor = Color.White,
                    disabledContainerColor = Primary.copy(alpha = 0.22f),
                    disabledContentColor = Color.White
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
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Icon(Icons.Default.IosShare, contentDescription = null)
                Spacer(modifier = Modifier.size(6.dp))
                Text("공유")
            }
        }

        OutlinedButton(
            onClick = onShareImage,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(999.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
        ) {
            Icon(Icons.Default.Create, contentDescription = null)
            Spacer(modifier = Modifier.size(6.dp))
            Text("이미지로 공유")
        }

        Button(
            onClick = onReset,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(999.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = GlassSurfaceStrong,
                contentColor = Color.White
            )
        ) {
            Icon(Icons.Default.Home, contentDescription = null)
            Spacer(modifier = Modifier.size(6.dp))
            Text("홈으로 돌아가기", fontWeight = FontWeight.SemiBold)
        }
    }
}
