package com.android.sample.model.authentication

import com.google.firebase.auth.FirebaseUser

/** Sealed class representing the result of an authentication operation */
sealed class AuthResult {
  data class Success(val user: FirebaseUser) : AuthResult()

  data class Error(val message: String) : AuthResult()

  data class RequiresSignUp(val email: String, val user: FirebaseUser) : AuthResult()

  /**
   * User signed in successfully but email is not verified. User remains signed in so they can
   * resend verification email without re-entering password.
   */
  data class UnverifiedEmail(val user: FirebaseUser) : AuthResult()
}
