package com.android.sample.model.authentication

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Singleton that manages the current user session throughout the app.
 *
 * This class:
 * - Tracks the currently authenticated user
 * - Provides user ID and email to all parts of the app
 * - Listens to Firebase Auth state changes
 * - Emits StateFlow for reactive UI updates
 *
 * Usage:
 * ```kotlin
 * // Get current user ID
 * val userId = UserSessionManager.getCurrentUserId()
 *
 * // Observe auth state in composables
 * val authState by UserSessionManager.authState.collectAsStateWithLifecycle()
 *
 * // Check if user is signed in
 * if (UserSessionManager.isUserSignedIn()) { ... }
 * ```
 */
object UserSessionManager {
  private val auth: FirebaseAuth = FirebaseAuth.getInstance()

  // StateFlow to observe authentication state changes
  private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
  val authState: StateFlow<AuthState> = _authState.asStateFlow()

  // StateFlow to observe current user
  private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
  val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

  init {
    // Listen to auth state changes
    auth.addAuthStateListener { firebaseAuth ->
      val user = firebaseAuth.currentUser
      _currentUser.value = user
      _authState.value =
          when {
            user != null -> AuthState.Authenticated(user.uid, user.email)
            else -> AuthState.Unauthenticated
          }
    }
  }

  /**
   * Get the current user's ID
   *
   * @return User ID if authenticated, null otherwise
   */
  fun getCurrentUserId(): String? {
    return testUserId ?: auth.currentUser?.uid
  }

  /**
   * Log out the current user
   *
   * This will:
   * - Sign out from Firebase Auth
   * - Update the auth state to Unauthenticated
   * - Clear the current user
   */
  fun logout() {
    auth.signOut()
    _currentUser.value = null
    _authState.value = AuthState.Unauthenticated
  }

  // Test-only methods - DO NOT USE IN PRODUCTION CODE
  private var testUserId: String? = null

  /**
   * FOR TESTING ONLY: Set a fake user ID for testing purposes This bypasses Firebase Auth and
   * should only be used in tests
   */
  @Deprecated("FOR TESTING ONLY", level = DeprecationLevel.WARNING)
  fun setCurrentUserId(userId: String) {
    testUserId = userId
    _authState.value = AuthState.Authenticated(userId, "test@example.com")
  }

  /** FOR TESTING ONLY: Clear the test session This should be called in test cleanup */
  @Deprecated("FOR TESTING ONLY", level = DeprecationLevel.WARNING)
  fun clearSession() {
    testUserId = null
    _authState.value = AuthState.Unauthenticated
  }

  /**
   * Check if a user is signed in
   *
   * @return true if authenticated, false otherwise
   */
  fun isUserSignedIn(): Boolean {
    return testUserId != null || auth.currentUser != null
  }
}

/** Sealed class representing the authentication state */
sealed class AuthState {
  /** Loading state - checking authentication status */
  object Loading : AuthState()

  /** User is authenticated */
  data class Authenticated(val userId: String, val email: String?) : AuthState()

  /** User is not authenticated */
  object Unauthenticated : AuthState()
}
