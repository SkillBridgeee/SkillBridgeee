package com.android.sample.model.authentication

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

/**
 * Repository for handling Firebase Authentication operations. Provides methods for email/password
 * and Google Sign-In authentication.
 */
class AuthenticationRepository(private val auth: FirebaseAuth = FirebaseAuth.getInstance()) {

  /**
   * Sign in with email and password
   *
   * @return Result containing FirebaseUser on success or Exception on failure
   */
  suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
    return try {
      val result = auth.signInWithEmailAndPassword(email, password).await()
      result.user?.let { Result.success(it) }
          ?: Result.failure(Exception("Sign in failed: No user"))
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /**
   * Sign in with Google credential
   *
   * @return Result containing FirebaseUser on success or Exception on failure
   */
  suspend fun signInWithCredential(credential: AuthCredential): Result<FirebaseUser> {
    return try {
      val result = auth.signInWithCredential(credential).await()
      result.user?.let { Result.success(it) }
          ?: Result.failure(Exception("Sign in failed: No user"))
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /** Sign out the current user */
  fun signOut() {
    auth.signOut()
  }

  /**
   * Get the current signed-in user
   *
   * @return FirebaseUser if signed in, null otherwise
   */
  fun getCurrentUser(): FirebaseUser? {
    return auth.currentUser
  }

  /**
   * Check if a user is currently signed in
   *
   * @return true if user is signed in, false otherwise
   */
  fun isUserSignedIn(): Boolean {
    return auth.currentUser != null
  }
}
