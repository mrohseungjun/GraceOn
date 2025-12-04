package com.graceon.feature.worry

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.graceon.core.ui.component.GradientCard
import com.graceon.core.ui.theme.*
import com.graceon.domain.model.Category
import com.graceon.domain.model.ColorType
import com.graceon.domain.model.IconType
import com.graceon.feature.worry.WorryContract

@Composable
fun WorryScreen(
    viewModel: WorryViewModel,
    onNavigateToGacha: (String?, String?, String?, Boolean) -> Unit,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

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

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AnimatedContent(
                targetState = state.step,
                transitionSpec = {
                    slideInHorizontally { it } + fadeIn() togetherWith
                            slideOutHorizontally { -it } + fadeOut()
                },
                label = "worry_step_transition"
            ) { step ->
                when (step) {
                    WorryContract.State.Step.Intro -> IntroStep(
                        onCategoryMode = { viewModel.handleIntent(WorryContract.Intent.StartCategoryMode) },
                        onAiMode = { viewModel.handleIntent(WorryContract.Intent.StartAiMode) }
                    )
                    WorryContract.State.Step.CategorySelection -> CategorySelectionStep(
                        categories = state.categories,
                        onSelectCategory = { viewModel.handleIntent(WorryContract.Intent.SelectCategory(it)) },
                        onBack = { viewModel.handleIntent(WorryContract.Intent.NavigateBack) }
                    )
                    WorryContract.State.Step.DetailSelection -> {
                        state.selectedCategory?.let { category ->
                            DetailSelectionStep(
                                category = category,
                                onSelectDetail = { viewModel.handleIntent(WorryContract.Intent.SelectDetail(it)) },
                                onBack = { viewModel.handleIntent(WorryContract.Intent.NavigateBack) }
                            )
                        }
                    }
                    WorryContract.State.Step.CustomInput -> CustomInputStep(
                        customWorry = state.customWorry,
                        onWorryChange = { viewModel.handleIntent(WorryContract.Intent.UpdateCustomWorry(it)) },
                        onConfirm = { viewModel.handleIntent(WorryContract.Intent.ConfirmCustomWorry) },
                        onBack = { viewModel.handleIntent(WorryContract.Intent.NavigateBack) }
                    )
                }
            }
        }
    }
}

@Composable
private fun IntroStep(
    onCategoryMode: () -> Unit,
    onAiMode: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = IndigoPrimary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "지금 어떤 고민이\n당신의 마음을 누르나요?",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "고민을 선택하거나 적어주시면\nAI가 당신에게 필요한 말씀을 처방해드립니다.",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onCategoryMode,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = IndigoPrimary
            )
        ) {
            Icon(imageVector = Icons.Default.Star, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("고민 카드 뽑기", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onAiMode,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(imageVector = Icons.Default.Edit, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("직접 고민 적기", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun CategorySelectionStep(
    categories: List<Category>,
    onSelectCategory: (Category) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "가장 큰 고민의 주제는 무엇인가요?",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(categories) { category ->
                GradientCard(
                    title = category.title,
                    icon = {
                        Icon(
                            imageVector = category.iconType.toImageVector(),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    },
                    gradientColors = category.colorType.toGradientColors(),
                    onClick = { onSelectCategory(category) },
                    modifier = Modifier.height(180.dp)
                )
            }
        }
    }
}

@Composable
private fun DetailSelectionStep(
    category: Category,
    onSelectDetail: (com.graceon.domain.model.DetailWorry) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = IndigoPrimary.copy(alpha = 0.1f)
        ) {
            Text(
                text = category.title,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelLarge,
                color = IndigoPrimary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "더 구체적으로 알려주세요",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        category.details.forEach { detail ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                onClick = { onSelectDetail(detail) },
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = detail.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomInputStep(
    customWorry: String,
    onWorryChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "당신의 고민을 들려주세요",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "구체적으로 적을수록 더 꼭 맞는 처방이 나옵니다.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = customWorry,
            onValueChange = onWorryChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            placeholder = { Text("예: 요즘 취업 준비로 너무 불안하고 자존감이 낮아지는 것 같아...") },
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onConfirm,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = customWorry.isNotBlank(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("처방전 받으러 가기", style = MaterialTheme.typography.titleMedium)
        }
    }
}

// Helper Extensions
private fun IconType.toImageVector(): ImageVector = when (this) {
    IconType.BRIEFCASE -> Icons.Default.AccountBox
    IconType.USER -> Icons.Default.Person
    IconType.SUN -> Icons.Default.Star
    IconType.HEART -> Icons.Default.Favorite
}

private fun ColorType.toGradientColors(): Pair<Color, Color> = when (this) {
    ColorType.BLUE -> BlueGradientStart to BlueGradientEnd
    ColorType.PINK -> PinkGradientStart to PinkGradientEnd
    ColorType.YELLOW -> YellowGradientStart to YellowGradientEnd
    ColorType.PURPLE -> PurpleGradientStart to PurpleGradientEnd
}
