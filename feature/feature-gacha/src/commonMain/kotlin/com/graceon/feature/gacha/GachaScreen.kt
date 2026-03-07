package com.graceon.feature.gacha

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.graceon.core.ui.component.GraceOnAmbientBackground
import com.graceon.core.ui.component.GraceOnScaffold
import com.graceon.core.ui.theme.GlassBorder
import com.graceon.core.ui.theme.GlassSurfaceStrong
import com.graceon.core.ui.theme.Primary
import com.graceon.core.ui.theme.Tertiary
import com.graceon.domain.model.Prescription

@Composable
fun GachaScreen(
    viewModel: GachaViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToResult: (Prescription, String?, String?, String?, Boolean) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.handleIntent(GachaContract.Intent.PullLever)
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is GachaContract.Effect.NavigateToResult -> {
                    onNavigateToResult(
                        effect.prescription,
                        effect.categoryId,
                        effect.detailId,
                        effect.customWorry,
                        effect.isAiMode
                    )
                }
                is GachaContract.Effect.ShowError -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    GraceOnScaffold(
        title = null,
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
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                LoadingArtwork(stage = state.stage)

                Spacer(modifier = Modifier.height(28.dp))

                AnimatedContent(
                    targetState = state.stage,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "gacha_stage"
                ) { stage ->
                    val copy = stageCopy(stage = stage, hasError = state.error != null)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = copy.title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center,
                            lineHeight = 34.sp
                        )
                        Text(
                            text = copy.description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 24.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(26.dp))

                ProgressPanel(progress = state.stage.progress())

                Spacer(modifier = Modifier.height(16.dp))

                TipPanel(
                    message = tipMessage(
                        stage = state.stage,
                        hasError = state.error != null
                    )
                )

                if (state.error != null && state.stage == GachaContract.State.Stage.Idle) {
                    Spacer(modifier = Modifier.height(28.dp))

                    Button(
                        onClick = { viewModel.handleIntent(GachaContract.Intent.PullLever) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp),
                        shape = RoundedCornerShape(999.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("다시 시도", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingArtwork(stage: GachaContract.State.Stage) {
    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(220.dp)
                .alpha(0.8f)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Primary.copy(alpha = 0.28f),
                            Color.Transparent
                        )
                    ),
                    RoundedCornerShape(999.dp)
                )
        )

        Surface(
            modifier = Modifier.size(124.dp),
                    color = GlassSurfaceStrong,
            shape = RoundedCornerShape(999.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (stage == GachaContract.State.Stage.Idle) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(38.dp)
                    )
                } else {
                    CircularProgressIndicator(
                        modifier = Modifier.size(40.dp),
                        strokeWidth = 3.dp,
                        color = Primary
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgressPanel(progress: Float) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = GlassSurfaceStrong,
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "은혜를 불러오는 중...",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(999.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(10.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Primary, Tertiary)
                            ),
                            RoundedCornerShape(999.dp)
                        )
                )
            }
        }
    }
}

@Composable
private fun TipPanel(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = GlassSurfaceStrong,
        shape = RoundedCornerShape(22.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.padding(top = 2.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "오늘의 팁",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

private data class StageCopy(
    val title: String,
    val description: String
)

private fun stageCopy(
    stage: GachaContract.State.Stage,
    hasError: Boolean
): StageCopy = when (stage) {
    GachaContract.State.Stage.Idle -> StageCopy(
        title = if (hasError) "잠시 연결이 끊겼습니다" else "당신을 위한 위로를\n찾고 있어요",
        description = if (hasError) {
            "네트워크 상태를 확인한 뒤 다시 시도하면 말씀 생성을 이어갈 수 있습니다."
        } else {
            "가장 어울리는 말씀과 위로의 한마디를 자동으로 준비하고 있습니다."
        }
    )
    GachaContract.State.Stage.Shaking -> StageCopy(
        title = "감정과 상황을\n정리하고 있어요",
        description = "지금 느끼는 무게와 가장 어울리는 성경 구절을 찾고 있습니다."
    )
    GachaContract.State.Stage.Dispensing -> StageCopy(
        title = "말씀을 카드 형태로\n다듬고 있어요",
        description = "조금만 기다리면 결과 화면으로 이어집니다."
    )
    GachaContract.State.Stage.Opening -> StageCopy(
        title = "거의 다 왔습니다",
        description = "AI의 한마디와 함께 말씀을 정리하는 마지막 단계입니다."
    )
    GachaContract.State.Stage.Complete -> StageCopy(
        title = "준비 완료",
        description = "잠시 후 결과 화면으로 이동합니다."
    )
}

private fun tipMessage(
    stage: GachaContract.State.Stage,
    hasError: Boolean
): String = when (stage) {
    GachaContract.State.Stage.Idle -> {
        if (hasError) {
            "일시적인 오류일 수 있습니다. 연결이 안정되면 다시 시도해보세요."
        } else {
            "화면에 들어오면 바로 말씀 추천이 시작됩니다."
        }
    }
    GachaContract.State.Stage.Shaking -> "감정 맥락과 성경 구절을 함께 정리하고 있습니다."
    GachaContract.State.Stage.Dispensing -> "말씀 카드와 위로의 한마디를 다듬고 있습니다."
    GachaContract.State.Stage.Opening -> "거의 준비되었습니다. 잠시만 기다려주세요."
    GachaContract.State.Stage.Complete -> "완료되면 결과 화면으로 자동 이동합니다."
}

private fun GachaContract.State.Stage.progress(): Float = when (this) {
    GachaContract.State.Stage.Idle -> 0f
    GachaContract.State.Stage.Shaking -> 0.42f
    GachaContract.State.Stage.Dispensing -> 0.72f
    GachaContract.State.Stage.Opening -> 0.92f
    GachaContract.State.Stage.Complete -> 1f
}
