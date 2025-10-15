package com.android.sample.model.authentication

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** ViewModel for handling authentication operations in the UI */
class AuthenticationViewModel(context: Context) : ViewModel() {

  private val authService = AuthenticationServiceProvider.getAuthenticationService(context)

  private val _uiState = MutableStateFlow(AuthUiState())
  val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

  private val _authResult = MutableStateFlow<AuthResult?>(null)
  val authResult: StateFlow<AuthResult?> = _authResult.asStateFlow()

  init {
    // Update sign-in button state whenever email or password changes
    updateSignInButtonState()
  }

  /** Update email field */
  fun updateEmail(email: String) {
    _uiState.value = _uiState.value.copy(email = email)
    updateSignInButtonState()
    updateSignUpButtonState()
  }

  /** Update password field */
  fun updatePassword(password: String) {
    _uiState.value = _uiState.value.copy(password = password)
    updateSignInButtonState()
    updateSignUpButtonState()
  }

  /** Update selected role */
  fun updateSelectedRole(role: UserRole) {
    _uiState.value = _uiState.value.copy(selectedRole = role)
  }

  /** Update name field (for sign-up) */
  fun updateName(name: String) {
    _uiState.value = _uiState.value.copy(name = name)
    updateSignUpButtonState()
  }

  // TODO: Add methods for other sign-up fields as needed
  // Example:
  // fun updateAddress(address: String) { ... }

  /** Update sign-in button enabled state based on email and password */
  private fun updateSignInButtonState() {
    val currentState = _uiState.value
    val isEnabled =
        currentState.email.isNotEmpty() &&
            currentState.password.isNotEmpty() &&
            !currentState.isLoading
    _uiState.value = currentState.copy(isSignInButtonEnabled = isEnabled)
  }

  /** Update sign-up button enabled state based on required fields */
  private fun updateSignUpButtonState() {
    val currentState = _uiState.value
    val isEnabled =
        currentState.name.isNotEmpty() &&
            currentState.email.isNotEmpty() &&
            currentState.password.isNotEmpty() &&
            !currentState.isLoading
    // TODO: Add validation for other required sign-up fields here
    _uiState.value = currentState.copy(isSignUpButtonEnabled = isEnabled)
  }

  /** Show success message */
  fun showSuccessMessage(show: Boolean) {
    _uiState.value = _uiState.value.copy(showSuccessMessage = show)
  }

  /** Sign in with current email and password from state */
  fun signIn() {
    val currentState = _uiState.value
    signInWithEmailAndPassword(currentState.email, currentState.password)
  }

  /** Send password reset email using current email from state */
  fun sendPasswordReset() {
    val currentState = _uiState.value
    if (currentState.email.isNotEmpty()) {
      sendPasswordResetEmail(currentState.email)
    } else {
      setError("Please enter your email address first")
    }
  }

  /** Sign up with current form data (simplified - no confirm password) */
  fun signUp() {
    val currentState = _uiState.value
    signUpWithEmailAndPassword(currentState.email, currentState.password, currentState.name)
  }

  /** Sign in with email and password */
  fun signInWithEmailAndPassword(email: String, password: String) {
    if (!isValidEmail(email) || password.length < 6) {
      _uiState.value =
          _uiState.value.copy(error = "Please enter a valid email and password (min 6 characters)")
      updateSignInButtonState()
      return
    }

    _uiState.value = _uiState.value.copy(isLoading = true, error = null)
    updateSignInButtonState()

    viewModelScope.launch {
      val result = authService.signInWithEmailAndPassword(email, password)
      _authResult.value = result
      _uiState.value =
          _uiState.value.copy(
              isLoading = false,
              error = if (result is AuthResult.Error) result.exception.message else null,
              showSuccessMessage = result is AuthResult.Success)
      updateSignInButtonState()
    }
  }

  /** Sign up with email and password */
  fun signUpWithEmailAndPassword(email: String, password: String, name: String) {
    if (!isValidEmail(email) || password.length < 6 || name.isBlank()) {
      _uiState.value =
          _uiState.value.copy(
              error = "Please enter valid email, password (min 6 characters), and name")
      return
    }

    _uiState.value = _uiState.value.copy(isLoading = true, error = null)

    viewModelScope.launch {
      val result = authService.signUpWithEmailAndPassword(email, password, name)
      _authResult.value = result
      _uiState.value =
          _uiState.value.copy(
              isLoading = false,
              error = if (result is AuthResult.Error) result.exception.message else null)
    }
  }

  /** Handle Google Sign-In result */
  fun handleGoogleSignInResult(result: AuthResult) {
    _authResult.value = result
    _uiState.value =
        _uiState.value.copy(
            isLoading = false,
            error = if (result is AuthResult.Error) result.exception.message else null)
  }

  /** Send password reset email */
  fun sendPasswordResetEmail(email: String) {
    if (!isValidEmail(email)) {
      _uiState.value = _uiState.value.copy(error = "Please enter a valid email address")
      return
    }

    _uiState.value = _uiState.value.copy(isLoading = true, error = null)

    viewModelScope.launch {
      val success = authService.sendPasswordResetEmail(email)
      _uiState.value =
          _uiState.value.copy(
              isLoading = false,
              error = if (!success) "Failed to send password reset email" else null,
              message = if (success) "Password reset email sent!" else null)
    }
  }

  /** Sign out current user */
  fun signOut() {
    viewModelScope.launch {
      authService.signOut()
      _authResult.value = null
      _uiState.value = AuthUiState()
    }
  }

  /** Clear error message */
  fun clearError() {
    _uiState.value = _uiState.value.copy(error = null)
  }

  /** Clear message */
  fun clearMessage() {
    _uiState.value = _uiState.value.copy(message = null)
  }

  /** Check if user is currently signed in */
  fun isUserSignedIn(): Boolean {
    return authService.isUserSignedIn()
  }

  /** Get current user */
  fun getCurrentUser(): AuthUser? {
    return authService.getCurrentUser()
  }

  /** Set error message (for UI integration) */
  fun setError(message: String) {
    _uiState.value = _uiState.value.copy(error = message)
  }

  private fun isValidEmail(email: String): Boolean {
    return try {
      // Use Android's Patterns if available (production)
      android.util.Patterns.EMAIL_ADDRESS?.matcher(email)?.matches() == true
    } catch (e: Exception) {
      // Fallback for unit tests where Android framework is not available
      email.contains("@") && email.contains(".") && email.length > 5
    }
  }
}
