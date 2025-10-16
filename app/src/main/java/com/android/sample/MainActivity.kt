package com.android.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.android.sample.ui.components.BottomNavBar
import com.android.sample.ui.components.TopAppBar
import com.android.sample.ui.navigation.AppNavGraph
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import okhttp3.OkHttpClient

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

    setContent { MainApp() }
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
fun MainApp() {
  val navController = rememberNavController()

  Scaffold(topBar = { TopAppBar(navController) }, bottomBar = { BottomNavBar(navController) }) {
      paddingValues ->
    androidx.compose.foundation.layout.Box(modifier = Modifier.padding(paddingValues)) {
      AppNavGraph(navController = navController)
    }
  }
}
