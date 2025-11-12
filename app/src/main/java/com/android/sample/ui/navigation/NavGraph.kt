package com.android.sample.ui.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.android.sample.model.authentication.AuthenticationViewModel
import com.android.sample.model.authentication.UserSessionManager
import com.android.sample.model.skill.MainSubject
import com.android.sample.ui.HomePage.HomeScreen
import com.android.sample.ui.HomePage.MainPageViewModel
import com.android.sample.ui.bookings.BookingDetailsScreen
import com.android.sample.ui.bookings.MyBookingsScreen
import com.android.sample.ui.bookings.MyBookingsViewModel
import com.android.sample.ui.login.LoginScreen
import com.android.sample.ui.map.MapScreen
import com.android.sample.ui.newListing.NewListingScreen
import com.android.sample.ui.profile.MyProfileScreen
import com.android.sample.ui.profile.MyProfileViewModel
import com.android.sample.ui.profile.ProfileScreen
import com.android.sample.ui.signup.SignUpScreen
import com.android.sample.ui.signup.SignUpViewModel
import com.android.sample.ui.subject.SubjectListScreen
import com.android.sample.ui.subject.SubjectListViewModel

private const val TAG = "NavGraph"

/**
 * Helper function to navigate to listing details screen Avoids code duplication across different
 * navigation paths
 */
private fun navigateToListing(navController: NavHostController, listingId: String) {
  navController.navigate(NavRoutes.createListingRoute(listingId))
}

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
  val profileID = remember { mutableStateOf("") }
  val bookingId = remember { mutableStateOf("") }

  NavHost(navController = navController, startDestination = NavRoutes.LOGIN) {
    composable(NavRoutes.LOGIN) {
      LaunchedEffect(Unit) { RouteStackManager.addRoute(NavRoutes.LOGIN) }
      LoginScreen(
          viewModel = authViewModel,
          onGoogleSignIn = onGoogleSignIn,
          onNavigateToSignUp = { // Add this navigation callback
            navController.navigate(NavRoutes.SIGNUP_BASE)
          })
    }

    composable(NavRoutes.MAP) {
      LaunchedEffect(Unit) { RouteStackManager.addRoute(NavRoutes.MAP) }
      MapScreen(
          requestLocationOnStart = true,
          onProfileClick = { profileId ->
            navController.navigate(NavRoutes.createProfileRoute(profileId))
          })
    }

    composable(NavRoutes.PROFILE) {
      val currentUserId = UserSessionManager.getCurrentUserId() ?: "guest"
      LaunchedEffect(Unit) { RouteStackManager.addRoute(NavRoutes.PROFILE) }
      MyProfileScreen(
          profileViewModel = profileViewModel,
          profileId = currentUserId,
          onListingClick = { listingId -> navigateToListing(navController, listingId) },
          onLogout = {
            // Clear the authentication state to reset email/password fields
            authViewModel.signOut()
            navController.navigate(NavRoutes.LOGIN) { popUpTo(0) { inclusive = true } }
          })
    }

    composable(NavRoutes.HOME) {
      LaunchedEffect(Unit) { RouteStackManager.addRoute(NavRoutes.HOME) }
      HomeScreen(
          mainPageViewModel = mainPageViewModel,
          onNavigateToProfile = { profileId ->
            profileID.value = profileId
            navController.navigate(NavRoutes.OTHERS_PROFILE)
          },
          onNavigateToSubjectList = { subject ->
            academicSubject.value = subject
            navController.navigate(NavRoutes.SKILLS)
          },
          onNavigateToAddNewListing = { navController.navigate(NavRoutes.NEW_SKILL) })
    }

    composable(NavRoutes.SKILLS) { backStackEntry ->
      LaunchedEffect(Unit) { RouteStackManager.addRoute(NavRoutes.SKILLS) }
      val viewModel: SubjectListViewModel = viewModel(backStackEntry)
      SubjectListScreen(
          viewModel = viewModel,
          subject = academicSubject.value,
          onListingClick = { listingId -> navigateToListing(navController, listingId) })
    }

    composable(NavRoutes.BOOKINGS) {
      LaunchedEffect(Unit) { RouteStackManager.addRoute(NavRoutes.BOOKINGS) }
      MyBookingsScreen(
          onBookingClick = { bkgId ->
            bookingId.value = bkgId
            navController.navigate(NavRoutes.BOOKING_DETAILS)
          })
    }

    composable(
        route = NavRoutes.NEW_SKILL,
        arguments = listOf(navArgument("profileId") { type = NavType.StringType })) { backStackEntry
          ->
          val profileId = backStackEntry.arguments?.getString("profileId") ?: ""
          LaunchedEffect(Unit) { RouteStackManager.addRoute(NavRoutes.NEW_SKILL) }
          NewListingScreen(profileId = profileId, navController = navController)
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
    composable(route = NavRoutes.OTHERS_PROFILE) {
      LaunchedEffect(Unit) { RouteStackManager.addRoute(NavRoutes.OTHERS_PROFILE) }
      // todo add other parameters
      ProfileScreen(
          profileId = profileID.value,
          onProposalClick = { listingId -> navigateToListing(navController, listingId) },
          onRequestClick = { listingId -> navigateToListing(navController, listingId) })
    }
    composable(
        route = NavRoutes.LISTING,
        arguments = listOf(navArgument("listingId") { type = NavType.StringType })) { backStackEntry
          ->
          val listingId = backStackEntry.arguments?.getString("listingId") ?: ""
          LaunchedEffect(Unit) { RouteStackManager.addRoute(NavRoutes.LISTING) }
          com.android.sample.ui.listing.ListingScreen(
              listingId = listingId, onNavigateBack = { navController.popBackStack() })
        }

    composable(route = NavRoutes.BOOKING_DETAILS) {
      LaunchedEffect(Unit) { RouteStackManager.addRoute(NavRoutes.BOOKING_DETAILS) }
      BookingDetailsScreen(
          bookingId = bookingId.value,
          onCreatorClick = { profileId ->
            profileID.value = profileId
            navController.navigate(NavRoutes.OTHERS_PROFILE)
          })
    }
  }
}
