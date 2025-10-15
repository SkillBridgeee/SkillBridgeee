@file:Suppress("DEPRECATION")

package com.android.sample.model.authentication

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

/**
 * Helper class for managing Google Sign-In flow. Handles the activity result launcher and Google
 * Sign-In client configuration.
 */
class GoogleSignInHelper(
    activity: ComponentActivity,
    private val onSignInResult: (ActivityResult) -> Unit
) {
  private val googleSignInClient: GoogleSignInClient
  private val signInLauncher: ActivityResultLauncher<android.content.Intent>

  init {
    // Configure Google Sign-In
    val gso =
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(
                "1061045584009-duiljd2t9ijc3u8vc9193a4ecpk2di5f.apps.googleusercontent.com")
            .requestEmail()
            .build()

    googleSignInClient = GoogleSignIn.getClient(activity, gso)

    // Register activity result launcher
    signInLauncher =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result ->
          onSignInResult(result)
        }
  }

  /** Launch Google Sign-In intent */
  fun signInWithGoogle() {
    val signInIntent = googleSignInClient.signInIntent
    signInLauncher.launch(signInIntent)
  }

  /** This function will be used later when signout is implemented* */
  /** Sign out from Google */
  fun signOut() {
    googleSignInClient.signOut()
  }
}
