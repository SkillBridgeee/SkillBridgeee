package com.android.sample.ui.navigation

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
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
import com.android.sample.ui.listing.ListingScreen
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
 * Application navigation graph entry point.
 *
 * Composes a [NavHost] and registers all app routes. This function is intentionally thin: it wires
 * route builder extensions and holds ephemeral navigation state (selected subject,
 * profile/booking/conversation ids).
 *
 * Side effects:
 * - `startDestination` defaults to [NavRoutes.SPLASH] and can be overridden for tests to avoid
 *   auto-login flows.
 *
 * @param navController NavHostController used across registered routes.
 * @param bookingsViewModel Shared view model for bookings related screens.
 * @param profileViewModel Shared view model for profile related screens.
 * @param mainPageViewModel Shared view model for the home/main screen.
 * @param newListingViewModel ViewModel used for creating/editing listings.
 * @param authViewModel Authentication view model used for sign out / auth state.
 * @param bookingDetailsViewModel ViewModel for booking details.
 * @param discussionViewModel ViewModel for discussions.
 * @param onGoogleSignIn Callback invoked by login/sign\-in screens to start Google sign in.
 * @param startDestination Optional start destination to override splash auto-login (useful for
 *   tests).
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
    addMessagesRoute(navController,convId)
  }
}

/**
 * Registers the login route and presents the login UI.
 *
 * Side effects:
 * - Adds route to [RouteStackManager] on entry.
 * - `onGoogleSignIn` is forwarded to the UI so the host can handle external sign\-in flows.
 *
 * @param navController Controller used for navigation from the login screen.
 * @param authViewModel ViewModel instance used by the login screen.
 * @param onGoogleSignIn Host callback to start the Google sign\-in flow.
 */
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

/**
 * Registers the map route.
 *
 * Presents [MapScreen] and adds the route to [RouteStackManager]. The route requests location on
 * start by default.
 */
private fun NavGraphBuilder.addMapRoute() {
  composable(NavRoutes.MAP) {
    LaunchedEffect(Unit) { RouteStackManager.addRoute(NavRoutes.MAP) }
    MapScreen(requestLocationOnStart = true)
  }
}

/**
 * Registers the current user's profile route.
 *
 * Reads the current user id from [UserSessionManager] (falls back to `"guest"`). Handles logout by
 * calling [AuthenticationViewModel.signOut] and navigating back to login while attempting to clear
 * the back stack.
 *
 * @param navController Controller used for navigation from profile actions.
 * @param profileViewModel ViewModel used to populate profile content.
 * @param authViewModel Authentication view model used to perform sign out.
 */
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

/**
 * Registers the home route.
 *
 * Presents [HomeScreen] and wires callbacks for subject selection, adding a new listing (guarded by
 * authentication) and navigating to listing details.
 *
 * @param navController Controller used for navigation from home actions.
 * @param mainPageViewModel ViewModel for home content.
 * @param academicSubject Mutable state used to pass the selected subject to the skills screen.
 */
private fun NavGraphBuilder.addHomeRoute(
    navController: NavHostController,
    mainPageViewModel: MainPageViewModel,
    academicSubject: MutableState<MainSubject?>,
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

/**
 * Registers the skills/subject list route.
 *
 * Scopes a [SubjectListViewModel] to the backStackEntry and passes the currently selected subject
 * into [SubjectListScreen].
 *
 * @param navController Controller used for navigation from the skills screen.
 * @param academicSubject Shared state containing the currently selected subject.
 */
private fun NavGraphBuilder.addSkillsRoute(
    navController: NavHostController,
    academicSubject: MutableState<MainSubject?>,
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

/**
 * Registers the bookings list route.
 *
 * Presents [MyBookingsScreen] and updates `bookingId` state when a booking is clicked, then
 * navigates to booking details.
 *
 * @param navController Controller used for navigation from bookings.
 * @param bookingsViewModel ViewModel that backs the bookings list screen.
 * @param bookingId Mutable state used to store the selected booking id for details screen.
 */
private fun NavGraphBuilder.addBookingsRoute(
    navController: NavHostController,
    bookingsViewModel: MyBookingsViewModel,
    bookingId: MutableState<String>,
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

/**
 * Registers the splash route and defers auto-login logic to [SplashScreen].
 *
 * @param navController Controller used to navigate away from splash.
 * @param authViewModel Authentication view model used to sign out on errors.
 */
private fun NavGraphBuilder.addSplashRoute(
    navController: NavHostController,
    authViewModel: AuthenticationViewModel,
) {
  composable(NavRoutes.SPLASH) {
    SplashScreen(navController = navController, authViewModel = authViewModel)
  }
}

/**
 * Lightweight composable that implements the splash auto-login flow.
 *
 * Side effects performed inside a [LaunchedEffect]:
 * - Reads the current Firebase user and decides to navigate to login or continue.
 * - Calls [handleAuthenticatedUser] for profile/email validation and signs out on exceptions.
 *
 * Displays a centered [CircularProgressIndicator] while work completes.
 *
 * @param navController Controller used to perform navigation decisions.
 * @param authViewModel Authentication view model to perform sign out on failures.
 */
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

/**
 * Registers the new listing route.
 *
 * Defines route arguments for `profileId` and optional `listingId` (nullable). Extracts arguments
 * from the backStackEntry and presents [NewListingScreen].
 *
 * @param navController Controller used to navigate after creating/updating a listing.
 * @param newListingViewModel ViewModel used by the new listing screen.
 */
fun NavGraphBuilder.addNewSkillRoute(
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

/**
 * Registers the sign up route and provides a factory-scoped [SignUpViewModel].
 *
 * Extracts an optional `email` argument and constructs [SignUpViewModel] with it. Handles
 * navigation callbacks:
 * - on successful submit -> navigates to login.
 * - on Google sign up success -> navigates to home.
 *
 * @param navController Controller used for navigation from sign up flows.
 */
fun NavGraphBuilder.addSignUpRoute(navController: NavHostController) {
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
/**
 * Registers the "other user's profile" route.
 *
 * Presents [ProfileScreen] using the externally provided `profileID` mutable state. Clicking
 * proposals or requests navigates to listing details.
 *
 * @param navController Controller used for navigation from the others profile screen.
 * @param profileID Mutable state containing the profile id of the displayed user.
 */
fun NavGraphBuilder.addOthersProfileRoute(
    navController: NavHostController,
    profileID: MutableState<String>,
) {
  composable(route = NavRoutes.OTHERS_PROFILE) {
    LaunchedEffect(Unit) { RouteStackManager.addRoute(NavRoutes.OTHERS_PROFILE) }
    ProfileScreen(
        profileId = profileID.value,
        onProposalClick = { listingId -> navigateToListing(navController, listingId) },
        onRequestClick = { listingId -> navigateToListing(navController, listingId) })
  }
}

/**
 * Registers the listing details route.
 *
 * Declares a required `listingId` route argument, extracts it from the backStackEntry and presents
 * the listing screen. Provides navigation callbacks for back and editing.
 *
 * @param navController Controller used to navigate from the listing details screen.
 */
fun NavGraphBuilder.addListingRoute(navController: NavHostController) {
  composable(
      route = NavRoutes.LISTING,
      arguments = listOf(navArgument("listingId") { type = NavType.StringType })) { backStackEntry
        ->
        val listingId = backStackEntry.arguments?.getString("listingId") ?: ""
        LaunchedEffect(Unit) { RouteStackManager.addRoute(NavRoutes.LISTING) }
        ListingScreen(
            listingId = listingId,
            onNavigateBack = { navController.popBackStack() },
            onEditListing = { navigateToNewListing(navController, listingId) })
      }
}

/**
 * Registers the booking details route.
 *
 * Presents [BookingDetailsScreen] and wires callbacks:
 * - Clicking the creator populates `profileID` and navigates to [NavRoutes.OTHERS_PROFILE].
 *
 * @param navController Controller used for navigation from booking details.
 * @param bookingDetailsViewModel ViewModel for booking details content.
 * @param bookingId Mutable state containing the selected booking id.
 * @param profileID Mutable state used to pass a selected profile id to other screens.
 */
fun NavGraphBuilder.addBookingDetailsRoute(
    navController: NavHostController,
    bookingDetailsViewModel: BookingDetailsViewModel,
    bookingId: MutableState<String>,
    profileID: MutableState<String>,
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

/**
 * Registers the discussion route.
 *
 * Presents [DiscussionScreen] and updates `convId` when a conversation is selected, then navigates
 * to the messages screen.
 *
 * @param navController Controller used for navigation from discussion screens.
 * @param discussionViewModel ViewModel that backs the discussion UI.
 * @param convId Mutable state used to store the selected conversation id.
 */
fun NavGraphBuilder.addDiscussionRoute(
    navController: NavHostController,
    discussionViewModel: DiscussionViewModel,
    convId: MutableState<String>,
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

/**
 * Registers the Terms of Service route.
 *
 * Presents [ToSScreen] and adds the route to [RouteStackManager].
 */
fun NavGraphBuilder.addToSRoute() {
  composable(route = NavRoutes.TOS) {
    LaunchedEffect(Unit) { RouteStackManager.addRoute(NavRoutes.TOS) }
    ToSScreen()
  }
}

/**
 * Registers the messages route.
 *
 * If `convId` is non\-empty:
 * - Creates a [MessageViewModel] scoped to the conversation id via [remember].
 * - Presents [MessageScreen] with the view model and conversation id.
 *
 * If `convId` is empty:
 * - Presents a fallback UI telling the user no conversation is selected.
 *
 * @param convId Mutable state containing the current conversation id.
 */
fun NavGraphBuilder.addMessagesRoute(
    navController: NavHostController,
    convId: MutableState<String>,
) {
  composable(NavRoutes.MESSAGES) {
    LaunchedEffect(Unit) { RouteStackManager.addRoute(NavRoutes.MESSAGES) }

    val currentConvId = convId.value
    if (currentConvId.isNotEmpty()) {
      val messageViewModel = remember(currentConvId) { MessageViewModel() }

      MessageScreen(
          viewModel = messageViewModel,
          convId = currentConvId,
          onConversationDeleted = {navController.popBackStack()},
      )
    } else {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("No conversation selected")
      }
    }
  }
}
