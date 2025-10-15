package com.android.sample.model.authentication

import android.content.Context
import com.android.sample.model.user.ProfileRepository
import com.android.sample.model.user.ProfileRepositoryProvider

/** Provider class for authentication services */
object AuthenticationServiceProvider {

  private var authenticationService: AuthenticationService? = null
  private var testAuthRepository: AuthenticationRepository? = null
  private var testProfileRepository: ProfileRepository? = null
  private var isTestMode: Boolean = false

  /** Initialize and get the authentication service instance */
  fun getAuthenticationService(context: Context): AuthenticationService {
    if (authenticationService == null) {
      // If we're in test mode, use only test repositories and avoid any Firebase code paths
      if (isTestMode) {
        if (testAuthRepository == null || testProfileRepository == null) {
          throw IllegalStateException(
              "Test mode is enabled but test repositories are not properly set")
        }
        authenticationService = AuthenticationService(testAuthRepository!!, testProfileRepository!!)
      } else {
        // Production mode - use Firebase repositories
        authenticationService = createProductionService(context)
      }
    }
    return authenticationService!!
  }

  /**
   * Create production service with Firebase dependencies (separated to avoid class loading in test
   * mode)
   */
  private fun createProductionService(context: Context): AuthenticationService {
    val authRepository = FirebaseAuthenticationRepository(context)
    val profileRepository = ProfileRepositoryProvider.repository
    return AuthenticationService(authRepository, profileRepository)
  }

  /** Reset the authentication service (useful for testing) */
  fun resetAuthenticationService() {
    authenticationService = null
  }

  /** Set a test authentication repository (for testing purposes only) */
  fun setTestAuthRepository(repository: AuthenticationRepository?) {
    testAuthRepository = repository
  }

  /** Set a test profile repository (for testing purposes only) */
  fun setTestProfileRepository(repository: ProfileRepository?) {
    testProfileRepository = repository
  }

  /** Enable test mode to completely avoid Firebase initialization */
  fun enableTestMode() {
    isTestMode = true
  }

  /** Disable test mode to allow Firebase initialization */
  fun disableTestMode() {
    isTestMode = false
    testAuthRepository = null
    testProfileRepository = null
  }

  /** Check if we're in test mode */
  fun isInTestMode(): Boolean {
    return isTestMode
  }
}
