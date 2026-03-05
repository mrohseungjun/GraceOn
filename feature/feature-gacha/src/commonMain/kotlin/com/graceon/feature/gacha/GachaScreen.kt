package com.graceon.feature.gacha

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

import com.graceon.core.ui.theme.*
import com.graceon.domain.model.Prescription

@Composable
fun GachaScreen(
    viewModel: GachaViewModel,
    onNavigateToResult: (Prescription, String?, String?, String?, Boolean) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

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
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFEEF2FF),
                            Color(0xFFE0E7FF)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // Animated Background Particles
            BackgroundParticles()

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(24.dp)
            ) {
                // Title Area
                GachaHeader(state.stage)
                
                Spacer(modifier = Modifier.height(40.dp))

                // Machine Area
                GachaMachine(
                    stage = state.stage,
                    onPull = { viewModel.handleIntent(GachaContract.Intent.PullLever) }
                )
            }

            // Opening Effect Overlay
            OpeningEffect(visible = state.stage == GachaContract.State.Stage.Opening)
        }
    }
}

@Composable
private fun BackgroundParticles() {
    val infiniteTransition = rememberInfiniteTransition(label = "background_particles")
    
    repeat(5) { index ->
        val offsetY by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = -100f,
            animationSpec = infiniteRepeatable(
                animation = tween(3000 + index * 1000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "particle_y_$index"
        )
        
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 3000 + index * 1000
                    0f at 0
                    0.5f at 1500
                    0f at 3000
                },
                repeatMode = RepeatMode.Restart
            ),
            label = "particle_alpha_$index"
        )

        Box(
            modifier = Modifier
                .offset(
                    x = ((index * 70) - 140).dp,
                    y = offsetY.dp
                )
                .size(10.dp)
                .background(
                    color = if (index % 2 == 0) IndigoPrimary.copy(alpha = alpha * 0.3f) 
                           else PurplePrimary.copy(alpha = alpha * 0.3f),
                    shape = CircleShape
                )
        )
    }
}

@Composable
private fun GachaHeader(stage: GachaContract.State.Stage) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "힐링 말씀",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = IndigoPrimary,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        AnimatedContent(
            targetState = stage,
            transitionSpec = {
                 fadeIn(animationSpec = tween(300)) + slideInVertically { 20 } togetherWith
                        fadeOut(animationSpec = tween(300)) + slideOutVertically { -20 }
            },
            label = "header_text"
        ) { targetStage ->
              Text(
                text = when (targetStage) {
                    GachaContract.State.Stage.Idle -> "마음을 담아 레버를 돌려주세요"
                    GachaContract.State.Stage.Shaking -> "당신을 위한 말씀을 찾고 있어요..."
                    GachaContract.State.Stage.Dispensing -> "말씀이 나오고 있어요!"
                    GachaContract.State.Stage.Opening -> "말씀을 확인하세요"
                    GachaContract.State.Stage.Complete -> "완료!"
                },
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
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
            .width(320.dp),
        shape = RoundedCornerShape(32.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Box(
                contentAlignment = Alignment.TopCenter
            ) {
                // Machine Body
                MachineBody()
                
                // Content (Capsules)
                Box(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .size(240.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Shaking Capsules
                    if (stage != GachaContract.State.Stage.Dispensing && 
                        stage != GachaContract.State.Stage.Opening && 
                        stage != GachaContract.State.Stage.Complete) {
                        
                        val shakeOffset by animateFloatAsState(
                            targetValue = if (stage == GachaContract.State.Stage.Shaking) 1f else 0f,
                            animationSpec = if (stage == GachaContract.State.Stage.Shaking) {
                                infiniteRepeatable(
                                    animation = tween(100),
                                    repeatMode = RepeatMode.Reverse
                                )
                            } else tween(0),
                            label = "shake"
                        )

                        Box(
                            modifier = Modifier
                                .offset(x = (shakeOffset * 5).dp, y = (shakeOffset * 5).dp)
                        ) {
                            repeat(5) { index ->
                                Capsule(
                                    color = capsuleColors[index],
                                    modifier = Modifier.offset(
                                        x = (index * 30 - 60).dp,
                                        y = (index % 2 * 40 - 20).dp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Dropped Capsule
            Box(
                modifier = Modifier
                    .height(80.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = stage == GachaContract.State.Stage.Dispensing || stage == GachaContract.State.Stage.Opening,
                    enter = slideInVertically { -100 } + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    Capsule(
                        color = selectedCapsuleColor,
                        modifier = Modifier.scale(1.2f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Lever Button
            LeverButton(
                isIdle = stage == GachaContract.State.Stage.Idle,
                isShaking = stage == GachaContract.State.Stage.Shaking,
                onPull = onPull
            )
        }
    }
}

@Composable
private fun MachineBody() {
    Box(
        modifier = Modifier
            .width(280.dp)
            .height(360.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFAFAFA), Color(0xFFE0E0E0))
                ),
                shape = RoundedCornerShape(topStart = 140.dp, topEnd = 140.dp, bottomStart = 32.dp, bottomEnd = 32.dp)
            )
            .border(
                width = 4.dp,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFFFFFFF), Color(0xFFBDBDBD))
                ),
                shape = RoundedCornerShape(topStart = 140.dp, topEnd = 140.dp, bottomStart = 32.dp, bottomEnd = 32.dp)
            )
    ) {
        // Glass Dome Background with Shine
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
                .size(240.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFE0F2FE).copy(alpha = 0.4f),
                            Color(0xFFDBEAFE).copy(alpha = 0.1f)
                        )
                    ),
                    shape = CircleShape
                )
                .border(
                    width = 6.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.White.copy(alpha = 0.8f), Color.White.copy(alpha = 0.2f))
                    ),
                    shape = CircleShape
                )
        ) {
            // Shine effect
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .offset(x = 40.dp, y = 40.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.White.copy(alpha = 0.8f), Color.Transparent)
                        ),
                        shape = CircleShape
                    )
            )
        }

        // Base
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(110.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            IndigoPrimary,
                            IndigoSecondary
                        )
                    ),
                    shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            // Metallic trim
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF94A3B8),
                                Color(0xFFE2E8F0),
                                Color(0xFF94A3B8)
                            )
                        )
                    )
            )

            // Dispenser Hole
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF374151), Color(0xFF111827))
                        ),
                        shape = CircleShape
                    )
                    .border(
                        width = 4.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF6B7280), Color(0xFF374151))
                        ),
                        shape = CircleShape
                    )
            ) {
                // Hole depth
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(56.dp)
                        .background(Color.Black.copy(alpha = 0.7f), CircleShape)
                )
            }
        }
    }
}

@Composable
private fun Capsule(
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(color, color.copy(alpha = 0.8f))
                ),
                shape = CircleShape
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.3f),
                shape = CircleShape
            )
    ) {
        // Upper Highlight
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 8.dp, y = 8.dp)
                .size(12.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.8f), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )

        // Split Line (Metallic)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .align(Alignment.Center)
                .background(Color.White.copy(alpha = 0.2f))
        )
    }
}

@Composable
private fun LeverButton(
    isIdle: Boolean,
    isShaking: Boolean,
    onPull: () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (isShaking) 90f else 0f,
        animationSpec = tween(500),
        label = "lever_rotation"
    )

    Button(
        onClick = onPull,
        enabled = isIdle,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        shape = RoundedCornerShape(32.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(0.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 8.dp,
            pressedElevation = 4.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = if (isIdle) {
                        Brush.horizontalGradient(colors = listOf(IndigoPrimary, PurplePrimary))
                    } else {
                        Brush.horizontalGradient(colors = listOf(Color.Gray, Color.Gray))
                    },
                    shape = RoundedCornerShape(32.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .size(28.dp)
                        .rotate(rotation)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (isShaking) "말씀을 찾는 중..." else "고민 넣고 뽑기",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun OpeningEffect(visible: Boolean) {
    androidx.compose.animation.AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(500)),
        exit = fadeOut(tween(500))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White.copy(alpha = 0.9f))
        )
    }
}
