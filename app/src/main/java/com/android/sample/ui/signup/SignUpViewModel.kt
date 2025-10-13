package com.android.sample.ui.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.model.user.FakeProfileRepository
import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepository
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

class SignUpViewModel(private val repo: ProfileRepository = FakeProfileRepository()) : ViewModel() {
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
    _state.update { s ->
      val ok =
          s.name.isNotBlank() &&
              s.surname.isNotBlank() &&
              s.email.contains("@") &&
              s.password.length >= 6
      s.copy(canSubmit = ok, error = null)
    }
  }

  private fun submit() {
    viewModelScope.launch {
      _state.update { it.copy(submitting = true, error = null, submitSuccess = false) }
      val current = _state.value
      try {
        val newUid = repo.getNewUid()
        val fullName =
            listOf(current.name.trim(), current.surname.trim())
                .filter { it.isNotEmpty() }
                .joinToString(" ")
        val profile =
            Profile(
                userId = newUid,
                name = fullName,
                email = current.email,
                levelOfEducation = current.levelOfEducation,
                description = current.description)
        repo.addProfile(profile)
        _state.update { it.copy(submitting = false, submitSuccess = true) }
      } catch (t: Throwable) {
        _state.update { it.copy(submitting = false, error = t.message ?: "Unknown error") }
      }
    }
  }
}
