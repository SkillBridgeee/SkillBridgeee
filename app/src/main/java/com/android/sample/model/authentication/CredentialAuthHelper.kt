@file:Suppress("DEPRECATION")

package com.android.sample.model.authentication

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.PasswordCredential
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.GoogleAuthProvider

/**
 * Helper class for managing authentication using Credential Manager API. Handles password
 * credentials using modern Credential Manager. For Google Sign-In, provides a helper to get
 * GoogleSignInClient.
 */
class CredentialAuthHelper(private val context: Context) {

  private val credentialManager by lazy {
    try {
      CredentialManager.create(context)
    } catch (e: Exception) {
      // Log error but don't crash - this can happen if Play Services isn't available
      println("CredentialManager creation failed: ${e.message}")
      null
    }
  }

  companion object {
    const val WEB_CLIENT_ID =
        "1061045584009-duiljd2t9ijc3u8vc9193a4ecpk2di5f.apps.googleusercontent.com"
  }

  /**
   * Get GoogleSignInClient for initiating Google Sign-In flow This uses the traditional Google
   * Sign-In SDK which is simpler and more reliable
   */
  fun getGoogleSignInClient(): GoogleSignInClient {
    val gso =
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(WEB_CLIENT_ID)
            .requestEmail()
            .build()

    return GoogleSignIn.getClient(context, gso)
  }

  /**
   * Get saved password credential using Credential Manager
   *
   * @return Result containing PasswordCredential or exception
   */
  suspend fun getPasswordCredential(): Result<PasswordCredential> {
    return try {
      val manager = credentialManager ?: return Result.failure(
        Exception("CredentialManager not available")
      )

      val request = GetCredentialRequest.Builder().build()

      val result = manager.getCredential(request = request, context = context)

      handlePasswordResult(result)
    } catch (e: GetCredentialException) {
      Result.failure(Exception("No saved credentials found: ${e.message}", e))
    } catch (e: Exception) {
      Result.failure(Exception("Unexpected error: ${e.message}", e))
    }
  }

  /** Convert Google ID token to Firebase AuthCredential */
  fun getFirebaseCredential(idToken: String): AuthCredential {
    return GoogleAuthProvider.getCredential(idToken, null)
  }

  private fun handlePasswordResult(result: GetCredentialResponse): Result<PasswordCredential> {
    return when (val credential = result.credential) {
      is PasswordCredential -> {
        Result.success(credential)
      }
      else -> {
        Result.failure(Exception("No password credential found"))
      }
    }
  }
}
