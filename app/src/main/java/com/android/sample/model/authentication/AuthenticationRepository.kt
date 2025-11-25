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
   * Sign in with email and password. Checks if email is verified before allowing sign in.
   *
   * @return Result containing FirebaseUser on success or Exception on failure
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

      // Check if email is verified
      if (!user.isEmailVerified) {
        // Sign out unverified user
        auth.signOut()
        return Result.failure(
            Exception(
                "Please verify your email before logging in. Check your inbox for the verification link."))
      }

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
   * Reload the current user's data from the server
   *
   * @return Result indicating success or failure
   */
  @Suppress("unused")
  suspend fun reloadUser(): Result<Unit> {
    return try {
      val user = auth.currentUser
      if (user == null) {
        return Result.failure(Exception("No user is currently signed in"))
      }

      user.reload().await()
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(normalizeAuthException(e))
    }
  }

  /**
   * Resend verification email for a specific email/password combination. Signs in the user
   * temporarily to send the email, then signs out.
   *
   * @return Result indicating success or failure
   */
  suspend fun resendVerificationEmail(email: String, password: String): Result<Unit> {
    return try {
      // Sign in to get the user object
      val result = auth.signInWithEmailAndPassword(email, password).await()
      val user = result.user

      if (user == null) {
        return Result.failure(Exception("Failed to sign in"))
      }

      // Reload to check current verification status
      user.reload().await()

      if (user.isEmailVerified) {
        // Already verified, sign out and return success
        auth.signOut()
        return Result.failure(Exception("Email is already verified. You can now log in."))
      }

      // Send verification email
      user.sendEmailVerification().await()

      // Sign out the user
      auth.signOut()

      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(normalizeAuthException(e))
    }
  }
}
