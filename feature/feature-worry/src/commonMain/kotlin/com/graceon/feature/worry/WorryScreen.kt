package com.graceon.feature.worry

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.imePadding
import com.graceon.core.ui.component.GraceOnAmbientBackground
import com.graceon.core.ui.component.GraceOnBottomBar
import com.graceon.core.ui.component.GraceOnBottomTab
import com.graceon.core.ui.component.GraceOnScaffold
import com.graceon.core.ui.theme.GlassBorder
import com.graceon.core.ui.theme.GlassSurface
import com.graceon.core.ui.theme.GlassSurfaceStrong
import com.graceon.core.ui.theme.Primary
import com.graceon.domain.model.Category
import com.graceon.domain.model.ColorType
import com.graceon.domain.model.DetailWorry
import com.graceon.domain.model.IconType

@Composable
fun WorryScreen(
    viewModel: WorryViewModel,
    onNavigateToGacha: (String?, String?, String?, Boolean) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToSaved: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

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
                is WorryContract.Effect.ShowError -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    LaunchedEffect(state.step) {
        if (state.step == WorryContract.State.Step.Intro) {
            viewModel.handleIntent(WorryContract.Intent.RefreshDailyUsage)
        }
    }

    GraceOnScaffold(
        title = state.step.titleOrNull(state.selectedCategory),
        onNavigateBack = if (state.step == WorryContract.State.Step.Intro) null else {
            { viewModel.handleIntent(WorryContract.Intent.NavigateBack) }
        },
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

            AnimatedWorryContent(
                state = state,
                onStartCategoryMode = {
                    viewModel.handleIntent(WorryContract.Intent.StartCategoryMode)
                },
                onStartAiMode = {
                    viewModel.handleIntent(WorryContract.Intent.StartAiMode)
                },
                onSelectCategory = {
                    viewModel.handleIntent(WorryContract.Intent.SelectCategory(it))
                },
                onSelectDetail = {
                    viewModel.handleIntent(WorryContract.Intent.SelectDetail(it))
                },
                onWorryChange = {
                    viewModel.handleIntent(WorryContract.Intent.UpdateCustomWorry(it))
                },
                onConfirm = {
                    viewModel.handleIntent(WorryContract.Intent.ConfirmCustomWorry)
                },
                onNavigateToSaved = onNavigateToSaved
            )

            if (state.step == WorryContract.State.Step.Intro) {
                GraceOnBottomBar(
                    activeTab = GraceOnBottomTab.Home,
                    onHomeClick = {},
                    onWordClick = {
                        viewModel.handleIntent(WorryContract.Intent.StartCategoryMode)
                    },
                    onSavedClick = onNavigateToSaved,
                    onProfileClick = onNavigateToProfile,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun AnimatedWorryContent(
    state: WorryContract.State,
    onStartCategoryMode: () -> Unit,
    onStartAiMode: () -> Unit,
    onSelectCategory: (Category) -> Unit,
    onSelectDetail: (DetailWorry) -> Unit,
    onWorryChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onNavigateToSaved: () -> Unit
) {
    AnimatedContent(
        targetState = state.step,
        transitionSpec = {
            val forward = targetState.stepIndex() >= initialState.stepIndex()
            val enterOffset: (Int) -> Int = { fullWidth -> if (forward) fullWidth / 5 else -fullWidth / 5 }
            val exitOffset: (Int) -> Int = { fullWidth -> if (forward) -fullWidth / 6 else fullWidth / 6 }

            (slideInHorizontally(initialOffsetX = enterOffset) + fadeIn()) togetherWith
                (slideOutHorizontally(targetOffsetX = exitOffset) + fadeOut()) using
                SizeTransform(clip = false)
        },
        label = "worry_content"
    ) { step ->
        when (step) {
            WorryContract.State.Step.Intro -> IntroStep(
                categories = state.categories,
                dailyUsage = state.dailyUsage,
                onStartCategoryMode = onStartCategoryMode,
                onStartAiMode = onStartAiMode,
                onSelectCategory = onSelectCategory,
                onNavigateToSaved = onNavigateToSaved
            )
            WorryContract.State.Step.CategorySelection -> CategorySelectionStep(
                categories = state.categories,
                onSelectCategory = onSelectCategory
            )
            WorryContract.State.Step.DetailSelection -> state.selectedCategory?.let {
                DetailSelectionStep(category = it, onSelectDetail = onSelectDetail)
            }
            WorryContract.State.Step.CustomInput -> CustomInputStep(
                customWorry = state.customWorry,
                isAiMode = state.isAiMode,
                onWorryChange = onWorryChange,
                onConfirm = onConfirm
            )
        } ?: Spacer(modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun IntroStep(
    categories: List<Category>,
    dailyUsage: WorryContract.DailyUsageUiState,
    onStartCategoryMode: () -> Unit,
    onStartAiMode: () -> Unit,
    onSelectCategory: (Category) -> Unit,
    onNavigateToSaved: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "GraceOn.",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "안녕하세요,\n오늘 마음은 어떠신가요?",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 38.sp
                )
                Text(
                    text = "당신의 이야기에 귀 기울일 준비가 되었어요.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            HeroModeCard(
                title = "오늘 내게 주시는 말씀",
                description = "복잡한 고민 없이, 지금 당신에게 가장 필요한 위로를 바로 뽑아보세요.",
                onClick = onStartAiMode
            )
        }

        item {
            DailyUsageCard(dailyUsage = dailyUsage)
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "상황별 맞춤 위로 찾기",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                TextButton(onClick = onStartCategoryMode) {
                    Text("전체보기", color = Primary)
                }
            }
        }

        item {
            CategoryCardGrid(
                categories = categories.take(4),
                onSelectCategory = onSelectCategory
            )
        }

        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = GlassSurface,
                shape = RoundedCornerShape(28.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onNavigateToSaved)
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.fillMaxWidth(0.88f)) {
                        Text(
                            text = "최근 저장한 말씀",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "마음에 남는 말씀을 다시 꺼내보고 기도문도 확인하세요.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Primary
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyUsageCard(
    dailyUsage: WorryContract.DailyUsageUiState
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = GlassSurface,
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "오늘 무료 횟수",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = if (dailyUsage.isLoading) {
                    "남은 횟수를 확인하는 중..."
                } else {
                    buildString {
                        append("${dailyUsage.remainingToday}회 남음 · 총 ${dailyUsage.dailyLimit}회 중 ${dailyUsage.usedToday}회 사용")
                        if (dailyUsage.rewardedCredits > 0) {
                            append(" · 광고 보상 ${dailyUsage.rewardedCredits}회 보유")
                        }
                    }
                },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (dailyUsage.remainingToday > 0) {
                    "오늘은 아직 말씀을 더 받을 수 있어요."
                } else if (dailyUsage.rewardedAvailableToday > 0) {
                    "무료 횟수를 모두 사용했습니다. 광고를 보고 추가 1회를 받을 수 있어요."
                } else {
                    "오늘 무료 횟수와 광고 보상을 모두 사용했습니다. 내일 다시 열립니다."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun HeroModeCard(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = GlassSurfaceStrong,
        shape = RoundedCornerShape(32.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Primary.copy(alpha = 0.25f),
                            Color.Transparent
                        )
                    )
                )
                .padding(22.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.22f), RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Primary
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    lineHeight = 32.sp
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 24.sp
                )
            }
        }
    }
}

@Composable
private fun IntroCategoryCard(
    category: Category,
    onClick: () -> Unit
) {
    val palette = category.colorType.palette()
    val accent = palette.accent
    val subtitle = category.description ?: category.details.take(2).joinToString(", ") { it.title }

    Card(
        onClick = onClick,
        modifier = Modifier.height(150.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = palette.container),
        border = androidx.compose.foundation.BorderStroke(1.dp, palette.border)
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
                    .background(accent.copy(alpha = 0.18f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = category.iconType.toIcon(),
                    contentDescription = null,
                    tint = accent
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = category.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun CategorySelectionStep(
    categories: List<Category>,
    onSelectCategory: (Category) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        StepHeader(
            title = "어떤 영역에서\n위로가 필요하신가요?",
            subtitle = "가장 고민이 되는 영역을 선택해주세요."
        )

        CategoryCardGrid(
            categories = categories,
            onSelectCategory = onSelectCategory
        )
    }
}

@Composable
private fun CategoryCardGrid(
    categories: List<Category>,
    onSelectCategory: (Category) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        categories.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEach { category ->
                    Box(modifier = Modifier.weight(1f)) {
                        IntroCategoryCard(
                            category = category,
                            onClick = { onSelectCategory(category) }
                        )
                    }
                }

                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun DetailSelectionStep(
    category: Category,
    onSelectDetail: (DetailWorry) -> Unit
) {
    val palette = category.colorType.palette()
    val accent = palette.accent

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Surface(
            color = palette.container,
            shape = RoundedCornerShape(999.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = category.iconType.toIcon(),
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = category.title,
                    color = accent,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        StepHeader(
            title = "조금 더 구체적으로\n알려주세요",
            subtitle = "지금 마음에 가장 가까운 고민을 골라주세요."
        )

        category.details.forEach { detail ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectDetail(detail) },
                color = palette.container.copy(alpha = 0.92f),
                shape = RoundedCornerShape(22.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, palette.border)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.fillMaxWidth(0.88f)) {
                        Text(
                            text = detail.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "선택하면 바로 말씀을 찾기 시작합니다.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Primary
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomInputStep(
    customWorry: String,
    isAiMode: Boolean,
    onWorryChange: (String) -> Unit,
    onConfirm: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .imePadding()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        if (isAiMode) {
            Surface(
                color = GlassSurfaceStrong,
                shape = RoundedCornerShape(999.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "AI 고민 나누기",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        StepHeader(
            title = "조금 더 자세히\n이야기해 주실래요?",
            subtitle = "적어주신 내용을 바탕으로 더 꼭 맞는 말씀을 준비합니다."
        )

        TextField(
            value = customWorry,
            onValueChange = onWorryChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
            singleLine = false,
            maxLines = 12,
            placeholder = {
                Text(
                    text = "어떤 일로 마음이 힘드신가요? 여기에 편하게 적어주세요.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            shape = RoundedCornerShape(28.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = GlassSurfaceStrong,
                unfocusedContainerColor = GlassSurfaceStrong,
                disabledContainerColor = GlassSurfaceStrong,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Primary,
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            textStyle = MaterialTheme.typography.bodyLarge.copy(lineHeight = 25.sp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${customWorry.length} / 500",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "선택 사항",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Button(
            onClick = onConfirm,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            shape = RoundedCornerShape(999.dp),
            enabled = customWorry.isNotBlank(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.55f)
            )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = null
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text("말씀 뽑으러 가기", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun StepHeader(
    title: String,
    subtitle: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            lineHeight = 38.sp
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 24.sp
        )
    }
}

private fun WorryContract.State.Step.titleOrNull(selectedCategory: Category?): String? = when (this) {
    WorryContract.State.Step.Intro -> null
    WorryContract.State.Step.CategorySelection -> "말씀 찾기"
    WorryContract.State.Step.DetailSelection -> selectedCategory?.title ?: "세부 고민"
    WorryContract.State.Step.CustomInput -> "AI에게 고민 나누기"
}

private fun WorryContract.State.Step.stepIndex(): Int = when (this) {
    WorryContract.State.Step.Intro -> 0
    WorryContract.State.Step.CategorySelection -> 1
    WorryContract.State.Step.DetailSelection -> 2
    WorryContract.State.Step.CustomInput -> 3
}

private data class CategoryPalette(
    val accent: Color,
    val container: Color,
    val border: Color
)

private fun IconType.toIcon(): ImageVector = when (this) {
    IconType.BRIEFCASE -> Icons.Outlined.WorkOutline
    IconType.USER -> Icons.Default.PersonOutline
    IconType.SUN -> Icons.Default.LightMode
    IconType.HEART -> Icons.Default.FavoriteBorder
    IconType.SPARKLE -> Icons.Default.AutoAwesome
    IconType.EDIT -> Icons.Default.Create
}

@Composable
private fun ColorType.palette(): CategoryPalette {
    val isLightTheme = MaterialTheme.colorScheme.background.luminance() > 0.5f

    return if (isLightTheme) {
        when (this) {
            ColorType.BLUE -> CategoryPalette(
                accent = Color(0xFF936D53),
                container = Color(0xFFF2E4D6),
                border = Color(0xFFE0C8B5)
            )
            ColorType.PINK -> CategoryPalette(
                accent = Color(0xFFB57A6C),
                container = Color(0xFFF4E1D8),
                border = Color(0xFFE6C5B8)
            )
            ColorType.YELLOW -> CategoryPalette(
                accent = Color(0xFFC99960),
                container = Color(0xFFF5E8D5),
                border = Color(0xFFE7D0AF)
            )
            ColorType.PURPLE -> CategoryPalette(
                accent = Color(0xFF9C7368),
                container = Color(0xFFF1E1DA),
                border = Color(0xFFE0C7BC)
            )
        }
    } else {
        when (this) {
            ColorType.BLUE -> CategoryPalette(
                accent = Color(0xFFD7B59C),
                container = Color(0xFF2F2420),
                border = Color(0xFF4C3931)
            )
            ColorType.PINK -> CategoryPalette(
                accent = Color(0xFFD2A091),
                container = Color(0xFF322320),
                border = Color(0xFF523A34)
            )
            ColorType.YELLOW -> CategoryPalette(
                accent = Color(0xFFE0BC8F),
                container = Color(0xFF34281F),
                border = Color(0xFF5C4735)
            )
            ColorType.PURPLE -> CategoryPalette(
                accent = Color(0xFFC9A59C),
                container = Color(0xFF302220),
                border = Color(0xFF4B3834)
            )
        }
    }
}

@Composable
private fun ColorType.accentColor(): Color {
    return palette().accent
}
