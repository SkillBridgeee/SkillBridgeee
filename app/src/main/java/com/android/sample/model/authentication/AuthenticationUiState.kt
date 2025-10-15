package com.android.sample.model.authentication

/** Data class representing the UI state for authentication screens */
data class AuthenticationUiState(
    val email: String = "",
    val password: String = "",
    val selectedRole: UserRole = UserRole.LEARNER,
    val isLoading: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val showSuccessMessage: Boolean = false
) {
  val isSignInButtonEnabled: Boolean
    get() = email.isNotBlank() && password.isNotBlank() && !isLoading
}
