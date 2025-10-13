package com.android.sample.ui.signup

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

enum class Role {
  LEARNER,
  TUTOR
}

data class SignUpUiState(
    val role: Role = Role.LEARNER,
    val name: String = "",
    val pseudo: String = "",
    val address: String = "",
    val diplomas: String = "",
    val description: String = "",
    val email: String = "",
    val password: String = "",
    val submitting: Boolean = false,
    val error: String? = null,
    val canSubmit: Boolean = false
)

sealed interface SignUpEvent {
  data class RoleChanged(val role: Role) : SignUpEvent

  data class NameChanged(val value: String) : SignUpEvent

  data class PseudoChanged(val value: String) : SignUpEvent

  data class AddressChanged(val value: String) : SignUpEvent

  data class DiplomasChanged(val value: String) : SignUpEvent

  data class DescriptionChanged(val value: String) : SignUpEvent

  data class EmailChanged(val value: String) : SignUpEvent

  data class PasswordChanged(val value: String) : SignUpEvent

  object Submit : SignUpEvent
}

class SignUpViewModel : ViewModel() {
  private val _state = MutableStateFlow(SignUpUiState())
  val state: StateFlow<SignUpUiState> = _state

  fun onEvent(e: SignUpEvent) {
    when (e) {
      is SignUpEvent.RoleChanged -> _state.update { it.copy(role = e.role) }
      is SignUpEvent.NameChanged -> _state.update { it.copy(name = e.value) }
      is SignUpEvent.PseudoChanged -> _state.update { it.copy(pseudo = e.value) }
      is SignUpEvent.AddressChanged -> _state.update { it.copy(address = e.value) }
      is SignUpEvent.DiplomasChanged -> _state.update { it.copy(diplomas = e.value) }
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
              s.pseudo.isNotBlank() &&
              s.email.contains("@") &&
              s.password.length >= 6
      s.copy(canSubmit = ok, error = null)
    }
  }

  // For now: fake submit (no repo yet)
  private fun submit() {
    _state.update { it.copy(submitting = true) }
    // TODO: integrate with your real repository when ready
    _state.update { it.copy(submitting = false) }
  }
}
