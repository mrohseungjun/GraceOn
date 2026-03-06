package com.graceon.feature.worry

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.graceon.core.ui.component.GraceOnScaffold
import com.graceon.core.ui.theme.*
import com.graceon.domain.model.Category
import com.graceon.domain.model.ColorType
import com.graceon.domain.model.IconType

@Composable
fun WorryScreen(
    viewModel: WorryViewModel,
    onNavigateToGacha: (String?, String?, String?, Boolean) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToSaved: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val currentTitle = when (state.step) {
        WorryContract.State.Step.Intro -> null
        WorryContract.State.Step.CategorySelection -> "고민 선택"
        WorryContract.State.Step.DetailSelection -> state.selectedCategory?.title ?: "세부 고민"
        WorryContract.State.Step.CustomInput -> "AI 고민 나누기"
    }
    val canNavigateBack = state.step != WorryContract.State.Step.Intro

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is WorryContract.Effect.NavigateToGacha -> {
                    onNavigateToGacha(
                        effect.categoryId,
                        effect.detailId,
                        effect.customWorry,
                        effect.isAiMode
                    )
                }
                is WorryContract.Effect.NavigateBack -> onNavigateBack()
                is WorryContract.Effect.ShowError -> {
                    // Show snackbar or toast
                }
            }
        }
    }

    GraceOnScaffold(
        title = currentTitle,
        onNavigateBack = if (canNavigateBack) {
            { viewModel.handleIntent(WorryContract.Intent.NavigateBack) }
        } else {
            null
        },
        backgroundBrush = Brush.verticalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.background,
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f)
            )
        )
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AnimatedContent(
                targetState = state.step,
                transitionSpec = {
                    val targetIndex = targetState.stepIndex()
                    val initialIndex = initialState.stepIndex()
                    val forward = targetIndex >= initialIndex

                    val enter = slideInHorizontally(
                        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
                    ) { fullWidth -> if (forward) fullWidth else -fullWidth } +
                        fadeIn(animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing))

                    val exit = slideOutHorizontally(
                        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing)
                    ) { fullWidth -> if (forward) -fullWidth else fullWidth } +
                        fadeOut(animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing))

                    enter togetherWith exit using SizeTransform(clip = false)
                },
                label = "worry_step_transition"
            ) { step ->
                when (step) {
                    WorryContract.State.Step.Intro -> IntroStep(
                        onCategoryMode = { viewModel.handleIntent(WorryContract.Intent.StartCategoryMode) },
                        onAiMode = { viewModel.handleIntent(WorryContract.Intent.StartAiMode) },
                        onSavedPrescriptions = onNavigateToSaved
                    )
                    WorryContract.State.Step.CategorySelection -> CategorySelectionStep(
                        categories = state.categories,
                        onSelectCategory = { viewModel.handleIntent(WorryContract.Intent.SelectCategory(it)) }
                    )
                    WorryContract.State.Step.DetailSelection -> {
                        state.selectedCategory?.let { category ->
                            DetailSelectionStep(
                                category = category,
                                onSelectDetail = { viewModel.handleIntent(WorryContract.Intent.SelectDetail(it)) }
                            )
                        } ?: Box(modifier = Modifier.fillMaxSize())
                    }
                    WorryContract.State.Step.CustomInput -> CustomInputStep(
                        customWorry = state.customWorry,
                        onWorryChange = { viewModel.handleIntent(WorryContract.Intent.UpdateCustomWorry(it)) },
                        onConfirm = { viewModel.handleIntent(WorryContract.Intent.ConfirmCustomWorry) }
                    )
                }
            }
        }
    }
}

@Composable
private fun WorryStepLayout(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .padding(top = 12.dp, bottom = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

@Composable
private fun WorryHeader(
    title: String,
    subtitle: String?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            lineHeight = 36.sp
        )

        if (!subtitle.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun WorryContract.State.Step.stepIndex(): Int = when (this) {
    WorryContract.State.Step.Intro -> 0
    WorryContract.State.Step.CategorySelection -> 1
    WorryContract.State.Step.DetailSelection -> 2
    WorryContract.State.Step.CustomInput -> 3
}

@Composable
private fun IntroStep(
    onCategoryMode: () -> Unit,
    onAiMode: () -> Unit,
    onSavedPrescriptions: () -> Unit = {}
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isVisible = true
    }
    
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    WorryStepLayout(modifier = Modifier.scale(scale)) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Icon
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Favorite,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Grace Note",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "마음이 지칠 때, 위로가 필요할 때\n당신에게 꼭 맞는 말씀을 전해드려요",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            IntroActions(
                onCategoryMode = onCategoryMode,
                onAiMode = onAiMode,
                onSavedPrescriptions = onSavedPrescriptions
            )
        }
    }
}

@Composable
private fun IntroActions(
    onCategoryMode: () -> Unit,
    onAiMode: () -> Unit,
    onSavedPrescriptions: () -> Unit
) {
    Button(
        onClick = onCategoryMode,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(18.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.PlayArrow,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "고민 카테고리 선택",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    FilledTonalButton(
        onClick = onAiMode,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(18.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.PlayArrow,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "AI에게 고민 나누기",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }

    Spacer(modifier = Modifier.height(24.dp))

    TextButton(onClick = onSavedPrescriptions) {
        Text(
            text = "저장된 말씀 보기",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(6.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun CategorySelectionStep(
    categories: List<Category>,
    onSelectCategory: (Category) -> Unit
) {
    WorryStepLayout {
        WorryHeader(
            title = "어떤 고민이\n마음을 무겁게 하나요?",
            subtitle = "카테고리를 선택해주세요"
        )

        Spacer(modifier = Modifier.height(20.dp))

        CategoryGrid(
            categories = categories,
            onSelectCategory = onSelectCategory
        )
    }
}

@Composable
private fun CategoryGrid(
    categories: List<Category>,
    onSelectCategory: (Category) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(categories) { category ->
            CategoryCard(
                category = category,
                onClick = { onSelectCategory(category) }
            )
        }
    }
}

@Composable
private fun CategoryCard(
    category: Category,
    onClick: () -> Unit
) {
    val icon = category.iconType.toIcon()
    val (color, bgColor) = category.colorType.toCategoryColors()

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        modifier = Modifier.height(140.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = color
                )
            }

            Text(
                text = category.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
    }
}

@Composable
private fun DetailSelectionStep(
    category: Category,
    onSelectDetail: (com.graceon.domain.model.DetailWorry) -> Unit
) {
    val icon = category.iconType.toIcon()
    val (color, bgColor) = category.colorType.toCategoryColors()

    WorryStepLayout {
        DetailHeader(
            icon = icon,
            color = color,
            bgColor = bgColor,
            title = category.title
        )

        Spacer(modifier = Modifier.height(16.dp))

        WorryHeader(
            title = "조금 더 구체적으로\n알려주세요",
            subtitle = null
        )

        Spacer(modifier = Modifier.height(20.dp))

        DetailList(
            details = category.details,
            onSelectDetail = onSelectDetail,
            modifier = Modifier.verticalScroll(rememberScrollState())
        )
    }
}

@Composable
private fun DetailHeader(
    icon: ImageVector,
    color: Color,
    bgColor: Color,
    title: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = color
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

@Composable
private fun DetailList(
    details: List<com.graceon.domain.model.DetailWorry>,
    onSelectDetail: (com.graceon.domain.model.DetailWorry) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        details.forEach { detail ->
            Card(
                onClick = { onSelectDetail(detail) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = detail.title,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "선택하면 다음 단계로 이동해요",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun CustomInputStep(
    customWorry: String,
    onWorryChange: (String) -> Unit,
    onConfirm: () -> Unit
) {
    WorryStepLayout {
        CustomInputHeader()

        Spacer(modifier = Modifier.height(16.dp))

        WorryHeader(
            title = "당신의 마음을\n들려주세요",
            subtitle = "구체적으로 적을수록 더 꼭 맞는 말씀이 나와요"
        )

        Spacer(modifier = Modifier.height(20.dp))

        CustomInputForm(
            customWorry = customWorry,
            onWorryChange = onWorryChange,
            onConfirm = onConfirm
        )
    }
}

@Composable
private fun CustomInputHeader() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Outlined.MailOutline,
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "AI에게 고민 나누기",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
private fun ColumnScope.CustomInputForm(
    customWorry: String,
    onWorryChange: (String) -> Unit,
    onConfirm: () -> Unit
) {
    OutlinedTextField(
        value = customWorry,
        onValueChange = onWorryChange,
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        placeholder = {
            Text(
                text = "예: 요즘 취업 준비로 너무 불안하고 자존감이 낮아지는 것 같아...",
                color = MaterialTheme.colorScheme.outline
            )
        },
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        ),
        textStyle = MaterialTheme.typography.bodyLarge.copy(lineHeight = 26.sp)
    )

    Spacer(modifier = Modifier.height(16.dp))

    Button(
        onClick = onConfirm,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = customWorry.isNotBlank(),
        shape = RoundedCornerShape(18.dp)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.Send,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "말씀 받으러 가기",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// Helper Extensions
private fun IconType.toIcon(): ImageVector = when (this) {
    IconType.BRIEFCASE -> Icons.Outlined.Create
    IconType.USER -> Icons.Outlined.Person
    IconType.SUN -> Icons.Outlined.WbSunny
    IconType.HEART -> Icons.Outlined.Favorite
}

private fun ColorType.toCategoryColors(): Pair<Color, Color> = when (this) {
    ColorType.BLUE -> CategoryBlue to CategoryBlueBg
    ColorType.PINK -> CategoryPink to CategoryPinkBg
    ColorType.YELLOW -> CategoryAmber to CategoryAmberBg
    ColorType.PURPLE -> CategoryPurple to CategoryPurpleBg
}
