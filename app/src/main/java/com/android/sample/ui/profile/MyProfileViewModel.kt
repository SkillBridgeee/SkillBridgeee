package com.android.sample.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** UI state for the MyProfile screen. Holds all data needed to edit a profile */
data class MyProfileUIState(
    val name: String = "John Doe",
    val email: String = "john.doe@epfl.ch",
    val location: String = "EPFL",
    val bio: String = "Very nice guy :)",
    val errorMsg: String? = null,
    val invalidNameMsg: String? = null,
    val invalidEmailMsg: String? = null,
    val invalidLocationMsg: String? = null,
    val invalidBioMsg: String? = null,
) {
    val isValid: Boolean
        get() =
            invalidNameMsg == null &&
                    invalidEmailMsg == null &&
                    invalidLocationMsg == null &&
                    invalidBioMsg == null &&
                    name.isNotEmpty() &&
                    email.isNotEmpty() &&
                    location.isNotEmpty() &&
                    bio.isNotEmpty()
}

class MyProfileViewModel() : ViewModel() {
    private val _uiState = MutableStateFlow(MyProfileUIState())
    val uiState: StateFlow<MyProfileUIState> = _uiState.asStateFlow()

    /** Removes any error message from the UI state */
    fun clearErrorMsg() {
        _uiState.value = _uiState.value.copy(errorMsg = null)
    }

    /** Loads the profile data (to be implemented) */
    fun loadProfile() {
        viewModelScope.launch {
            try {
                // TODO: Load profile data here
            } catch (_: Exception) {
                // TODO: Handle error
            }
        }
    }


    // Updates the name and validates it
    fun setName(name: String) {
        _uiState.value =
            _uiState.value.copy(
                name = name,
                invalidNameMsg =
                    if (name.isBlank()) "Name cannot be empty" else null)
    }

    // Updates the email and validates it
    fun setEmail(email: String) {
        _uiState.value =
            _uiState.value.copy(
                email = email,
                invalidEmailMsg =
                    if (email.isBlank()) "Email cannot be empty" else null)
    }

    // Updates the location and validates it
    fun setLocation(location: String) {
        _uiState.value =
            _uiState.value.copy(
                location = location,
                invalidLocationMsg =
                    if (location.isBlank()) "Location cannot be empty" else null)
    }

    // Updates the bio and validates it
    fun setBio(bio: String) {
        _uiState.value =
            _uiState.value.copy(
                bio = bio,
                invalidBioMsg =
                    if (bio.isBlank()) "Bio cannot be empty" else null)
    }

}
