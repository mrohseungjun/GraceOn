package com.graceon.feature.gacha

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.graceon.core.ui.theme.*
import com.graceon.domain.model.Prescription
import com.graceon.feature.gacha.GachaContract

@Composable
fun GachaScreen(
    viewModel: GachaViewModel,
    onNavigateToResult: (Prescription, String?, String?, String?, Boolean) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

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
                is GachaContract.Effect.ShowError -> {
                    // Show error toast/snackbar
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8F9FF),
                        Color(0xFFE8EEFF)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Title with animation
            val scale by animateFloatAsState(
                targetValue = if (state.stage == GachaContract.State.Stage.Shaking) 1.05f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "title_scale"
            )
            
            Text(
                text = "🙏 하늘 약국",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = IndigoPrimary,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .scale(scale)
            )
            
            Text(
                text = when (state.stage) {
                    GachaContract.State.Stage.Idle -> "마음을 담아 레버를 돌려주세요"
                    GachaContract.State.Stage.Shaking -> "AI가 당신을 위한 말씀을 찾고 있어요..."
                    GachaContract.State.Stage.Dispensing -> "처방전이 나오고 있어요!"
                    GachaContract.State.Stage.Opening -> "처방전을 확인하세요"
                    GachaContract.State.Stage.Complete -> "완료!"
                },
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            GachaMachine(
                stage = state.stage,
                onPull = { viewModel.handleIntent(GachaContract.Intent.PullLever) }
            )
        }

        // Flash effect when opening
        AnimatedVisibility(
            visible = state.stage == GachaContract.State.Stage.Opening,
            enter = fadeIn(tween(800)),
            exit = fadeOut(tween(400))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White,
                                Color.White.copy(alpha = 0.8f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }
    }
}

@Composable
private fun GachaMachine(
    stage: GachaContract.State.Stage,
    onPull: () -> Unit
) {
    val capsuleColors = remember {
        listOf(
            Color(0xFFEF4444),
            Color(0xFF3B82F6),
            Color(0xFFFBBF24),
            Color(0xFF10B981),
            Color(0xFF8B5CF6)
        )
    }
    val selectedCapsuleColor = remember { capsuleColors.random() }

    Card(
        modifier = Modifier
            .width(320.dp)
            .padding(16.dp),
        shape = RoundedCornerShape(32.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            // Gacha Machine Body
            Box(
                modifier = Modifier
                    .width(280.dp)
                    .height(360.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFFAFAFA),
                                Color(0xFFEEEEEE)
                            )
                        ),
                        shape = RoundedCornerShape(topStart = 140.dp, topEnd = 140.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
                    )
                    .border(
                        width = 3.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFE0E0E0),
                                Color(0xFFBDBDBD)
                            )
                        ),
                        shape = RoundedCornerShape(topStart = 140.dp, topEnd = 140.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
                    ),
                contentAlignment = Alignment.TopCenter
            ) {
            // Glass Dome
            Box(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .size(240.dp)
                    .background(
                        color = Color(0xFFDCFCE7).copy(alpha = 0.5f),
                        shape = CircleShape
                    )
                    .border(
                        width = 4.dp,
                        color = Color.White.copy(alpha = 0.4f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Capsules inside dome
                this@Column.AnimatedVisibility(
                    visible = stage != GachaContract.State.Stage.Dispensing && stage != GachaContract.State.Stage.Opening && stage != GachaContract.State.Stage.Complete
                ) {
                    val shakeOffset by animateFloatAsState(
                        targetValue = if (stage == GachaContract.State.Stage.Shaking) 1f else 0f,
                        animationSpec = if (stage == GachaContract.State.Stage.Shaking) {
                            infiniteRepeatable(
                                animation = tween(100),
                                repeatMode = RepeatMode.Reverse
                            )
                        } else {
                            tween(0)
                        },
                        label = "shake"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .offset(x = (shakeOffset * 5).dp, y = (shakeOffset * 5).dp)
                    ) {
                        // Multiple capsules
                        repeat(5) { index ->
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .offset(
                                        x = (index * 30 - 60).dp,
                                        y = (index % 2 * 40 + 80).dp
                                    )
                                    .background(capsuleColors[index], CircleShape)
                                    .border(2.dp, Color.Black.copy(alpha = 0.1f), CircleShape)
                            )
                        }
                    }
                }
            }

            // Machine Base
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(IndigoPrimary, IndigoSecondary)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Dispenser hole
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color(0xFF1F2937), CircleShape)
                        .border(4.dp, Color(0xFF4B5563), CircleShape)
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(48.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    )
                }
            }
        }

        // Dropped Capsule Animation
        AnimatedVisibility(
            visible = stage == GachaContract.State.Stage.Dispensing || stage == GachaContract.State.Stage.Opening,
            enter = slideInVertically(
                initialOffsetY = { -200 },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) + fadeIn(),
            exit = scaleOut(targetScale = 5f) + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .size(64.dp)
                    .background(selectedCapsuleColor, CircleShape)
                    .border(4.dp, Color.White.copy(alpha = 0.5f), CircleShape)
            ) {
                // Capsule split line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .align(Alignment.Center)
                        .background(Color.Black.copy(alpha = 0.1f))
                )
            }
        }

            Spacer(modifier = Modifier.height(32.dp))

            // Pull Lever Button
            val rotation by animateFloatAsState(
                targetValue = if (stage == GachaContract.State.Stage.Shaking) 90f else 0f,
                animationSpec = tween(500),
                label = "lever_rotation"
            )

            Button(
                onClick = onPull,
                enabled = stage == GachaContract.State.Stage.Idle,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .rotate(rotation),
                shape = RoundedCornerShape(32.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = IndigoPrimary,
                    disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 12.dp,
                    disabledElevation = 0.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier
                        .size(28.dp)
                        .then(
                            if (stage == GachaContract.State.Stage.Shaking) {
                                Modifier.rotate(
                                    animateFloatAsState(
                                        targetValue = 360f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(1000, easing = LinearEasing),
                                            repeatMode = RepeatMode.Restart
                                        ),
                                        label = "spin"
                                    ).value
                                )
                            } else Modifier
                        )
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = when (stage) {
                        GachaContract.State.Stage.Idle -> "✨ 고민 넣고 뽑기"
                        GachaContract.State.Stage.Shaking -> "🔍 AI가 말씀을 찾는 중..."
                        else -> "📋 처방 중..."
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
