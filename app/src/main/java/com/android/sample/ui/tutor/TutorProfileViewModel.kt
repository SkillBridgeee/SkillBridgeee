package com.android.sample.ui.tutor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.model.skill.Skill
import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepository
import com.android.sample.model.user.ProfileRepositoryProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

/**
 * UI state for the TutorProfile screen. This state holds the data needed to display a tutor's
 * profile.
 *
 * @param loading Whether the data is still loading.
 * @param profile The profile of the tutor.
 * @param skills The list of skills the tutor offers.
 */
data class TutorUiState(
    val loading: Boolean = true,
    val profile: Profile? = null,
    val skills: List<Skill> = emptyList(),
    val error: String? = null
)

/**
 * ViewModel for the TutorProfile screen.
 *
 * @param repository The repository to fetch tutor data.
 */
class TutorProfileViewModel(
    private val repository: ProfileRepository = ProfileRepositoryProvider.repository,
) : ViewModel() {

  private val _state = MutableStateFlow(TutorUiState())
  val state: StateFlow<TutorUiState> = _state.asStateFlow()

    private var loadJob: Job? = null

  /**
   * Loads the tutor data for the given tutor ID. If the data is already loaded, this function does
   * nothing.
   *
   * @param tutorId The ID of the tutor to load.
   */
  fun load(tutorId: String) {
      val currentId = _state.value.profile?.userId
      if (currentId == tutorId && !_state.value.loading) return

      loadJob?.cancel()
      loadJob = viewModelScope.launch {
          _state.value = _state.value.copy(loading = true)

          val (profile, skills) = supervisorScope {
              val profileDeferred = async { repository.getProfile(tutorId) }
              val skillsDeferred = async { repository.getSkillsForUser(tutorId) }

              val profile = runCatching { profileDeferred.await() }.getOrNull()
              val skills = runCatching { skillsDeferred.await() }.getOrElse { emptyList() }

              profile to skills
          }

          _state.value = TutorUiState(
              loading = false,
              profile = profile,
              skills = skills
          )
      }
  }
}
