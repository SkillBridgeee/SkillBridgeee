package com.android.sample.ui.subject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.model.authentication.UserSessionManager
import com.android.sample.model.booking.Booking
import com.android.sample.model.booking.BookingRepository
import com.android.sample.model.booking.BookingRepositoryProvider
import com.android.sample.model.booking.BookingStatus
import com.android.sample.model.listing.Listing
import com.android.sample.model.listing.ListingRepository
import com.android.sample.model.listing.ListingRepositoryProvider
import com.android.sample.model.rating.RatingInfo
import com.android.sample.model.skill.MainSubject
import com.android.sample.model.skill.SkillsHelper
import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepository
import com.android.sample.model.user.ProfileRepositoryProvider
import java.util.Date
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

/**
 * UI state for the Subject List screen
 *
 * @param mainSubject The subject to filter on
 * @param query The search query
 * @param selectedSkill The skill to filter on
 * @param skillsForSubject The list of skills for the current subject
 * @param allListings All listings fetched from the repository
 * @param listings The filtered listings to display
 * @param isLoading Whether the data is currently loading
 * @param error Any error message to display
 */
data class SubjectListUiState(
    val mainSubject: MainSubject = MainSubject.MUSIC,
    val query: String = "",
    val selectedSkill: String? = null,
    val skillsForSubject: List<String> = SkillsHelper.getSkillNames(MainSubject.MUSIC),
    val allListings: List<ListingUiModel> = emptyList(),
    val listings: List<ListingUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * Ui model that combines a listing with its creator’s profile and rating information into a single
 * object for easy display in the interface.
 *
 * @param listing The listing being offered
 * @param creator The profile of the listing's creator
 * @param creatorRating The rating information of the listing's creator
 */
data class ListingUiModel(
    val listing: Listing,
    val creator: Profile?,
    val creatorRating: RatingInfo
)

/**
 * ViewModel for the Subject List screen
 *
 * @param listingRepo Repository for listings
 * @param profileRepo Repository for profiles
 */
class SubjectListViewModel(
    private val listingRepo: ListingRepository = ListingRepositoryProvider.repository,
    private val profileRepo: ProfileRepository = ProfileRepositoryProvider.repository,
    private val bookingRepo: BookingRepository = BookingRepositoryProvider.repository
) : ViewModel() {
  private val _ui = MutableStateFlow(SubjectListUiState())
  val ui: StateFlow<SubjectListUiState> = _ui

  private var loadJob: Job? = null

  /**
   * Refresh listings filtered on selected subject
   *
   * @param subject The subject to filter on
   */
  fun refresh(subject: MainSubject?) {
    loadJob?.cancel()
    loadJob =
        viewModelScope.launch {
          _ui.update {
            it.copy(
                isLoading = true,
                error = null,
                mainSubject = subject ?: it.mainSubject,
                selectedSkill = null)
          }

          // The try/catch block prevents UI crash in case a suspend function throws an exception
          try {
            val all = listingRepo.getAllListings()

            val uiModels = supervisorScope {
              all.map { listing ->
                    async {
                      val creator = profileRepo.getProfile(listing.creatorUserId)
                      ListingUiModel(
                          listing = listing,
                          creator = creator,
                          creatorRating = creator?.tutorRating ?: RatingInfo())
                    }
                  }
                  .awaitAll()
            }

            _ui.update { it.copy(allListings = uiModels, isLoading = false) }
            applyFilters()
          } catch (t: Throwable) {
            _ui.update { it.copy(isLoading = false, error = t.message ?: "Unknown error") }
          }
        }
  }

  /**
   * Helper to be called when the search query changes
   *
   * @param newQuery The new search query
   */
  fun onQueryChanged(newQuery: String) {
    _ui.update { it.copy(query = newQuery) }
    applyFilters()
  }

  /**
   * Helper to be called when the selected skill changes
   *
   * @param skill The new selected skill
   */
  fun onSkillSelected(skill: String?) {
    _ui.update { it.copy(selectedSkill = skill) }
    applyFilters()
  }

  /** Apply both query and skill filtering */
  private fun applyFilters() {
    val state = _ui.value
    /**
     * Helper to normalize skill strings for comparison
     *
     * @param s The skill string
     */
    fun key(s: String) = s.trim().lowercase()
    val selectedSkillKey = state.selectedSkill?.let(::key)

    // Apply filters to all listings
    val filtered =
        state.allListings.filter { item ->
          val listing = item.listing

          val matchesSubject = listing.skill.mainSubject == state.mainSubject

          val matchesQuery =
              state.query.isBlank() || listing.description.contains(state.query, ignoreCase = true)

          val matchesSkill =
              selectedSkillKey == null ||
                  listing.skill.mainSubject == state.mainSubject &&
                      key(listing.skill.skill) == selectedSkillKey

          matchesSubject && matchesQuery && matchesSkill
        }

    // Sort by creator rating
    val sorted =
        filtered.sortedWith(
            compareByDescending<ListingUiModel> { it.creatorRating.averageRating }
                .thenByDescending { it.creatorRating.totalRatings }
                .thenBy { it.creator?.name })

    _ui.update { it.copy(listings = sorted) }
  }

  /**
   * Helper to convert MainSubject enum to user-friendly string
   *
   * @param subject The main subject
   */
  fun subjectToString(subject: MainSubject?): String =
      when (subject) {
        MainSubject.ACADEMICS -> "Academics"
        MainSubject.SPORTS -> "Sports"
        MainSubject.MUSIC -> "Music"
        MainSubject.ARTS -> "Arts"
        MainSubject.TECHNOLOGY -> "Technology"
        MainSubject.LANGUAGES -> "Languages"
        MainSubject.CRAFTS -> "Crafts"
        null -> "Subjects"
      }

  /**
   * Helper to get skill names for a given main subject
   *
   * @param mainSubject The main subject
   */
  fun getSkillsForSubject(mainSubject: MainSubject?): List<String> {
    if (mainSubject == null) return emptyList()
    return SkillsHelper.getSkillNames(mainSubject)
  }

  // todo à refaire déguelasse
  fun BookListing(listingUIModel: ListingUiModel) {
    viewModelScope.launch {
      val userId = UserSessionManager.getCurrentUserId()
      val newBooking =
          Booking(
              bookingId = bookingRepo.getNewUid(),
              associatedListingId = listingUIModel.listing.listingId,
              listingCreatorId = listingUIModel.listing.creatorUserId,
              bookerId = userId!!,
              sessionStart = Date(),
              sessionEnd = Date(),
              status = BookingStatus.PENDING,
              price = listingUIModel.listing.hourlyRate)
      bookingRepo.addBooking(newBooking)
    }
  }
}
