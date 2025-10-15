package com.android.sample.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.android.sample.ui.bookings.MyBookingsScreen
import com.android.sample.ui.bookings.MyBookingsViewModel
import com.android.sample.ui.profile.MyProfileViewModel
import com.android.sample.HomeScreen
import com.android.sample.MainPageViewModel
import com.android.sample.ui.profile.MyProfileScreen
import com.android.sample.ui.screens.newSkill.NewSkillScreen
import com.android.sample.ui.login.LoginScreen
import com.android.sample.ui.subject.SubjectListScreen
import com.android.sample.ui.subject.SubjectListViewModel


/**
 * AppNavGraph - Main navigation configuration for the SkillBridge app
 *
 * This file defines all navigation routes and their corresponding screen composables. Each route is
 * registered with the NavHost and includes route tracking via RouteStackManager.
 *
 * Usage:
 * - Call AppNavGraph(navController) from your main activity/composable
 * - Navigation is handled through the provided NavHostController
 *
 * Adding a new screen:
 * 1. Add route constant to NavRoutes object
 * 2. Import the new screen composable
 * 3. Add composable() block with LaunchedEffect for route tracking
 * 4. Pass navController parameter if screen needs navigation
 *
 * Removing a screen:
 * 1. Delete the composable() block
 * 2. Remove unused import
 * 3. Remove route constant from NavRoutes (if no longer needed)
 *
 * Note: All screens automatically register with RouteStackManager for back navigation tracking
 */
@Composable
fun AppNavGraph(
  navController: NavHostController,
  bookingsViewModel: MyBookingsViewModel,
  profileViewModel: MyProfileViewModel,
  mainPageViewModel: MainPageViewModel
) {
  NavHost(navController = navController, startDestination = NavRoutes.LOGIN) {

    composable(NavRoutes.LOGIN) {
      LaunchedEffect(Unit) { RouteStackManager.addRoute(NavRoutes.LOGIN) }
      LoginScreen(
        onGoogleSignIn = {}, // Add google auth here once ready
        onGitHubSignIn = { // Temporary functionality to go to home page while auth isn't done
          navController.navigate(NavRoutes.HOME) {
            popUpTo(NavRoutes.LOGIN) { inclusive = true }
          }
        }
      )
    }

    composable(NavRoutes.PROFILE) {
      LaunchedEffect(Unit) { RouteStackManager.addRoute(NavRoutes.PROFILE) }
      MyProfileScreen(
        profileViewModel = profileViewModel,
        profileId = "test" // Using the same hardcoded user ID from MainActivity
      )
    }

    composable(NavRoutes.HOME) {
      LaunchedEffect(Unit) { RouteStackManager.addRoute(NavRoutes.HOME) }
      HomeScreen(
        mainPageViewModel = mainPageViewModel,
        onNavigateToNewSkill = { profileId ->
          navController.navigate(NavRoutes.createNewSkillRoute(profileId))
        }
      )
    }

    composable(NavRoutes.SKILLS) {
      LaunchedEffect(Unit) { RouteStackManager.addRoute(NavRoutes.SKILLS) }
      SubjectListScreen(
        viewModel = SubjectListViewModel(), // You may need to provide this through dependency injection
        onBookTutor = { profile ->
          // Navigate to booking or profile screen when tutor is booked
          // Example: navController.navigate("booking/${profile.uid}")
        }
      )
    }

    composable(NavRoutes.BOOKINGS) {
      LaunchedEffect(Unit) { RouteStackManager.addRoute(NavRoutes.BOOKINGS) }
      MyBookingsScreen(viewModel = bookingsViewModel, navController = navController)
    }

    composable(
      route = NavRoutes.NEW_SKILL,
      arguments = listOf(navArgument("profileId") { type = NavType.StringType })
    ) { backStackEntry ->
      val profileId = backStackEntry.arguments?.getString("profileId") ?: ""
      LaunchedEffect(Unit) { RouteStackManager.addRoute(NavRoutes.NEW_SKILL) }
      NewSkillScreen(profileId = profileId)
    }
  }
}
