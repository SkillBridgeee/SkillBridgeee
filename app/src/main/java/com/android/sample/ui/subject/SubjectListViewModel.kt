package com.android.sample.ui.subject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.model.listing.Listing
import com.android.sample.model.listing.ListingRepository
import com.android.sample.model.listing.ListingRepositoryProvider
import com.android.sample.model.rating.RatingInfo
import com.android.sample.model.skill.MainSubject
import com.android.sample.model.skill.SkillsHelper
import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepository
import com.android.sample.model.user.ProfileRepositoryProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

/** UI state for the Subject List screen */
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

/** Combined listing + creator UI model */
data class ListingUiModel(
    val listing: Listing,
    val creator: Profile?,
    val creatorRating: RatingInfo
)

/** ViewModel now loads LISTINGS (still supports filtering & sorting) */
class SubjectListViewModel(
    private val listingRepo: ListingRepository = ListingRepositoryProvider.repository,
    private val profileRepo: ProfileRepository = ProfileRepositoryProvider.repository
) : ViewModel() {

  private val _ui = MutableStateFlow(SubjectListUiState())
  val ui: StateFlow<SubjectListUiState> = _ui

  private var loadJob: Job? = null

  /** Refresh listings filtered on selected subject */
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

  /** When search query changes */
  fun onQueryChanged(newQuery: String) {
    _ui.update { it.copy(query = newQuery) }
    applyFilters()
  }

  /** When skill selected */
  fun onSkillSelected(skill: String?) {
    _ui.update { it.copy(selectedSkill = skill) }
    applyFilters()
  }

  /** Apply both query and skill filtering */
  private fun applyFilters() {
    val state = _ui.value

    fun key(s: String) = s.trim().lowercase()
    val selectedSkillKey = state.selectedSkill?.let(::key)

    val filtered =
        state.allListings.filter { item ->
          val profile = item.creator
          val listing = item.listing

          val matchesSubject = listing.skill.mainSubject == state.mainSubject

          val matchesQuery =
              state.query.isBlank() ||
                  profile?.name?.contains(state.query, ignoreCase = true) == true ||
                  listing.description.contains(state.query, ignoreCase = true)

          val matchesSkill =
              selectedSkillKey == null ||
                  listing.skill.mainSubject == state.mainSubject &&
                      key(listing.skill.skill) == selectedSkillKey

          matchesSubject && matchesQuery && matchesSkill
        }

    // Sort by creator rating → include unrated ones (0)
    val sorted =
        filtered.sortedWith(
            compareByDescending<ListingUiModel> { it.creatorRating.averageRating }
                .thenByDescending { it.creatorRating.totalRatings }
                .thenBy { it.creator?.name })

    _ui.update { it.copy(listings = sorted) }
  }

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

  fun getSkillsForSubject(mainSubject: MainSubject?): List<String> {
    if (mainSubject == null) return emptyList()
    return SkillsHelper.getSkillNames(mainSubject)
  }
}
