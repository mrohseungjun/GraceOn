package com.graceon.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.graceon.core.common.RewardCreditActionResult
import com.graceon.core.ui.component.GraceOnAmbientBackground
import com.graceon.core.ui.component.GraceOnBottomBar
import com.graceon.core.ui.component.GraceOnBottomTab
import com.graceon.core.ui.component.GraceOnScaffold
import com.graceon.core.ui.theme.GlassBorder
import com.graceon.core.ui.theme.GlassSurface
import com.graceon.core.ui.theme.GlassSurfaceStrong
import com.graceon.core.ui.theme.Primary
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    isDailyVerseNotificationEnabled: Boolean,
    isDarkThemeEnabled: Boolean,
    appVersion: String,
    rewardedCredits: Int,
    rewardedAvailableToday: Int,
    onToggleDailyVerseNotification: (Boolean) -> Unit,
    onToggleDarkTheme: (Boolean) -> Unit,
    onWatchRewardAd: suspend () -> RewardCreditActionResult,
    onLogout: () -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateToWord: () -> Unit,
    onNavigateToSaved: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var isRewardLoading by remember { mutableStateOf(false) }

    GraceOnScaffold(
        title = "마이",
        onNavigateBack = null,
        snackbarHostState = snackbarHostState,
        backgroundBrush = Brush.verticalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.background,
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.42f)
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
                    .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                ProfileHeroCard()
                PreferenceCard(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.NotificationsNone,
                            contentDescription = null,
                            tint = Primary
                        )
                    },
                    title = "오늘의 말씀 알림",
                    description = if (isDailyVerseNotificationEnabled) {
                        "매일 오전 9시에 오늘의 말씀 알림을 받습니다."
                    } else {
                        "오늘의 말씀 리마인더를 켤 수 있습니다."
                    },
                    value = if (isDailyVerseNotificationEnabled) "켜짐" else "꺼짐",
                    onClick = { onToggleDailyVerseNotification(!isDailyVerseNotificationEnabled) }
                )
                ThemeCard(
                    isDarkThemeEnabled = isDarkThemeEnabled,
                    onToggleDarkTheme = onToggleDarkTheme
                )
                PreferenceCard(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.PlayCircleOutline,
                            contentDescription = null,
                            tint = Primary
                        )
                    },
                    title = "광고로 횟수 추가",
                    description = when {
                        isRewardLoading -> "광고를 준비하거나 보상을 반영하는 중입니다."
                        rewardedAvailableToday > 0 -> "리워드 광고를 시청하고 말씀 추가 1회를 받을 수 있습니다."
                        else -> "오늘은 광고 보상으로 추가 횟수를 모두 받았습니다."
                    },
                    value = when {
                        isRewardLoading -> "처리중"
                        rewardedAvailableToday > 0 -> "광고 보기"
                        else -> "완료"
                    },
                    enabled = rewardedAvailableToday > 0 && !isRewardLoading,
                    onClick = {
                        coroutineScope.launch {
                            isRewardLoading = true
                            when (val result = onWatchRewardAd()) {
                                is RewardCreditActionResult.Success -> {
                                    snackbarHostState.showSnackbar("광고 보상 1회가 지급되었습니다. 현재 ${result.rewardedCredits}회 보유")
                                }
                                is RewardCreditActionResult.Error -> {
                                    snackbarHostState.showSnackbar(result.message)
                                }
                            }
                            isRewardLoading = false
                        }
                    }
                )
                PreferenceCard(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.BookmarkBorder,
                            contentDescription = null,
                            tint = Primary
                        )
                    },
                    title = "앱 버전",
                    description = "현재 설치된 GraceOn 버전 정보입니다.",
                    value = if (appVersion.isBlank()) "1.0" else appVersion,
                    onClick = {}
                )
                PreferenceCard(
                    icon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            tint = Primary
                        )
                    },
                    title = "로그아웃",
                    description = "현재 세션을 종료하고 로그인 화면으로 돌아갑니다.",
                    value = "",
                    onClick = onLogout
                )
            }

            GraceOnBottomBar(
                activeTab = GraceOnBottomTab.Profile,
                onHomeClick = onNavigateHome,
                onWordClick = onNavigateToWord,
                onSavedClick = onNavigateToSaved,
                onProfileClick = {},
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            )
        }
    }
}

@Composable
private fun ProfileHeroCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = GlassSurfaceStrong,
        shape = RoundedCornerShape(32.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(Primary.copy(alpha = 0.16f), RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PersonOutline,
                    contentDescription = null,
                    tint = Primary
                )
            }
            Text(
                text = "앱 설정",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "알림, 테마, 앱 정보를 한 곳에서 관리할 수 있습니다.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 24.sp
            )
        }
    }
}

@Composable
private fun PreferenceCard(
    icon: @Composable () -> Unit,
    title: String,
    description: String,
    value: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick),
        color = GlassSurface,
        shape = RoundedCornerShape(26.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Primary.copy(alpha = 0.12f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    icon()
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }
            }
            Spacer(modifier = Modifier.size(10.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                color = if (enabled) Primary else MaterialTheme.colorScheme.outline,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ThemeCard(
    isDarkThemeEnabled: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = GlassSurface,
        shape = RoundedCornerShape(26.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "테마",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ThemeOption(
                    label = "다크",
                    selected = isDarkThemeEnabled,
                    icon = Icons.Default.DarkMode,
                    modifier = Modifier.weight(1f),
                    onClick = { onToggleDarkTheme(true) }
                )
                ThemeOption(
                    label = "화이트",
                    selected = !isDarkThemeEnabled,
                    icon = Icons.Default.LightMode,
                    modifier = Modifier.weight(1f),
                    onClick = { onToggleDarkTheme(false) }
                )
            }
        }
    }
}

@Composable
private fun ThemeOption(
    label: String,
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else GlassSurfaceStrong,
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.45f) else GlassBorder
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
