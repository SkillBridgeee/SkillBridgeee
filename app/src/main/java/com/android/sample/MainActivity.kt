package com.android.sample

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.android.sample.model.authentication.AuthResult
import com.android.sample.model.authentication.AuthenticationViewModel
import com.android.sample.model.authentication.GoogleSignInHelper
import com.android.sample.model.authentication.UserSessionManager
import com.android.sample.model.booking.BookingRepositoryProvider
import com.android.sample.model.communication.newImplementation.conversation.ConversationRepositoryProvider
import com.android.sample.model.communication.newImplementation.overViewConv.OverViewConvRepositoryProvider
import com.android.sample.model.listing.ListingRepositoryProvider
import com.android.sample.model.rating.RatingRepositoryProvider
import com.android.sample.model.user.ProfileRepositoryProvider
import com.android.sample.ui.HomePage.MainPageViewModel
import com.android.sample.ui.bookings.BookingDetailsViewModel
import com.android.sample.ui.bookings.MyBookingsViewModel
import com.android.sample.ui.communication.DiscussionViewModel
import com.android.sample.ui.communication.MessageViewModel
import com.android.sample.ui.components.BottomNavBar
import com.android.sample.ui.components.TopAppBar
import com.android.sample.ui.navigation.AppNavGraph
import com.android.sample.ui.navigation.NavRoutes
import com.android.sample.ui.newListing.NewListingViewModel
import com.android.sample.ui.profile.MyProfileViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import okhttp3.OkHttpClient

object HttpClientProvider {
  var client: OkHttpClient = OkHttpClient()
}

class MainActivity : ComponentActivity() {
  private lateinit var authViewModel: AuthenticationViewModel
  private lateinit var googleSignInHelper: GoogleSignInHelper

  companion object {
    // Automatically use Firebase emulators based on build configuration
    // To enable emulators: Change USE_FIREBASE_EMULATOR to "true" in build.gradle.kts (debug
    // buildType)
    // Release builds ALWAYS use production Firebase (USE_FIREBASE_EMULATOR = false)
    // For physical devices, update FIREBASE_EMULATOR_HOST in build.gradle.kts to your local IP
    init {
      // If BuildConfig is red you should run the generateDebugBuildConfig task on gradle
      if (BuildConfig.USE_FIREBASE_EMULATOR) {
        Firebase.firestore.useEmulator("10.0.2.2", 8080)
        Firebase.auth.useEmulator("10.0.2.2", 9099)
      } else {
        Log.d("MainActivity", "🌐 Using production Firebase servers")
      }
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Initialize ALL repository providers BEFORE creating ViewModels.
    try {
      ProfileRepositoryProvider.init(this)
      ListingRepositoryProvider.init(this)
      BookingRepositoryProvider.init(this)
      RatingRepositoryProvider.init(this)
      OverViewConvRepositoryProvider.init(this)
      ConversationRepositoryProvider.init(this)
    } catch (e: Exception) {
      println("Repository initialization failed: ${e.message}")
    }

    // Initialize authentication components
    authViewModel = AuthenticationViewModel(this)
    googleSignInHelper =
        GoogleSignInHelper(this) { result -> authViewModel.handleGoogleSignInResult(result) }

    setContent {
      MainApp(
          authViewModel = authViewModel, onGoogleSignIn = { googleSignInHelper.signInWithGoogle() })
    }
  }
}

/**
 * Checks if an email is a test email based on known test domains. Test domains
 * include: @example.test, @test.com (for E2E tests)
 *
 * @param email The email address to check
 * @return true if the email is a test email, false otherwise
 */
internal fun isTestEmail(email: String?): Boolean {
  if (email == null) return false
  val testDomains = listOf("@example.test", "@test.com", "@e2etest.com")
  return testDomains.any { email.endsWith(it, ignoreCase = true) }
}

class MyViewModelFactory(private val sessionManager: UserSessionManager) :
    ViewModelProvider.Factory {
  @Suppress("UNCHECKED_CAST")
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    return when (modelClass) {
      MyBookingsViewModel::class.java -> {
        MyBookingsViewModel() as T
      }
      MyProfileViewModel::class.java -> {
        MyProfileViewModel(sessionManager = sessionManager) as T
      }
      MainPageViewModel::class.java -> {
        MainPageViewModel() as T
      }
      NewListingViewModel::class.java -> {
        NewListingViewModel() as T
      }
      BookingDetailsViewModel::class.java -> {
        BookingDetailsViewModel() as T
      }
      DiscussionViewModel::class.java -> {
        DiscussionViewModel() as T
      }
      MessageViewModel::class.java -> {
        MessageViewModel() as T
      }
      else -> throw IllegalArgumentException("Unknown ViewModel class")
    }
  }
}

/** I used this to test which is why there are non used imports up there */

/**
 * @Composable fun LoginApp() { val context = LocalContext.current val viewModel:
 *   AuthenticationViewModel = remember { AuthenticationViewModel(context) }
 *
 * // Register activity result launcher for Google Sign-In val googleSignInLauncher =
 * rememberLauncherForActivityResult( contract = ActivityResultContracts.StartActivityForResult()) {
 * result -> viewModel.handleGoogleSignInResult(result) }
 *
 * LoginScreen( viewModel = viewModel, onGoogleSignIn = { val signInIntent =
 * viewModel.getGoogleSignInClient().signInIntent googleSignInLauncher.launch(signInIntent) }) }
 */

/**
 * Performs auto-login by checking if user is authenticated and has a valid profile. Navigates to
 * HOME if successful, or signs out the user if they don't have a profile or their email is not
 * verified.
 */
internal suspend fun performAutoLogin(
    navController: NavHostController,
    authViewModel: AuthenticationViewModel
) {
  val currentUserId = UserSessionManager.getCurrentUserId()

  if (currentUserId == null) {
    Log.d("MainActivity", "Auto-login: No user authenticated - staying at LOGIN")
    return
  }

  Log.d("MainActivity", "Auto-login: Found authenticated user: $currentUserId")

  try {
    handleAuthenticatedUser(currentUserId, navController, authViewModel)
  } catch (e: Exception) {
    Log.e("MainActivity", "Auto-login: Error checking profile - signing out", e)
    authViewModel.signOut()
  }
}

/** Handles an authenticated user by checking their profile and email verification status. */
internal suspend fun handleAuthenticatedUser(
    userId: String,
    navController: NavHostController,
    authViewModel: AuthenticationViewModel,
    skipEmulatorCheck: Boolean = false, // For testing: allows overriding emulator behavior
    dispatcher: kotlinx.coroutines.CoroutineDispatcher = kotlinx.coroutines.Dispatchers.Main
) {
  val profile = ProfileRepositoryProvider.repository.getProfile(userId)

  if (profile == null) {
    Log.d("MainActivity", "Auto-login: User has no profile - signing out")
    authViewModel.signOut()
    return
  }

  val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser

  // Determine if we should skip email verification
  // Skip verification in these cases:
  // 1. Using Firebase emulator (for all E2E tests)
  // 2. Test email domain in debug builds (for UI-based E2E tests)
  val shouldSkipVerification =
      when {
        // Case 1: Firebase emulator - always skip (unless explicitly testing verification)
        BuildConfig.USE_FIREBASE_EMULATOR && !skipEmulatorCheck -> {
          Log.d("MainActivity", "Auto-login: Using emulator - skipping email verification")
          true
        }
        // Case 2: Test email domain in debug build with SKIP_EMAIL_VERIFICATION enabled
        BuildConfig.SKIP_EMAIL_VERIFICATION && isTestEmail(firebaseUser?.email) -> {
          Log.d(
              "MainActivity",
              "Auto-login: Test email domain detected - skipping email verification")
          true
        }
        // Case 3: Production or real email - check actual verification
        else -> false
      }

  val isEmailVerified =
      if (shouldSkipVerification) {
        true
      } else {
        firebaseUser?.isEmailVerified ?: true
      }

  if (isEmailVerified) {
    Log.d("MainActivity", "Auto-login: User has profile and is verified - navigating to HOME")

    // Try to navigate, but handle the case where NavHost isn't ready yet
    try {
      // Navigation must happen on the main thread
      kotlinx.coroutines.withContext(dispatcher) {
        navController.navigate(NavRoutes.HOME) { popUpTo(NavRoutes.LOGIN) { inclusive = true } }
      }
    } catch (_: IllegalArgumentException) {
      // NavHost not ready yet - this can happen during app startup
      Log.d("MainActivity", "Auto-login: NavHost not ready yet, will navigate on next composition")
      // Don't sign out - the user is valid, navigation will happen once NavHost is ready
    } catch (_: IllegalStateException) {
      // Main thread issue - log and don't sign out
      Log.d(
          "MainActivity", "Auto-login: Thread issue during navigation, user remains authenticated")
    }
  } else {
    Log.d("MainActivity", "Auto-login: Email not verified - signing out")
    authViewModel.signOut()
  }
}

@Composable
fun MainApp(authViewModel: AuthenticationViewModel, onGoogleSignIn: () -> Unit) {
  val navController = rememberNavController()
  val authResult by authViewModel.authResult.collectAsStateWithLifecycle()

  // One-time auto-login check on app start
  LaunchedEffect(Unit) {
    // Small delay to ensure NavHost is initialized before attempting navigation
    kotlinx.coroutines.delay(100)

    // Wait for auth state to be ready
    val currentUserId = UserSessionManager.getCurrentUserId()
    if (currentUserId != null || authViewModel.authResult.value != null) {
      performAutoLogin(navController, authViewModel)
    }
  }

  // Navigate based on authentication result from explicit login/signup actions
  LaunchedEffect(authResult) {
    when (val result = authResult) {
      is AuthResult.Success -> {
        Log.d("MainActivity", "Auth success - navigating to HOME")
        navController.navigate(NavRoutes.HOME) { popUpTo(NavRoutes.LOGIN) { inclusive = true } }
        authViewModel.clearAuthResult() // Clear after navigation
      }
      is AuthResult.RequiresSignUp -> {
        // Navigate to signup screen when Google user doesn't have a profile
        val email = result.email
        Log.d("MainActivity", "Google user requires sign up, email: $email")
        val route = NavRoutes.createSignUpRoute(email)
        Log.d("MainActivity", "Navigating to route: $route")
        navController.navigate(route) { popUpTo(NavRoutes.LOGIN) { inclusive = false } }
        authViewModel.clearAuthResult() // Clear after navigation
      }
      else -> {
        // No navigation for Error or null
      }
    }
  }

  // To track the current route
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = navBackStackEntry?.destination?.route

  // Get current user ID from UserSessionManager
  val sessionManager = UserSessionManager
  val factory = MyViewModelFactory(sessionManager)

  val bookingsViewModel: MyBookingsViewModel = viewModel(factory = factory)
  val profileViewModel: MyProfileViewModel = viewModel(factory = factory)
  val mainPageViewModel: MainPageViewModel = viewModel(factory = factory)
  val newListingViewModel: NewListingViewModel = viewModel(factory = factory)
  val bookingDetailsViewModel: BookingDetailsViewModel = viewModel(factory = factory)
  val discussionViewModel: DiscussionViewModel = viewModel(factory = factory)

  // Define main screens that should show bottom nav
  val mainScreenRoutes =
      listOf(NavRoutes.HOME, NavRoutes.BOOKINGS, NavRoutes.PROFILE, NavRoutes.MAP)

  // Check if current route should show bottom nav
  val showBottomNav = mainScreenRoutes.contains(currentRoute)

  val noTopBarRoutes =
      setOf(
          NavRoutes.LOGIN,
      )

  Scaffold(
      topBar = {
        if (!noTopBarRoutes.contains(currentRoute)) {
          TopAppBar(navController)
        }
      },
      bottomBar = {
        if (showBottomNav) {
          BottomNavBar(navController)
        }
      }) { paddingValues ->
        androidx.compose.foundation.layout.Box(modifier = Modifier.padding(paddingValues)) {
          AppNavGraph(
              navController = navController,
              bookingsViewModel = bookingsViewModel,
              profileViewModel = profileViewModel,
              mainPageViewModel = mainPageViewModel,
              newListingViewModel = newListingViewModel,
              bookingDetailsViewModel = bookingDetailsViewModel,
              authViewModel = authViewModel,
              discussionViewModel = discussionViewModel,
              onGoogleSignIn = onGoogleSignIn)
        }
      }
}
