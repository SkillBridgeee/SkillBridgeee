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
    val topTutors: List<Profile> = emptyList(),
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
 * ViewModel for the Subject List screen.
 *
 * Uses a repository provided by [ProfileRepositoryProvider] by default (like in your example).
 */
class SubjectListViewModel(
    private val repository: ProfileRepository = ProfileRepositoryProvider.repository,
    private val tutorsPerTopSection: Int = 3
) : ViewModel() {

    private val _ui = MutableStateFlow(SubjectListUiState())
    val ui: StateFlow<SubjectListUiState> = _ui

    private var loadJob: Job? = null

    /** Call this to refresh state (mirrors getAllTodos/refreshUIState approach). */
    fun refresh() {
        loadJob?.cancel()
        loadJob =
            viewModelScope.launch {
                _ui.update { it.copy(isLoading = true, error = null) }
                try {
                    // 1) Load all profiles
                    val allProfiles = repository.getAllProfiles()

                    // 2) Load skills for each profile (parallelized)
                    val skillsByUser: Map<String, List<Skill>> =
                        allProfiles
                            .map { p -> async { p.userId to repository.getSkillsForUser(p.userId) } }
                            .awaitAll()
                            .toMap()

                    // 3) Compute top tutors
                    val top =
                        allProfiles.sortedWith(
                            compareByDescending<Profile> { it.tutorRating.averageRating }
                                .thenByDescending { it.tutorRating.totalRatings }
                                .thenBy { it.name })
                            .take(tutorsPerTopSection)

                    // 4) Update raw state, then apply current filters
                    _ui.update {
                        it.copy(
                            topTutors = top,
                            allTutors = allProfiles,
                            userSkills = skillsByUser,
                            isLoading = false,
                            error = null)
                    }
                    applyFilters()
                } catch (t: Throwable) {
                    _ui.update { it.copy(isLoading = false, error = t.message ?: "Unknown error") }
                }
            }
    }

    fun onQueryChanged(newQuery: String) {
        _ui.update { it.copy(query = newQuery) }
        applyFilters()
    }

    fun onSkillSelected(skill: String?) {
        _ui.update { it.copy(selectedSkill = skill) }
        applyFilters()
    }

    /** Applies in-memory query & skill filters (no suspend calls here). */
    private fun applyFilters() {
        val state = _ui.value
        val topIds = state.topTutors.map { it.userId }.toSet()

        // normalize a skill key for robust matching
        fun key(s: String) = s.trim().lowercase()

        val selectedSkillKey = state.selectedSkill?.let(::key)

        val filtered = state.allTutors.filter { profile ->
            // exclude top tutors from the list
            if (profile.userId in topIds) return@filter false

            val matchesQuery =
                state.query.isBlank() ||
                        profile.name.contains(state.query, ignoreCase = true) ||
                        profile.description.contains(state.query, ignoreCase = true)

            val matchesSkill =
                selectedSkillKey == null ||
                        state.userSkills[profile.userId].orEmpty().any {
                            it.mainSubject == state.mainSubject &&
                                    key(it.skill) == selectedSkillKey
                        }

            matchesQuery && matchesSkill
        }

        _ui.update { it.copy(tutors = filtered) }
    }


}
