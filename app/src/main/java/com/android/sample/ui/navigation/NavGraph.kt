package com.android.sample.ui.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.android.sample.HomeScreen
import com.android.sample.MainPageViewModel
import com.android.sample.model.authentication.AuthenticationViewModel
import com.android.sample.model.authentication.UserSessionManager
import com.android.sample.model.skill.MainSubject
import com.android.sample.ui.bookings.MyBookingsScreen
import com.android.sample.ui.bookings.MyBookingsViewModel
import com.android.sample.ui.login.LoginScreen
import com.android.sample.ui.profile.MyProfileScreen
import com.android.sample.ui.profile.MyProfileViewModel
import com.android.sample.ui.screens.newSkill.NewSkillScreen
import com.android.sample.ui.signup.SignUpScreen
import com.android.sample.ui.signup.SignUpViewModel
import com.android.sample.ui.subject.SubjectListScreen
import com.android.sample.ui.subject.SubjectListViewModel

private const val TAG = "NavGraph"

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
    mainPageViewModel: MainPageViewModel,
    authViewModel: AuthenticationViewModel,
    onGoogleSignIn: () -> Unit
) {
  val academicSubject = remember { mutableStateOf<MainSubject?>(null) }

  NavHost(navController = navController, startDestination = NavRoutes.LOGIN) {
    composable(NavRoutes.LOGIN) {
      LaunchedEffect(Unit) { RouteStackManager.addRoute(NavRoutes.LOGIN) }
      LoginScreen(
          viewModel = authViewModel,
          onGoogleSignIn = onGoogleSignIn,
          onGitHubSignIn = { // Temporary functionality to go to home page while GitHub auth isn't
            // implemented
            navController.navigate(NavRoutes.HOME) { popUpTo(NavRoutes.LOGIN) { inclusive = true } }
          },
          onNavigateToSignUp = { // Add this navigation callback
            navController.navigate(NavRoutes.SIGNUP)
          })
    }

    composable(NavRoutes.PROFILE) {
      val currentUserId = UserSessionManager.getCurrentUserId() ?: "guest"
      LaunchedEffect(Unit) { RouteStackManager.addRoute(NavRoutes.PROFILE) }
      MyProfileScreen(profileViewModel = profileViewModel, profileId = currentUserId)
    }

    composable(NavRoutes.HOME) {
      LaunchedEffect(Unit) { RouteStackManager.addRoute(NavRoutes.HOME) }
      HomeScreen(
          mainPageViewModel = mainPageViewModel,
          onNavigateToNewSkill = { profileId ->
            navController.navigate(NavRoutes.createNewSkillRoute(profileId))
          },
          onNavigateToSubjectList = { subject ->
            academicSubject.value = subject
            navController.navigate(NavRoutes.SKILLS)
          })
    }

    composable(NavRoutes.SKILLS) {
      LaunchedEffect(Unit) { RouteStackManager.addRoute(NavRoutes.SKILLS) }
      SubjectListScreen(
          viewModel =
              SubjectListViewModel(), // You may need to provide this through dependency injection
          onBookTutor = { profile ->
            // Navigate to booking or profile screen when tutor is booked
            // Example: navController.navigate("booking/${profile.uid}")
          },
          subject = academicSubject.value)
    }

    composable(NavRoutes.BOOKINGS) {
      LaunchedEffect(Unit) { RouteStackManager.addRoute(NavRoutes.BOOKINGS) }
      MyBookingsScreen(viewModel = bookingsViewModel, navController = navController)
    }

    composable(
        route = NavRoutes.NEW_SKILL,
        arguments = listOf(navArgument("profileId") { type = NavType.StringType })) { backStackEntry
          ->
          val profileId = backStackEntry.arguments?.getString("profileId") ?: ""
          LaunchedEffect(Unit) { RouteStackManager.addRoute(NavRoutes.NEW_SKILL) }
          NewSkillScreen(profileId = profileId)
        }

    composable(
        route = NavRoutes.SIGNUP,
        arguments =
            listOf(
                navArgument("email") {
                  type = NavType.StringType
                  nullable = true
                  defaultValue = null
                })) { backStackEntry ->
          LaunchedEffect(Unit) { RouteStackManager.addRoute(NavRoutes.SIGNUP) }
          val email = backStackEntry.arguments?.getString("email")

          // Debug logging
          Log.d(TAG, "SignUp - Received email parameter: $email")

          // Create ViewModel with email parameter so it's available immediately
          val viewModel = SignUpViewModel(initialEmail = email)

          SignUpScreen(
              vm = viewModel,
              onSubmitSuccess = {
                // Navigate to login after successful signup
                navController.navigate(NavRoutes.LOGIN) {
                  popUpTo(NavRoutes.SIGNUP_BASE) { inclusive = true }
                }
              })
        }
  }
}
