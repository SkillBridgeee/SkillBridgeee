package com.android.sample.ui.signup

import android.util.Log
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
    val isGoogleSignUp: Boolean = false // True if user is already authenticated via Google
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
    private val profileRepository: ProfileRepository = ProfileRepositoryProvider.repository
) : ViewModel() {

  companion object {
    private const val TAG = "SignUpViewModel"
  }

  private val _state = MutableStateFlow(SignUpUiState())
  val state: StateFlow<SignUpUiState> = _state

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

      val password = s.password
      // Check if user is already authenticated (e.g., Google Sign-In)
      val isAuthenticated = authRepository.getCurrentUser() != null
      val passwordOk =
          if (isAuthenticated) {
            // Password not required for already authenticated users
            true
          } else {
            // Password required for new sign-ups
            password.length >= 8 && password.any { it.isDigit() } && password.any { it.isLetter() }
          }

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
        // Check if user is already authenticated (e.g., via Google Sign-In)
        val currentUser = authRepository.getCurrentUser()

        if (currentUser != null) {
          // User is already authenticated (Google Sign-In), just create profile
          try {
            val fullName =
                listOf(current.name.trim(), current.surname.trim())
                    .filter { it.isNotEmpty() }
                    .joinToString(" ")

            val profile =
                Profile(
                    userId = currentUser.uid,
                    name = fullName,
                    email = current.email.trim(),
                    levelOfEducation = current.levelOfEducation.trim(),
                    description = current.description.trim(),
                    location = buildLocation(current.address))

            profileRepository.addProfile(profile)
            _state.update { it.copy(submitting = false, submitSuccess = true) }
          } catch (e: Exception) {
            _state.update {
              it.copy(submitting = false, error = "Profile creation failed: ${e.message}")
            }
          }
        } else {
          // User is not authenticated, create Firebase Auth account first
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
        }
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
