package com.android.sample.model.authentication

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

/**
 * Repository for handling Firebase Authentication operations. Provides methods for email/password
 * and Google Sign-In authentication.
 */
class AuthenticationRepository(private val auth: FirebaseAuth = FirebaseAuth.getInstance()) {

  /**
   * Normalizes Firebase authentication exceptions into user-friendly error messages.
   *
   * @param e The exception from Firebase Auth
   * @return A normalized exception with a user-friendly message
   */
  private fun normalizeAuthException(e: Exception): Exception {
    return when (e) {
      is FirebaseAuthException -> {
        val message =
            when (e.errorCode) {
              "ERROR_EMAIL_ALREADY_IN_USE" -> "This email is already registered"
              "ERROR_INVALID_EMAIL" -> "Invalid email format"
              "ERROR_WEAK_PASSWORD" -> "Password is too weak. Use at least 6 characters"
              "ERROR_WRONG_PASSWORD" -> "Incorrect password"
              "ERROR_USER_NOT_FOUND" -> "No account found with this email"
              "ERROR_USER_DISABLED" -> "This account has been disabled"
              "ERROR_TOO_MANY_REQUESTS" -> "Too many attempts. Please try again later"
              "ERROR_OPERATION_NOT_ALLOWED" -> "This sign-in method is not enabled"
              "ERROR_INVALID_CREDENTIAL" -> "Invalid credentials. Please try again"
              "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" ->
                  "An account already exists with a different sign-in method"
              "ERROR_CREDENTIAL_ALREADY_IN_USE" ->
                  "This credential is already associated with a different account"
              else -> e.message ?: "Authentication failed"
            }
        Exception(message, e)
      }
      else -> e
    }
  }

  /**
   * Sign in with email and password. Checks if email is verified before allowing full access.
   *
   * SECURITY NOTE: User remains signed in even if unverified, but the app should restrict access to
   * sensitive features until verification is complete. This allows users to resend verification
   * emails without re-entering their password.
   *
   * @return Result containing FirebaseUser on success or Exception on failure. The FirebaseUser is
   *   returned even if email is not verified - check user.isEmailVerified in the UI layer.
   */
  suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
    return try {
      val result = auth.signInWithEmailAndPassword(email, password).await()
      val user = result.user

      if (user == null) {
        return Result.failure(Exception("Sign in failed: No user"))
      }

      // Reload user to get latest verification status
      user.reload().await()

      // Return the user even if unverified - let the UI layer handle the verification prompt
      // This allows the user to call resendVerificationEmail() without re-entering their password
      Result.success(user)
    } catch (e: Exception) {
      Result.failure(normalizeAuthException(e))
    }
  }

  /**
   * Create a new user with email and password
   *
   * @return Result containing FirebaseUser on success or Exception on failure
   */
  suspend fun signUpWithEmail(email: String, password: String): Result<FirebaseUser> {
    return try {
      val result = auth.createUserWithEmailAndPassword(email, password).await()
      result.user?.let { Result.success(it) }
          ?: Result.failure(Exception("Sign up failed: No user created"))
    } catch (e: Exception) {
      Result.failure(normalizeAuthException(e))
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
      Result.failure(normalizeAuthException(e))
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

  /**
   * Send email verification to the current user
   *
   * @return Result indicating success or failure
   */
  suspend fun sendEmailVerification(): Result<Unit> {
    return try {
      val user = auth.currentUser
      if (user == null) {
        return Result.failure(Exception("No user is currently signed in"))
      }

      user.sendEmailVerification().await()
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(normalizeAuthException(e))
    }
  }

  /**
   * Check if the current user's email is verified
   *
   * @return true if email is verified, false otherwise or if no user is signed in
   */
  @Suppress("unused")
  suspend fun isEmailVerified(): Boolean {
    val user = auth.currentUser ?: return false

    // Reload user to get latest verification status from server
    try {
      user.reload().await()
    } catch (_: Exception) {
      return false
    }

    return user.isEmailVerified
  }

  /**
   * Resend verification email for the currently signed-in user. This is the secure,
   * Firebase-recommended approach that uses the existing authenticated session without requiring
   * password re-entry.
   *
   * SECURITY: This method never asks for or handles passwords, eliminating password exposure risks.
   *
   * @return Result indicating success or failure
   */
  suspend fun resendVerificationEmail(): Result<Unit> {
    return try {
      val user = auth.currentUser
      if (user == null) {
        return Result.failure(Exception("Please sign in first to resend verification email"))
      }

      // Reload to check current verification status
      user.reload().await()

      if (user.isEmailVerified) {
        return Result.failure(
            Exception("Email is already verified. You can now access all features."))
      }

      // Send verification email
      user.sendEmailVerification().await()

      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(normalizeAuthException(e))
    }
  }
}
