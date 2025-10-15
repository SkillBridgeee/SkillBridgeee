package com.android.sample.ui.subject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.model.skill.MainSubject
import com.android.sample.model.skill.Skill
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

/** UI state for the Subject List screen */
data class SubjectListUiState(
    val mainSubject: MainSubject = MainSubject.MUSIC,
    val query: String = "",
    val selectedSkill: String? = null,
    val skillsForSubject: List<String> = SkillsHelper.getSkillNames(MainSubject.MUSIC),
    /** Full set of tutors loaded from repo (before any filters) */
    val allTutors: List<Profile> = emptyList(),
    /** The currently displayed list (after filters applied) */
    val tutors: List<Profile> = emptyList(),
    /** Cache of each tutor's skills so filtering is non-suspending */
    val userSkills: Map<String, List<Skill>> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel for the Subject List screen. Loads and holds the list of tutors, applying search and
 * skill filters as needed.
 *
 * @param repository The profile repository to load tutors from
 */
class SubjectListViewModel(
    private val repository: ProfileRepository = ProfileRepositoryProvider.repository
) : ViewModel() {

  private val _ui = MutableStateFlow(SubjectListUiState())
  val ui: StateFlow<SubjectListUiState> = _ui

  private var loadJob: Job? = null

  /** Refreshes the list of tutors by loading from the repository. */
  fun refresh() {
    // Cancel any ongoing load
    loadJob?.cancel()
    // Start a new load
    loadJob =
        viewModelScope.launch {
          _ui.update { it.copy(isLoading = true, error = null) }
          try {
            // 1) Load all profiles
            val allProfiles = repository.getAllProfiles()

            // 2) Load skills for each profile in parallel
            val skillsByUser: Map<String, List<Skill>> =
                allProfiles
                    // For each tutor start an async child coroutine that loads that user’s
                    // skills and returns a (userId to skills) pair.
                    .map { p -> async { p.userId to repository.getSkillsForUser(p.userId) } }
                    .awaitAll()
                    .toMap()

            // 3) Update raw state, then apply current filters
            _ui.update {
              it.copy(
                  allTutors = allProfiles,
                  userSkills = skillsByUser,
                  isLoading = false,
                  error = null)
            }
            // Apply filters to update displayed list (e.g filter by query or skill)
            applyFilters()
          } catch (t: Throwable) {
            _ui.update { it.copy(isLoading = false, error = t.message ?: "Unknown error") }
          }
        }
  }

  /**
   * Called when the search query changes. Updates the query state and reapplies filters to the full
   * list.
   *
   * @param newQuery The new search query string
   */
  fun onQueryChanged(newQuery: String) {
    _ui.update { it.copy(query = newQuery) }
    applyFilters()
  }

  /**
   * Called when a skill is selected from the category dropdown. Updates the selected skill state
   * and reapplies filters to the full list.
   *
   * @param skill The selected skill, or null to clear the filter
   */
  fun onSkillSelected(skill: String?) {
    _ui.update { it.copy(selectedSkill = skill) }
    applyFilters()
  }

  /** Applies the current search query and skill filter to the full list, then sorts by rating. */
  private fun applyFilters() {
    val state = _ui.value

    // normalize a skill key for easier matching
    fun key(s: String) = s.trim().lowercase()
    val selectedSkillKey = state.selectedSkill?.let(::key)

    val filtered =
        state.allTutors.filter { profile ->
          val matchesQuery =
              // Match if query is blank, or name or description contains the query
              state.query.isBlank() ||
                  profile.name.contains(state.query, ignoreCase = true) ||
                  profile.description.contains(state.query, ignoreCase = true)

          val matchesSkill =
              // Match if no skill selected, or if user has the selected skill for this subject
              selectedSkillKey == null ||
                  state.userSkills[profile.userId].orEmpty().any {
                    it.mainSubject == state.mainSubject && key(it.skill) == selectedSkillKey
                  }
          // Include if matches both query and skill
          matchesQuery && matchesSkill
        }

    // Sort best-first for the single list
    val sorted =
        filtered.sortedWith(
            // Sort by average rating (desc), then by total ratings (desc), then by name (asc)
            compareByDescending<Profile> { it.tutorRating.averageRating }
                .thenByDescending { it.tutorRating.totalRatings }
                .thenBy { it.name })

    _ui.update { it.copy(tutors = sorted) }
  }
}
