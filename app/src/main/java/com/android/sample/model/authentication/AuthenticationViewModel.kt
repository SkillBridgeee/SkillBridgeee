@file:Suppress("DEPRECATION")

package com.android.sample.model.authentication

import android.content.Context
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.model.user.ProfileRepository
import com.android.sample.model.user.ProfileRepositoryProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for managing authentication state and operations. Follows MVVM architecture pattern
 * with Credential Manager API for passwords and Google Sign-In SDK for Google authentication.
 */
@Suppress("CONTEXT_RECEIVER_MEMBER_IS_DEPRECATED")
class AuthenticationViewModel(
    @Suppress("StaticFieldLeak") private val context: Context,
    private val repository: AuthenticationRepository = AuthenticationRepository(),
    private val credentialHelper: CredentialAuthHelper = CredentialAuthHelper(context),
    private val profileRepository: ProfileRepository = ProfileRepositoryProvider.repository
) : ViewModel() {

  companion object {
    private const val TAG = "AuthViewModel"
  }

  private val _uiState = MutableStateFlow(AuthenticationUiState())
  val uiState: StateFlow<AuthenticationUiState> = _uiState.asStateFlow()

  private val _authResult = MutableStateFlow<AuthResult?>(null)
  val authResult: StateFlow<AuthResult?> = _authResult.asStateFlow()

  /** Helper function to set loading state */
  private fun setLoading() {
    _uiState.update { it.copy(isLoading = true, error = null) }
  }

  /** Helper function to clear loading state on success */
  private fun clearLoading() {
    _uiState.update { it.copy(isLoading = false, error = null) }
  }

  /** Helper function to set error state and clear loading */
  private fun setErrorState(errorMessage: String) {
    _uiState.update { it.copy(isLoading = false, error = errorMessage) }
  }

  /** Update the email field */
  fun updateEmail(email: String) {
    _uiState.update { it.copy(email = email, error = null, message = null) }
  }

  /** Update the password field */
  fun updatePassword(password: String) {
    _uiState.update { it.copy(password = password, error = null, message = null) }
  }

  /** Sign in with email and password */
  fun signIn() {
    val email = _uiState.value.email
    val password = _uiState.value.password

    if (email.isBlank() || password.isBlank()) {
      _uiState.update { it.copy(error = "Email and password cannot be empty") }
      return
    }

    setLoading()

    viewModelScope.launch {
      val result = repository.signInWithEmail(email, password)
      result.fold(
          onSuccess = { user ->
            // Check if email is verified
            if (!user.isEmailVerified) {
              // Keep user signed in but show unverified state
              // They can now call resendVerificationEmail() without re-entering password
              _authResult.value = AuthResult.UnverifiedEmail(user)
              _uiState.update {
                it.copy(
                    isLoading = false,
                    error =
                        "Please verify your email to access all features. Check your inbox or click 'Resend Verification Email'.")
              }
              return@launch
            }

            // Check if profile exists for this user
            val profile =
                try {
                  withContext(Dispatchers.IO) { profileRepository.getProfile(user.uid) }
                } catch (_: Exception) {
                  null
                }

            if (profile == null) {
              // No profile exists - this is a verified user logging in for the first time
              // They need to complete sign up to create their profile
              val userEmail = user.email ?: email
              Log.d(TAG, "Verified user needs to complete profile. Email: $userEmail")
              _authResult.value = AuthResult.RequiresSignUp(userEmail, user)
              clearLoading()
            } else {
              // Profile exists - successful login
              _authResult.value = AuthResult.Success(user)
              clearLoading()
            }
          },
          onFailure = { exception ->
            val errorMessage = exception.message ?: "Sign in failed"
            _authResult.value = AuthResult.Error(errorMessage)
            setErrorState(errorMessage)
          })
    }
  }

  /** Handle Google Sign-In result from activity */
  @Suppress("DEPRECATION")
  fun handleGoogleSignInResult(result: ActivityResult) {
    setLoading()

    try {
      val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
      val account = task.getResult(ApiException::class.java)

      account.idToken?.let { idToken ->
        val firebaseCredential = credentialHelper.getFirebaseCredential(idToken)

        viewModelScope.launch {
          val authResult = repository.signInWithCredential(firebaseCredential)
          authResult.fold(
              onSuccess = { user ->
                // Check if profile exists for this user
                val profile =
                    try {
                      withContext(Dispatchers.IO) { profileRepository.getProfile(user.uid) }
                    } catch (_: Exception) {
                      null
                    }

                if (profile == null) {
                  // No profile exists - user needs to sign up
                  val email = user.email ?: account.email ?: ""
                  Log.d(
                      TAG,
                      "User needs sign up. Firebase email: ${user.email}, Google email: ${account.email}, Final email: $email")
                  _authResult.value = AuthResult.RequiresSignUp(email, user)
                  clearLoading()
                } else {
                  // Profile exists - successful login
                  _authResult.value = AuthResult.Success(user)
                  clearLoading()
                }
              },
              onFailure = { exception ->
                val errorMessage = exception.message ?: "Google sign in failed"
                _authResult.value = AuthResult.Error(errorMessage)
                setErrorState(errorMessage)
              })
        }
      }
          ?: run {
            _authResult.value = AuthResult.Error("No ID token received")
            setErrorState("No ID token received")
          }
    } catch (e: ApiException) {
      val errorMessage = "Google sign in failed: ${e.message}"
      _authResult.value = AuthResult.Error(errorMessage)
      setErrorState(errorMessage)
    }
  }

  /** Get GoogleSignInClient for initiating sign-in */
  fun getGoogleSignInClient() = credentialHelper.getGoogleSignInClient()

  /** Try to get saved password credential using Credential Manager */
  fun getSavedCredential() {
    setLoading()

    viewModelScope.launch {
      val result = credentialHelper.getPasswordCredential()
      result.fold(
          onSuccess = { passwordCredential ->
            // Auto-fill the email and password
            _uiState.update {
              it.copy(
                  email = passwordCredential.id,
                  password = passwordCredential.password,
                  isLoading = false,
                  message = "Credential loaded")
            }
          },
          onFailure = { exception ->
            // Silently fail - no saved credentials is not an error
            _uiState.update { it.copy(isLoading = false) }
          })
    }
  }

  /** Sign out the current user */
  fun signOut() {
    repository.signOut()
    credentialHelper.getGoogleSignInClient().signOut()
    _authResult.value = null
    _uiState.update {
      AuthenticationUiState() // Reset to default state
    }
  }

  /** Set error message */
  fun setError(message: String) {
    _uiState.update { it.copy(error = message, isLoading = false) }
  }

  /** Show or hide success message */
  fun showSuccessMessage(show: Boolean) {
    _uiState.update { it.copy(showSuccessMessage = show) }
  }

  /**
   * Resend verification email to the currently signed-in user. This is the secure approach that
   * uses the existing authenticated session without requiring password re-entry.
   *
   * SECURITY: This method never asks for or handles passwords, eliminating password exposure risks.
   * Users should be kept signed in after signup so they can call this method easily.
   */
  fun resendVerificationEmail() {
    setLoading()

    viewModelScope.launch {
      val result = repository.resendVerificationEmail()
      result.fold(
          onSuccess = {
            _uiState.update {
              it.copy(
                  isLoading = false,
                  error = null,
                  message = "Verification email sent! Please check your inbox.")
            }
          },
          onFailure = { exception ->
            val errorMessage = exception.message ?: "Failed to resend verification email"
            setErrorState(errorMessage)
          })
    }
  }
}
