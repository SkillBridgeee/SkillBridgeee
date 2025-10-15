package com.android.sample.model.authentication

/** Interface for Google Sign-In operations to abstract platform dependencies from ViewModel */
interface GoogleSignInManager {
  fun signInWithGoogle(onResult: (AuthResult) -> Unit)

  fun isAvailable(): Boolean
}

/** Implementation of GoogleSignInManager that wraps GoogleSignInHelper */
class GoogleSignInManagerImpl(private val googleSignInHelper: GoogleSignInHelper?) :
    GoogleSignInManager {

  override fun signInWithGoogle(onResult: (AuthResult) -> Unit) {
    googleSignInHelper?.signInWithGoogle()
        ?: run { onResult(AuthResult.Error(Exception("Google Sign-In not available"))) }
  }

  override fun isAvailable(): Boolean {
    return googleSignInHelper != null
  }
}
