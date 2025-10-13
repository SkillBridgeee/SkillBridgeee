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
                            tutors = allProfiles, // temporary; will be filtered below
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

        val filtered = state.tutors.filter { profile ->
            val matchesQuery =
                state.query.isBlank() ||
                        profile.name.contains(state.query, ignoreCase = true) ||
                        profile.description.contains(state.query, ignoreCase = true)

            val matchesSkill =
                state.selectedSkill.isNullOrBlank() ||
                        state.userSkills[profile.userId].orEmpty().any {
                            it.mainSubject == state.mainSubject && it.skill == state.selectedSkill
                        }

            matchesQuery && matchesSkill && (profile.userId !in topIds)
        }

        _ui.update { it.copy(tutors = filtered) }
    }

}
