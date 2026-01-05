package com.stepsync.presentation.challenges

import androidx. compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation. lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose. foundation.lazy.grid.LazyVerticalGrid
import androidx.compose. foundation.lazy.grid.items
import androidx.compose.material. icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose. material. icons.filled.EmojiEvents
import androidx.compose.material. icons.filled.Group
import androidx.compose.material. icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui. Alignment
import androidx.compose. ui.Modifier
import androidx. compose.ui.text.style.TextOverflow
import androidx. compose.ui.unit.dp
import com.stepsync.data.model.Challenge
import com.stepsync.util.ResponsiveUtils
import java.text.SimpleDateFormat
import java.util.*

/**
 * Main Challenges screen - browse and join challenges
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengesScreen(
    viewModel: ChallengeViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToChallengeDetail: (String) -> Unit
) {
    val activeChallenges by viewModel. activeChallenges.collectAsState()
    val myChallenges by viewModel.myChallenges.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }

    // ✅ Get responsive values
    val contentPadding = ResponsiveUtils.getContentPadding()
    val cardSpacing = ResponsiveUtils.getCardSpacing()
    val maxContentWidth = ResponsiveUtils.getMaxContentWidth()
    val windowSize = ResponsiveUtils.getWindowSize()

    // Handle UI state changes
    LaunchedEffect(uiState) {
        when (uiState) {
            is ChallengeUiState.Success -> {
                snackbarMessage = (uiState as ChallengeUiState.Success).message
                showSnackbar = true
                viewModel.resetUiState()
            }
            is ChallengeUiState.Error -> {
                snackbarMessage = (uiState as ChallengeUiState. Error).message
                showSnackbar = true
                viewModel. resetUiState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Challenges") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = {
            if (showSnackbar) {
                Snackbar(
                    action = {
                        TextButton(onClick = { showSnackbar = false }) {
                            Text("Dismiss")
                        }
                    },
                    modifier = Modifier.padding(contentPadding)
                ) {
                    Text(snackbarMessage)
                }
            }
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
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("All Challenges") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("My Challenges") }
                    )
                }

                val challenges = if (selectedTab == 0) activeChallenges else myChallenges

                when {
                    uiState is ChallengeUiState.Loading -> {
                        Box(
                            modifier = Modifier. fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    challenges.isEmpty() -> {
                        Box(
                            modifier = Modifier. fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (selectedTab == 0)
                                    "No active challenges available.\nCheck back later!"
                                else
                                    "You haven't joined any challenges yet.\nBrowse and join one! ",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    else -> {
                        // ✅ Adaptive layout:  List on phones, Grid on tablets
                        if (windowSize == ResponsiveUtils.WindowSize. COMPACT) {
                            // Phone:  List view
                            LazyColumn(
                                modifier = Modifier. fillMaxSize(),
                                contentPadding = PaddingValues(contentPadding),
                                verticalArrangement = Arrangement. spacedBy(cardSpacing)
                            ) {
                                items(challenges) { challenge ->
                                    ChallengeCard(
                                        challenge = challenge,
                                        onClick = { onNavigateToChallengeDetail(challenge.id) },
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
                                items(challenges) { challenge ->
                                    ChallengeCard(
                                        challenge = challenge,
                                        onClick = { onNavigateToChallengeDetail(challenge. id) },
                                        contentPadding = contentPadding
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengeCard(
    challenge: Challenge,
    onClick: () -> Unit,
    contentPadding: androidx.compose.ui.unit.Dp = 16.dp
) {
    Card(
        modifier = Modifier. fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier. padding(contentPadding),  // ✅ Responsive padding
            verticalArrangement = Arrangement. spacedBy(12.dp)
        ) {
            // Challenge name
            Row(
                modifier = Modifier. fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = challenge.name,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Active badge
                if (challenge.isActive) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "ACTIVE",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Description
            Text(
                text = challenge.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Challenge details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Step goal
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${challenge.stepGoal} steps",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Participants
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme. primary
                    )
                    Text(
                        text = "${challenge.participantCount} joined",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Duration
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = formatDateRange(challenge.startDate, challenge.endDate),
                        style = MaterialTheme. typography.bodySmall
                    )
                }
            }
        }
    }
}

/**
 * Format date range for display
 */
private fun formatDateRange(startDate:  Long, endDate: Long): String {
    val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
    val start = dateFormat.format(Date(startDate))
    val end = dateFormat.format(Date(endDate))
    return "$start - $end"
}