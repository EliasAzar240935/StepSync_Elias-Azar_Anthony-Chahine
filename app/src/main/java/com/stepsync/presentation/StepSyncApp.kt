package com.stepsync.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.stepsync.presentation.auth.AuthViewModel
import com.stepsync.presentation.auth.LoginScreen
import com.stepsync.presentation.auth.RegisterScreen
import com.stepsync.presentation.goals.GoalsScreen
import com.stepsync.presentation.goals.GoalsViewModel
import com.stepsync.presentation.home.HomeScreen
import com.stepsync.presentation.home.HomeViewModel
import com.stepsync.presentation.profile.ProfileScreen
import com.stepsync.presentation.profile.ProfileViewModel
import com.stepsync.presentation.social.SocialScreen
import com.stepsync.presentation.social.SocialViewModel
import androidx.compose.ui.Alignment
import com.stepsync.presentation.achievements.AchievementsViewModel
import com.stepsync.presentation. achievements.AchievementsScreen
import com.stepsync.presentation.challenges.ChallengeViewModel
import com.stepsync. presentation.challenges.ChallengesScreen
import com.stepsync.presentation.challenges.ChallengeDetailScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument

/**
 * Main app composable with navigation
 * Uses Firebase Auth state to determine the start destination
 */
@Composable
fun StepSyncApp(
    navController: NavHostController = rememberNavController()
) {
    // Get AuthViewModel to check authentication state
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.authState.collectAsState()
    
    // Determine if user is authenticated based on Firebase Auth state
    val isAuthenticated = authState != null
    
    // Show loading state while checking authentication
    var isInitialized by remember { mutableStateOf(false) }
    
    LaunchedEffect(authState) {
        // Mark as initialized once we get the first auth state
        isInitialized = true
    }
    
    if (!isInitialized) {
        // Show loading while checking auth state
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }
    
    val startDestination = if (isAuthenticated) Screen.Home.route else Screen.Login.route
    
    // Handle navigation when auth state changes
    LaunchedEffect(isAuthenticated) {
        val currentRoute = navController.currentDestination?.route
        if (isAuthenticated && currentRoute == Screen.Login.route) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        } else if (!isAuthenticated && currentRoute != Screen.Login.route && currentRoute != Screen.Register.route) {
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            val viewModel: AuthViewModel = hiltViewModel()
            LoginScreen(
                viewModel = viewModel,
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Register.route) {
            val viewModel: AuthViewModel = hiltViewModel()
            RegisterScreen(
                viewModel = viewModel,
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Home.route) {
            val viewModel: HomeViewModel = hiltViewModel()
            HomeScreen(
                viewModel = viewModel,
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateToActivity = {
                    navController.navigate(Screen.Activity.route)
                },
                onNavigateToGoals = {
                    navController.navigate(Screen.Goals.route)
                },
                onNavigateToSocial = {
                    navController.navigate(Screen.Social.route)
                },
                onNavigateToChallenges = {
                    navController.navigate(Screen.Challenges.route)
                }
            )
        }
        
        composable(Screen.Profile.route) {
            val viewModel: ProfileViewModel = hiltViewModel()
            ProfileScreen(
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.navigateUp()  // ✅ Goes back to previous screen
                }
            )
        }

        composable(Screen.Activity.route) {
            val viewModel: AchievementsViewModel = hiltViewModel()
            AchievementsScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Goals. route) {
            val viewModel:  GoalsViewModel = hiltViewModel()
            GoalsScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Social.route) {
            val viewModel: SocialViewModel = hiltViewModel()
            SocialScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("challenges") {
            val viewModel:  ChallengeViewModel = hiltViewModel()
            ChallengesScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToChallengeDetail = { challengeId ->
                    navController.navigate("challenge_detail/$challengeId")  // ADD THIS
                }
            )
        }

        // Challenge Detail Screen - ADD THIS WHOLE BLOCK
        composable(
            route = "challenge_detail/{challengeId}",
            arguments = listOf(navArgument("challengeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val challengeId = backStackEntry.arguments?.getString("challengeId") ?: ""
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry("challenges")
            }
            val viewModel:  ChallengeViewModel = hiltViewModel(parentEntry)

            ChallengeDetailScreen(
                challengeId = challengeId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen. ChallengeDetail.route) { backStackEntry ->
            val challengeId = backStackEntry.arguments?.getString("challengeId") ?: ""
            val viewModel: ChallengeViewModel = hiltViewModel()
            ChallengeDetailScreen(
                challengeId = challengeId,
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
