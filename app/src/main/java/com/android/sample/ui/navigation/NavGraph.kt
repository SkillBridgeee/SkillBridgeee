package com.android.sample.ui.navigation

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.android.sample.handleAuthenticatedUser
import com.android.sample.model.authentication.AuthenticationViewModel
import com.android.sample.model.authentication.UserSessionManager
import com.android.sample.model.skill.MainSubject
import com.android.sample.ui.HomePage.HomeScreen
import com.android.sample.ui.HomePage.MainPageViewModel
import com.android.sample.ui.bookings.BookingDetailsScreen
import com.android.sample.ui.bookings.BookingDetailsViewModel
import com.android.sample.ui.bookings.MyBookingsScreen
import com.android.sample.ui.bookings.MyBookingsViewModel
import com.android.sample.ui.communication.DiscussionScreen
import com.android.sample.ui.communication.DiscussionViewModel
import com.android.sample.ui.communication.MessageScreen
import com.android.sample.ui.communication.MessageViewModel
import com.android.sample.ui.login.LoginScreen
import com.android.sample.ui.map.MapScreen
import com.android.sample.ui.newListing.NewListingScreen
import com.android.sample.ui.newListing.NewListingViewModel
import com.android.sample.ui.profile.MyProfileScreen
import com.android.sample.ui.profile.MyProfileViewModel
import com.android.sample.ui.profile.ProfileScreen
import com.android.sample.ui.signup.SignUpScreen
import com.android.sample.ui.signup.SignUpViewModel
import com.android.sample.ui.subject.SubjectListScreen
import com.android.sample.ui.subject.SubjectListViewModel
import com.android.sample.ui.tos.ToSScreen
import com.google.firebase.auth.FirebaseAuth

private const val TAG = "NavGraph"

/**
 * Helper function to navigate to listing details screen Avoids code duplication across different
 * navigation paths
 */
private fun navigateToListing(navController: NavHostController, listingId: String) {
  navController.navigate(NavRoutes.createListingRoute(listingId))
}

/** Helper function to navigate to new listing screen if user is authenticated */
private fun navigateToNewListing(navController: NavHostController, listingId: String? = null) {
  val currentUserId = UserSessionManager.getCurrentUserId()
  if (currentUserId != null) {
    navController.navigate(NavRoutes.createNewSkillRoute(currentUserId, listingId))
  }
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
    newListingViewModel: NewListingViewModel,
    authViewModel: AuthenticationViewModel,
    bookingDetailsViewModel: BookingDetailsViewModel,
    discussionViewModel: DiscussionViewModel,
    onGoogleSignIn: () -> Unit
) {
  val academicSubject = remember { mutableStateOf<MainSubject?>(null) }
  val profileID = remember { mutableStateOf("") }
  val bookingId = remember { mutableStateOf("") }
  val convId = remember { mutableStateOf("") }

  NavHost(navController = navController, startDestination = NavRoutes.SPLASH) {
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
      MapScreen(requestLocationOnStart = true)
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
          onNavigateToSubjectList = { subject ->
            academicSubject.value = subject
            navController.navigate(NavRoutes.SKILLS)
          },
          onNavigateToAddNewListing = {
            val currentUserId = UserSessionManager.getCurrentUserId()
            if (currentUserId != null) {
              navController.navigate(NavRoutes.createNewSkillRoute(currentUserId))
            }
          },
          onNavigateToListingDetails = { listingId -> navigateToListing(navController, listingId) })
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
          },
          viewModel = bookingsViewModel)
    }

    composable(NavRoutes.SPLASH) {
      LaunchedEffect(Unit) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser

        if (firebaseUser == null) {
          // No authenticated Firebase user → always go to LOGIN
          navController.navigate(NavRoutes.LOGIN) { popUpTo(NavRoutes.SPLASH) { inclusive = true } }
        } else {
          try {
            // Try to handle authenticated user (profile + email checks)
            handleAuthenticatedUser(firebaseUser.uid, navController, authViewModel)

            // If handleAuthenticatedUser signed the user out (no profile / not verified),
            // FirebaseAuth.currentUser will now be null → go to LOGIN.
            val stillLoggedIn = FirebaseAuth.getInstance().currentUser != null
            if (!stillLoggedIn) {
              navController.navigate(NavRoutes.LOGIN) {
                popUpTo(NavRoutes.SPLASH) { inclusive = true }
              }
            }
            // If it succeeded, handleAuthenticatedUser already navigated to HOME.
          } catch (e: Exception) {
            Log.e(TAG, "Splash: error during auto-login", e)
            authViewModel.signOut()
            navController.navigate(NavRoutes.LOGIN) {
              popUpTo(NavRoutes.SPLASH) { inclusive = true }
            }
          }
        }
      }

      // Simple loading UI
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
      }
    }

    composable(
        route = NavRoutes.NEW_SKILL,
        arguments =
            listOf(
                navArgument("profileId") { type = NavType.StringType },
                navArgument("listingId") {
                  type = NavType.StringType
                  nullable = true
                  defaultValue = null
                })) { backStackEntry ->
          val profileId = backStackEntry.arguments?.getString("profileId") ?: ""
          val listingId = backStackEntry.arguments?.getString("listingId")
          LaunchedEffect(Unit) { RouteStackManager.addRoute(NavRoutes.NEW_SKILL) }
          NewListingScreen(
              profileId = profileId,
              listingId = listingId,
              skillViewModel = newListingViewModel,
              navController = navController,
          )
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

          // Use viewModel() to ensure single instance per navigation entry
          val viewModel: SignUpViewModel =
              viewModel(
                  factory =
                      object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                          return SignUpViewModel(initialEmail = email) as T
                        }
                      })

          SignUpScreen(
              vm = viewModel,
              onSubmitSuccess = {
                // Email/password users - navigate to login to verify and sign in
                navController.navigate(NavRoutes.LOGIN) {
                  popUpTo(0) { inclusive = false } // Clear entire backstack except LOGIN
                }
              },
              onGoogleSignUpSuccess = {
                // Google users - navigate directly to HOME (stay authenticated)
                navController.navigate(NavRoutes.HOME) {
                  popUpTo(0) { inclusive = true } // Clear entire backstack including LOGIN
                }
              },
              onBackPressed = {
                // User pressed back during Google signup - navigate to LOGIN
                navController.navigate(NavRoutes.LOGIN) { popUpTo(0) { inclusive = false } }
              },
              onNavigateToToS = { navController.navigate(NavRoutes.TOS) })
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
              listingId = listingId,
              onNavigateBack = { navController.popBackStack() },
              onEditListing = { navigateToNewListing(navController, listingId) })
        }

    composable(route = NavRoutes.BOOKING_DETAILS) {
      LaunchedEffect(Unit) { RouteStackManager.addRoute(NavRoutes.BOOKING_DETAILS) }
      BookingDetailsScreen(
          bookingId = bookingId.value,
          onCreatorClick = { profileId ->
            profileID.value = profileId
            navController.navigate(NavRoutes.OTHERS_PROFILE)
          },
          bkgViewModel = bookingDetailsViewModel)
    }

    composable(NavRoutes.DISCUSSION) {
      LaunchedEffect(Unit) { RouteStackManager.addRoute(NavRoutes.DISCUSSION) }

      DiscussionScreen(
          viewModel = discussionViewModel,
          onConversationClick = { convIdClicked ->
            convId.value = convIdClicked
            navController.navigate(NavRoutes.MESSAGES)
          })
    }
    composable(route = NavRoutes.TOS) {
      LaunchedEffect(Unit) { RouteStackManager.addRoute(NavRoutes.TOS) }
      ToSScreen()
    }

    composable(NavRoutes.MESSAGES) {
      LaunchedEffect(Unit) { RouteStackManager.addRoute(NavRoutes.MESSAGES) }

      val currentConvId = convId.value
      if (currentConvId.isNotEmpty()) {
        val messageViewModel = remember(currentConvId) { MessageViewModel() }

        MessageScreen(
            viewModel = messageViewModel,
            convId = currentConvId,
        )
      } else {
        // No conversation selected, show empty state
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Text("No conversation selected")
        }
      }
    }
  }
}
