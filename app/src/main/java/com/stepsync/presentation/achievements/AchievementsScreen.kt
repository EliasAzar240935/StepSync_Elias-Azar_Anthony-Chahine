package com.stepsync.presentation.achievements

import androidx. compose.foundation.background
import androidx.compose.foundation. layout.*
import androidx.compose.foundation. lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation. lazy.grid.GridCells
import androidx.compose. foundation.lazy.grid.LazyVerticalGrid
import androidx.compose. foundation.lazy.grid.items
import androidx.compose.foundation. shape.CircleShape
import androidx.compose. material. icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui. Alignment
import androidx.compose. ui.Modifier
import androidx. compose.ui.draw.alpha
import androidx.compose.ui. graphics.Color
import androidx.compose. ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.stepsync.data.model.Achievement
import com.stepsync. data.model.AchievementCategory
import androidx.compose.ui.draw.clip
import com.stepsync.data.model.AchievementTier
import com.stepsync.util.ResponsiveUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(
    viewModel: AchievementsViewModel,
    onNavigateBack: () -> Unit
) {
    val achievements by viewModel.achievements.collectAsState()
    val totalPoints by viewModel.totalPoints.collectAsState()
    val unlockedCount by viewModel.unlockedCount.collectAsState()
    val totalCount by viewModel.totalCount.collectAsState()

    // ✅ Get responsive values
    val contentPadding = ResponsiveUtils.getContentPadding()
    val cardSpacing = ResponsiveUtils.getCardSpacing()
    val maxContentWidth = ResponsiveUtils.getMaxContentWidth()
    val windowSize = ResponsiveUtils.getWindowSize()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Achievements") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        // ✅ Center content with max width on larger screens
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment. TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = maxContentWidth)  // ✅ Limit width on tablets
            ) {
                // Stats Card
                AchievementStatsCard(
                    totalPoints = totalPoints,
                    unlockedCount = unlockedCount,
                    totalCount = totalCount,
                    contentPadding = contentPadding,
                    windowSize = windowSize
                )

                // Achievements List
                when {
                    achievements.isEmpty() && totalCount == 0 -> {
                        // Still loading or no achievements initialized
                        Box(
                            modifier = Modifier
                                . fillMaxSize()
                                . padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                CircularProgressIndicator()
                                Text(
                                    text = "Loading achievements...",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    achievements.isEmpty() && totalCount > 0 -> {
                        // Achievements exist but list is empty (shouldn't happen)
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No achievements found",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    else -> {
                        // ✅ Show achievements in grid on larger screens, list on phones
                        if (windowSize == ResponsiveUtils.WindowSize.COMPACT) {
                            // Phone:  List view
                            LazyColumn(
                                modifier = Modifier. fillMaxSize(),
                                contentPadding = PaddingValues(contentPadding),  // ✅ Responsive padding
                                verticalArrangement = Arrangement.spacedBy(cardSpacing)  // ✅ Responsive spacing
                            ) {
                                items(achievements) { achievement ->
                                    AchievementCard(
                                        achievement = achievement,
                                        contentPadding = contentPadding
                                    )
                                }
                            }
                        } else {
                            // Tablet: Grid view
                            val columns = if (windowSize == ResponsiveUtils.WindowSize.MEDIUM) 2 else 3
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(columns),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(contentPadding),
                                horizontalArrangement = Arrangement. spacedBy(cardSpacing),
                                verticalArrangement = Arrangement.spacedBy(cardSpacing)
                            ) {
                                items(achievements) { achievement ->
                                    AchievementCard(
                                        achievement = achievement,
                                        contentPadding = contentPadding,
                                        isCompact = true  // ✅ Compact card for grid
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AchievementStatsCard(
    totalPoints: Int,
    unlockedCount:  Int,
    totalCount: Int,
    contentPadding:  androidx.compose.ui.unit. Dp,
    windowSize: ResponsiveUtils.WindowSize
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(contentPadding),  // ✅ Responsive padding
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        // ✅ Stack stats vertically on small screens, horizontally on larger screens
        if (windowSize == ResponsiveUtils.WindowSize.COMPACT) {
            Column(
                modifier = Modifier
                    . fillMaxWidth()
                    . padding(contentPadding),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatItem(
                    icon = Icons.Default.EmojiEvents,
                    label = "Points",
                    value = totalPoints. toString(),
                    color = Color(0xFFFFD700)
                )
                Divider()
                StatItem(
                    icon = Icons.Default.Stars,
                    label = "Unlocked",
                    value = "$unlockedCount / $totalCount",
                    color = MaterialTheme.colorScheme.primary
                )
                Divider()
                val percentage = if (totalCount > 0) (unlockedCount * 100) / totalCount else 0
                StatItem(
                    icon = Icons.Default.TrendingUp,
                    label = "Progress",
                    value = "$percentage%",
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(contentPadding),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatItem(
                    icon = Icons.Default.EmojiEvents,
                    label = "Points",
                    value = totalPoints.toString(),
                    color = Color(0xFFFFD700)
                )

                VerticalDivider(
                    modifier = Modifier
                        .height(50.dp)
                        .width(1.dp)
                )

                StatItem(
                    icon = Icons.Default.Stars,
                    label = "Unlocked",
                    value = "$unlockedCount / $totalCount",
                    color = MaterialTheme.colorScheme.primary
                )

                VerticalDivider(
                    modifier = Modifier
                        .height(50.dp)
                        . width(1.dp)
                )

                val percentage = if (totalCount > 0) (unlockedCount * 100) / totalCount else 0
                StatItem(
                    icon = Icons.Default.TrendingUp,
                    label = "Progress",
                    value = "$percentage%",
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
fun VerticalDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.onPrimaryContainer. copy(alpha = 0.2f))
    )
}

@Composable
fun StatItem(
    icon: androidx. compose.ui.graphics.vector. ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(32.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer. copy(alpha = 0.7f)
        )
    }
}

@Composable
fun AchievementCard(
    achievement: Achievement,
    contentPadding: androidx.compose. ui.unit.Dp = 16.dp,
    isCompact: Boolean = false  // ✅ Compact layout for grid view
) {
    val tierColor = getTierColor(achievement.tier)
    val isLocked = ! achievement.isUnlocked

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (isLocked) 0.7f else 1f),
        colors = CardDefaults. cardColors(
            containerColor = if (isLocked)
                MaterialTheme.colorScheme.surfaceVariant
            else
                MaterialTheme.colorScheme. secondaryContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isLocked) 2.dp else 4.dp
        )
    ) {
        if (isCompact) {
            // ✅ Compact vertical layout for grid
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(contentPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(tierColor.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getIconForAchievement(achievement),
                        contentDescription = null,
                        tint = if (isLocked) Color.Gray else tierColor,
                        modifier = Modifier.size(28.dp)
                    )

                    if (isLocked) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = Color. Gray,
                            modifier = Modifier
                                .size(18.dp)
                                .align(Alignment.BottomEnd)
                                .background(MaterialTheme.colorScheme.surface, CircleShape)
                                .padding(2.dp)
                        )
                    }
                }

                // Title & Tier
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = achievement.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isLocked)
                            MaterialTheme.colorScheme.onSurfaceVariant
                        else
                            MaterialTheme.colorScheme.onSecondaryContainer,
                        maxLines = 1
                    )
                }

                TierBadge(achievement.tier)

                // Description
                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isLocked)
                        MaterialTheme.colorScheme.onSurfaceVariant. copy(alpha = 0.7f)
                    else
                        MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                    maxLines = 2
                )

                // Progress
                if (isLocked) {
                    LinearProgressIndicator(
                        progress = achievement.progress,
                        modifier = Modifier
                            . fillMaxWidth()
                            . height(6.dp)
                            .clip(MaterialTheme.shapes.small),
                        color = tierColor,
                        trackColor = Color. Gray. copy(alpha = 0.3f)
                    )
                    Text(
                        text = "${achievement.currentProgress} / ${achievement.requirement}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme. onSurfaceVariant
                    )
                }

                // Points
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Stars,
                        contentDescription = null,
                        tint = if (isLocked) Color.Gray else Color(0xFFFFD700),
                        modifier = Modifier. size(16.dp)
                    )
                    Text(
                        text = "+${achievement.points}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight. Bold,
                        color = if (isLocked) Color.Gray else Color(0xFFFFD700)
                    )
                }

                if (! isLocked) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Unlocked",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        } else {
            // ✅ Full horizontal layout for list
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(contentPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(tierColor.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getIconForAchievement(achievement),
                        contentDescription = null,
                        tint = if (isLocked) Color.Gray else tierColor,
                        modifier = Modifier. size(32.dp)
                    )

                    if (isLocked) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = Color.Gray,
                            modifier = Modifier
                                .size(20.dp)
                                .align(Alignment.BottomEnd)
                                .background(MaterialTheme.colorScheme.surface, CircleShape)
                                .padding(2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier. width(16.dp))

                // Content
                Column(
                    modifier = Modifier. weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = achievement.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isLocked)
                                MaterialTheme. colorScheme.onSurfaceVariant
                            else
                                MaterialTheme.colorScheme.onSecondaryContainer
                        )

                        TierBadge(achievement.tier)
                    }

                    Spacer(modifier = Modifier. height(4.dp))

                    Text(
                        text = achievement.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isLocked)
                            MaterialTheme.colorScheme.onSurfaceVariant. copy(alpha = 0.7f)
                        else
                            MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )

                    if (isLocked) {
                        Spacer(modifier = Modifier. height(8.dp))

                        // Progress bar
                        Column {
                            LinearProgressIndicator(
                                progress = achievement.progress,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    . clip(MaterialTheme.shapes. small),
                                color = tierColor,
                                trackColor = Color.Gray.copy(alpha = 0.3f)
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "${achievement.currentProgress} / ${achievement.requirement}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme. onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier. width(12.dp))

                // Points
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default. Stars,
                            contentDescription = null,
                            tint = if (isLocked) Color.Gray else Color(0xFFFFD700),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "+${achievement.points}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isLocked) Color.Gray else Color(0xFFFFD700)
                        )
                    }

                    if (!isLocked) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Unlocked",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TierBadge(tier:  AchievementTier) {
    val tierColor = getTierColor(tier)

    Surface(
        shape = MaterialTheme.shapes.small,
        color = tierColor. copy(alpha = 0.2f)
    ) {
        Text(
            text = tier.name,
            style = MaterialTheme. typography.labelSmall,
            color = tierColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun getTierColor(tier: AchievementTier): Color {
    return when (tier) {
        AchievementTier. BRONZE -> Color(0xFFCD7F32)
        AchievementTier.SILVER -> Color(0xFFC0C0C0)
        AchievementTier.GOLD -> Color(0xFFFFD700)
        AchievementTier. PLATINUM -> Color(0xFFE5E4E2)
        AchievementTier. DIAMOND -> Color(0xFFB9F2FF)
    }
}

@Composable
fun getIconForAchievement(achievement: Achievement): androidx.compose.ui.graphics.vector.ImageVector {
    return when (achievement.category) {
        AchievementCategory.STEPS -> Icons.Default.DirectionsWalk
        AchievementCategory.ACTIVITIES -> Icons.Default. FitnessCenter
        AchievementCategory.GOALS -> Icons. Default.Flag
        AchievementCategory.SOCIAL -> Icons. Default.People
        AchievementCategory.STREAKS -> Icons.Default.LocalFireDepartment
        AchievementCategory. SPECIAL -> Icons.Default. EmojiEvents
    }
}