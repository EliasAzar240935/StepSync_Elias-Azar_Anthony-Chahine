package com.stepsync.presentation.home

import androidx. compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy. items
import androidx.compose.material. icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.stepsync.data.model.StepRecord
import com. stepsync.util.Constants
import com.stepsync.util.ResponsiveUtils

/**
 * Home screen showing step count and progress
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToProfile: () -> Unit,
    onNavigateToActivity: () -> Unit,
    onNavigateToGoals: () -> Unit,
    onNavigateToSocial: () -> Unit,
    onNavigateToChallenges: () -> Unit
) {
    val currentUser by viewModel.currentUser. collectAsState()
    val todaySteps by viewModel.todaySteps.collectAsState()
    val recentSteps by viewModel.recentSteps. collectAsState()

    // ✅ Get responsive values
    val contentPadding = ResponsiveUtils.getContentPadding()
    val cardSpacing = ResponsiveUtils.getCardSpacing()
    val maxContentWidth = ResponsiveUtils.getMaxContentWidth()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("StepSync") },
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons. Default.Person, contentDescription = "Profile")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToActivity,
                    icon = { Icon(Icons.Default.Star, contentDescription = "Achievements") },
                    label = { Text("Awards") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToGoals,
                    icon = { Icon(Icons.Default.Flag, contentDescription = "Goals") },
                    label = { Text("Goals") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToSocial,
                    icon = { Icon(Icons.Default.People, contentDescription = "Social") },
                    label = { Text("Social") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToChallenges,
                    icon = { Icon(Icons.Default.EmojiEvents, contentDescription = "Challenges") },
                    label = { Text("Challenges") }
                )
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = maxContentWidth)  // ✅ Limit width on tablets
                    .padding(contentPadding),  // ✅ Responsive padding
                verticalArrangement = Arrangement. spacedBy(cardSpacing)  // ✅ Responsive spacing
            ) {
                item {
                    currentUser?.let { user ->
                        Text(
                            text = "Welcome, ${user.name}!",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(contentPadding),  // ✅ Responsive padding
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Today's Steps",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            val steps = todaySteps?.steps ?: 0
                            Text(
                                text = steps.toString(),
                                style = MaterialTheme.typography.displayLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            val goal = currentUser?. dailyStepGoal ?:  Constants.DEFAULT_DAILY_STEP_GOAL
                            val progress = steps. toFloat() / goal.toFloat()

                            LinearProgressIndicator(
                                progress = progress. coerceIn(0f, 1f),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier. height(8.dp))
                            Text(
                                text = "Goal: $goal steps",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(cardSpacing)  // ✅ Responsive spacing
                    ) {
                        Card(
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(contentPadding),  // ✅ Responsive padding
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.DirectionsWalk,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp)  // ✅ Consistent icon size
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                val distance = todaySteps?.distance ?: 0f
                                Text(
                                    text = String.format("%.0f m", distance),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Distance",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(contentPadding),  // ✅ Responsive padding
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.LocalFireDepartment,
                                    contentDescription = null,
                                    modifier = Modifier. size(32.dp)  // ✅ Consistent icon size
                                )
                                Spacer(modifier = Modifier. height(8.dp))
                                val calories = todaySteps?.calories ?: 0f
                                Text(
                                    text = "${calories.toInt()} kcal",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Calories",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                items(recentSteps) { record ->
                    StepRecordItem(
                        record = record,
                        contentPadding = contentPadding  // ✅ Pass responsive padding
                    )
                }
            }
        }
    }
}

@Composable
fun StepRecordItem(
    record: StepRecord,
    contentPadding: androidx.compose.ui.unit. Dp = 16.dp  // ✅ Accept responsive padding
) {
    Card(
        modifier = Modifier. fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),  // ✅ Responsive padding
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = record.date,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${record.steps} steps",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Column(
                horizontalAlignment = Alignment. End
            ) {
                Text(
                    text = String. format("%.0f m", record.distance),
                    style = MaterialTheme.typography. bodyMedium
                )
                Text(
                    text = "${record.calories. toInt()} kcal",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}