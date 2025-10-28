package com.android.sample.ui.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.HttpClientProvider
import com.android.sample.model.map.Location
import com.android.sample.model.map.LocationRepository
import com.android.sample.model.map.NominatimLocationRepository
import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepository
import com.android.sample.model.user.ProfileRepositoryProvider
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** UI state for the MyProfile screen. Holds all data needed to edit a profile */
data class MyProfileUIState(
    val name: String? = "",
    val email: String? = "",
    val selectedLocation: Location? = null,
    val locationQuery: String = "",
    val locationSuggestions: List<Location> = emptyList(),
    val description: String? = "",
    val invalidNameMsg: String? = null,
    val invalidEmailMsg: String? = null,
    val invalidLocationMsg: String? = null,
    val invalidDescMsg: String? = null,
) {
  // Checks if all fields are valid
  val isValid: Boolean
    get() =
        invalidNameMsg == null &&
            invalidEmailMsg == null &&
            invalidLocationMsg == null &&
            invalidDescMsg == null &&
            name?.isNotBlank() == true &&
            email?.isNotBlank() == true &&
            selectedLocation != null &&
            description?.isNotBlank() == true
}

// ViewModel to manage profile editing logic and state
class MyProfileViewModel(
    private val repository: ProfileRepository = ProfileRepositoryProvider.repository,
    private val locationRepository: LocationRepository =
        NominatimLocationRepository(HttpClientProvider.client)
) : ViewModel() {
  // Holds the current UI state
  private val _uiState = MutableStateFlow(MyProfileUIState())
  val uiState: StateFlow<MyProfileUIState> = _uiState.asStateFlow()

  private val nameMsgError = "Name cannot be empty"
  private val emailEmptyMsgError = "Email cannot be empty"
  private val emailInvalidMsgError = "Email is not in the right format"
  private val locationMsgError = "Location cannot be empty"
  private val descMsgError = "Description cannot be empty"

  /** Loads the profile data (to be implemented) */
  fun loadProfile(userId: String) {
    try {
      viewModelScope.launch {
        val profile = repository.getProfile(userId = userId)
        _uiState.value =
            MyProfileUIState(
                name = profile?.name,
                email = profile?.email,
                selectedLocation = profile?.location,
                description = profile?.description)
      }
    } catch (e: Exception) {
      Log.e("MyProfileViewModel", "Error loading ToDo by ID: $userId", e)
    }
  }

  /**
   * Edits a Profile.
   *
   * @param userId The ID of the profile to edit.
   * @return true if the update process was started, false if validation failed.
   */
  fun editProfile() {
    val state = _uiState.value
    if (!state.isValid) {
      setError()
      return
    }
    val currentId = Firebase.auth.currentUser?.uid ?: ""
    val profile =
        Profile(
            userId = currentId,
            name = state.name ?: "",
            email = state.email ?: "",
            location = state.selectedLocation!!,
            description = state.description ?: "")

    editProfileToRepository(userId = currentId, profile = profile)
  }

  /**
   * Edits a Profile in the repository.
   *
   * @param userId The ID of the profile to be edited.
   * @param profile The Profile object containing the new values.
   */
  private fun editProfileToRepository(userId: String, profile: Profile) {
    viewModelScope.launch {
      try {
        repository.updateProfile(userId = userId, profile = profile)
      } catch (e: Exception) {
        Log.e("MyProfileViewModel", "Error updating Profile", e)
      }
    }
  }

  // Set all messages error, if invalid field
  fun setError() {
    _uiState.update { currentState ->
      currentState.copy(
          invalidNameMsg = currentState.name?.let { if (it.isBlank()) nameMsgError else null },
          invalidEmailMsg = validateEmail(currentState.email ?: ""),
          invalidLocationMsg =
              if (currentState.selectedLocation == null) locationMsgError else null,
          invalidDescMsg =
              currentState.description?.let { if (it.isBlank()) descMsgError else null })
    }
  }

  // Updates the name and validates it
  fun setName(name: String) {
    _uiState.value =
        _uiState.value.copy(
            name = name, invalidNameMsg = if (name.isBlank()) nameMsgError else null)
  }

  // Updates the email and validates it
  fun setEmail(email: String) {
    _uiState.value = _uiState.value.copy(email = email, invalidEmailMsg = validateEmail(email))
  }

  // Updates the desc and validates it
  fun setDescription(desc: String) {
    _uiState.value =
        _uiState.value.copy(
            description = desc, invalidDescMsg = if (desc.isBlank()) descMsgError else null)
  }

  // Checks if the email format is valid
  private fun isValidEmail(email: String): Boolean {
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    return email.matches(emailRegex.toRegex())
  }

  // Return the good error message corresponding of the given input
  private fun validateEmail(email: String): String? {
    return when {
      email.isBlank() -> emailEmptyMsgError
      !isValidEmail(email) -> emailInvalidMsgError
      else -> null
    }
  }

  fun setLocation(location: Location) {
    _uiState.value = _uiState.value.copy(selectedLocation = location, locationQuery = location.name)
  }

  fun setLocationQuery(query: String) {
    _uiState.value = _uiState.value.copy(locationQuery = query)

    if (query.isNotEmpty()) {
      viewModelScope.launch {
        try {
          val results = locationRepository.search(query)
          _uiState.value =
              _uiState.value.copy(locationSuggestions = results, invalidLocationMsg = null)
        } catch (_: Exception) {
          _uiState.value = _uiState.value.copy(locationSuggestions = emptyList())
        }
      }
    } else {
      _uiState.value =
          _uiState.value.copy(
              locationSuggestions = emptyList(), invalidLocationMsg = locationMsgError)
    }
  }
}
