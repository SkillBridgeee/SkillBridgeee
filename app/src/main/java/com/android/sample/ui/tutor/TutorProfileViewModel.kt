package com.android.sample.ui.tutor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.model.user.Tutor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI state for the TutorProfile screen. This state holds the data needed to display a tutor's
 * profile.
 * @param loading Indicates if the data is still loading.
 * @param tutor The tutor data to be displayed, or null if not yet loaded.
 */
data class TutorUiState(val loading: Boolean = true, val tutor: Tutor? = null)

/**
 * ViewModel for the TutorProfile screen. This ViewModel manages the state of the tutor profile
 * screen.
 * @param repository The repository to fetch tutor data.
 */
class TutorProfileViewModel(
    private val repository: TutorRepository,
) : ViewModel() {

  private val _state = MutableStateFlow(TutorUiState())
  val state: StateFlow<TutorUiState> = _state.asStateFlow()

    /**
     * Loads the tutor data for the given tutor ID. If the data is already loaded, this function
     * does nothing.
     * @param tutorId The ID of the tutor to load.
     */
  fun load(tutorId: String) {
    if (!_state.value.loading) return
    viewModelScope.launch {
      val t = repository.getTutorById(tutorId)
      _state.value = TutorUiState(loading = false, tutor = t)
    }
  }
}
