package com.stepsync.presentation.profile

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation. lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material. icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose. material.icons.filled.ContentCopy
import androidx.compose. material. icons.filled.ExitToApp
import androidx.compose.material. icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui. Alignment
import androidx.compose. ui.Modifier
import androidx. compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.stepsync.data.model.Achievement
import com.stepsync. data.model.User
import com.stepsync.util.ResponsiveUtils

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigateToLogin: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    when (uiState) {
        is ProfileUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is ProfileUiState.NotAuthenticated -> {
            LaunchedEffect(Unit) {
                onNavigateToLogin()
            }
        }
        is ProfileUiState.Success -> {
            val user = (uiState as ProfileUiState.Success).user
            ProfileContent(
                user = user,
                onLogout = {
                    viewModel.logout()
                    onNavigateToLogin()
                },
                onNavigateBack = onNavigateBack
            )
        }
        is ProfileUiState.Error -> {
            val message = (uiState as ProfileUiState.Error).message
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = message, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier. height(16.dp))
                    Button(onClick = onNavigateToLogin) {
                        Text("Go to Login")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileContent(
    user:  User,
    onLogout:  () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showCopiedMessage by remember { mutableStateOf(false) }

    // ✅ Get responsive values
    val contentPadding = ResponsiveUtils.getContentPadding()
    val cardSpacing = ResponsiveUtils.getCardSpacing()
    val maxContentWidth = ResponsiveUtils.getMaxContentWidth()
    val windowSize = ResponsiveUtils.getWindowSize()

    // Show snackbar when code is copied
    LaunchedEffect(showCopiedMessage) {
        if (showCopiedMessage) {
            snackbarHostState.showSnackbar(
                message = "Friend code copied! ",
                duration = SnackbarDuration. Short
            )
            showCopiedMessage = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        // ✅ Center content with max width on larger screens
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment. TopCenter
        ) {
            // ✅ CHANGED TO LAZYCOLUMN FOR PROPER SCROLLING
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .widthIn(max = maxContentWidth),  // ✅ Limit width on tablets
                contentPadding = PaddingValues(contentPadding),  // ✅ Responsive padding
                verticalArrangement = Arrangement.spacedBy(cardSpacing),  // ✅ Responsive spacing
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Spacer(modifier = Modifier. height(cardSpacing))
                }

                // User Avatar/Icon
                item {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.size(
                            when (windowSize) {
                                ResponsiveUtils.WindowSize. COMPACT -> 100.dp
                                ResponsiveUtils.WindowSize.MEDIUM -> 120.dp
                                ResponsiveUtils.WindowSize. EXPANDED -> 140.dp
                            }
                        ),  // ✅ Responsive icon size
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                // User Name
                item {
                    Text(
                        text = user. name,
                        style = when (windowSize) {
                            ResponsiveUtils.WindowSize. COMPACT -> MaterialTheme.typography.headlineMedium
                            else -> MaterialTheme.typography.headlineLarge
                        },  // ✅ Responsive text size
                        fontWeight = FontWeight.Bold
                    )
                }

                // Friend Code Card - PROMINENT DISPLAY
                item {
                    Card(
                        modifier = Modifier. fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(contentPadding),  // ✅ Responsive padding
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Your Friend Code",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = user.friendCode. ifEmpty { "Loading..." },
                                    style = when (windowSize) {
                                        ResponsiveUtils.WindowSize.COMPACT -> MaterialTheme. typography.headlineLarge
                                        else -> MaterialTheme.typography.displaySmall
                                    },  // ✅ Responsive friend code size
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Spacer(modifier = Modifier. width(8.dp))

                                IconButton(
                                    onClick = {
                                        copyToClipboard(context, user.friendCode)
                                        showCopiedMessage = true
                                    },
                                    enabled = user.friendCode. isNotEmpty()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy Friend Code",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Text(
                                text = "Share this code with friends to connect! ",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                // Personal Information Card
                item {
                    PersonalInfoCard(
                        user = user,
                        contentPadding = contentPadding,
                        modifier = Modifier. fillMaxWidth()
                    )
                }

                // ✅ SPACER BEFORE LOGOUT BUTTON
                item {
                    Spacer(modifier = Modifier.height(cardSpacing))
                }

                // Logout Button
                item {
                    Button(
                        onClick = onLogout,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Logout")
                    }
                }

                // ✅ BOTTOM PADDING TO ENSURE BUTTON IS VISIBLE
                item {
                    Spacer(modifier = Modifier.height(contentPadding))
                }
            }
        }
    }
}

@Composable
private fun PersonalInfoCard(
    user: User,
    contentPadding: androidx.compose. ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),  // ✅ Responsive padding
            verticalArrangement = Arrangement. spacedBy(8.dp)
        ) {
            Text(
                text = "Personal Information",
                style = MaterialTheme. typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            ProfileInfoRow(label = "Email", value = user.email)
            Divider()
            ProfileInfoRow(label = "Age", value = "${user.age} years")
            Divider()
            ProfileInfoRow(label = "Weight", value = "${user. weight} kg")
            Divider()
            ProfileInfoRow(label = "Height", value = "${user.height} cm")
            Divider()
            ProfileInfoRow(label = "Daily Step Goal", value = "${user.dailyStepGoal} steps")
            Divider()
            ProfileInfoRow(label = "Fitness Goal", value = formatFitnessGoal(user.fitnessGoal))
        }
    }
}

@Composable
private fun ProfileInfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement. SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme. colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography. bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData. newPlainText("Friend Code", text)
    clipboard.setPrimaryClip(clip)
}

private fun formatFitnessGoal(goal: String): String {
    return when (goal) {
        "weight_loss" -> "Weight Loss"
        "muscle_gain" -> "Muscle Gain"
        "fitness" -> "General Fitness"
        "health" -> "Health Improvement"
        else -> goal. replaceFirstChar { it.uppercase() }
    }
}