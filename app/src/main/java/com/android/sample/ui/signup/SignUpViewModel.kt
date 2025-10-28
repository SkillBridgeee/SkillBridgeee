package com.android.sample.ui.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.model.authentication.AuthenticationRepository
import com.android.sample.model.map.Location
import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepository
import com.android.sample.model.user.ProfileRepositoryProvider
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class Role {
  LEARNER,
  TUTOR
}

data class SignUpUiState(
    val role: Role = Role.LEARNER,
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
    val submitSuccess: Boolean = false
)

sealed interface SignUpEvent {
  data class RoleChanged(val role: Role) : SignUpEvent

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
    private val authRepository: AuthenticationRepository = AuthenticationRepository(),
    private val profileRepository: ProfileRepository = ProfileRepositoryProvider.repository
) : ViewModel() {
  private val _state = MutableStateFlow(SignUpUiState())
  val state: StateFlow<SignUpUiState> = _state

  fun onEvent(e: SignUpEvent) {
    when (e) {
      is SignUpEvent.RoleChanged -> _state.update { it.copy(role = e.role) }
      is SignUpEvent.NameChanged -> _state.update { it.copy(name = e.value) }
      is SignUpEvent.SurnameChanged -> _state.update { it.copy(surname = e.value) }
      is SignUpEvent.AddressChanged -> _state.update { it.copy(address = e.value) }
      is SignUpEvent.LevelOfEducationChanged ->
          _state.update { it.copy(levelOfEducation = e.value) }
      is SignUpEvent.DescriptionChanged -> _state.update { it.copy(description = e.value) }
      is SignUpEvent.EmailChanged -> _state.update { it.copy(email = e.value) }
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

      val password = s.password
      val passwordOk =
          password.length >= 8 && password.any { it.isDigit() } && password.any { it.isLetter() }
      val levelOk = s.levelOfEducation.trim().isNotEmpty()
      val ok = nameOk && surnameOk && emailOk && passwordOk && levelOk
      s.copy(canSubmit = ok, error = null)
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
      try {
        // Step 1: Create Firebase Authentication account
        val authResult = authRepository.signUpWithEmail(current.email.trim(), current.password)

        authResult.fold(
            onSuccess = { firebaseUser ->
              // Step 2: Create user profile in Firestore using the Firebase Auth UID
              try {
                val fullName =
                    listOf(current.name.trim(), current.surname.trim())
                        .filter { it.isNotEmpty() }
                        .joinToString(" ")

                val profile =
                    Profile(
                        userId = firebaseUser.uid, // Use Firebase Auth UID
                        name = fullName,
                        email = current.email.trim(),
                        levelOfEducation = current.levelOfEducation.trim(),
                        description = current.description.trim(),
                        location = buildLocation(current.address))

                profileRepository.addProfile(profile)
                _state.update { it.copy(submitting = false, submitSuccess = true) }
              } catch (e: Exception) {
                // Profile creation failed after auth success.
                // Note: The Firebase Auth user remains created. Consider calling
                // firebaseUser.delete() to roll back, but that requires handling
                // re-authentication complexity. For now, we leave the auth user and show error.
                _state.update {
                  it.copy(
                      submitting = false,
                      error = "Account created but profile failed: ${e.message}")
                }
              }
            },
            onFailure = { exception ->
              // Firebase Auth account creation failed - use error codes for better detection
              val errorMessage =
                  if (exception is FirebaseAuthException) {
                    when (exception.errorCode) {
                      "ERROR_EMAIL_ALREADY_IN_USE" -> "This email is already registered"
                      "ERROR_INVALID_EMAIL" -> "Invalid email format"
                      "ERROR_WEAK_PASSWORD" -> "Password is too weak"
                      else -> exception.message ?: "Sign up failed"
                    }
                  } else {
                    exception.message ?: "Sign up failed"
                  }
              _state.update { it.copy(submitting = false, error = errorMessage) }
            })
      } catch (t: Throwable) {
        _state.update { it.copy(submitting = false, error = t.message ?: "Unknown error") }
      }
    }
  }

  // Store the entered address into Location.name. Replace with geocoding later if needed.
  private fun buildLocation(address: String): Location {
    return Location(name = address.trim())
  }
}
