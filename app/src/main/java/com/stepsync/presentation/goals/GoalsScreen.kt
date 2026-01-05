package com.stepsync.presentation.goals

import androidx. compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy. items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose. foundation.lazy.grid.LazyVerticalGrid
import androidx.compose. foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx. compose.material.icons.filled. Add
import androidx.compose.material. icons.filled.ArrowBack
import androidx.compose. material.icons.filled.CheckCircle
import androidx.compose. material. icons.filled.Delete
import androidx.compose.material. icons.filled. Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.stepsync.data.model.Goal
import com.stepsync.util.ResponsiveUtils
import java.text.SimpleDateFormat
import java. util.*

@Composable
fun GoalsScreen(
    viewModel: GoalsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val activeGoals by viewModel.activeGoals.collectAsState()
    val completedGoals by viewModel.completedGoals.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }

    val snackbarHostState = remember { SnackbarHostState() }

    // ✅ Get responsive values
    val contentPadding = ResponsiveUtils.getContentPadding()
    val cardSpacing = ResponsiveUtils.getCardSpacing()
    val maxContentWidth = ResponsiveUtils.getMaxContentWidth()
    val windowSize = ResponsiveUtils.getWindowSize()

    // Handle UI state changes
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is GoalUiState.Success -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel. resetUiState()
                showCreateDialog = false
            }
            is GoalUiState. Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetUiState()
            }
            else -> {}
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // ✅ Center content with max width on larger screens
        Box(
            modifier = Modifier. fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = maxContentWidth)  // ✅ Limit width on tablets
            ) {
                // Header with back button and refresh
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(contentPadding),  // ✅ Responsive padding
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                        Text(
                            text = "My Goals",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = { viewModel.refreshGoals() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                }

                // Tabs
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Active (${activeGoals.size})") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Completed (${completedGoals.size})") }
                    )
                }

                // Content
                when (selectedTab) {
                    0 -> ActiveGoalsTab(
                        goals = activeGoals,
                        onDeleteGoal = viewModel::deleteGoal,
                        onCompleteGoal = viewModel::markGoalAsCompleted,
                        contentPadding = contentPadding,
                        cardSpacing = cardSpacing,
                        windowSize = windowSize
                    )
                    1 -> CompletedGoalsTab(
                        goals = completedGoals,
                        onDeleteGoal = viewModel::deleteGoal,
                        contentPadding = contentPadding,
                        cardSpacing = cardSpacing,
                        windowSize = windowSize
                    )
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = { showCreateDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(contentPadding)  // ✅ Responsive padding
        ) {
            Icon(Icons.Default.Add, contentDescription = "Create Goal")
        }

        // Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
        )
    }

    // Create Goal Dialog
    if (showCreateDialog) {
        CreateGoalDialog(
            onDismiss = { showCreateDialog = false },
            onCreateGoal = viewModel::createGoal
        )
    }
}

@Composable
fun ActiveGoalsTab(
    goals:  List<Goal>,
    onDeleteGoal: (String) -> Unit,
    onCompleteGoal: (String) -> Unit,
    contentPadding: androidx.compose.ui.unit.Dp,
    cardSpacing: androidx.compose.ui. unit.Dp,
    windowSize: ResponsiveUtils.WindowSize
) {
    if (goals.isEmpty()) {
        Box(
            modifier = Modifier. fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "No active goals",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Tap + to create your first goal",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme. onSurfaceVariant
                )
            }
        }
    } else {
        // ✅ Adaptive layout:  List on phones, Grid on tablets
        if (windowSize == ResponsiveUtils.WindowSize. COMPACT) {
            // Phone:  List view
            LazyColumn(
                modifier = Modifier. fillMaxSize(),
                contentPadding = PaddingValues(contentPadding),  // ✅ Responsive padding
                verticalArrangement = Arrangement.spacedBy(cardSpacing)  // ✅ Responsive spacing
            ) {
                items(goals, key = { it.id }) { goal ->
                    GoalCard(
                        goal = goal,
                        onDelete = { onDeleteGoal(goal.id) },
                        onComplete = { onCompleteGoal(goal. id) },
                        contentPadding = contentPadding
                    )
                }
            }
        } else {
            // Tablet: Grid view
            val columns = if (windowSize == ResponsiveUtils.WindowSize. MEDIUM) 2 else 3
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                modifier = Modifier. fillMaxSize(),
                contentPadding = PaddingValues(contentPadding),
                horizontalArrangement = Arrangement. spacedBy(cardSpacing),
                verticalArrangement = Arrangement.spacedBy(cardSpacing)
            ) {
                items(goals, key = { it.id }) { goal ->
                    GoalCard(
                        goal = goal,
                        onDelete = { onDeleteGoal(goal.id) },
                        onComplete = { onCompleteGoal(goal.id) },
                        contentPadding = contentPadding
                    )
                }
            }
        }
    }
}

@Composable
fun CompletedGoalsTab(
    goals: List<Goal>,
    onDeleteGoal: (String) -> Unit,
    contentPadding: androidx.compose.ui. unit.Dp,
    cardSpacing: androidx.compose.ui.unit.Dp,
    windowSize: ResponsiveUtils. WindowSize
) {
    if (goals.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No completed goals yet",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme. colorScheme.onSurfaceVariant
            )
        }
    } else {
        // ✅ Adaptive layout:  List on phones, Grid on tablets
        if (windowSize == ResponsiveUtils.WindowSize.COMPACT) {
            // Phone: List view
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(contentPadding),
                verticalArrangement = Arrangement.spacedBy(cardSpacing)
            ) {
                items(goals, key = { it.id }) { goal ->
                    CompletedGoalCard(
                        goal = goal,
                        onDelete = { onDeleteGoal(goal.id) },
                        contentPadding = contentPadding
                    )
                }
            }
        } else {
            // Tablet: Grid view
            val columns = if (windowSize == ResponsiveUtils. WindowSize.MEDIUM) 2 else 3
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(contentPadding),
                horizontalArrangement = Arrangement.spacedBy(cardSpacing),
                verticalArrangement = Arrangement.spacedBy(cardSpacing)
            ) {
                items(goals, key = { it.id }) { goal ->
                    CompletedGoalCard(
                        goal = goal,
                        onDelete = { onDeleteGoal(goal.id) },
                        contentPadding = contentPadding
                    )
                }
            }
        }
    }
}

@Composable
fun GoalCard(
    goal: Goal,
    onDelete: () -> Unit,
    onComplete: () -> Unit,
    contentPadding:  androidx.compose.ui.unit. Dp = 16.dp
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),  // ✅ Responsive padding
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = goal. title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (goal.description.isNotBlank()) {
                        Text(
                            text = goal.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Goal Type Badge
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = goal.goalType.name,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Divider()

            // Progress
            Row(
                modifier = Modifier. fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${goal.currentSteps} / ${goal.targetSteps} steps",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${(goal.progress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme. colorScheme.primary
                )
            }

            LinearProgressIndicator(
                progress = goal.progress,
                modifier = Modifier.fillMaxWidth()
            )

            // End Date
            Text(
                text = "Ends:  ${dateFormat.format(Date(goal.endDate))} (${goal.daysRemaining} days left)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults. outlinedButtonColors(
                        contentColor = MaterialTheme. colorScheme.error
                    )
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Delete")
                }

                Button(
                    onClick = onComplete,
                    modifier = Modifier.weight(1f),
                    enabled = goal.progress >= 1f
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier. size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Complete")
                }
            }
        }
    }
}

@Composable
fun CompletedGoalCard(
    goal: Goal,
    onDelete:  () -> Unit,
    contentPadding: androidx.compose.ui.unit.Dp = 16.dp
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults. cardColors(
            containerColor = MaterialTheme.colorScheme. surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                . fillMaxWidth()
                .padding(contentPadding),  // ✅ Responsive padding
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier. fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default. CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = goal. title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (goal.description.isNotBlank()) {
                        Text(
                            text = goal.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Text(
                text = "${goal.currentSteps} / ${goal.targetSteps} steps",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            goal.completedAt?. let {
                Text(
                    text = "Completed on ${dateFormat.format(Date(it))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}