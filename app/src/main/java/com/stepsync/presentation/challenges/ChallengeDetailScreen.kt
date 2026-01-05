package com.stepsync.presentation.challenges

import androidx.compose.foundation. layout.*
import androidx.compose. foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose. material.icons.Icons
import androidx. compose.material.icons.filled. ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.stepsync.data. model.Challenge
import com.stepsync.data.model.LeaderboardEntry
import com.stepsync.util.ResponsiveUtils
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengeDetailScreen(
    challengeId: String,
    viewModel: ChallengeViewModel,
    onNavigateBack: () -> Unit
) {
    val challenge by viewModel.selectedChallenge.collectAsState()
    val userParticipation by viewModel.userParticipation.collectAsState()
    val leaderboard by viewModel.leaderboard.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var hasJoined by remember { mutableStateOf(false) }

    // ✅ Get responsive values
    val contentPadding = ResponsiveUtils.getContentPadding()
    val cardSpacing = ResponsiveUtils.getCardSpacing()
    val maxContentWidth = ResponsiveUtils.getMaxContentWidth()
    val windowSize = ResponsiveUtils.getWindowSize()

    LaunchedEffect(challengeId) {
        viewModel.loadChallengeDetails(challengeId)
        hasJoined = viewModel.hasJoinedChallenge(challengeId)
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is ChallengeUiState. Success -> {
                hasJoined = viewModel.hasJoinedChallenge(challengeId)
                viewModel.resetUiState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Challenge Details") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.clearSelectedChallenge()
                        onNavigateBack()
                    }) {
                        Icon(Icons. Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (challenge == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // ✅ Center content with max width on larger screens
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.TopCenter
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = maxContentWidth)  // ✅ Limit width on tablets
                        .padding(contentPadding),  // ✅ Responsive padding
                    verticalArrangement = Arrangement.spacedBy(cardSpacing)  // ✅ Responsive spacing
                ) {
                    // Challenge Header
                    item {
                        ChallengeHeader(
                            challenge = challenge!! ,
                            hasJoined = hasJoined,
                            onJoinClick = { viewModel.joinChallenge(challengeId) },
                            onLeaveClick = { viewModel.leaveChallenge(challengeId) },
                            isLoading = uiState is ChallengeUiState. Loading,
                            contentPadding = contentPadding,
                            windowSize = windowSize
                        )
                    }

                    // User Progress (if joined)
                    if (hasJoined && userParticipation != null) {
                        item {
                            UserProgressCard(
                                participation = userParticipation!!,
                                challenge = challenge!!,
                                contentPadding = contentPadding
                            )
                        }
                    }

                    // Leaderboard Header
                    item {
                        Text(
                            text = "Leaderboard",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Leaderboard Content
                    if (leaderboard. isEmpty()) {
                        item {
                            Text(
                                text = "No participants yet.  Be the first to join!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme. onSurfaceVariant
                            )
                        }
                    } else {
                        items(leaderboard) { entry ->
                            LeaderboardItem(
                                entry = entry,
                                contentPadding = contentPadding
                            )
                        }
                    }
                }
            }
        }

        // Show snackbar for errors
        if (uiState is ChallengeUiState. Error) {
            LaunchedEffect(uiState) {
                // Show error message
                viewModel.resetUiState()
            }
        }
    }
}

@Composable
fun ChallengeHeader(
    challenge: Challenge,
    hasJoined:  Boolean,
    onJoinClick: () -> Unit,
    onLeaveClick: () -> Unit,
    isLoading: Boolean,
    contentPadding: androidx.compose.ui.unit. Dp,
    windowSize: ResponsiveUtils.WindowSize
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),  // ✅ Responsive padding
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(
                        when (windowSize) {
                            ResponsiveUtils.WindowSize. COMPACT -> 32.dp
                            else -> 40.dp
                        }
                    )  // ✅ Larger icon on tablets
                )
                Text(
                    text = challenge.name,
                    style = when (windowSize) {
                        ResponsiveUtils.WindowSize. COMPACT -> MaterialTheme.typography.headlineSmall
                        else -> MaterialTheme. typography.headlineMedium
                    },  // ✅ Larger text on tablets
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // Description
            Text(
                text = challenge.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Divider()

            // ✅ Adaptive layout: Stack vertically on phones, side-by-side on tablets
            if (windowSize == ResponsiveUtils.WindowSize.COMPACT) {
                // Phone:  Vertical stack
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement. spacedBy(12.dp)
                ) {
                    ChallengeInfoRow(
                        label = "Daily Goal",
                        value = "${challenge.stepGoal} steps",
                        alignment = Alignment.Start
                    )
                    ChallengeInfoRow(
                        label = "Participants",
                        value = "${challenge.participantCount}",
                        alignment = Alignment.Start
                    )
                }
            } else {
                // Tablet: Side-by-side
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ChallengeInfoRow(
                        label = "Daily Goal",
                        value = "${challenge.stepGoal} steps",
                        alignment = Alignment.Start
                    )
                    ChallengeInfoRow(
                        label = "Participants",
                        value = "${challenge.participantCount}",
                        alignment = Alignment.End
                    )
                }
            }

            // Dates
            Text(
                text = "Duration:  ${dateFormat.format(Date(challenge.startDate))} - ${dateFormat.format(Date(challenge.endDate))}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer. copy(alpha = 0.8f)
            )

            // Join/Leave Button
            Button(
                onClick = if (hasJoined) onLeaveClick else onJoinClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = ! isLoading,
                colors = if (hasJoined) {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme. error
                    )
                } else {
                    ButtonDefaults. buttonColors()
                }
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme. onPrimary
                    )
                } else {
                    Text(if (hasJoined) "Leave Challenge" else "Join Challenge")
                }
            }
        }
    }
}

@Composable
fun ChallengeInfoRow(
    label: String,
    value: String,
    alignment: Alignment. Horizontal
) {
    Column(horizontalAlignment = alignment) {
        Text(
            text = label,
            style = MaterialTheme. typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer. copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun UserProgressCard(
    participation: com.stepsync.data.model.ChallengeParticipation,
    challenge: Challenge,
    contentPadding: androidx.compose.ui.unit.Dp
) {
    // ✅ ADD THESE 2 LINES
    val durationInDays = ((challenge.endDate - challenge.startDate) / (1000 * 60 * 60 * 24)).toInt()
    val totalGoal = challenge.stepGoal * durationInDays
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),  // ✅ Responsive padding
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Your Progress",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier. fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Total Steps",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = participation.currentSteps.toString(),
                        style = MaterialTheme.typography. titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme. colorScheme.primary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Status",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (participation.isCompleted) "Completed" else "In Progress",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (participation.isCompleted)
                            MaterialTheme.colorScheme.secondary
                        else
                            MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Progress bar
            // ✅ REPLACE WITH:
            val progress = (participation.currentSteps.toFloat() / totalGoal).coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth()
            )

            // ✅ REPLACE WITH:
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Goal: ${String.format("%,d", totalGoal)} steps total",
                    style = MaterialTheme.typography. bodySmall,
                    color = MaterialTheme.colorScheme. onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${String.format("%,d", challenge.stepGoal)} steps/day × $durationInDays days",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun LeaderboardItem(
    entry: LeaderboardEntry,
    contentPadding: androidx.compose.ui.unit.Dp = 16.dp
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),  // ✅ Responsive padding
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement. spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Rank badge
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = when (entry.rank) {
                        1 -> MaterialTheme. colorScheme.primary
                        2 -> MaterialTheme.colorScheme.secondary
                        3 -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Text(
                        text = "#${entry.rank}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (entry.rank <= 3) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }

                Text(
                    text = entry. userName + if (entry.isCurrentUser) " (You)" else "",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (entry.isCurrentUser) FontWeight.Bold else FontWeight.Normal
                )
            }

            // Steps
            Text(
                text = entry.steps.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme. primary
            )
        }
    }
}