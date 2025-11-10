package com.android.sample.ui.screens.newSkill

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.HttpClientProvider
import com.android.sample.model.listing.ListingRepository
import com.android.sample.model.listing.ListingRepositoryProvider
import com.android.sample.model.listing.ListingType
import com.android.sample.model.listing.Proposal
import com.android.sample.model.listing.Request
import com.android.sample.model.map.Location
import com.android.sample.model.map.LocationRepository
import com.android.sample.model.map.NominatimLocationRepository
import com.android.sample.model.skill.MainSubject
import com.android.sample.model.skill.Skill
import com.android.sample.model.skill.SkillsHelper
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
 * - listingType: whether this is a proposal (offer) or request (seeking)
 * - errorMsg: global error (e.g. network)
 * - invalid*Msg: per-field validation messages
 */
data class SkillUIState(
    val title: String = "",
    val description: String = "",
    val price: String = "",
    val subject: MainSubject? = null,
    val selectedSubSkill: String? = null,
    val subSkillOptions: List<String> = emptyList(),
    val listingType: ListingType? = null,
    val selectedLocation: Location? = null,
    val locationQuery: String = "",
    val locationSuggestions: List<Location> = emptyList(),
    val invalidTitleMsg: String? = null,
    val invalidDescMsg: String? = null,
    val invalidPriceMsg: String? = null,
    val invalidSubjectMsg: String? = null,
    val invalidSubSkillMsg: String? = null,
    val invalidListingTypeMsg: String? = null,
    val invalidLocationMsg: String? = null
) {

  /** Indicates whether the current UI state is valid for submission. */
  val isValid: Boolean
    get() =
        invalidTitleMsg == null &&
            invalidDescMsg == null &&
            invalidPriceMsg == null &&
            invalidSubjectMsg == null &&
            invalidSubSkillMsg == null &&
            invalidListingTypeMsg == null &&
            invalidLocationMsg == null &&
            title.isNotBlank() &&
            description.isNotBlank() &&
            price.isNotBlank() &&
            subject != null &&
            listingType != null &&
            selectedSubSkill?.isNotBlank() == true &&
            selectedLocation != null
}

/**
 * ViewModel responsible for the NewSkillScreen UI logic.
 *
 * Exposes a StateFlow of [SkillUIState] and provides functions to update the state and perform
 * simple validation.
 */
class NewSkillViewModel(
    private val listingRepository: ListingRepository = ListingRepositoryProvider.repository,
    private val locationRepository: LocationRepository =
        NominatimLocationRepository(HttpClientProvider.client),
    private val userId: String = Firebase.auth.currentUser?.uid ?: ""
) : ViewModel() {
  // Internal mutable UI state
  private val _uiState = MutableStateFlow(SkillUIState())
  // Public read-only state flow for the UI to observe
  val uiState: StateFlow<SkillUIState> = _uiState.asStateFlow()

  private var locationSearchJob: Job? = null
  private val locationSearchDelayTime: Long = 1000

  private val titleMsgError = "Title cannot be empty"
  private val descMsgError = "Description cannot be empty"
  private val priceEmptyMsg = "Price cannot be empty"
  private val priceInvalidMsg = "Price must be a positive number"
  private val subjectMsgError = "You must choose a subject"
  private val listingTypeMsgError = "You must choose a listing type"
  private val subSkillMsgError = "You must choose a sub-subject"
  private val locationMsgError = "You must choose a location"

  /**
   * Placeholder to load an existing skill.
   *
   * Kept as a coroutine scope for future asynchronous loading.
   */
  fun load() {}

  fun addListing() {
    val state = _uiState.value
    if (state.isValid) {
      val price = state.price.toDouble()
      val specificSkill = state.selectedSubSkill!!
      val newSkill =
          Skill(
              mainSubject = state.subject!!,
              skill = specificSkill,
          )

      when (state.listingType!!) {
        ListingType.PROPOSAL -> {
          val newProposal =
              Proposal(
                  listingId = listingRepository.getNewUid(),
                  creatorUserId = userId,
                  skill = newSkill,
                  description = state.description,
                  location = state.selectedLocation!!,
                  hourlyRate = price)
          addProposalToRepository(proposal = newProposal)
        }
        ListingType.REQUEST -> {
          val newRequest =
              Request(
                  listingId = listingRepository.getNewUid(),
                  creatorUserId = userId,
                  skill = newSkill,
                  description = state.description,
                  location = state.selectedLocation!!,
                  hourlyRate = price)
          addRequestToRepository(request = newRequest)
        }
      }
    } else {
      setError()
    }
  }

  private fun addProposalToRepository(proposal: Proposal) {
    viewModelScope.launch {
      try {
        listingRepository.addProposal(proposal)
      } catch (e: Exception) {
        Log.e("NewSkillViewModel", "Error adding Proposal", e)
      }
    }
  }

  private fun addRequestToRepository(request: Request) {
    viewModelScope.launch {
      try {
        listingRepository.addRequest(request)
      } catch (e: Exception) {
        Log.e("NewSkillViewModel", "Error adding Request", e)
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
          invalidSubjectMsg = if (currentState.subject == null) subjectMsgError else null,
          // Set sub-skill error only when a subject is selected but no sub-skill chosen
          invalidSubSkillMsg =
              if (currentState.subject != null && currentState.selectedSubSkill.isNullOrBlank())
                  subSkillMsgError
              else null,
          invalidListingTypeMsg =
              if (currentState.listingType == null) listingTypeMsgError else null,
          invalidLocationMsg =
              if (currentState.selectedLocation == null) locationMsgError else null)
    }
  }

  // --- State update helpers used by the UI ---

  /** Update the title and validate presence. If the title is blank, sets `invalidTitleMsg`. */
  fun setTitle(title: String) {
    _uiState.update { currentState ->
      currentState.copy(
          title = title, invalidTitleMsg = if (title.isBlank()) titleMsgError else null)
    }
  }

  /**
   * Update the description and validate presence. If the description is blank, sets
   * `invalidDescMsg`.
   */
  fun setDescription(description: String) {
    _uiState.update { currentState ->
      currentState.copy(
          description = description,
          invalidDescMsg = if (description.isBlank()) descMsgError else null)
    }
  }

  /**
   * Update the price and validate format.
   *
   * Rules:
   * - empty -> "Price cannot be empty"
   * - non positive number or non-numeric -> "Price must be a positive number or null (0.0)"
   */
  fun setPrice(price: String) {
    _uiState.update { currentState ->
      currentState.copy(
          price = price,
          invalidPriceMsg =
              if (price.isBlank()) priceEmptyMsg
              else if (!isPosNumber(price)) priceInvalidMsg else null)
    }
  }

  /** Update the selected main subject. */
  fun setSubject(sub: MainSubject) {
    val options = SkillsHelper.getSkillNames(sub)
    _uiState.value =
        _uiState.value.copy(
            subject = sub,
            subSkillOptions = options,
            selectedSubSkill = null,
            invalidSubjectMsg = null,
            invalidSubSkillMsg = null)
  }

  /** Update the selected listing type (PROPOSAL or REQUEST). */
  fun setListingType(type: ListingType) {
    _uiState.update { currentState ->
      currentState.copy(listingType = type, invalidListingTypeMsg = null)
    }
  }

  /** Set a chosen sub-skill string. */
  fun setSubSkill(subSkill: String) {
    _uiState.value = _uiState.value.copy(selectedSubSkill = subSkill, invalidSubSkillMsg = null)
  }

  // Update the selected location and the locationQuery
  fun setLocation(location: Location) {
    _uiState.update { currentState ->
      currentState.copy(selectedLocation = location, locationQuery = location.name)
    }
  }

  /**
   * Updates the location query in the UI state and fetches matching location suggestions.
   *
   * This function updates the current `locationQuery` value and triggers a search operation if the
   * query is not empty. The search is performed asynchronously within the `viewModelScope` using
   * the [locationRepository].
   *
   * @param query The new location search query entered by the user.
   * @see locationRepository
   * @see viewModelScope
   */
  fun setLocationQuery(query: String) {
    _uiState.update { it.copy(locationQuery = query) }

    locationSearchJob?.cancel()

    if (query.isNotBlank()) {
      locationSearchJob =
          viewModelScope.launch {
            delay(locationSearchDelayTime)
            try {
              val results = locationRepository.search(query)
              _uiState.update { it.copy(locationSuggestions = results, invalidLocationMsg = null) }
            } catch (_: Exception) {
              _uiState.update { it.copy(locationSuggestions = emptyList()) }
            }
          }
    } else {
      _uiState.update {
        it.copy(
            locationSuggestions = emptyList(),
            invalidLocationMsg = locationMsgError,
            selectedLocation = null)
      }
    }
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
