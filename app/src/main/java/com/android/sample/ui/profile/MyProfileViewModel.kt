package com.android.sample.ui.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.model.map.Location
import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepository
import com.android.sample.model.user.ProfileRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** UI state for the MyProfile screen. Holds all data needed to edit a profile */
data class MyProfileUIState(
    val name: String = "",
    val email: String = "",
    val location: Location? = Location(name = ""),
    val description: String = "",
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
            name.isNotBlank() &&
            email.isNotBlank() &&
            location != null &&
            description.isNotBlank()
}

// ViewModel to manage profile editing logic and state
class MyProfileViewModel(
    private val repository: ProfileRepository = ProfileRepositoryProvider.repository
) : ViewModel() {
  // Holds the current UI state
  private val _uiState = MutableStateFlow(MyProfileUIState())
  val uiState: StateFlow<MyProfileUIState> = _uiState.asStateFlow()

  /** Loads the profile data (to be implemented) */
  fun loadProfile(userId: String) {
    viewModelScope.launch {
      try {
        viewModelScope.launch {
          val profile = repository.getProfile(userId = userId)
          _uiState.value =
              MyProfileUIState(
                  name = profile.name,
                  email = profile.email,
                  location = profile.location,
                  description = profile.description)
        }
      } catch (e: Exception) {
        Log.e("MyProfileViewModel", "Error loading ToDo by ID: $userId", e)
      }
    }
  }

  /**
   * Edits a Profile.
   *
   * @param userId The ID of the profile to edit.
   * @return true if the update process was started, false if validation failed.
   */
  fun editProfile(userId: String): Boolean {
    val state = _uiState.value
    if (!state.isValid) {
      return false
    }

    val profile =
        Profile(
            userId = userId,
            name = state.name,
            email = state.email,
            location = state.location ?: Location(name = ""),
            description = state.description)

    editProfileToRepository(userId = userId, profile = profile)
    return true
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

  // Updates the name and validates it
  fun setName(name: String) {
    _uiState.value =
        _uiState.value.copy(
            name = name, invalidNameMsg = if (name.isBlank()) "Name cannot be empty" else null)
  }

  // Updates the email and validates it
  fun setEmail(email: String) {
    _uiState.value =
        _uiState.value.copy(
            email = email,
            invalidEmailMsg =
                if (email.isBlank()) "Email cannot be empty"
                else if (!isValidEmail(email)) "Email is not in the right format" else null)
  }

  // Updates the location and validates it
  fun setLocation(locationName: String) {
    _uiState.value =
        _uiState.value.copy(
            location = if (locationName.isBlank()) null else Location(name = locationName),
            invalidLocationMsg = if (locationName.isBlank()) "Location cannot be empty" else null)
  }

  // Updates the desc and validates it
  fun setDescription(desc: String) {
    _uiState.value =
        _uiState.value.copy(
            description = desc,
            invalidDescMsg = if (desc.isBlank()) "Description cannot be empty" else null)
  }

  // Checks if the email format is valid
  private fun isValidEmail(email: String): Boolean {
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    return email.matches(emailRegex.toRegex())
  }
}
