package com.android.sample.model.authentication

import android.app.Activity
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.launch

/**
 * Simplified Google Sign-In Helper - closer to old project approach This removes dependency
 * injection complexity that might be causing issues
 */
class GoogleSignInHelper(
    private val activity: ComponentActivity,
    private val onSignInResult: (AuthResult) -> Unit
) {

  // Direct repository access instead of through service provider
  private val firebaseAuthRepo = FirebaseAuthenticationRepository(activity)

  private val googleSignInLauncher: ActivityResultLauncher<Intent> =
      activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result
        ->
        if (result.resultCode == Activity.RESULT_OK) {
          val task: Task<GoogleSignInAccount> =
              GoogleSignIn.getSignedInAccountFromIntent(result.data)
          handleSignInResult(task)
        } else {
          onSignInResult(AuthResult.Error(Exception("Google Sign-In cancelled")))
        }
      }

  /** Start Google Sign-In flow */
  fun signInWithGoogle() {
    try {
      val signInIntent = firebaseAuthRepo.googleSignInClient.signInIntent
      googleSignInLauncher.launch(signInIntent)
    } catch (e: Exception) {
      onSignInResult(AuthResult.Error(Exception("Failed to start Google Sign-In: ${e.message}")))
    }
  }

  private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
    try {
      val account = completedTask.getResult(ApiException::class.java)
      val idToken = account.idToken
      if (idToken != null) {
        // Use the simplified token-based sign-in (like your old project)
        activity.lifecycleScope.launch {
          val result = firebaseAuthRepo.signInWithGoogleToken(idToken)
          onSignInResult(result)
        }
      } else {
        onSignInResult(AuthResult.Error(Exception("Failed to get ID token from Google")))
      }
    } catch (e: ApiException) {
      onSignInResult(AuthResult.Error(Exception("Google Sign-In failed: ${e.message}")))
    }
  }
}
