package com.android.sample.ui.signup

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.model.authentication.AuthenticationRepository
import com.android.sample.model.user.ProfileRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Holds the state of individual password requirements. */
data class PasswordRequirements(
    val minLength: Boolean = false,
    val hasLetter: Boolean = false,
    val hasDigit: Boolean = false,
    val hasSpecial: Boolean = false
) {
  /** Returns true if all requirements are met */
  val allMet: Boolean
    get() = minLength && hasLetter && hasDigit && hasSpecial
}

data class SignUpUiState(
    val name: String = "",
    val surname: String = "",
    val address: String = "",
    val levelOfEducation: String = "",
    val description: String = "",
    val email: String = "",
    val password: String = "",
    val submitting: Boolean = false,
    val error: String? = null,
    val canSubmit: Boolean = false,
    val submitSuccess: Boolean = false,
    val isGoogleSignUp: Boolean = false, // True if user is already authenticated via Google
    val passwordRequirements: PasswordRequirements = PasswordRequirements()
)

sealed interface SignUpEvent {

  data class NameChanged(val value: String) : SignUpEvent

  data class SurnameChanged(val value: String) : SignUpEvent

  data class AddressChanged(val value: String) : SignUpEvent

  data class LevelOfEducationChanged(val value: String) : SignUpEvent

  data class DescriptionChanged(val value: String) : SignUpEvent

  data class EmailChanged(val value: String) : SignUpEvent

  data class PasswordChanged(val value: String) : SignUpEvent

  object Submit : SignUpEvent
}

class SignUpViewModel(
    initialEmail: String? = null,
    private val authRepository: AuthenticationRepository = AuthenticationRepository(),
    private val signUpUseCase: SignUpUseCase =
        SignUpUseCase(AuthenticationRepository(), ProfileRepositoryProvider.repository)
) : ViewModel() {

  companion object {
    private const val TAG = "SignUpViewModel"
  }

  private val _state = MutableStateFlow(SignUpUiState())
  val state: StateFlow<SignUpUiState> = _state

  /**
   * Validates password and returns individual requirement states. Extracted to a helper function to
   * avoid duplication between UI and validation logic.
   */
  private fun validatePassword(password: String): PasswordRequirements {
    return PasswordRequirements(
        minLength = password.length >= 8,
        hasLetter = password.any { it.isLetter() },
        hasDigit = password.any { it.isDigit() },
        hasSpecial = Regex("[^A-Za-z0-9]").containsMatchIn(password))
  }

  init {
    // Check if user is already authenticated (Google Sign-In) and pre-fill email
    if (!initialEmail.isNullOrBlank()) {
      val isAuthenticated = authRepository.getCurrentUser() != null
      Log.d(TAG, "Init - Email: $initialEmail, User authenticated: $isAuthenticated")
      _state.update { it.copy(email = initialEmail, isGoogleSignUp = isAuthenticated) }
      validate()
    }
  }

  /** Called when user navigates away from signup without completing */
  fun onSignUpAbandoned() {
    // If this was a Google sign-up (user is authenticated but no profile was created)
    // sign them out so they go through the flow again next time
    if (_state.value.isGoogleSignUp && !_state.value.submitSuccess) {
      Log.d(TAG, "Sign-up abandoned - signing out Google user")
      authRepository.signOut()
    }
  }

  fun onEvent(e: SignUpEvent) {
    when (e) {
      is SignUpEvent.NameChanged -> _state.update { it.copy(name = e.value) }
      is SignUpEvent.SurnameChanged -> _state.update { it.copy(surname = e.value) }
      is SignUpEvent.AddressChanged -> _state.update { it.copy(address = e.value) }
      is SignUpEvent.LevelOfEducationChanged ->
          _state.update { it.copy(levelOfEducation = e.value) }
      is SignUpEvent.DescriptionChanged -> _state.update { it.copy(description = e.value) }
      is SignUpEvent.EmailChanged -> {
        // Don't allow email changes for Google sign-ups
        if (!_state.value.isGoogleSignUp) {
          _state.update { it.copy(email = e.value) }
        }
      }
      is SignUpEvent.PasswordChanged -> _state.update { it.copy(password = e.value) }
      SignUpEvent.Submit -> submit()
    }
    validate()
  }

  private fun validate() {
    val namePattern = Regex("^[\\p{L} ]+\$") // Unicode letters and spaces only

    _state.update { s ->
      val nameTrim = s.name.trim()
      val surnameTrim = s.surname.trim()
      val nameOk = nameTrim.isNotEmpty() && namePattern.matches(nameTrim)
      val surnameOk = surnameTrim.isNotEmpty() && namePattern.matches(surnameTrim)

      val emailTrim = s.email.trim()
      val emailOk = run {
        // require exactly one '@', non-empty local and domain, and at least one dot in domain
        val atCount = emailTrim.count { it == '@' }
        if (atCount != 1) return@run false
        val (local, domain) = emailTrim.split("@", limit = 2)
        local.isNotEmpty() && domain.isNotEmpty() && domain.contains('.')
      }

      // Validate password and get requirements
      val passwordReqs = validatePassword(s.password)

      // Check if user is already authenticated (e.g., Google Sign-In)
      val isAuthenticated = authRepository.getCurrentUser() != null
      val passwordOk =
          if (isAuthenticated) {
            // Password not required for already authenticated users
            true
          } else {
            // All password requirements must be met for new sign-ups
            passwordReqs.allMet
          }

      val levelOk = s.levelOfEducation.trim().isNotEmpty()
      val ok = nameOk && surnameOk && emailOk && passwordOk && levelOk
      s.copy(canSubmit = ok, error = null, passwordRequirements = passwordReqs)
    }
  }

  private fun submit() {
    // Early return if form validation fails
    if (!_state.value.canSubmit) {
      return
    }

    viewModelScope.launch {
      _state.update { it.copy(submitting = true, error = null, submitSuccess = false) }
      val current = _state.value

      // Create request object from current state
      val request =
          SignUpRequest(
              name = current.name,
              surname = current.surname,
              email = current.email,
              password = current.password,
              levelOfEducation = current.levelOfEducation,
              description = current.description,
              address = current.address)

      // Execute sign-up through use case
      val result = signUpUseCase.execute(request)

      // Update UI state based on result
      when (result) {
        is SignUpResult.Success -> {
          _state.update { it.copy(submitting = false, submitSuccess = true) }
        }
        is SignUpResult.Error -> {
          _state.update { it.copy(submitting = false, error = result.message) }
        }
      }
    }
  }
}
