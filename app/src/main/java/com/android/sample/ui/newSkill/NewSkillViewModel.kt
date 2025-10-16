package com.android.sample.ui.screens.newSkill

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.model.listing.ListingRepository
import com.android.sample.model.listing.ListingRepositoryProvider
import com.android.sample.model.listing.Proposal
import com.android.sample.model.skill.MainSubject
import com.android.sample.model.skill.Skill
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
    val title: String = "",
    val description: String = "",
    val price: String = "",
    val subject: MainSubject? = null,
    val invalidTitleMsg: String? = null,
    val invalidDescMsg: String? = null,
    val invalidPriceMsg: String? = null,
    val invalidSubjectMsg: String? = null,
) {

  /** Indicates whether the current UI state is valid for submission. */
  val isValid: Boolean
    get() =
        invalidTitleMsg == null &&
            invalidDescMsg == null &&
            invalidPriceMsg == null &&
            invalidSubjectMsg == null &&
            title.isNotBlank() &&
            description.isNotBlank() &&
            price.isNotBlank() &&
            subject != null
}

/**
 * ViewModel responsible for the NewSkillScreen UI logic.
 *
 * Exposes a StateFlow of [SkillUIState] and provides functions to update the state and perform
 * simple validation.
 */
class NewSkillViewModel(
    private val listingRepository: ListingRepository = ListingRepositoryProvider.repository
) : ViewModel() {
  // Internal mutable UI state
  private val _uiState = MutableStateFlow(SkillUIState())
  // Public read-only state flow for the UI to observe
  val uiState: StateFlow<SkillUIState> = _uiState.asStateFlow()

  private val titleMsgError = "Title cannot be empty"
  private val descMsgError = "Description cannot be empty"
  private val priceEmptyMsg = "Price cannot be empty"
  private val priceInvalidMsg = "Price must be a positive number"
  private val subjectMsgError = "You must choose a subject"

  /**
   * Placeholder to load an existing skill.
   *
   * Kept as a coroutine scope for future asynchronous loading.
   */
  fun load() {}

  fun addProfile(userId: String) {
    val state = _uiState.value
    if (state.isValid) {
      val newSkill =
          Skill(
              userId = userId,
              mainSubject = state.subject!!,
              skill = state.title,
          )

      val newProposal =
          Proposal(
              listingId = listingRepository.getNewUid(),
              creatorUserId = userId,
              skill = newSkill,
              description = state.description)

      addSkillToRepository(proposal = newProposal)
    } else {
      setError()
    }
  }

  private fun addSkillToRepository(proposal: Proposal) {
    viewModelScope.launch {
      try {
        listingRepository.addProposal(proposal)
      } catch (e: Exception) {
        Log.e("NewSkillViewModel", "Error adding NewSkill", e)
      }
    }
  }

  // Set all messages error, if invalid field
  fun setError() {
    _uiState.update { currentState ->
      currentState.copy(
          invalidTitleMsg = if (currentState.title.isBlank()) titleMsgError else null,
          invalidDescMsg = if (currentState.description.isBlank()) descMsgError else null,
          invalidPriceMsg =
              if (currentState.price.isBlank()) priceEmptyMsg
              else if (!isPosNumber(currentState.price)) priceInvalidMsg else null,
          invalidSubjectMsg = if (currentState.subject == null) subjectMsgError else null)
    }
  }

  // --- State update helpers used by the UI ---

  /** Update the title and validate presence. If the title is blank, sets `invalidTitleMsg`. */
  fun setTitle(title: String) {
    _uiState.value =
        _uiState.value.copy(
            title = title, invalidTitleMsg = if (title.isBlank()) titleMsgError else null)
  }

  /**
   * Update the description and validate presence. If the description is blank, sets
   * `invalidDescMsg`.
   */
  fun setDescription(description: String) {
    _uiState.value =
        _uiState.value.copy(
            description = description,
            invalidDescMsg = if (description.isBlank()) descMsgError else null)
  }

  /**
   * Update the price and validate format.
   *
   * Rules:
   * - empty -> "Price cannot be empty"
   * - non positive number or non-numeric -> "Price must be a positive number or null (0.0)"
   */
  fun setPrice(price: String) {
    _uiState.value =
        _uiState.value.copy(
            price = price,
            invalidPriceMsg =
                if (price.isBlank()) priceEmptyMsg
                else if (!isPosNumber(price)) priceInvalidMsg else null)
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
