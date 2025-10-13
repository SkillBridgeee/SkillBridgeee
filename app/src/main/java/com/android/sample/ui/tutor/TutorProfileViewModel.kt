package com.android.sample.ui.tutor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.model.skill.Skill
import com.android.sample.model.user.ProfileRepositoryProvider
import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
    val skills: List<Skill> = emptyList()
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

  /**
   * Loads the tutor data for the given tutor ID. If the data is already loaded, this function does
   * nothing.
   *
   * @param tutorId The ID of the tutor to load.
   */
  fun load(tutorId: String) {
    if (!_state.value.loading) return
    viewModelScope.launch {
      val profile = repository.getProfileById(tutorId)
      val skills = repository.getSkillsForUser(tutorId)
      _state.value = TutorUiState(loading = false, profile = profile, skills = skills)
    }
  }
}
