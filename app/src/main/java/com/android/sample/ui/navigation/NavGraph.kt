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
import androidx.navigation.NavGraphBuilder
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
 * Helper function to navigate to listing details screen. Avoids code duplication across different
 * navigation paths.
 */
internal fun navigateToListing(navController: NavHostController, listingId: String) {
  navController.navigate(NavRoutes.createListingRoute(listingId))
}

/** Helper function to navigate to new listing screen if user is authenticated. */
internal fun navigateToNewListing(navController: NavHostController, listingId: String? = null) {
  val currentUserId = UserSessionManager.getCurrentUserId()
  if (currentUserId != null) {
    navController.navigate(NavRoutes.createNewSkillRoute(currentUserId, listingId))
  }
}

/**
 * Main navigation entry point for the application.
 *
 * @param startDestination Allows tests to bypass SPLASH to avoid auto-login side effects.
 *
 * The function itself is intentionally thin and delegates each route group to its own builder
 * extension to keep cognitive complexity below Sonar's threshold.
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
    onGoogleSignIn: () -> Unit,
    startDestination: String = NavRoutes.SPLASH,
) {
  val academicSubject = remember { mutableStateOf<MainSubject?>(null) }
  val profileID = remember { mutableStateOf("") }
  val bookingId = remember { mutableStateOf("") }
  val convId = remember { mutableStateOf("") }

  NavHost(navController = navController, startDestination = startDestination) {
    addLoginRoute(navController, authViewModel, onGoogleSignIn)
    addMapRoute()
    addProfileRoute(navController, profileViewModel, authViewModel)
    addHomeRoute(navController, mainPageViewModel, academicSubject)
    addSkillsRoute(navController, academicSubject)
    addBookingsRoute(navController, bookingsViewModel, bookingId)
    addSplashRoute(navController, authViewModel)
    addNewSkillRoute(navController, newListingViewModel)
    addSignUpRoute(navController)
    addOthersProfileRoute(navController, profileID)
    addListingRoute(navController)
    addBookingDetailsRoute(navController, bookingDetailsViewModel, bookingId, profileID)
    addDiscussionRoute(navController, discussionViewModel, convId)
    addToSRoute()
    addMessagesRoute(navController, convId)
  }
}

/* ------------------------- Route helpers (NavGraphBuilder) ------------------------- */

private fun NavGraphBuilder.addLoginRoute(
    navController: NavHostController,
    authViewModel: AuthenticationViewModel,
    onGoogleSignIn: () -> Unit,
) {
  composable(NavRoutes.LOGIN) {
    LaunchedEffect(Unit) { RouteStackManager.addRoute(NavRoutes.LOGIN) }
    LoginScreen(
        viewModel = authViewModel,
        onGoogleSignIn = onGoogleSignIn,
        onNavigateToSignUp = { navController.navigate(NavRoutes.SIGNUP_BASE) })
  }
}

private fun NavGraphBuilder.addMapRoute() {
  composable(NavRoutes.MAP) {
    LaunchedEffect(Unit) { RouteStackManager.addRoute(NavRoutes.MAP) }
    MapScreen(requestLocationOnStart = true)
  }
}

private fun NavGraphBuilder.addProfileRoute(
    navController: NavHostController,
    profileViewModel: MyProfileViewModel,
    authViewModel: AuthenticationViewModel,
) {
  composable(NavRoutes.PROFILE) {
    val currentUserId = UserSessionManager.getCurrentUserId() ?: "guest"
    LaunchedEffect(Unit) { RouteStackManager.addRoute(NavRoutes.PROFILE) }
    MyProfileScreen(
        profileViewModel = profileViewModel,
        profileId = currentUserId,
        onListingClick = { listingId -> navigateToListing(navController, listingId) },
        onLogout = {
          authViewModel.signOut()
          navController.navigate(NavRoutes.LOGIN) { popUpTo(0) { inclusive = true } }
        })
  }
}

private fun NavGraphBuilder.addHomeRoute(
    navController: NavHostController,
    mainPageViewModel: MainPageViewModel,
    academicSubject: androidx.compose.runtime.MutableState<MainSubject?>,
) {
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
}

private fun NavGraphBuilder.addSkillsRoute(
    navController: NavHostController,
    academicSubject: androidx.compose.runtime.MutableState<MainSubject?>,
) {
  composable(NavRoutes.SKILLS) { backStackEntry ->
    LaunchedEffect(Unit) { RouteStackManager.addRoute(NavRoutes.SKILLS) }
    val viewModel: SubjectListViewModel = viewModel(backStackEntry)
    SubjectListScreen(
        viewModel = viewModel,
        subject = academicSubject.value,
        onListingClick = { listingId -> navigateToListing(navController, listingId) })
  }
}

private fun NavGraphBuilder.addBookingsRoute(
    navController: NavHostController,
    bookingsViewModel: MyBookingsViewModel,
    bookingId: androidx.compose.runtime.MutableState<String>,
) {
  composable(NavRoutes.BOOKINGS) {
    LaunchedEffect(Unit) { RouteStackManager.addRoute(NavRoutes.BOOKINGS) }
    MyBookingsScreen(
        onBookingClick = { bkgId ->
          bookingId.value = bkgId
          navController.navigate(NavRoutes.BOOKING_DETAILS)
        },
        viewModel = bookingsViewModel)
  }
}

private fun NavGraphBuilder.addSplashRoute(
    navController: NavHostController,
    authViewModel: AuthenticationViewModel,
) {
  composable(NavRoutes.SPLASH) {
    SplashScreen(navController = navController, authViewModel = authViewModel)
  }
}

@Composable
private fun SplashScreen(
    navController: NavHostController,
    authViewModel: AuthenticationViewModel,
) {
  LaunchedEffect(Unit) {
    val firebaseUser = FirebaseAuth.getInstance().currentUser

    if (firebaseUser == null) {
      navController.navigate(NavRoutes.LOGIN) { popUpTo(NavRoutes.SPLASH) { inclusive = true } }
    } else {
      try {
        // profile + email checks
        handleAuthenticatedUser(firebaseUser.uid, navController, authViewModel)

        val stillLoggedIn = FirebaseAuth.getInstance().currentUser != null
        if (!stillLoggedIn) {
          navController.navigate(NavRoutes.LOGIN) { popUpTo(NavRoutes.SPLASH) { inclusive = true } }
        }
      } catch (e: Exception) {
        Log.e(TAG, "Splash: error during auto-login", e)
        authViewModel.signOut()
        navController.navigate(NavRoutes.LOGIN) { popUpTo(NavRoutes.SPLASH) { inclusive = true } }
      }
    }
  }

  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    CircularProgressIndicator()
  }
}

private fun NavGraphBuilder.addNewSkillRoute(
    navController: NavHostController,
    newListingViewModel: NewListingViewModel,
) {
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
}

private fun NavGraphBuilder.addSignUpRoute(navController: NavHostController) {
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

        Log.d(TAG, "SignUp - Received email parameter: $email")

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
              navController.navigate(NavRoutes.LOGIN) { popUpTo(0) { inclusive = false } }
            },
            onGoogleSignUpSuccess = {
              navController.navigate(NavRoutes.HOME) { popUpTo(0) { inclusive = true } }
            },
            onBackPressed = {
              navController.navigate(NavRoutes.LOGIN) { popUpTo(0) { inclusive = false } }
            },
            onNavigateToToS = { navController.navigate(NavRoutes.TOS) })
      }
}

private fun NavGraphBuilder.addOthersProfileRoute(
    navController: NavHostController,
    profileID: androidx.compose.runtime.MutableState<String>,
) {
  composable(route = NavRoutes.OTHERS_PROFILE) {
    LaunchedEffect(Unit) { RouteStackManager.addRoute(NavRoutes.OTHERS_PROFILE) }
    ProfileScreen(
        profileId = profileID.value,
        onProposalClick = { listingId -> navigateToListing(navController, listingId) },
        onRequestClick = { listingId -> navigateToListing(navController, listingId) })
  }
}

private fun NavGraphBuilder.addListingRoute(navController: NavHostController) {
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
}

private fun NavGraphBuilder.addBookingDetailsRoute(
    navController: NavHostController,
    bookingDetailsViewModel: BookingDetailsViewModel,
    bookingId: androidx.compose.runtime.MutableState<String>,
    profileID: androidx.compose.runtime.MutableState<String>,
) {
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
}

private fun NavGraphBuilder.addDiscussionRoute(
    navController: NavHostController,
    discussionViewModel: DiscussionViewModel,
    convId: androidx.compose.runtime.MutableState<String>,
) {
  composable(NavRoutes.DISCUSSION) {
    LaunchedEffect(Unit) { RouteStackManager.addRoute(NavRoutes.DISCUSSION) }
    DiscussionScreen(
        viewModel = discussionViewModel,
        onConversationClick = { convIdClicked ->
          convId.value = convIdClicked
          navController.navigate(NavRoutes.MESSAGES)
        })
  }
}

private fun NavGraphBuilder.addToSRoute() {
  composable(route = NavRoutes.TOS) {
    LaunchedEffect(Unit) { RouteStackManager.addRoute(NavRoutes.TOS) }
    ToSScreen()
  }
}

private fun NavGraphBuilder.addMessagesRoute(
    navController: NavHostController,
    convId: androidx.compose.runtime.MutableState<String>,
) {
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
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("No conversation selected")
      }
    }
  }
}
