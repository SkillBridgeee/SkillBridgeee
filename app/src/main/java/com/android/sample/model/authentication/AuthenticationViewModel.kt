@file:Suppress("DEPRECATION")

package com.android.sample.model.authentication

import android.content.Context
import androidx.activity.result.ActivityResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for managing authentication state and operations. Follows MVVM architecture pattern
 * with Credential Manager API for passwords and Google Sign-In SDK for Google authentication.
 */
@Suppress("CONTEXT_RECEIVER_MEMBER_IS_DEPRECATED")
class AuthenticationViewModel(
    @Suppress("StaticFieldLeak") private val context: Context,
    private val repository: AuthenticationRepository = AuthenticationRepository(),
    private val credentialHelper: CredentialAuthHelper = CredentialAuthHelper(context)
) : ViewModel() {

  private val _uiState = MutableStateFlow(AuthenticationUiState())
  val uiState: StateFlow<AuthenticationUiState> = _uiState.asStateFlow()

  private val _authResult = MutableStateFlow<AuthResult?>(null)
  val authResult: StateFlow<AuthResult?> = _authResult.asStateFlow()

  /** Update the email field */
  fun updateEmail(email: String) {
    _uiState.update { it.copy(email = email, error = null, message = null) }
  }

  /** Update the password field */
  fun updatePassword(password: String) {
    _uiState.update { it.copy(password = password, error = null, message = null) }
  }

  /** Update the selected user role */
  fun updateSelectedRole(role: UserRole) {
    _uiState.update { it.copy(selectedRole = role) }
  }

  /** Sign in with email and password */
  fun signIn() {
    val email = _uiState.value.email
    val password = _uiState.value.password

    if (email.isBlank() || password.isBlank()) {
      _uiState.update { it.copy(error = "Email and password cannot be empty") }
      return
    }

    _uiState.update { it.copy(isLoading = true, error = null) }

    viewModelScope.launch {
      val result = repository.signInWithEmail(email, password)
      result.fold(
          onSuccess = { user ->
            _authResult.value = AuthResult.Success(user)
            _uiState.update { it.copy(isLoading = false, error = null) }
          },
          onFailure = { exception ->
            val errorMessage = exception.message ?: "Sign in failed"
            _authResult.value = AuthResult.Error(errorMessage)
            _uiState.update { it.copy(isLoading = false, error = errorMessage) }
          })
    }
  }

  /** Handle Google Sign-In result from activity */
  @Suppress("DEPRECATION")
  fun handleGoogleSignInResult(result: ActivityResult) {
    _uiState.update { it.copy(isLoading = true, error = null) }

    try {
      val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
      val account = task.getResult(ApiException::class.java)

      account.idToken?.let { idToken ->
        val firebaseCredential = credentialHelper.getFirebaseCredential(idToken)

        viewModelScope.launch {
          val authResult = repository.signInWithCredential(firebaseCredential)
          authResult.fold(
              onSuccess = { user ->
                _authResult.value = AuthResult.Success(user)
                _uiState.update { it.copy(isLoading = false, error = null) }
              },
              onFailure = { exception ->
                val errorMessage = exception.message ?: "Google sign in failed"
                _authResult.value = AuthResult.Error(errorMessage)
                _uiState.update { it.copy(isLoading = false, error = errorMessage) }
              })
        }
      }
          ?: run {
            _authResult.value = AuthResult.Error("No ID token received")
            _uiState.update { it.copy(isLoading = false, error = "No ID token received") }
          }
    } catch (e: ApiException) {
      val errorMessage = "Google sign in failed: ${e.message}"
      _authResult.value = AuthResult.Error(errorMessage)
      _uiState.update { it.copy(isLoading = false, error = errorMessage) }
    }
  }

  /** Get GoogleSignInClient for initiating sign-in */
  fun getGoogleSignInClient() = credentialHelper.getGoogleSignInClient()

  /** Try to get saved password credential using Credential Manager */
  fun getSavedCredential() {
    _uiState.update { it.copy(isLoading = true, error = null) }

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
}
