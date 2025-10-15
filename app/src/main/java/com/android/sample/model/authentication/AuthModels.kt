package com.android.sample.model.authentication

/** Data class representing an authenticated user */
data class AuthUser(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?
)

/** Sealed class representing authentication result */
sealed class AuthResult {
  data class Success(val user: AuthUser) : AuthResult()

  data class Error(val exception: Exception) : AuthResult()
}

/** User role enum matching the LoginScreen */
enum class UserRole(val displayName: String) {
  LEARNER("Learner"),
  TUTOR("Tutor")
}

/** UI State for authentication screens - contains all UI-related state */
data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val email: String = "",
    val password: String = "",
    val selectedRole: UserRole = UserRole.LEARNER,
    val showSuccessMessage: Boolean = false,
    val isSignInButtonEnabled: Boolean = false,
    // Sign-up specific fields
    val name: String = "",
    val isSignUpButtonEnabled: Boolean = false
    // TODO: Add other sign-up fields as needed (e.g., address, skills, etc.)
)
