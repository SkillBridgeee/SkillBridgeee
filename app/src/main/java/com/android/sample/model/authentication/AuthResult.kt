package com.android.sample.model.authentication

import com.google.firebase.auth.FirebaseUser

/** Sealed class representing the result of an authentication operation */
sealed class AuthResult {
  data class Success(val user: FirebaseUser) : AuthResult()

  data class Error(val message: String) : AuthResult()
}
