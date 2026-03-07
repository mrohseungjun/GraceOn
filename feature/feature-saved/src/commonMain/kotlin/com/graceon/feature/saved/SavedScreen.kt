package com.graceon.feature.saved

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.graceon.core.ui.component.GraceOnAmbientBackground
import com.graceon.core.ui.component.GraceOnBottomBar
import com.graceon.core.ui.component.GraceOnBottomTab
import com.graceon.core.ui.component.GraceOnScaffold
import com.graceon.core.ui.theme.GlassBorder
import com.graceon.core.ui.theme.GlassSurface
import com.graceon.core.ui.theme.GlassSurfaceStrong
import com.graceon.core.ui.theme.Primary
import com.graceon.domain.model.SavedPrescription
import kotlin.math.absoluteValue

@Composable
fun SavedScreen(
    viewModel: SavedViewModel,
    onNavigateBack: () -> Unit,
    onNavigateHome: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedPrescription by remember { mutableStateOf<SavedPrescription?>(null) }
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is SavedContract.Effect.ShowDeleteSuccess -> snackbarHostState.showSnackbar("삭제되었습니다")
                is SavedContract.Effect.ShowError -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    GraceOnScaffold(
        title = if (isSearchActive) null else "보관함",
        titleContent = if (isSearchActive) {
            {
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("말씀, 메시지 검색") },
                    singleLine = true,
                    shape = RoundedCornerShape(999.dp),
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = GlassSurfaceStrong,
                        unfocusedContainerColor = GlassSurfaceStrong,
                        disabledContainerColor = GlassSurfaceStrong,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        } else {
            null
        },
        onNavigateBack = {
            if (isSearchActive) {
                isSearchActive = false
                searchQuery = ""
            } else {
                onNavigateBack()
            }
        },
        snackbarHostState = snackbarHostState,
        backgroundBrush = Brush.verticalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.background,
                Color(0xFF07131E)
            )
        ),
        topBarContainerColor = Color.Transparent,
        actions = {
            if (isSearchActive && searchQuery.isNotBlank()) {
                IconButton(onClick = { searchQuery = "" }) {
                    Icon(Icons.Default.Close, contentDescription = "지우기")
                }
            } else {
                IconButton(onClick = { isSearchActive = true }) {
                    Icon(Icons.Default.Search, contentDescription = "검색")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            GraceOnAmbientBackground()

            when {
                state.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        androidx.compose.material3.CircularProgressIndicator(color = Primary)
                    }
                }
                state.prescriptions.isEmpty() -> {
                    EmptyState()
                }
                else -> {
                    val filteredList = remember(state.prescriptions, searchQuery) {
                        state.prescriptions.filter { prescription ->
                            searchQuery.isBlank() ||
                                prescription.verse.contains(searchQuery, ignoreCase = true) ||
                                prescription.message.contains(searchQuery, ignoreCase = true) ||
                                (prescription.prayer?.contains(searchQuery, ignoreCase = true) == true)
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 120.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredList, key = { it.id }) { prescription ->
                            SavedPrescriptionCard(
                                prescription = prescription,
                                onDelete = {
                                    viewModel.handleIntent(SavedContract.Intent.DeletePrescription(prescription.id))
                                },
                                onClick = { selectedPrescription = prescription }
                            )
                        }
                    }
                }
            }

            GraceOnBottomBar(
                activeTab = GraceOnBottomTab.Saved,
                onHomeClick = onNavigateHome,
                onWordClick = onNavigateHome,
                onSavedClick = {},
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            )

            selectedPrescription?.let { prescription ->
                SavedPrescriptionDetailDialog(
                    prescription = prescription,
                    onDismiss = { selectedPrescription = null }
                )
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = GlassSurfaceStrong,
            shape = RoundedCornerShape(28.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.BookmarkBorder,
                    contentDescription = null,
                    modifier = Modifier.size(42.dp),
                    tint = Primary
                )
                Text(
                    text = "아직 저장된 말씀이 없습니다",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "마음에 남는 말씀을 저장해두면 이곳에서 다시 꺼내볼 수 있습니다.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )
            }
        }
    }
}

@Composable
private fun SavedPrescriptionCard(
    prescription: SavedPrescription,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("삭제 확인") },
            text = { Text("이 말씀을 보관함에서 제거하시겠습니까?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("삭제")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("취소")
                }
            }
        )
    }

    val verseParts = prescription.verse.split("(")
    val verseText = verseParts.getOrNull(0)?.trim()?.replace("+", " ") ?: prescription.verse
    val verseReference = verseParts.getOrNull(1)?.replace(")", "")?.trim()?.replace("+", " ").orEmpty()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = GlassSurfaceStrong,
        shape = RoundedCornerShape(30.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SavedHeroThumbnail(seed = prescription.id)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = GlassSurface,
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        text = formatDate(prescription.savedAt),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "삭제",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (verseReference.isNotBlank()) {
                    Text(
                        text = verseReference,
                        style = MaterialTheme.typography.labelMedium,
                        color = Primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = verseText,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 32.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Surface(
                color = GlassSurface,
                shape = RoundedCornerShape(18.dp)
            ) {
                Text(
                    text = prescription.message.replace("+", " "),
                    modifier = Modifier.padding(14.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 21.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatSectionDate(prescription.savedAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    color = Primary.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.IosShare,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Text(
                            text = "보기",
                            style = MaterialTheme.typography.labelLarge,
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
private fun SavedHeroThumbnail(seed: String) {
    val palette = remember(seed) {
        when (seed.hashCode().absoluteValue % 3) {
            0 -> listOf(Color(0xFF111827), Color(0xFF0EA5E9), Color(0xFF38BDF8))
            1 -> listOf(Color(0xFF0F172A), Color(0xFF14B8A6), Color(0xFF6EE7B7))
            else -> listOf(Color(0xFF172554), Color(0xFF2563EB), Color(0xFFA78BFA))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(colors = palette))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0x7005070A))
                    )
                )
        )
    }
}

@Composable
private fun SavedPrescriptionDetailDialog(
    prescription: SavedPrescription,
    onDismiss: () -> Unit
) {
    val verseParts = prescription.verse.split("(")
    val verseText = verseParts.getOrNull(0)?.trim()?.replace("+", " ") ?: prescription.verse
    val verseReference = verseParts.getOrNull(1)?.replace(")", "")?.trim()?.replace("+", " ").orEmpty()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(28.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "저장된 말씀",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "닫기")
                    }
                }

                SavedHeroThumbnail(seed = prescription.id + "detail")

                if (verseReference.isNotBlank()) {
                    Text(
                        text = verseReference,
                        style = MaterialTheme.typography.labelLarge,
                        color = Primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = verseText,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    lineHeight = 38.sp
                )

                Surface(
                    color = GlassSurface,
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "AI의 한마디",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = prescription.message.replace("+", " "),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 26.sp
                        )
                    }
                }

                prescription.prayer?.let { prayer ->
                    Surface(
                        color = GlassSurfaceStrong,
                        shape = RoundedCornerShape(22.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "기도문",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = prayer,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 26.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
