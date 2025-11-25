package com.android.sample.ui.newListing

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.HttpClientProvider
import com.android.sample.model.authentication.UserSessionManager
import com.android.sample.model.listing.Listing
import com.android.sample.model.listing.ListingRepository
import com.android.sample.model.listing.ListingRepositoryProvider
import com.android.sample.model.listing.ListingType
import com.android.sample.model.listing.Proposal
import com.android.sample.model.listing.Request
import com.android.sample.model.map.GpsLocationProvider
import com.android.sample.model.map.Location
import com.android.sample.model.map.LocationRepository
import com.android.sample.model.map.NominatimLocationRepository
import com.android.sample.model.skill.MainSubject
import com.android.sample.model.skill.Skill
import com.android.sample.model.skill.SkillsHelper
import java.util.Locale
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
data class ListingUIState(
    val listingId: String? = null,
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
    val invalidLocationMsg: String? = null,
    val addSuccess: Boolean = false,
    val isSaving: Boolean = false
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
 * ViewModel responsible for the NewListingScreen UI logic.
 *
 * Exposes a StateFlow of [ListingUIState] and provides functions to update the state and perform
 * simple validation.
 */
class NewListingViewModel(
    private val listingRepository: ListingRepository = ListingRepositoryProvider.repository,
    private val locationRepository: LocationRepository =
        NominatimLocationRepository(HttpClientProvider.client),
) : ViewModel() {
  // Internal mutable UI state
  private val _uiState = MutableStateFlow(ListingUIState())
  // Public read-only state flow for the UI to observe
  val uiState: StateFlow<ListingUIState> = _uiState.asStateFlow()

  private val userId: String
    get() = UserSessionManager.getCurrentUserId() ?: ""

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

  fun load(listingId: String?) {
    if (listingId == null) {
      _uiState.value = ListingUIState() // Reset state for new listing
      return
    }

    viewModelScope.launch {
      try {
        val listing = listingRepository.getListing(listingId)
        if (listing != null) {
          val subSkillOptions = SkillsHelper.getSkillNames(listing.skill.mainSubject)
          _uiState.update {
            it.copy(
                listingId = listing.listingId,
                title = listing.title,
                description = listing.description,
                price = listing.hourlyRate.toString(),
                subject = listing.skill.mainSubject,
                selectedSubSkill = listing.skill.skill,
                subSkillOptions = subSkillOptions,
                listingType = listing.type,
                selectedLocation = listing.location,
                locationQuery = listing.location.name)
          }
        }
      } catch (e: Exception) {
        Log.e("NewListingViewModel", "Failed to load listing", e)
      }
    }
  }

  /**
   * Attempts to add a new listing based on the current UI state. Validates the input fields and, if
   * valid, creates a new Listing object (either a Proposal or Request) and saves it to the
   * repository.
   */
  fun addListing() {
    val state = _uiState.value

    if (state.isSaving) return

    if (!state.isValid) {
      setError()
      return
    }

    val price = state.price.toDouble()
    val specificSkill = state.selectedSubSkill!!
    val mainSubject = state.subject!!
    val listingType = state.listingType!!
    val selectedLocation = state.selectedLocation!!

    val newSkill = Skill(mainSubject = mainSubject, skill = specificSkill)
    val isEditMode = state.listingId != null

    val listing: Listing =
        when (listingType) {
          ListingType.PROPOSAL ->
              Proposal(
                  listingId = state.listingId ?: listingRepository.getNewUid(),
                  creatorUserId = userId,
                  skill = newSkill,
                  title = state.title,
                  description = state.description,
                  location = selectedLocation,
                  hourlyRate = price)
          ListingType.REQUEST ->
              Request(
                  listingId = state.listingId ?: listingRepository.getNewUid(),
                  creatorUserId = userId,
                  skill = newSkill,
                  title = state.title,
                  description = state.description,
                  location = selectedLocation,
                  hourlyRate = price)
        }

    _uiState.update { it.copy(isSaving = true) }
    viewModelScope.launch {
      try {
        if (isEditMode) {
          listingRepository.updateListing(listing.listingId, listing)
        } else {
          when (listing) {
            is Proposal -> listingRepository.addProposal(listing)
            is Request -> listingRepository.addRequest(listing)
          }
        }
        _uiState.update { it.copy(addSuccess = true) }
      } catch (e: Exception) {
        Log.e("NewListingViewModel", "Error saving listing", e)
      }
    }
  }

  // Set all messages error, if invalid field
  // kotlin
  fun setError() {
    _uiState.update { currentState ->
      val invalidTitle = if (currentState.title.isBlank()) titleMsgError else null
      val invalidDesc = if (currentState.description.isBlank()) descMsgError else null
      val invalidPrice =
          if (currentState.price.isBlank()) priceEmptyMsg
          else if (!isPosNumber(currentState.price)) priceInvalidMsg else null
      val invalidSubject = if (currentState.subject == null) subjectMsgError else null
      val invalidSubSkill = computeInvalidSubSkill(currentState)
      val invalidListingType = if (currentState.listingType == null) listingTypeMsgError else null
      val invalidLocation = if (currentState.selectedLocation == null) locationMsgError else null

      currentState.copy(
          invalidTitleMsg = invalidTitle,
          invalidDescMsg = invalidDesc,
          invalidPriceMsg = invalidPrice,
          invalidSubjectMsg = invalidSubject,
          invalidSubSkillMsg = invalidSubSkill,
          invalidListingTypeMsg = invalidListingType,
          invalidLocationMsg = invalidLocation)
    }
  }

  private fun computeInvalidSubSkill(currentState: ListingUIState): String? {
    return if (currentState.subject != null && currentState.selectedSubSkill.isNullOrBlank()) {
      subSkillMsgError
    } else {
      null
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

  fun clearAddSuccess() {
    _uiState.update { it.copy(addSuccess = false) }
  }

  /**
   * Fetches the current GPS location using the provided [GpsLocationProvider] and updates the UI
   * state with the obtained location.
   *
   * @param provider The [GpsLocationProvider] used to obtain the current GPS location.
   * @param context The [Context] used for geocoding the location into a human-readable address.
   */
  @Suppress("DEPRECATION")
  fun fetchLocationFromGps(provider: GpsLocationProvider, context: Context) {
    viewModelScope.launch {
      try {
        val androidLoc = provider.getCurrentLocation()
        if (androidLoc != null) {
          val geocoder = Geocoder(context, Locale.getDefault())
          val addresses: List<Address> =
              geocoder.getFromLocation(androidLoc.latitude, androidLoc.longitude, 1)?.toList()
                  ?: emptyList()
          val addressText =
              if (addresses.isNotEmpty()) {
                val address = addresses[0]
                listOfNotNull(address.locality, address.adminArea, address.countryName)
                    .joinToString(", ")
              } else {
                "${androidLoc.latitude}, ${androidLoc.longitude}"
              }
          val mapLocation =
              Location(
                  latitude = androidLoc.latitude,
                  longitude = androidLoc.longitude,
                  name = addressText)
          _uiState.update {
            it.copy(
                selectedLocation = mapLocation,
                locationQuery = addressText,
                invalidLocationMsg = null)
          }
        } else {
          _uiState.update { it.copy(invalidLocationMsg = "Failed to obtain GPS location") }
        }
      } catch (_: SecurityException) {
        _uiState.update { it.copy(invalidLocationMsg = "Location permission denied") }
      } catch (_: Exception) {
        _uiState.update { it.copy(invalidLocationMsg = "Failed to obtain GPS location") }
      }
    }
  }
  /** Handles the event when location permission is denied by setting an error message. */
  fun onLocationPermissionDenied() {
    _uiState.update { it.copy(invalidLocationMsg = "Location permission denied") }
  }

  /** Sets the list of location suggestions in the UI state. */
  fun setLocationSuggestions(list: List<Location>) {
    _uiState.update { it.copy(locationSuggestions = list) }
  }

  /** Resets the UI state to start creating a new listing. */
  fun startCreateMode() {
    _uiState.value = ListingUIState()
  }
}
