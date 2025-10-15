package com.android.sample.model.authentication

/** Repository interface for authentication operations */
interface AuthenticationRepository {
  suspend fun signInWithEmailAndPassword(email: String, password: String): AuthResult

  suspend fun signUpWithEmailAndPassword(email: String, password: String, name: String): AuthResult

  suspend fun signInWithGoogle(): AuthResult

  suspend fun signOut()

  fun getCurrentUser(): AuthUser?

  fun isUserSignedIn(): Boolean

  suspend fun sendPasswordResetEmail(email: String): Boolean

  suspend fun deleteAccount(): Boolean
}
