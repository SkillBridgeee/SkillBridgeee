package com.android.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.android.sample.model.authentication.AuthenticationViewModel
import com.android.sample.model.authentication.GoogleSignInHelper
import com.android.sample.ui.components.BottomNavBar
import com.android.sample.ui.components.TopAppBar
import com.android.sample.ui.navigation.AppNavGraph
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    try {
      Firebase.firestore.useEmulator("10.0.2.2", 8080)
      Firebase.auth.useEmulator("10.0.2.2", 9099)
    } catch (e: Exception) {
      // Log the error but don't crash the app
      println("Firebase emulator connection failed: ${e.message}")
      // App will continue to work with production Firebase
    }

    setContent {
      // Show only LoginScreen for now
      MainApp()
    }
  }
}

@Composable
fun LoginApp() {
  val context = LocalContext.current
  val activity = context as? ComponentActivity
  val viewModel: AuthenticationViewModel = remember { AuthenticationViewModel(context) }

  // Google Sign-In helper setup with error handling
  val googleSignInHelper =
      remember(activity) {
        try {
          activity?.let { act ->
            GoogleSignInHelper(act) { result ->
              try {
                viewModel.handleGoogleSignInResult(result)
              } catch (e: Exception) {
                println("Google Sign-In result handling failed: ${e.message}")
                viewModel.setError("Google Sign-In processing failed: ${e.message}")
              }
            }
          }
        } catch (e: Exception) {
          println("Google Sign-In helper initialization failed: ${e.message}")
          null
        }
      }

  LoginScreen(
      viewModel = viewModel,
      onGoogleSignIn = {
        try {
          googleSignInHelper?.signInWithGoogle()
              ?: run { viewModel.setError("Google Sign-In is not available") }
        } catch (e: Exception) {
          println("Google Sign-In failed: ${e.message}")
          viewModel.setError("Google Sign-In failed: ${e.message}")
        }
      })
}

@Composable
fun MainApp() {
  val navController = rememberNavController()

  Scaffold(topBar = { TopAppBar(navController) }, bottomBar = { BottomNavBar(navController) }) {
      paddingValues ->
    androidx.compose.foundation.layout.Box(modifier = Modifier.padding(paddingValues)) {
      AppNavGraph(navController = navController)
    }
  }
}
