package com.android.sample.ui.screens.newSkill

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch



/** UI state for the MyProfile screen. This state holds the data needed to edit a profile */
data class SkillUIState(
    val ownerId: String = "John Doe",
    val title: String = "",
    val description: String = "",
    val price: String = "",

    val errorMsg: String? = null,
    val invalidTitleMsg: String? = null,
    val invalidDescMsg: String? = null,
    val invalidPriceMsg: String? = null,
) {
    val isValid: Boolean
        get() =
            invalidTitleMsg == null &&
                    invalidDescMsg == null &&
                    invalidPriceMsg == null &&
                    title.isNotEmpty() &&
                    description.isNotEmpty()
}

class NewSkillOverviewModel() : ViewModel() {
    // Profile UI state
    private val _uiState = MutableStateFlow(SkillUIState())
    val uiState: StateFlow<SkillUIState> = _uiState.asStateFlow()

    /** Clears the error message in the UI state. */
    fun clearErrorMsg() {
        _uiState.value = _uiState.value.copy(errorMsg = null)
    }

    /** Sets an error message in the UI state. */
    private fun setErrorMsg(errorMsg: String) {
        _uiState.value = _uiState.value.copy(errorMsg = errorMsg)
    }


    fun loadSkill() {
        viewModelScope.launch {
            try {

            } catch (_: Exception) {

            }
        }
    }


    fun addSkill() : Boolean {return true}

    private fun addSkillRepository() {}

    // Functions to update the UI state.
    fun setTitle(title: String) {
        _uiState.value =
            _uiState.value.copy(
                title = title, invalidTitleMsg = if (title.isBlank()) "Title cannot be empty" else null)
    }

    fun setDesc(description: String) {
        _uiState.value =
            _uiState.value.copy(
                description = description,
                invalidDescMsg =
                    if (description.isBlank())
                        "Description cannot be empty"
                    else null
            )
    }


    fun setPrice(price: String) {
        _uiState.value =
            _uiState.value.copy(
                price = price,
                invalidPriceMsg =
                    if (price.isBlank()) "Price cannot be empty" else null)
    }

}
