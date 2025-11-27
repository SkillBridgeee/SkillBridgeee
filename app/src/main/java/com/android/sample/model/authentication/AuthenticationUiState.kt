package com.android.sample.model.authentication

/** Data class representing the UI state for authentication screens */
data class AuthenticationUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val showSuccessMessage: Boolean = false,
    // Password reset fields
    val showPasswordResetDialog: Boolean = false,
    val resetEmail: String = "",
    val passwordResetError: String? = null,
    val passwordResetMessage: String? = null,
    val passwordResetCooldownSeconds: Int = 0
) {
  val isSignInButtonEnabled: Boolean
    get() = email.isNotBlank() && password.isNotBlank() && !isLoading

  val isPasswordResetButtonEnabled: Boolean
    get() = resetEmail.isNotBlank() && passwordResetCooldownSeconds == 0
}
