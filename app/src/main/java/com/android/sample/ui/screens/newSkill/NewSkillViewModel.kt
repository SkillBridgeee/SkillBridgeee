package com.android.sample.ui.screens.newSkill

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.model.skill.MainSubject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI state for the New Skill screen.
 *
 * Holds all data required to render and validate the new skill form:
 * - ownerId: identifier of the skill owner
 * - title, description, price: input fields
 * - subject: selected main subject
 * - errorMsg: global error (e.g. network)
 * - invalid*Msg: per-field validation messages
 */
data class SkillUIState(
    val ownerId: String = "John Doe",
    val title: String = "",
    val description: String = "",
    val price: String = "",
    val subject: MainSubject? = null,
    val errorMsg: String? = null,
    val invalidTitleMsg: String? = null,
    val invalidDescMsg: String? = null,
    val invalidPriceMsg: String? = null,
) {

  /** Indicates whether the current UI state is valid for submission. */
  val isValid: Boolean
    get() =
        invalidTitleMsg == null &&
            invalidDescMsg == null &&
            invalidPriceMsg == null &&
            title.isNotEmpty() &&
            description.isNotEmpty()
}

/**
 * ViewModel responsible for the NewSkillScreen UI logic.
 *
 * Exposes a StateFlow of [SkillUIState] and provides functions to update the state and perform
 * simple validation.
 */
class NewSkillViewModel() : ViewModel() {
  // Internal mutable UI state
  private val _uiState = MutableStateFlow(SkillUIState())
  // Public read-only state flow for the UI to observe
  val uiState: StateFlow<SkillUIState> = _uiState.asStateFlow()

  /** Clears the error message in the UI state. */
  fun clearErrorMsg() {
    _uiState.value = _uiState.value.copy(errorMsg = null)
  }

  /**
   * Placeholder to load an existing skill.
   *
   * Kept as a coroutine scope for future asynchronous loading.
   */
  fun loadSkill() {
    viewModelScope.launch { try {} catch (_: Exception) {} }
  }

  // --- State update helpers used by the UI ---

  /** Update the title and validate presence. If the title is blank, sets `invalidTitleMsg`. */
  fun setTitle(title: String) {
    _uiState.value =
        _uiState.value.copy(
            title = title, invalidTitleMsg = if (title.isBlank()) "Title cannot be empty" else null)
  }

  /**
   * Update the description and validate presence. If the description is blank, sets
   * `invalidDescMsg`.
   */
  fun setDesc(description: String) {
    _uiState.value =
        _uiState.value.copy(
            description = description,
            invalidDescMsg = if (description.isBlank()) "Description cannot be empty" else null)
  }

  /**
   * Update the price and validate format.
   *
   * Rules:
   * - empty -> "Price cannot be empty"
   * - non positive number or non-numeric -> "Price must be a positive number"
   */
  fun setPrice(price: String) {
    _uiState.value =
        _uiState.value.copy(
            price = price,
            invalidPriceMsg =
                if (price.isBlank()) "Price cannot be empty"
                else if (!isPosNumber(price)) "Price must be a positive number" else null)
  }

  /** Update the selected main subject. */
  fun setSubject(sub: MainSubject) {
    _uiState.value = _uiState.value.copy(subject = sub)
  }

  /** Returns true if the given string represents a non-negative number. */
  private fun isPosNumber(num: String): Boolean {
    return try {
      val res = num.toDouble()
      !res.isNaN() && (res >= 0.0)
    } catch (_: Exception) {
      false
    }
  }
}
