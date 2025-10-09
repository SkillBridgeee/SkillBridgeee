package com.android.sample.ui.tutor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.model.user.Tutor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TutorUiState(val loading: Boolean = true, val tutor: Tutor? = null)

class TutorProfileViewModel(
    private val repository: TutorRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main // injected for tests
) : ViewModel() {

  private val _state = MutableStateFlow(TutorUiState())
  val state: StateFlow<TutorUiState> = _state.asStateFlow()

  fun load(tutorId: String) {
    if (!_state.value.loading) return
    viewModelScope.launch {
      val t = repository.getTutorById(tutorId)
      _state.value = TutorUiState(loading = false, tutor = t)
    }
  }
}
