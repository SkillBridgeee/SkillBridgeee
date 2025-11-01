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
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.android.sample.model.authentication.AuthResult
import com.android.sample.model.authentication.AuthenticationViewModel
import com.android.sample.model.authentication.GoogleSignInHelper
import com.android.sample.model.authentication.UserSessionManager
import com.android.sample.model.booking.BookingRepositoryProvider
import com.android.sample.model.listing.ListingRepositoryProvider
import com.android.sample.model.rating.RatingRepositoryProvider
import com.android.sample.model.user.ProfileRepositoryProvider
import com.android.sample.ui.bookings.MyBookingsViewModel
import com.android.sample.ui.components.BottomNavBar
import com.android.sample.ui.components.TopAppBar
import com.android.sample.ui.navigation.AppNavGraph
import com.android.sample.ui.navigation.NavRoutes
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
    // Ensure emulator is only initialized once across the entire app lifecycle
    init {
      try {
        Firebase.firestore.useEmulator("10.0.2.2", 8080)
        Firebase.auth.useEmulator("10.0.2.2", 9099)
      } catch (_: IllegalStateException) {
        // Emulator already initialized - this is fine
        println("Firebase emulator already initialized")
      } catch (e: Exception) {
        // Other errors (network issues, etc.) - log but don't crash
        println("Firebase emulator connection failed: ${e.message}")
        // App will continue to work with production Firebase
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

class MyViewModelFactory(private val userId: String) : ViewModelProvider.Factory {
  @Suppress("UNCHECKED_CAST")
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    return when (modelClass) {
      MyBookingsViewModel::class.java -> {
        MyBookingsViewModel(userId = userId) as T
      }
      MyProfileViewModel::class.java -> {
        MyProfileViewModel(userId = userId) as T
      }
      MainPageViewModel::class.java -> {
        MainPageViewModel() as T
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
@Composable
fun MainApp(authViewModel: AuthenticationViewModel, onGoogleSignIn: () -> Unit) {
  val navController = rememberNavController()
  val authResult by authViewModel.authResult.collectAsStateWithLifecycle()

  // Navigate based on authentication result
  LaunchedEffect(authResult) {
    when (authResult) {
      is AuthResult.Success -> {
        navController.navigate(NavRoutes.HOME) { popUpTo(NavRoutes.LOGIN) { inclusive = true } }
      }
      is AuthResult.RequiresSignUp -> {
        // Navigate to signup screen when Google user doesn't have a profile
        val email = (authResult as AuthResult.RequiresSignUp).email
        Log.d("MainActivity", "Google user requires sign up, email: $email")
        val route = NavRoutes.createSignUpRoute(email)
        Log.d("MainActivity", "Navigating to route: $route")
        navController.navigate(route) { popUpTo(NavRoutes.LOGIN) { inclusive = false } }
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
  val currentUserId = UserSessionManager.getCurrentUserId() ?: "guest"
  val factory = MyViewModelFactory(currentUserId)

  val bookingsViewModel: MyBookingsViewModel = viewModel(factory = factory)
  val profileViewModel: MyProfileViewModel = viewModel(factory = factory)
  val mainPageViewModel: MainPageViewModel = viewModel(factory = factory)

  // Define main screens that should show bottom nav
  val mainScreenRoutes = listOf(NavRoutes.HOME, NavRoutes.BOOKINGS, NavRoutes.PROFILE)

  // Check if current route should show bottom nav
  val showBottomNav = mainScreenRoutes.contains(currentRoute)

  Scaffold(
      topBar = { TopAppBar(navController) },
      bottomBar = {
        if (showBottomNav) {
          BottomNavBar(navController)
        }
      }) { paddingValues ->
        androidx.compose.foundation.layout.Box(modifier = Modifier.padding(paddingValues)) {
          AppNavGraph(
              navController = navController,
              bookingsViewModel,
              profileViewModel,
              mainPageViewModel,
              authViewModel = authViewModel,
              onGoogleSignIn = onGoogleSignIn)
        }
      }
}
